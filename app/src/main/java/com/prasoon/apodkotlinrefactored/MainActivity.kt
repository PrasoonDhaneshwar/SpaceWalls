package com.prasoon.apodkotlinrefactored

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.preference.PreferenceManager
import com.prasoon.apodkotlinrefactored.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    private lateinit var navController: NavController


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

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isScheduleDailyWallpaper = prefs.getBoolean("schedule_wallpaper_checkbox_preference",false)
        Log.i("MainActivity", "Schedule Daily Wallpaper: $isScheduleDailyWallpaper")

    }
}