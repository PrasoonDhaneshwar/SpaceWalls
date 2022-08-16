package com.prasoon.apodkotlinrefactored.core.utils

import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker
import java.util.concurrent.TimeUnit

fun setAppTheme(mode: String) {
    when (mode) {
        "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
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

fun scheduleDailyWallpaper(context: Context, isSet: Boolean) {
    SCHEDULE_DAILY_WALLPAPER = isSet
    when (isSet) {
        true -> setPeriodicWorkRequest(context)
        false -> cancelWorkRequest(context)
    }
}

private fun setPeriodicWorkRequest(context:Context) {
    val repeatInterval: Long = 20
    val timeUnit: TimeUnit = TimeUnit.MINUTES

    Log.i("SettingsFragment", "setPeriodicWorkRequest: ${WallpaperWorker.WORK_NAME} for every $repeatInterval $timeUnit")
    //val myWorkBuilder = PeriodicWorkRequest.Builder(WallpaperWorker::class.java, 20, TimeUnit.MINUTES).addTag(WallpaperWorker.WORK_NAME)

    val myWorkBuilder = PeriodicWorkRequestBuilder<WallpaperWorker>(repeatInterval, timeUnit).addTag(WallpaperWorker.WORK_NAME)
    WorkManager.getInstance(context).enqueueUniquePeriodicWork(WallpaperWorker.WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, myWorkBuilder.build())
}

private fun cancelWorkRequest(context:Context) {
    Log.i("SettingsFragment", "cancelWorkRequest: ${WallpaperWorker.WORK_NAME}")
    WorkManager.getInstance(context).cancelUniqueWork(WallpaperWorker.WORK_NAME)
}