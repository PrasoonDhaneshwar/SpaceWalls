package com.prasoon.apodkotlinrefactored.core.common

import com.prasoon.apodkotlinrefactored.BuildConfig

object DateInput {
    var currentDate: String? = null
    var simpleDateFormat: String? = null

    fun convertDateToInteger(date: String): Int {
        val values = date.split("-")
        val year = values[0].toInt()
        val month = values[1].toInt()
        val day = values[2].toInt()

        return year * 10000 + month * 100 + day
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

    fun String.toIntDate() : Int {
        val values = this.split("-")
        val convertedLongDate = (values[0] + values[1] + values[2])
        return convertedLongDate.toInt()
    }
}