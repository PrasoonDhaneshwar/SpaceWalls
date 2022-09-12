package com.prasoon.apodkotlinrefactored.core.common

import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER

enum class ScheduleType(
    val title: String,
    val value: Int,
) {
    DAILY("SCHEDULE DAILY WALLPAPER", SCHEDULE_DAILY_WALLPAPER),
    ARCHIVE("SCHEDULE ARCHIVES", SCHEDULE_ARCHIVE_WALLPAPER),
    FAVORITES("SCHEDULE FAVORITES", SCHEDULE_FAVORITES_WALLPAPER);

    companion object{
        fun getTitle(value: Int): String {
            for (i in ScheduleType.values()) {
                if (i.value == value) {
                    return i.title
                }
            }
            return ""
        }
    }
}