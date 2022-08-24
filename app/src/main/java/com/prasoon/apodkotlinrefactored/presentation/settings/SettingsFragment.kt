package com.prasoon.apodkotlinrefactored.presentation.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants.FREQUENCY_ARCHIVE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.core.utils.*

class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "SettingsFragment"
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)

        val scheduleDailyWallpaperPref: CheckBoxPreference? = findPreference("schedule_wallpaper")
        val scheduleArchivePref: CheckBoxPreference? = findPreference("schedule_archive")
        val scheduleFavoritesPref: CheckBoxPreference? = findPreference("schedule_favorites")
        val scheduleFrequencyPref: Preference? = findPreference("frequency")
        val selectScreenPref: Preference? = findPreference("screen")

        val displayPref: Preference? = findPreference("display")
        val showNotifications: Preference? = findPreference("notifications")

        displayPref!!.setOnPreferenceChangeListener { _, newValue ->
            Log.i(TAG, "displayPref: $newValue")
            setAppTheme(newValue.toString())
            true // return status.
        }

        showNotifications!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            Log.i(TAG, "Notifications are: $isChecked")
            showNotification(isChecked)
            true // return status.
        }

        // SCREENS
        selectScreenPref!!.setOnPreferenceChangeListener { _, newValue ->
            Log.i(TAG, "selectScreenPref: $newValue")
            true // return status.
        }

        // FREQUENCY
        scheduleFrequencyPref!!.setOnPreferenceChangeListener { _, frequencyArchive ->

            Log.i(TAG, "scheduleFrequencyPref: $frequencyArchive")
            val frequency = scheduleFrequency(frequencyArchive.toString())
            setPeriodicWorkRequest(requireContext(), frequency, true)
            true // return status.
        }

        // SCHEDULE_DAILY_WALLPAPER
        scheduleDailyWallpaperPref!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            selectScreenPref.isEnabled = isChecked

            if (!isChecked && (!scheduleArchivePref!!.isEnabled) && (!scheduleFavoritesPref!!.isEnabled) && scheduleDailyWallpaperPref.isChecked)
                scheduleFrequencyPref.isEnabled = false

            scheduleArchivePref?.isEnabled = !isChecked
            scheduleFavoritesPref?.isEnabled = !isChecked

            Log.i(TAG, "scheduleDailyWallpaperPref: $isChecked")
            scheduleWallpaper(requireContext(), SCHEDULE_DAILY_WALLPAPER, isChecked, WallpaperFrequency.EVERY_DAY)
            true // return status.
        }

        // SCHEDULE_ARCHIVE_WALLPAPER
        scheduleArchivePref!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            selectScreenPref.isEnabled = isChecked
            scheduleFrequencyPref.isEnabled = isChecked

            scheduleDailyWallpaperPref.isEnabled = !isChecked
            scheduleFavoritesPref?.isEnabled = !isChecked

            Log.i(TAG, "scheduleArchivePref: $isChecked")
            scheduleWallpaper(requireContext(), SCHEDULE_DAILY_WALLPAPER, isChecked, FREQUENCY_ARCHIVE)

            true // return status.
        }

        // SCHEDULE_FAVORITES_WALLPAPER
        scheduleFavoritesPref!!.setOnPreferenceChangeListener { _, newValue ->

            val isChecked: Boolean = newValue.toString().toBoolean()
            selectScreenPref.isEnabled = isChecked
            scheduleFrequencyPref.isEnabled = isChecked

            scheduleArchivePref.isEnabled = !isChecked
            scheduleDailyWallpaperPref.isEnabled = !isChecked
            scheduleWallpaper(requireContext(), SCHEDULE_DAILY_WALLPAPER, isChecked, FREQUENCY_ARCHIVE)

            Log.i(TAG, "scheduleFavoritesPref: $isChecked")
            true // return status.
        }

        if (scheduleDailyWallpaperPref.isChecked) {
            scheduleArchivePref.isEnabled = false
            scheduleFavoritesPref.isEnabled = false
            scheduleFrequencyPref.isEnabled =false
        }

        if (scheduleArchivePref.isChecked) {
            scheduleDailyWallpaperPref.isEnabled = false
            scheduleFavoritesPref.isEnabled = false
        }

        if (scheduleFavoritesPref.isChecked) {
            scheduleDailyWallpaperPref.isEnabled = false
            scheduleArchivePref.isEnabled = false
        }

        if (!scheduleDailyWallpaperPref.isChecked && !scheduleArchivePref.isChecked && !scheduleFavoritesPref.isChecked) {
            scheduleFrequencyPref.isEnabled =false
            selectScreenPref.isEnabled =false
        }
    }
}