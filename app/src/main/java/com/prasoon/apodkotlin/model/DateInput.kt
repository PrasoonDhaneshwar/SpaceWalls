package com.prasoon.apodkotlin.model

object DateInput {
    var currentDate: String = String()
    var simpleDateFormat: String? = null

    fun convertDateToInteger(date: String): Int {
        val values = date.split("-")
        val year = values[0].toInt()
        val month = values[1].toInt()
        val day = values[2].toInt()

        return year * 10000 + month * 100 + day
    }
}