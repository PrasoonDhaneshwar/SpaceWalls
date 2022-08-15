package com.prasoon.apodkotlinrefactored

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.prasoon.apodkotlinrefactored.core.utils.scheduleDailyWallpaper
import com.prasoon.apodkotlinrefactored.core.utils.screenPreference
import com.prasoon.apodkotlinrefactored.core.utils.setAppTheme
import com.prasoon.apodkotlinrefactored.core.utils.showNotification
import com.prasoon.apodkotlinrefactored.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    private lateinit var navController: NavController
    private lateinit var settingPerf: SharedPreferences


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

        val isScheduleDailyWallpaper = settingPerf.getBoolean("schedule_wallpaper",false)
        //scheduleDailyWallpaper(isScheduleDailyWallpaper)
        Log.i(TAG, "Schedule Daily Wallpaper: $isScheduleDailyWallpaper")

        // Setup app theme.
        settingPerf.getString("display", "system")?.let { setAppTheme(it) }

        // Check for notifications
        val isNotificationSet = settingPerf.getBoolean("notifications", false)
        showNotification(isNotificationSet)
        Log.i(TAG, "Show Notification: $isNotificationSet")

        // Set Screens
        settingPerf.getString("screen", "home_screen")?.let {
            screenPreference(it)
            Log.i(TAG, "Screen Preference : $it")
        }

    }
}