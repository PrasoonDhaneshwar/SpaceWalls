package com.prasoon.apodkotlinrefactored.core.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker
import java.util.*
import java.util.concurrent.TimeUnit


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

fun screenPreference(screenPreference: String): Int {
    when (screenPreference) {
        "home_screen" -> SCREEN_PREFERENCE = HOME_SCREEN
        "lock_screen" -> SCREEN_PREFERENCE = LOCK_SCREEN
        "both_screens" -> SCREEN_PREFERENCE = BOTH_SCREENS
    }
    return SCREEN_PREFERENCE
}

fun scheduleFrequency(frequencyArchive: String): WallpaperFrequency {
    when (frequencyArchive) {
        "fifteen_minutes" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_FIFTEEN_MINUTES
        "thirty_minutes" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_THIRTY_MINUTES
        "one_hour" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_HOUR
        "two_hours" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_TWO_HOURS
        "four_hours" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_FOUR_HOURS
        "half_day" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_TWELVE_HOURS
        "day" -> WALLPAPER_FREQUENCY = WallpaperFrequency.EVERY_DAY
    }
    Log.d("SettingsFragment", "frequencyArchive: $frequencyArchive $WALLPAPER_FREQUENCY")
    return WALLPAPER_FREQUENCY
}


fun scheduleWallpaper(context: Context, scheduleType: Int, screenType: Int, isSet: Boolean, wallpaperFrequency: WallpaperFrequency) {
    when (isSet) {
        true -> setWorkRequest(context, screenType, wallpaperFrequency, scheduleType)
        false -> cancelWorkRequest(context)
    }
}

fun setWorkRequest(context: Context, screenType: Int, wallpaperFrequency: WallpaperFrequency, scheduleType: Int) {
    val repeatInterval = wallpaperFrequency.interval
    val timeUnit = wallpaperFrequency.timeUnit

    //val myWorkBuilder = PeriodicWorkRequest.Builder(WallpaperWorker::class.java, 20, TimeUnit.MINUTES).addTag(WallpaperWorker.WORK_NAME)
    val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()

    val myWorkBuilder: PeriodicWorkRequest.Builder
    val data = Data.Builder()
    data.putInt(SCREEN_PREFERENCE_FOR_WORKER, screenType)

    when (scheduleType) {
        SCHEDULE_DAILY_WALLPAPER -> {
            val hourOfTheDay = 17
            val minute = 0
            val repeatIntervalDays = 1L // days

            val flexTime = calculateFlex(hourOfTheDay, minute, repeatIntervalDays)
            Log.d("SettingsFragment","flexTime: $flexTime, in hours: ${flexTime / (1000 * 60 * 60)}")

            myWorkBuilder = PeriodicWorkRequestBuilder<WallpaperWorker>(
                repeatIntervalDays,
                TimeUnit.DAYS,
                flexTime,   // flex interval - worker will run somewhere within this period of time, but at the end of repeating interval
                TimeUnit.MILLISECONDS
            )
                .addTag(WallpaperWorker.WORK_NAME).apply {
                    setConstraints(constraints)
                }
            data.putInt(SCHEDULE_FOR_WORKER, SCHEDULE_DAILY_WALLPAPER)
            myWorkBuilder.setInputData(data.build())

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WallpaperWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                myWorkBuilder.build()
            )
            Log.d("SettingsFragment","setWorkRequest: ${WallpaperWorker.WORK_NAME}, Daily Wallpaper for every $repeatIntervalDays ${TimeUnit.DAYS} on screen: $screenType")
        }

        SCHEDULE_ARCHIVE_WALLPAPER -> {
            myWorkBuilder =
                PeriodicWorkRequestBuilder<WallpaperWorker>(repeatInterval, timeUnit).addTag(
                    WallpaperWorker.WORK_NAME
                ).apply {
                    //setInitialDelay(repeatInterval, timeUnit)
                    setConstraints(constraints)
                }
            data.putInt(SCHEDULE_FOR_WORKER, SCHEDULE_ARCHIVE_WALLPAPER)
            myWorkBuilder.setInputData(data.build())

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WallpaperWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                myWorkBuilder.build()
            )
            Log.d("SettingsFragment","setWorkRequest: ${WallpaperWorker.WORK_NAME}, from Archives for every $repeatInterval $timeUnit on screen: $screenType")

        }

        SCHEDULE_FAVORITES_WALLPAPER -> {
            myWorkBuilder =
                PeriodicWorkRequestBuilder<WallpaperWorker>(repeatInterval, timeUnit).addTag(
                    WallpaperWorker.WORK_NAME
                ).apply {
                    //setInitialDelay(repeatInterval, timeUnit)
                    setConstraints(constraints)
                }
            data.putInt(SCHEDULE_FOR_WORKER, SCHEDULE_FAVORITES_WALLPAPER)
            myWorkBuilder.setInputData(data.build())

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WallpaperWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                myWorkBuilder.build()
            )
            Log.d("SettingsFragment","setWorkRequest: ${WallpaperWorker.WORK_NAME}, from Favorites for every $repeatInterval $timeUnit on screen: $screenType")
        }
    }
}

private fun cancelWorkRequest(context: Context) {
    Log.d("SettingsFragment", "cancelWorkRequest: ${WallpaperWorker.WORK_NAME}")
    WorkManager.getInstance(context).cancelUniqueWork(WallpaperWorker.WORK_NAME)
}

fun calculateFlex(hourOfTheDay: Int, minutes: Int, periodInDays: Long): Long {
    // Initialize the calendar with today and the preferred time to run the job.
    val cal1 = Calendar.getInstance()
    cal1[Calendar.HOUR_OF_DAY] = hourOfTheDay
    cal1[Calendar.MINUTE] = minutes
    cal1[Calendar.SECOND] = 0

    // Initialize a calendar with now.
    val cal2 = Calendar.getInstance()

    if (cal2.timeInMillis < cal1.timeInMillis) {
        // Add the worker periodicity.
        cal2.timeInMillis = cal2.timeInMillis + TimeUnit.DAYS.toMillis(periodInDays)
    }

    val delta = cal2.timeInMillis - cal1.timeInMillis

    return if (delta > PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS) delta else PeriodicWorkRequest.MIN_PERIODIC_FLEX_MILLIS
}