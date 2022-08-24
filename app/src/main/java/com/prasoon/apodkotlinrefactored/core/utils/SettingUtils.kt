package com.prasoon.apodkotlinrefactored.core.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.FREQUENCY_ARCHIVE
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker

fun setAppTheme(mode: String) {
    when (mode) {
        "dark" -> {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
        "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
    }
}

fun showNotification(isSet: Boolean) {
    SHOW_NOTIFICATION = isSet
}

fun screenPreference(screenPreference: String) {
    when (screenPreference) {
        "home_screen" -> SCREEN_PREFERENCE = HOME_SCREEN
        "lock_screen" -> SCREEN_PREFERENCE = LOCK_SCREEN
        "both_screens" -> SCREEN_PREFERENCE = BOTH_SCREENS
    }
}
fun scheduleFrequency(frequencyArchive: String): WallpaperFrequency {
    when(frequencyArchive) {
        "one_hour" -> FREQUENCY_ARCHIVE = WallpaperFrequency.EVERY_HOUR
        "two_hours" -> FREQUENCY_ARCHIVE = WallpaperFrequency.EVERY_TWO_HOURS
        "four_hours" -> FREQUENCY_ARCHIVE = WallpaperFrequency.EVERY_FOUR_HOURS
        "half_day" -> FREQUENCY_ARCHIVE = WallpaperFrequency.EVERY_TWELVE_HOURS
        "day" -> FREQUENCY_ARCHIVE = WallpaperFrequency.EVERY_DAY
    }
    Log.i("SettingsFragment", "frequencyArchive: $frequencyArchive $FREQUENCY_ARCHIVE")
    return FREQUENCY_ARCHIVE
}


fun scheduleWallpaper(context: Context, scheduleType: Int, isSet: Boolean, wallpaperFrequency: WallpaperFrequency) {
    when (scheduleType) {
        SCHEDULE_DAILY_WALLPAPER -> {
            when (isSet) {
                true -> setPeriodicWorkRequest(context, wallpaperFrequency, false)
                false -> cancelWorkRequest(context)
            }
        }

        SCHEDULE_ARCHIVE_WALLPAPER -> {
            when (isSet) {
                true -> setPeriodicWorkRequest(context, wallpaperFrequency, true)
                false -> cancelWorkRequest(context)
            }
        }

        SCHEDULE_FAVORITES_WALLPAPER -> {
            when (isSet) {
                true -> setPeriodicWorkRequest(context, wallpaperFrequency, true)
                false -> cancelWorkRequest(context)
            }
        }
    }
}

fun setPeriodicWorkRequest(context:Context, wallpaperFrequency: WallpaperFrequency, delay: Boolean) {
    val repeatInterval = wallpaperFrequency.interval
    val timeUnit = wallpaperFrequency.timeUnit

    Log.i("SettingsFragment", "setPeriodicWorkRequest: ${WallpaperWorker.WORK_NAME} for every $repeatInterval $timeUnit")
    //val myWorkBuilder = PeriodicWorkRequest.Builder(WallpaperWorker::class.java, 20, TimeUnit.MINUTES).addTag(WallpaperWorker.WORK_NAME)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
    val myWorkBuilder = PeriodicWorkRequestBuilder<WallpaperWorker>(repeatInterval, timeUnit).addTag(WallpaperWorker.WORK_NAME).apply {
        if (delay) {
            setInitialDelay(repeatInterval, timeUnit)
        }
        setConstraints(constraints)
    }
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(WallpaperWorker.WORK_NAME, ExistingPeriodicWorkPolicy.REPLACE, myWorkBuilder.build())
}

private fun cancelWorkRequest(context:Context) {
    Log.i("SettingsFragment", "cancelWorkRequest: ${WallpaperWorker.WORK_NAME}")
    WorkManager.getInstance(context).cancelUniqueWork(WallpaperWorker.WORK_NAME)
}