package com.prasoon.apodkotlinrefactored.core.common

import java.util.concurrent.TimeUnit

enum class WallpaperFrequency(
    val title: String,
    val interval: Long,
    val timeUnit: TimeUnit
) {
    EVERY_HOUR("One hour", 1L, TimeUnit.HOURS),
    EVERY_TWO_HOURS("Two hours", 2L, TimeUnit.HOURS),
    EVERY_FOUR_HOURS("Four hours", 4L, TimeUnit.HOURS),
    EVERY_TWELVE_HOURS("Half day", 12L, TimeUnit.HOURS),
    EVERY_DAY("Day", 1L, TimeUnit.DAYS),
}