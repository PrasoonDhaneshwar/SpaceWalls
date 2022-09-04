package com.prasoon.apodkotlinrefactored.worker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.*
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_TYPE_ALARM
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER

class AlertReceiver: BroadcastReceiver() {
    val TAG = "AlertReceiver"

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context != null && intent != null) {
            val message = intent?.getStringExtra("test")

            val scheduleType = intent?.getIntExtra(SCHEDULE_TYPE_ALARM, SCHEDULE_DAILY_WALLPAPER)
            val screenType = intent?.getIntExtra(SCREEN_PREFERENCE_FOR_WORKER, BOTH_SCREENS)
            Log.d(TAG, "AlertReceiver scheduleType: $scheduleType")
            Log.d(TAG, "AlertReceiver screenType: $screenType")

            setWorkRequest(context, screenType!!, scheduleType!!)

            //NotificationUtils.displayNotification(context, "Broadcast", message!!  , false)
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