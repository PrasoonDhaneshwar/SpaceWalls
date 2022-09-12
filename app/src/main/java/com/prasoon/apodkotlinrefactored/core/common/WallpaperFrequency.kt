package com.prasoon.apodkotlinrefactored.core.common

import java.util.concurrent.TimeUnit

enum class WallpaperFrequency(
    val title: String,
    val interval: Long,
    val timeUnit: TimeUnit
) {
    EVERY_FIFTEEN_MINUTES("Fifteen minutes", 15L, TimeUnit.MINUTES),
    EVERY_THIRTY_MINUTES("Thirty minutes", 30L, TimeUnit.MINUTES),
    EVERY_HOUR("One hour", 1L, TimeUnit.HOURS),
    EVERY_TWO_HOURS("Two hours", 2L, TimeUnit.HOURS),
    EVERY_FOUR_HOURS("Four hours", 4L, TimeUnit.HOURS),
    EVERY_TWELVE_HOURS("Half day", 12L, TimeUnit.HOURS),
    EVERY_DAY("Day", 1L, TimeUnit.DAYS);

    companion object{
        fun getEnum(interval: Long): WallpaperFrequency {
            for (i in WallpaperFrequency.values()) {
                if (i.timeUnit.toMillis(i.interval) == interval) return i
            }
            return EVERY_DAY
        }
    }
}