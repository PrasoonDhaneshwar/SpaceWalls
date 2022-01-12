package com.prasoon.apodkotlin.model

object DateInput {
    var isDateSet = false
    var date: String? = null
    var apodAddedToFavorites = false

    // Date entered
    fun dateSet(date: String, dateSet: Boolean) : Boolean {
        isDateSet = dateSet
        this.date = date
        return isDateSet
    }
}