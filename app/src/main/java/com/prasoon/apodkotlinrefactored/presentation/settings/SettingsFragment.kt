package com.prasoon.apodkotlinrefactored.presentation.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.utils.scheduleDailyWallpaper
import com.prasoon.apodkotlinrefactored.core.utils.setAppTheme
import com.prasoon.apodkotlinrefactored.core.utils.showNotification
import com.prasoon.apodkotlinrefactored.worker.WallpaperWorker
import java.time.Duration
import java.util.concurrent.TimeUnit


class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "SettingsFragment"
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)


        val displayPref: Preference? = findPreference("display")
        displayPref!!.setOnPreferenceChangeListener { _, newValue ->
            Log.i(TAG, "displayPref: $newValue")
            setAppTheme(newValue.toString())
            true // return status.
        }

        val selectScreenPref: Preference? = findPreference("screen")
        selectScreenPref!!.setOnPreferenceChangeListener { _, newValue ->
            Log.i(TAG, "selectScreenPref: $newValue")
            true // return status.
        }

        val scheduleDailyWallpaperPref: Preference? = findPreference("schedule_wallpaper")
        scheduleDailyWallpaperPref!!.setOnPreferenceChangeListener { _, newValue ->

            val isChecked: Boolean = newValue.toString().toBoolean()
            Log.i(TAG, "scheduleDailyWallpaperPref: $isChecked")
            scheduleDailyWallpaper(requireContext(), isChecked)
            true // return status.
        }

        val showNotifications: Preference? = findPreference("notifications")
        showNotifications!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            Log.i(TAG, "Notifications are: $isChecked")
            showNotification(isChecked)
            true // return status.
        }
    }
}