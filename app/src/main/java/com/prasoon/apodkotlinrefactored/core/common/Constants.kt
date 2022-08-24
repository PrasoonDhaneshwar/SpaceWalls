package com.prasoon.apodkotlinrefactored.core.common

object Constants {
    val APOD_DATABASE_NAME = "apod_database"
    val BASE_URL = "https://api.nasa.gov/"

    var SHOW_NOTIFICATION: Boolean = false

    val LOAD_APOD_ARCHIVE_FACTOR = 10

    val SELECTED_SIMPLE_DATE_FORMAT = "SELECTED_SIMPLE_DATE_FORMAT"
    val CURRENT_DATE_FOR_API = "CURRENT_DATE_FOR_API"

    var INTENT_ACTION_VIEW = 1
    var INTENT_ACTION_SEND = 2

    val STORAGE_PERMISSION_CODE = 1

    val DOWNLOAD_IMAGE_MESSAGE_ID = 1001
    val DOWNLOAD_IMAGE_CHANNEL = "image_download_channel"
    val DOWNLOAD_IMAGE_TASK_NOTIFICATION = "image_download_task_notification"

    val DAILY_WALLPAPER_MESSAGE_ID = 1002
    val DAILY_WALLPAPER_CHANNEL = "image_download_channel"
    val DAILY_WALLPAPER_TASK_NOTIFICATION = "image_download_task_notification"


    val HOME_SCREEN = 1
    val LOCK_SCREEN = 2
    val BOTH_SCREENS = 3
    var SCREEN_PREFERENCE = HOME_SCREEN

    var FREQUENCY_ARCHIVE = WallpaperFrequency.EVERY_TWO_HOURS

    var SCHEDULE_DAILY_WALLPAPER = 1
    var SCHEDULE_ARCHIVE_WALLPAPER = 2
    var SCHEDULE_FAVORITES_WALLPAPER = 3

}