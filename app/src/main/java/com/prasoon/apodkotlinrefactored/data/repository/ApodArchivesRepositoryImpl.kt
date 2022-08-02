package com.prasoon.apodkotlinrefactored.data.repository

import android.util.Log
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.DateInput
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.domain.repository.ApodArchivesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.net.ProtocolException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class ApodArchivesRepositoryImpl : ApodArchivesRepository {
    private val TAG = "ApodArchivesRepositoryImpl"

    // Set to today's date
    private var todayDate = Date()
    private val currentCalendarDate: Calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
    private var iteration = 1

    override suspend fun fetchImageArchivesFromCurrentDate(): List<String> {
        val datesList: MutableList<String> = ArrayList()
        Log.i(TAG, "Starting point of date: ${todayDate}")

        val dateFormat = SimpleDateFormat("yyyy-MM-dd")    // change format of date to "2022-01-10"

        val calendarEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
        calendarEndDate.set(1995, Calendar.JUNE, 17, 0, 0)
        val endDate = calendarEndDate.time

        // Test like this
/*        val teststartingpointofDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
        teststartingpointofDate.set(2017, Calendar.MARCH, 19, 0, 0)
        val testDate = teststartingpointofDate.time
        currentCalendarDate.time = testDate*/

        var currentCalDate = Date()
        var i = 0
        while (i++ <= Constants.LOAD_APOD_ARCHIVE_FACTOR) {

            currentCalDate = currentCalendarDate.time
            if (currentCalDate.before(endDate)) return datesList

            currentCalendarDate.add(Calendar.DAY_OF_MONTH, -1)
            val parsedDate: String = dateFormat.format(currentCalendarDate.time)
            var imgUrl = String()
            val job = CoroutineScope(Dispatchers.IO).launch {
                imgUrl = createArchiveLinksWithDate(parsedDate)   // Wait for it to finish
            }
            job.join()
            datesList.add(imgUrl)
        }

        todayDate = currentCalendarDate.time

        Log.i(TAG, "fetchImageArchivesFromCurrentDate iteration: ${iteration++}: " + datesList)

        return datesList
    }

    private fun createArchiveLinksWithDate(date: String): String {
        lateinit var document: org.jsoup.nodes.Document
        var png = String()
        var youTubeLink = String()
        var webLink = String()
        val link: String
        var url = String()
        try {
            url = DateInput.createApodUrl(date)
            document = Jsoup.connect(url).get() // Network call, to be performed in separate thread
            png = document.select("img[src\$=.jpg]").attr("src")
            youTubeLink = document.select("iframe[width]").attr("src")
            webLink = document.select("iframe[src]").attr("src")
        } catch (e: UnknownHostException) {
            e.printStackTrace();
        } catch (e: ProtocolException) {
            e.printStackTrace();
        } catch (e: HttpStatusException) {
            Log.i(TAG, "link $url does not exist for : $date")
            e.printStackTrace();
        }
        if (png.isNotEmpty()) {
            link = "https://apod.nasa.gov/apod/$png"
        } else if (youTubeLink.isNotEmpty() && youTubeLink.contains("youtube")) {
            link = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(youTubeLink)
        } else {
            link = webLink  // todo: Don't handle it in app
        }
        Log.d(TAG, "createArchiveLinksWithDate for date: $date: IMG SRC: $png")
        Log.d(TAG, "createArchiveLinksWithDate link: $link")

        return link
    }

}