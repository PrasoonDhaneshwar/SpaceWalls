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
import org.json.JSONArray
import org.json.JSONException
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.IOException
import java.net.*
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*

class ApodArchivesRepositoryImpl(private val daoArchive: ApodArchiveDao) : ApodArchivesRepository {
    private val TAG = "ApodArchivesRepositoryImpl"

    // Set to today's date
    private var todayDate = Date()
    private val currentCalendarDate: Calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
    private var iteration = 1

//    override suspend fun fetchArchivesFromCurrentDate(): List<ApodArchive> {
//        val apodArchiveList: MutableList<ApodArchive> = ArrayList()
//        Log.d(TAG, "Starting point of date: $todayDate")
//
//        val dateFormat = SimpleDateFormat("yyyy-MM-dd")    // change format of date to "2022-01-10"
//
//        val calendarEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
//        // Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
//        calendarEndDate.set(1995, Calendar.JUNE, 17, 0, 0)
//        //val endDate = calendarEndDate.time
//        val endDate = DateUtils.getEndDate()
//
//        // Uncomment to test from a starting date
//        // currentCalendarDate.time = DateUtils.generateStartingPointOfDate()
//
//        var currentCalDate: Date
//        var i = 0
//        while (i++ <= Constants.LOAD_APOD_ARCHIVE_FACTOR) {
//            currentCalDate = currentCalendarDate.time
//            if (currentCalDate.before(endDate)) return apodArchiveList
//
//            val parsedDate: String = dateFormat.format(currentCalendarDate.time)
//            currentCalendarDate.add(Calendar.DAY_OF_MONTH, -1)
//
//            var apodArchive: ApodArchive
//
//            val job = CoroutineScope(Dispatchers.IO).launch {
//                val isDateExistInDB = daoArchive.isRowExist(parsedDate.toIntDate())
//                if (isDateExistInDB) {
//                    apodArchive = daoArchive.getApodArchiveFromDatePrimaryKey(parsedDate.toIntDate()).toApodArchive()
//                    Log.d(TAG, "Fetch from DB -> apodArchive: $apodArchive")
//                    Log.d(TAG, "Fetch from DB -> apodArchive bitmap size in bytes: ${(apodArchive.imageBitmapUI?.byteCount)}, Mb: ${(apodArchive.imageBitmapUI?.byteCount)?.div(1024f * 1024)}")
//                    // Store bitmap in DB
//                    if (apodArchive.imageBitmapUI == null) {
//                        val bitmap = BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
//                            withContext(Dispatchers.IO) {
//                                val cleanUrl = if (!apodArchive.link.startsWith("https://") && !apodArchive.link.startsWith("http://"))
//                                    "https:$apodArchive.link" else apodArchive.link
//                                URL(cleanUrl).openConnection()
//                            }.getInputStream()
//                        })
//                        if (bitmap!= null) daoArchive.updateApodArchiveImage(parsedDate.toIntDate(), bitmap)
//                    }
//
//                } else {
//                    apodArchive = createArchiveLinksWithDate(parsedDate)   // Wait for it to finish
//                    Log.d(TAG, "Fetch from Network -> apodArchive: $apodArchive")
//
//                    CoroutineScope(Dispatchers.IO).launch {
//                        if (apodArchive.link.contains("youtube") || apodArchive.link.contains("jpeg") || apodArchive.link.contains("jpg") || apodArchive.link.contains("png")) {
//                            daoArchive.insertApodArchive(apodArchive.toApodArchiveEntity())
//
//                            // can be added in another thread, since the job waits for jobAddToDb to finish
//                            CoroutineScope(Dispatchers.IO).launch {
//                                val bitmap = async {
//                                    BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
//                                        withContext(Dispatchers.IO) {
//                                            URL(apodArchive.link).openConnection()
//                                        }.getInputStream()
//                                    })
//                                }
//                                if (bitmap.await() != null) daoArchive.updateApodArchiveImage(parsedDate.toIntDate(), bitmap.await())
//                            }
//
//                        }
//                    }
//                }
//                // Don't add if empty archive received
//                if (apodArchive.title.isNotEmpty()) apodArchiveList.add(apodArchive)
//            }
//            job.join()
//        }
//
//        todayDate = currentCalendarDate.time
//        Log.d(TAG, "fetchImageArchivesFromCurrentDate with todayDate: $todayDate iteration: ${iteration++}, apodArchiveList: " + apodArchiveList)
//
//        return apodArchiveList
//    }

