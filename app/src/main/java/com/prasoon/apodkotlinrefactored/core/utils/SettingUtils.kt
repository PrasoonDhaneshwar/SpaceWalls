package com.prasoon.apodkotlinrefactored.core.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.work.*
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.ALARM_ONE_TIME_REQUEST_CODE
import com.prasoon.apodkotlinrefactored.core.common.Constants.ALARM_REQUEST_CODE
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_TYPE_ALARM
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY_ALARM
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.getTimeInHoursMinutesSeconds
import com.prasoon.apodkotlinrefactored.worker.AlarmReceiver
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker
import java.text.SimpleDateFormat
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

    if (isSetFromScreenOrFrequency) {
        processAlarm(context, screenType, wallpaperFrequency, scheduleType, !isSet)
        processAlarm(context, screenType, wallpaperFrequency, scheduleType, isSet)
    } else {
        processAlarm(context, screenType, wallpaperFrequency, scheduleType, isSet)
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
            val hourOfTheDay = 10
            val minute = 0
            val repeatIntervalDays = 1L // days

            val timeDiff = calculateTimeDifferenceForInitialDelay(hourOfTheDay, minute, repeatIntervalDays)
            Log.d("SettingsFragment","timeDiff will be scheduled in, ${getTimeInHoursMinutesSeconds(timeDiff)}")
            Toast.makeText(context, "Next wallpaper will be scheduled in, ${getTimeInHoursMinutesSeconds(timeDiff)}", Toast.LENGTH_LONG).show()

            myWorkBuilder = PeriodicWorkRequestBuilder<WallpaperWorker>(
                repeatIntervalDays,
                TimeUnit.DAYS,
/*                flexTime,   // flex interval - worker will run somewhere within this period of time, but at the end of repeating interval
                TimeUnit.MILLISECONDS*/
            )
                .addTag(WallpaperWorker.WORK_NAME).apply {
                    setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
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

fun calculateTimeDifferenceForInitialDelay(hourOfTheDay: Int, minutes: Int, periodInDays: Long): Long {
    // Initialize the calendar with today and the preferred time to run the job.
    val setTime = Calendar.getInstance()
    setTime[Calendar.HOUR_OF_DAY] = hourOfTheDay
    setTime[Calendar.MINUTE] = minutes
    setTime[Calendar.SECOND] = 0

    // Initialize a calendar with now.
    val currentTime = Calendar.getInstance()

    if (setTime.before(currentTime)) {
        setTime.add(Calendar.HOUR_OF_DAY, 24)
    }
    return setTime.timeInMillis - currentTime.timeInMillis
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
            // Create unique work request
            val tenAM =  GregorianCalendar()
            tenAM.set(Calendar.HOUR_OF_DAY, 10)
            tenAM.set(Calendar.MINUTE, 0)
            tenAM.set(Calendar.SECOND, 0)
            val sdf = SimpleDateFormat("HH:mm aa")
            val tenAMFormat = sdf.format(tenAM.time)

            if (isSet) {
                val timeNow = Calendar.getInstance()

                if (timeNow.timeInMillis > tenAM.timeInMillis) {
                    // Set daily wallpaper
                    val oneTimePendingIntent = PendingIntent.getBroadcast(context, ALARM_ONE_TIME_REQUEST_CODE, intent, PendingIntent.FLAG_IMMUTABLE or  PendingIntent.FLAG_UPDATE_CURRENT)
                    alarmManager.set(AlarmManager.RTC_WAKEUP, timeNow.timeInMillis + 1000*60*1, oneTimePendingIntent)
                    Log.d("AlertReceiver","alarmManager set for one time: $alarmManager")
                    Log.d("AlertReceiver","Alarm set for one time at: ${sdf.format(timeNow.time)}")
                    Toast.makeText(context, "Today's Wallpaper will be changing in a minute...", Toast.LENGTH_LONG).show()
                }
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, tenAM.timeInMillis, AlarmManager.INTERVAL_DAY , pendingIntent)
                Log.d("AlertReceiver","alarmManager set for repeating: $alarmManager")
                Log.d("AlertReceiver","Alarm set for: ${WallpaperWorker.WORK_NAME}, Daily Wallpaper for every: $repeatInterval $timeUnit on screen: ${ScreenPreference.getTitle(screenFlag)}")
                Toast.makeText(context, "Next wallpaper is scheduled for $tenAMFormat tomorrow", Toast.LENGTH_LONG).show()
            } else {
                Log.d("AlertReceiver","alarmManager: cancelled: $alarmManager")
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        SCHEDULE_ARCHIVE_WALLPAPER -> {
            if (isSet) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + 5000, wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval) , pendingIntent); // Millisec * Second * Minute
                //alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + 5000, 1000*60*1 , pendingIntent); // Millisec * Second * Minute
                Log.d("AlertReceiver","alarmManager set: $alarmManager")
                Log.d("AlertReceiver","Alarm set for: ${WallpaperWorker.WORK_NAME}, from Archives for every: $repeatInterval $timeUnit on screen: ${ScreenPreference.getTitle(screenFlag)}")
            } else {
                Log.d("AlertReceiver","alarmManager cancelled: $alarmManager")
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
        SCHEDULE_FAVORITES_WALLPAPER -> {
            if (isSet) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, Calendar.getInstance().timeInMillis + 5000, wallpaperFrequency.timeUnit.toMillis(wallpaperFrequency.interval) , pendingIntent); // Millisec * Second * Minute
                Log.d("AlertReceiver","alarmManager set: $alarmManager")
                Log.d("AlertReceiver","Alarm set for: ${WallpaperWorker.WORK_NAME}, from Favorites for every: $repeatInterval $timeUnit on screen: ${ScreenPreference.getTitle(screenFlag)}")
            } else {
                Log.d("AlertReceiver","alarmManager, cancelled: $alarmManager")
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }
}