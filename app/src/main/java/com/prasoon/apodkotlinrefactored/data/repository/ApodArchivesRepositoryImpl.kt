package com.prasoon.apodkotlinrefactored.data.repository

import android.graphics.BitmapFactory
import android.util.Log
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.ApodArchiveDao
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import com.prasoon.apodkotlinrefactored.domain.repository.ApodArchivesRepository
import kotlinx.coroutines.*
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.net.*
import java.text.SimpleDateFormat
import java.util.*

class ApodArchivesRepositoryImpl(private val daoArchive: ApodArchiveDao) : ApodArchivesRepository {
    private val TAG = "ApodArchivesRepositoryImpl"

    // Set to today's date
    private var todayDate = Date()
    private val currentCalendarDate: Calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
    private var iteration = 1
    override suspend fun fetchArchivesFromCurrentDate(): List<ApodArchive> {
        val apodArchiveList: MutableList<ApodArchive> = ArrayList()
        Log.d(TAG, "Starting point of date: $todayDate")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")    // change format of date to "2022-01-10"

        val calendarEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
        calendarEndDate.set(1995, Calendar.JUNE, 17, 0, 0)
        //val endDate = calendarEndDate.time
        val endDate = DateUtils.getEndDate()

        // Uncomment to test from a starting date
        // currentCalendarDate.time = DateUtils.generateStartingPointOfDate()

        var currentCalDate: Date
        var i = 0
        while (i++ <= Constants.LOAD_APOD_ARCHIVE_FACTOR) {
            currentCalDate = currentCalendarDate.time
            if (currentCalDate.before(endDate)) return apodArchiveList

            val parsedDate: String = dateFormat.format(currentCalendarDate.time)
            currentCalendarDate.add(Calendar.DAY_OF_MONTH, -1)

            var apodArchive: ApodArchive

            val job = CoroutineScope(Dispatchers.IO).launch {
                val isDateExistInDB = daoArchive.isRowExist(parsedDate.toIntDate())
                if (isDateExistInDB) {
                    apodArchive = daoArchive.getApodArchiveFromDatePrimaryKey(parsedDate.toIntDate()).toApodArchive()
                    Log.d(TAG, "Fetch from DB -> apodArchive: $apodArchive")
                    Log.d(TAG, "Fetch from DB -> apodArchive bitmap size in bytes: ${(apodArchive.imageBitmapUI?.byteCount)}, Mb: ${(apodArchive.imageBitmapUI?.byteCount)?.div(1024f * 1024)}")
                    // Store bitmap in DB
                    CoroutineScope(Dispatchers.IO).launch {
                        if (apodArchive.imageBitmapUI == null) {
                            val bitmap = BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                                withContext(Dispatchers.IO) {
                                    val cleanUrl = if (!apodArchive.link.startsWith("https://") && !apodArchive.link.startsWith("http://"))
                                        "https:$apodArchive.link" else apodArchive.link
                                    URL(cleanUrl).openConnection()
                                }.getInputStream()
                            })
                            if (bitmap!= null) daoArchive.updateApodArchiveImage(parsedDate.toIntDate(), bitmap)
                        }
                    }

                } else {
                    apodArchive = createArchiveLinksWithDate(parsedDate)   // Wait for it to finish
                    Log.d(TAG, "Fetch from Network -> apodArchive: $apodArchive")

                    CoroutineScope(Dispatchers.IO).launch {
                        if (apodArchive.link.contains("youtube") || apodArchive.link.contains("jpeg") || apodArchive.link.contains("jpg") || apodArchive.link.contains("png")) {
                            daoArchive.insertApodArchive(apodArchive.toApodArchiveEntity())

                            // can be added in another thread, since the job waits for jobAddToDb to finish
                            CoroutineScope(Dispatchers.IO).launch {
                                val bitmap = async {
                                    BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                                        withContext(Dispatchers.IO) {
                                            URL(apodArchive.link).openConnection()
                                        }.getInputStream()
                                    })
                                }
                                if (bitmap.await() != null) daoArchive.updateApodArchiveImage(parsedDate.toIntDate(), bitmap.await())
                            }

                        }
                    }
                }
                // Don't add if empty archive received
                if (apodArchive.title.isNotEmpty()) apodArchiveList.add(apodArchive)
            }
            job.join()
        }

        todayDate = currentCalendarDate.time
        Log.d(TAG, "fetchImageArchivesFromCurrentDate with todayDate: $todayDate iteration: ${iteration++}, apodArchiveList: " + apodArchiveList)

        return apodArchiveList
    }

    private fun createArchiveLinksWithDate(date: String): ApodArchive {
        val archive: ApodArchive
        lateinit var document: org.jsoup.nodes.Document
        var png = String()
        var youTubeLink = String()
        var webLink = String()
        var title = String()
        val link: String
        var url = String()
        try {
            url = DateUtils.createApodUrl(date)
            document = Jsoup.connect(url).get() // Network call, to be performed in separate thread

            png = document.select("img[src]").attr("src")
            youTubeLink = document.select("iframe[width]").attr("src")
            webLink = document.select("iframe[src]").attr("src")

            title = document.select("center").select("b").first()?.text() ?: ""
            if (title.isEmpty()) title = document.select("b").first()?.text() ?: ""

        } catch (e: UnknownHostException) {
            e.printStackTrace();
        } catch (e: ProtocolException) {
            e.printStackTrace();
        } catch (e: SocketTimeoutException) {
            e.printStackTrace();
        } catch (e: ConnectException) {
            e.printStackTrace();
        } catch (e: HttpStatusException) {
            Log.d(TAG, "link $url does not exist for : $date")
            e.printStackTrace();
        }
        if (png.contains("jpeg") || png.contains("jpg") || png.contains("gif") || png.contains("png")) {
            link = "https://apod.nasa.gov/apod/$png"
        } else if (youTubeLink.isNotEmpty() && youTubeLink.contains("youtube")) {
            link = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(youTubeLink)
        } else {
            link = webLink
        }
        Log.d(TAG, "createArchiveLinksWithDate title: $title")
        Log.d(TAG, "createArchiveLinksWithDate for date: $date: IMG SRC: $png")
        Log.d(TAG, "createArchiveLinksWithDate link: $link")

        archive = ApodArchive(date, title, link, false)

        return archive
    }

    override suspend fun fetchArchiveFromDate(date: String) = createArchiveLinksWithDate(date)