    override suspend fun fetchArchivesFromCurrentDate(): List<ApodArchive> = withContext(Dispatchers.IO) {
        val apodArchiveList = mutableListOf<ApodArchive>()

        // change format of date to "2022-01-10"
        val dateFormat = SimpleDateFormat("yyyy-MM-dd").apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

        // Date must be after Jun 16, 1995
        val endDate = DateUtils.getEndDate()

        // Uncomment to test from a starting date
        // currentCalendarDate.time = DateUtils.generateStartingPointOfDate()

        var count = 0

        while (count++ <= Constants.LOAD_APOD_ARCHIVE_FACTOR) {
            val currentDate = currentCalendarDate.time
            if (currentDate.before(endDate)) break

            val parsedDate = dateFormat.format(currentDate)
            val intDate = parsedDate.toIntDate()
            currentCalendarDate.add(Calendar.DAY_OF_MONTH, -1)

            val cached = if (daoArchive.isRowExist(intDate))
                daoArchive.getApodArchiveFromDatePrimaryKey(intDate).toApodArchive() else null

            val apodArchive: ApodArchive = if (cached != null && !cached.link.isBrokenApodLink()) {
                val archive = cached
                Log.d(TAG, "Fetch from DB -> apodArchive: $archive")
                Log.d(TAG, "Fetch from DB -> apodArchive bitmap size in bytes: ${(archive.imageBitmapUI?.byteCount)}, Mb: ${(archive.imageBitmapUI?.byteCount)?.div(1024f * 1024)}")
                if (archive.imageBitmapUI == null) {
                    launchBitmapFetchAndUpdate(intDate, archive.link)
                }
                archive
            } else {
                val archive = createArchiveLinksWithDate(parsedDate)
                Log.d(TAG, "Fetch from Network -> apodArchive: $archive")
                if (archive.title.isNotEmpty()) {
                    if (cached == null) daoArchive.insertApodArchive(archive.toApodArchiveEntity())
                    else daoArchive.repairArchive(intDate, archive.title, archive.link)
                    launchBitmapFetchAndUpdate(intDate, archive.link)
                }
                archive.copy(
                    isAddedToFavorites = cached?.isAddedToFavorites ?: false,
                    isSetWallpaperUI = cached?.isSetWallpaperUI ?: false
                )
            }

            // Don't add if empty archive received
            if (apodArchive.title.isNotEmpty()) {
                apodArchiveList.add(apodArchive)
            }
        }

        todayDate = currentCalendarDate.time
        Log.d(TAG, "fetchArchivesFromCurrentDate completed with todayDate: $todayDate, total items: ${apodArchiveList.size}")
        apodArchiveList
    }

