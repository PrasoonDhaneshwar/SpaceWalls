package com.prasoon.apodkotlinrefactored.core.common

import android.util.Log
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOAD_APOD_ARCHIVE_FACTOR
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import java.net.ProtocolException
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

object DateInput {
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
        val cal = GregorianCalendar()
        cal.set(Calendar.YEAR, Integer.parseInt(values[0]))
        cal.set(Calendar.MONTH, Integer.parseInt(values[1]) - 1)
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(values[2]))
        // val myFormat = "dd.MM.yyyy" // mention the format you need
        // val myFormat2 = "dd LLL yyyy HH:mm:ss aaa z" // mention the format you need
        val format = "LLL d, yyyy" // mention the format you need
        val selectedDate = SimpleDateFormat(format, Locale.US).format(cal.time)
        Log.i(TAG, "calendar simpleDateFormat: $selectedDate")    // Jul 1, 2022
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

    // https://api.nasa.gov/planetary/apod?api_key=XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3&date=2022-01-10
    fun createApodUrlApi(date: String): String {
        return "https://api.nasa.gov/planetary/apod?api_key=${BuildConfig.APOD_API_KEY}&date=$date"
    }
    fun getCurrentDateForInitialization() : String {
        val currentCalendarDateForInitialization: Calendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        return dateFormat.format(currentCalendarDateForInitialization.time)
    }
}