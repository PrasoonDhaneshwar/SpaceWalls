package com.prasoon.apodkotlinrefactored.core.utils

import android.util.Log
import com.prasoon.apodkotlinrefactored.BuildConfig
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ThreadLocalRandom

object DateUtils {
    private val TAG = "DateInput"

    var currentDate: String = String()
    var simpleDateFormat: String? = null

    fun convertDateToInteger(date: String): Int {
        val values = date.split("-")
        val year = values[0].toInt()
        val month = values[1].toInt()
        val day = values[2].toInt()

        return year * 10000 + month * 100 + day
    }

    fun String.toSimpleDateFormat(): String {
        val values = this.split("-")
        if (values.size == 1) return ""
        val cal = GregorianCalendar()
        cal.set(Calendar.YEAR, Integer.parseInt(values[0]))
        cal.set(Calendar.MONTH, Integer.parseInt(values[1]) - 1)
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(values[2]))
        // val myFormat = "dd.MM.yyyy" // mention the format you need
        // val myFormat2 = "dd LLL yyyy HH:mm:ss aaa z" // mention the format you need
        val format = "LLL d, yyyy" // mention the format you need
        val selectedDate = SimpleDateFormat(format, Locale.US).format(cal.time)
        Log.d(TAG, "calendar simpleDateFormat: $selectedDate")    // Jul 1, 2022
        return selectedDate.toString()
    }

    fun String.toIntDate(): Int {
        val values = this.split("-")
        if (values.size == 1) return 1
        val convertedLongDate = (values[0] + values[1] + values[2])
        return convertedLongDate.toInt()
    }

    fun createApodUrl(date: String?): String {
        if (date.isNullOrEmpty()) return ""
        val values = date.split("-")
        val packedDate = values[0].substring(2) + values[1] + values[2]
        return "https://apod.nasa.gov/apod/ap$packedDate.html"
    }

    // https://api.nasa.gov/planetary/apod?api_key=API_KEY&date=2022-01-10
    fun createApodUrlApi(date: String): String {
        return "https://api.nasa.gov/planetary/apod?api_key=${BuildConfig.APOD_API_KEY}&date=$date"
    }
    fun getCurrentDateForInitialization() : String {
        val currentCalendarDateForInitialization: Calendar = GregorianCalendar()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")     // change format of date to "2022-01-10"
        val timeZone = TimeZone.getTimeZone("UTC")  // Explicit set UTC TimeZone
        dateFormat.timeZone = timeZone
        return dateFormat.format(currentCalendarDateForInitialization.time)
    }

    fun getEndDate(): Date {
        val calendarEndDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        // Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
        calendarEndDate.set(1995, Calendar.JUNE, 17, 0, 0)
        return calendarEndDate.time
    }

    fun generateRandomDate(): String {
        val startingPointOfDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val startDate: Date = startingPointOfDate.time
        val endDate = getEndDate()
        val random: Long = ThreadLocalRandom.current().nextLong(endDate.time, startDate.getTime())
        val date = Date(random)
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")     // change format of date to "2022-01-10"
        val dateInString = dateFormat.format(date)
        Log.d(TAG, "generateRandomDate: Date: $date API format: $dateInString")
        return dateInString
    }

    // Test like this in fetchImageArchivesFromCurrentDate
    fun generateStartingPointOfDate() : Date {
        val testStartingPointOfDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        //Date must be after Jun 16, 1995, but changed here due to loop counting difference of 1 day
        testStartingPointOfDate.set(2015, Calendar.MARCH, 19, 0, 0)
        val testDate = testStartingPointOfDate.time
        return testDate
    }
}