/*    override fun fetchArchivesFromCurrentDate(): Flow<Resource<List<ApodArchive>>> = flow {
        val apodArchiveList: MutableList<ApodArchive> = ArrayList()
        Log.d(TAG, "Starting point of date: $todayDate")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")    // change format of date to "2022-01-10"

        val calendarEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
        calendarEndDate.set(1995, Calendar.JUNE, 17, 0, 0)
        //val endDate = calendarEndDate.time
        val endDate = DateUtils.getEndDate()

        // Uncomment to test from a starting date
        // currentCalendarDate.time = DateUtils.generateStartingPointOfDate()

        var currentCalDate: Date
        var i = 0
        while (i++ <= Constants.LOAD_APOD_ARCHIVE_FACTOR) {
            currentCalDate = currentCalendarDate.time
            if (currentCalDate.before(endDate)) {
                emit(Resource.Error("apodArchiveList"))
            }

            val parsedDate: String = dateFormat.format(currentCalendarDate.time)
            currentCalendarDate.add(Calendar.DAY_OF_MONTH, -1)

            var apodArchive: ApodArchive

            val job = CoroutineScope(Dispatchers.IO).launch {
                val isDateExistInDB = daoArchive.isRowIsExist(parsedDate.toIntDate())
                if (isDateExistInDB) {
                    apodArchive = daoArchive.getApodFromDatePrimaryKey(parsedDate.toIntDate()).toApodArchive()
                    Log.d(TAG, "Fetch from DB -> apodArchive: $apodArchive")

                } else {
                    apodArchive = createArchiveLinksWithDate(parsedDate)   // Wait for it to finish
                    Log.d(TAG, "Fetch from Network -> apodArchive: $apodArchive")

                    val jobAddToDb = CoroutineScope(Dispatchers.IO).launch {
                        daoArchive.insertApod(apodArchive.toApodArchiveEntity())
                    }
                    jobAddToDb.join()
                }
                // Don't add if empty archive received
                if (apodArchive.title.isNotEmpty()) apodArchiveList.add(apodArchive)
            }
            emit(Resource.Loading())
            job.join()
        }

        todayDate = currentCalendarDate.time
        Log.d(TAG, "fetchImageArchivesFromCurrentDate with todayDate: $todayDate iteration: ${iteration++}, apodArchiveList: " + apodArchiveList)

        emit(Resource.Success(data = apodArchiveList))
    }*/

}