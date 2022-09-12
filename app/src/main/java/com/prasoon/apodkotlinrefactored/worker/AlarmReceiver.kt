package com.prasoon.apodkotlinrefactored.worker

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import androidx.work.*
import com.prasoon.apodkotlinrefactored.core.common.Constants.ALARM_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.ALARM_REQUEST_CODE
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_TYPE_ALARM
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY_ALARM
import com.prasoon.apodkotlinrefactored.core.common.ScheduleType
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.core.utils.processAlarm


class AlarmReceiver: BroadcastReceiver() {
    val TAG = "AlertReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val alarmUp = PendingIntent.getBroadcast(
                context, ALARM_REQUEST_CODE,
                Intent("com.prasoon.apod.START_ALARM"),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            ) != null

            if (alarmUp) {
                Log.d(TAG, "Alarm is already active")
            }
            Log.d(TAG, "Intent info: $intent")


            if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
                Log.d(TAG, "System boot occurred, reset alarm preferences")
                val alarmPreference: SharedPreferences = context.getSharedPreferences(ALARM_PREFERENCE, Context.MODE_PRIVATE)

                val screenType = alarmPreference.getInt(SCREEN_PREFERENCE_FOR_WORKER, BOTH_SCREENS)
                val scheduleType = alarmPreference.getInt(SCHEDULE_TYPE_ALARM, SCHEDULE_DAILY_WALLPAPER)
                val frequency = alarmPreference.getLong(WALLPAPER_FREQUENCY_ALARM, AlarmManager.INTERVAL_DAY)
                val wallpaperFrequency = WallpaperFrequency.getEnum(frequency)
                Log.d(TAG, "SharedPreferences screenType: ${ScreenPreference.getTitle(screenType)}, scheduleType: ${ScheduleType.getTitle(scheduleType)}, wallpaperFrequency: $wallpaperFrequency")

                // Reset alarms
                processAlarm(context, screenType, wallpaperFrequency, scheduleType, false)
                processAlarm(context, screenType, wallpaperFrequency, scheduleType, true)

            } else {
                val scheduleType = intent.getIntExtra(SCHEDULE_TYPE_ALARM, SCHEDULE_DAILY_WALLPAPER)
                val screenType = intent.getIntExtra(SCREEN_PREFERENCE_FOR_WORKER, BOTH_SCREENS)
                val frequency = intent.getLongExtra(WALLPAPER_FREQUENCY_ALARM, AlarmManager.INTERVAL_DAY)
                Log.d(TAG, "Received setWorkRequest for scheduleType: ${ScheduleType.getTitle(scheduleType)} screenType: ${ScreenPreference.getTitle(screenType)} for every: ${WallpaperFrequency.getEnum(frequency)}")
                setWorkRequest(context, screenType, scheduleType)
            }
        }
    }

    private fun setWorkRequest(context: Context, screenType: Int, scheduleType: Int) {
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()

        val data = Data.Builder()
        data.putInt(SCREEN_PREFERENCE_FOR_WORKER, screenType)

        when (scheduleType) {
            SCHEDULE_DAILY_WALLPAPER -> data.putInt(SCHEDULE_FOR_WORKER, SCHEDULE_DAILY_WALLPAPER)
            SCHEDULE_ARCHIVE_WALLPAPER -> data.putInt(SCHEDULE_FOR_WORKER, SCHEDULE_ARCHIVE_WALLPAPER)
            SCHEDULE_FAVORITES_WALLPAPER -> data.putInt(SCHEDULE_FOR_WORKER, SCHEDULE_FAVORITES_WALLPAPER)
        }

        val myWorkBuilder = OneTimeWorkRequest
            .Builder(WallpaperWorker::class.java)
            .setInputData(data.build())
            .setConstraints(constraints)
            .addTag(WallpaperWorker.WORK_NAME)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WallpaperWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            myWorkBuilder
        )
    }
}