    // Helper function to fetch bitmap in background without blocking fetchArchivesFromCurrentDate
    private fun CoroutineScope.launchBitmapFetchAndUpdate(dateInt: Int, imageUrl: String) = launch(Dispatchers.IO) {
        try {
            val cleanUrl = if (!imageUrl.startsWith("https://") && !imageUrl.startsWith("http://")) "https:$imageUrl" else imageUrl
            val bitmap = withContext(Dispatchers.IO) {
                BitmapFactory.decodeStream(URL(cleanUrl).openConnection().getInputStream())
            }
            if (bitmap != null) {
                daoArchive.updateApodArchiveImage(dateInt, bitmap)
                Log.d(TAG, "Bitmap updated for date $dateInt")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch and update bitmap for $dateInt from $imageUrl", e)
        }
    }

    private suspend fun fetchDocumentFromUrl(url: String): Document? = withContext(Dispatchers.IO) {
        try {
            val connection = Jsoup.connect(url)
                .ignoreHttpErrors(true)
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                )
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                .header("Accept-Language", "en-US,en;q=0.5")
                .header("Connection", "keep-alive")
                .timeout(10000)
                .followRedirects(true)

            val document = connection.get()
            val statusCode = connection.response().statusCode()
            if (statusCode == 200) {
                document
            } else {
                // Optionally, log or handle different status codes, for example:
                println("Received HTTP $statusCode for $url")
                // Can still return the document if you want to parse error page HTML
                document
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Entries saved after NASA moved APOD to science.nasa.gov can point at the site logo or a doubled URL
    private fun String.isBrokenApodLink() =
        contains("nasa-logo") || contains("/apod/https:")

    // Old apYYMMDD.html addresses still redirect to the article for older dates; newer dates need a search
    private suspend fun resolveApodDocument(date: String): Document? {
        val legacy = fetchDocumentFromUrl(DateUtils.createApodUrl(date))
        if (legacy?.location()?.contains("/image-article/") == true) return legacy
        val articleUrl = findArticleUrl(date) ?: return null
        return fetchDocumentFromUrl(articleUrl)
    }

    private suspend fun findArticleUrl(date: String): String? = withContext(Dispatchers.IO) {
        try {
            val (year, month, day) = date.split("-")
            val monthName = DateFormatSymbols(Locale.ENGLISH).months[month.toInt() - 1]
                .lowercase(Locale.ENGLISH)
            val slug = "apod-$year-$monthName-${day.toInt()}"
            val body = Jsoup.connect("https://science.nasa.gov/wp-json/wp/v2/search")
                .data("search", slug)
                .data("per_page", "10")
                .data("subtype", "image-article")
                .ignoreContentType(true)
                .timeout(10000)
                .execute()
                .body()
            val results = JSONArray(body)
            (0 until results.length())
                .map { results.getJSONObject(it).getString("url") }
                .firstOrNull { it.contains("/$slug-") }   // trailing "-" keeps november-1 from matching november-10
        } catch (e: IOException) {
            Log.e(TAG, "Article search failed for $date", e)
            null
        } catch (e: JSONException) {
            Log.e(TAG, "Article search returned bad JSON for $date", e)
            null
        }
    }

    private suspend fun createArchiveLinksWithDate(date: String): ApodArchive {
        val archive: ApodArchive
        val document: Document?
        var png = String()
        var youTubeLink = String()
        var webLink = String()
        var title = String()
        var mediaLink = String()
        var url = String()
        var copyright: String? = ""
        var mediaType = "image"
        var explanationText = ""

        var noticeMessage: String? = null

        try {
            url = DateUtils.createApodUrl(date)
            Log.d(TAG, "Jsoup link $url for: $date")
            document = resolveApodDocument(date)
            if (document != null) {
                val media = document.selectFirst(".media-detail-hero__media")
                png = media?.selectFirst("img[src]")?.absUrl("src").orEmpty()
                youTubeLink = media?.selectFirst("iframe[src*=youtube]")?.attr("src").orEmpty()
                webLink = media?.selectFirst("video source[src]")?.absUrl("src")
                    ?: media?.selectFirst("iframe[src]")?.absUrl("src").orEmpty()

                title = document.selectFirst("h1")?.text().orEmpty()

                copyright = document.select(".media-detail-hero__meta-row")
                    .firstOrNull { it.selectFirst("th")?.text()?.contains("Credit") == true }
                    ?.selectFirst("td")?.text()?.takeIf { it.isNotEmpty() }

                explanationText = document.selectFirst(".media-detail-hero__description")
                    ?.text().orEmpty().removePrefix("Explanation:").trim()

                noticeMessage = document.select("center:contains(Due to the lapse in federal government funding)")
                    .firstOrNull()
                    ?.text()

                val statusCode = document.connection().response().statusCode()

                if (statusCode == 404 && noticeMessage != null) {
                    Log.d(TAG,"Received HTTP $statusCode for $url This noticeMessage is from the 404 (not found) page or similar error page")

                }
                Log.d(TAG, "createArchiveLinksWithDate for title: $title, date: $date: IMG SRC: $png, source: $mediaLink copyright: $copyright, mediaType: $mediaType, noticeMessage: $noticeMessage, explanationTextOnly: $explanationText")
            }

        } catch (e: UnknownHostException) {
            e.printStackTrace()
        } catch (e: SocketException) {
            e.printStackTrace()
        } catch (e: ProtocolException) {
            e.printStackTrace()
        } catch (e: SocketTimeoutException) {
            e.printStackTrace()
        } catch (e: ConnectException) {
            e.printStackTrace()
        } catch (e: HttpStatusException) {
            Log.d(TAG, "link $url does not exist for : $date")
            e.printStackTrace();
        }
        if (png.contains("jpeg") || png.contains("jpg") || png.contains("gif") || png.contains("png")) {
            mediaLink = png
        } else if (youTubeLink.isNotEmpty() && youTubeLink.contains("youtube")) {
            mediaLink = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(youTubeLink)
            mediaType = "video"
        } else {
            mediaLink = webLink
            mediaType = "video"
        }

        archive = ApodArchive(
            date = date,
            title = title,
            link = mediaLink,
            copyright = copyright,
            explanation = explanationText,
            isAddedToFavorites = false,
            mediaType = mediaType,
            noticeMessage = noticeMessage
        )

        return archive
    }

    override suspend fun fetchArchiveFromDate(date: String) =
        createArchiveLinksWithDate(date).copy(isAddedToFavorites = daoArchive.isFavorite(date.toIntDate()))
}