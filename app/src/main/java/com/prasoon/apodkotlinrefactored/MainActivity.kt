package com.prasoon.apodkotlinrefactored

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.database.CursorWindow
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.common.util.concurrent.ListenableFuture
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY
import com.prasoon.apodkotlinrefactored.core.utils.*
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.scheduleFrequency
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.screenPreference
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.setAppTheme
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.showNotification
import com.prasoon.apodkotlinrefactored.databinding.ActivityMainBinding
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker
import dagger.hilt.android.AndroidEntryPoint
import java.lang.reflect.Field
import java.util.concurrent.ExecutionException


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController
    private lateinit var settingPerf: SharedPreferences

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                // Prompt user to grant exact alarm permission
                val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(intent)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Change back to default theme ApodKotlinRefactored
        setTheme(R.style.Theme_ApodKotlinRefactored_NoActionBar)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup bottomNavigationView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.findNavController()

        binding.bottomNav.setupWithNavController(navController)

        settingPerf = PreferenceManager.getDefaultSharedPreferences(this)
        // Load default values from layout
        PreferenceManager.setDefaultValues(this, R.xml.settings_preference, false)

        var scheduleWallpaperType = "Not yet selected"
        if (settingPerf.getBoolean("schedule_wallpaper",false)) scheduleWallpaperType = "DAILY WALLPAPER"
        if (settingPerf.getBoolean("schedule_archive",false)) scheduleWallpaperType = "ARCHIVES"
        if (settingPerf.getBoolean("schedule_favorites",false)) scheduleWallpaperType = "FAVORITES"
        Log.d(TAG, "Schedule Wallpaper: $scheduleWallpaperType")

        // Setup app theme.
        settingPerf.getString("display", "system")?.let { setAppTheme(it) }

        // Check for notifications
        val isNotificationSet = settingPerf.getBoolean("notifications", false)
        showNotification(isNotificationSet)
        Log.d(TAG, "Show Notification: $isNotificationSet")

        // Set Screens
        settingPerf.getString("screen", "home_screen")?.let {
            SCREEN_PREFERENCE = screenPreference(it)
            Log.d(TAG, "Screen Preference: $it")
        }

        // FREQUENCY_ARCHIVE
        settingPerf.getString("frequency", "two_hours")?.let {
            WALLPAPER_FREQUENCY = scheduleFrequency(it)
        }
        isWorkScheduled(this)

        // Increase cursor size for each row
        try {
            val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
            field.isAccessible = true
            field.set(null, 100 * 1024 * 1024) //the 100MB is the new size
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    private fun isWorkScheduled(context: Context): Boolean {
        val instance = WorkManager.getInstance(context)
        val statuses: ListenableFuture<List<WorkInfo>> = instance.getWorkInfosByTag(WallpaperWorker.WORK_NAME)
        return try {
            var running = false
            val workInfoList: List<WorkInfo> = statuses.get()
            for (workInfo in workInfoList) {
                val state = workInfo.state
                Log.d(TAG, "workInfo: $workInfo state: $state")
                running = (state == WorkInfo.State.RUNNING) or (state == WorkInfo.State.ENQUEUED)
            }
            running
        } catch (e: ExecutionException) {
            e.printStackTrace()
            false
        } catch (e: InterruptedException) {
            e.printStackTrace()
            false
        }
    }
}