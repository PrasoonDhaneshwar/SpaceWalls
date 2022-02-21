package com.prasoon.apodkotlin.utils

object Constants {
    val APOD_DATABASE_NAME = "apod_database"
    val BASE_URL = "https://api.nasa.gov/"

    var INTENT_ACTION_VIEW = 1
    var INTENT_ACTION_SEND = 2

    val STORAGE_PERMISSION_CODE = 1
    // Night Mode preferences
    val NIGHT_MODE = "nightMode"

    val IMAGE_URL = "currentApod.url"
    val IMAGE_HD_URL = "currentApod.hdurl"
    val CURRENT_DATE = " DateInput.currentDate"
    val IMAGE_NAME = " imageName"
    val STORAGE_DIRECTORY_PATH = " storageDirectoryPath"
    val MESSAGE_ID = 1001
    val MESSAGE_CHANNEL = "image_download_channel"
    val TASK_NOTIFICATION = "image_download_task_notification"
}