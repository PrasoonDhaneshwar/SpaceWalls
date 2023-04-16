package com.prasoon.apodkotlinrefactored.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.ALARM_REQUEST_CODE
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_TYPE_ALARM
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY_ALARM
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.getTenAM
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.getTenAMFormat
import com.prasoon.apodkotlinrefactored.worker.AlarmReceiver
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker
import java.util.*

object SettingUtils {
    val TAG = "SettingUtils"

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
        Log.d(TAG, "frequencyArchive: $WALLPAPER_FREQUENCY")
        return WALLPAPER_FREQUENCY
    }

    fun scheduleWallpaper(context: Context, scheduleType: Int, screenType: Int, isSet: Boolean, isSetFromScreenOrFrequency: Boolean, wallpaperFrequency: WallpaperFrequency) {
        val alarmPreference: SharedPreferences = context.getSharedPreferences(
            Constants.ALARM_PREFERENCE,
            Context.MODE_PRIVATE
        )
        val alarmPreferencesEditor: SharedPreferences.Editor = alarmPreference.edit()
        alarmPreferencesEditor.putInt(SCHEDULE_TYPE_ALARM, scheduleType)
        alarmPreferencesEditor.putInt(SCREEN_PREFERENCE_FOR_WORKER, screenType)
        alarmPreferencesEditor.putLong(WALLPAPER_FREQUENCY_ALARM, wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval))
        alarmPreferencesEditor.apply()

        if (isSetFromScreenOrFrequency && scheduleType != SCHEDULE_DAILY_WALLPAPER) {
            processAlarm(context, screenType, wallpaperFrequency, scheduleType, !isSet)
            processAlarm(context, screenType, wallpaperFrequency, scheduleType, isSet)
        } else if (!isSetFromScreenOrFrequency && scheduleType != SCHEDULE_DAILY_WALLPAPER){
            processAlarm(context, screenType, wallpaperFrequency, scheduleType, isSet)
        }

        if (scheduleType == SCHEDULE_DAILY_WALLPAPER) {
            val timeNow = Calendar.getInstance()
            val tenAM = getTenAM()

            if (timeNow.timeInMillis / (1000 * 60 * 1) > tenAM.timeInMillis / (1000 * 60 * 1)) {    // Compare with minutes (Millisecond * Second * Minute)
                val adjustedTimeInMillis =  tenAM.timeInMillis + wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval)
                scheduleAlarmForDailyWallpaper(context, screenType, adjustedTimeInMillis, scheduleType, isSet, true)
            } else {
                val adjustedTimeInMillis =  tenAM.timeInMillis
                scheduleAlarmForDailyWallpaper(context, screenType, adjustedTimeInMillis, scheduleType, isSet, true)
            }
        }
    }

    fun scheduleAlarmForDailyWallpaper(context: Context, screenFlag: Int, triggerAtMillis: Long, scheduleType: Int, isSet: Boolean, isScheduledFromUi: Boolean) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "com.prasoon.apod.START_ALARM"
        intent.putExtra(SCREEN_PREFERENCE_FOR_WORKER, screenFlag)
        intent.putExtra(SCHEDULE_TYPE_ALARM, scheduleType)
        intent.putExtra(WALLPAPER_FREQUENCY_ALARM, AlarmManager.INTERVAL_DAY)
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE or  PendingIntent.FLAG_UPDATE_CURRENT)

        if (isSet) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMillis , pendingIntent)
            Log.d(TAG,"alarmManager set for repeating: $alarmManager")
            Log.d(TAG,"Alarm set for: ${WallpaperWorker.WORK_NAME}, Daily Wallpaper for every: DAY on screen: ${ScreenPreference.getTitle(screenFlag)}, at ${getTenAMFormat()}")
            if (isScheduledFromUi) {
                Toast.makeText(context, "Next wallpaper is scheduled for ${getTenAMFormat()}", Toast.LENGTH_LONG).show()
            }
        } else {
            Log.d(TAG,"alarmManager cancelled: $alarmManager")
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun processAlarm(context: Context, screenFlag: Int, wallpaperFrequency: WallpaperFrequency, scheduleType: Int, isSet: Boolean) {
        val alarmManager: AlarmManager =
            context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.action = "com.prasoon.apod.START_ALARM"
        intent.putExtra(SCREEN_PREFERENCE_FOR_WORKER, screenFlag)
        intent.putExtra(SCHEDULE_TYPE_ALARM, scheduleType)
        intent.putExtra(WALLPAPER_FREQUENCY_ALARM, wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval))
        val pendingIntent = PendingIntent.getBroadcast(context, ALARM_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE or  PendingIntent.FLAG_UPDATE_CURRENT)

        val repeatInterval = wallpaperFrequency.interval
        val timeUnit = wallpaperFrequency.timeUnit

        when (scheduleType) {

            SCHEDULE_DAILY_WALLPAPER -> {
                if (isSet) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval) , pendingIntent)
                    Log.d(TAG,"alarmManager set for repeating: $alarmManager")
                    Log.d(TAG,"Alarm set for: ${WallpaperWorker.WORK_NAME}, Daily Wallpaper for every: $repeatInterval $timeUnit on screen: ${ScreenPreference.getTitle(screenFlag)}")
                } else {
                    Log.d("AlertReceiver","alarmManager cancelled: $alarmManager")
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
            SCHEDULE_ARCHIVE_WALLPAPER -> {
                if (isSet) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval) , pendingIntent); // Millisecond * Second * Minute
                    //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + 5000, 1000*60*1 , pendingIntent); // Millisec * Second * Minute
                    Log.d(TAG,"alarmManager set: $alarmManager")
                    Log.d(TAG,"Alarm set for: ${WallpaperWorker.WORK_NAME}, from Archives for every: $repeatInterval $timeUnit on screen: ${ScreenPreference.getTitle(screenFlag)}")
                } else {
                    Log.d(TAG,"alarmManager cancelled: $alarmManager")
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
            SCHEDULE_FAVORITES_WALLPAPER -> {
                if (isSet) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval) , pendingIntent); // Millisecond * Second * Minute
                    Log.d(TAG,"alarmManager set: $alarmManager")
                    Log.d(TAG,"Alarm set for: ${WallpaperWorker.WORK_NAME}, from Favorites for every: $repeatInterval $timeUnit on screen: ${ScreenPreference.getTitle(screenFlag)}")
                } else {
                    Log.d(TAG,"alarmManager cancelled: $alarmManager")
                    alarmManager.cancel(pendingIntent)
                    pendingIntent.cancel()
                }
            }
        }
    }
}