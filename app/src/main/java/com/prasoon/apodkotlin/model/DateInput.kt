package com.prasoon.apodkotlin.model

import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

object DateInput {
    var apod:ApodModel? = null
    var currentdate: String? = null
    var simpleDateFormat : String? = null
    var apodAddedToFavorites = false
    var map : HashMap<String, Boolean> = HashMap<String, Boolean>()
}