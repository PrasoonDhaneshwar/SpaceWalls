package com.prasoon.apodkotlinrefactored.presentation.settings

import android.os.Bundle
import android.util.Log
import androidx.preference.CheckBoxPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_TYPE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.TOTAL_FAVORITES
import com.prasoon.apodkotlinrefactored.core.common.Constants.WALLPAPER_FREQUENCY
import com.prasoon.apodkotlinrefactored.core.common.WallpaperFrequency
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.getTenAMFormat
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.scheduleFrequency
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.scheduleWallpaper
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.screenPreference
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.setAppTheme
import com.prasoon.apodkotlinrefactored.core.utils.SettingUtils.showNotification
import com.prasoon.apodkotlinrefactored.data.local.ApodArchiveDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "SettingsFragment"

    @Inject
    lateinit var dbArchive: ApodArchiveDatabase
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
            Log.d(TAG, "displayPref: $newValue")
            setAppTheme(newValue.toString())
            true // return status.
        }

        showNotifications!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            Log.d(TAG, "Notifications are: $isChecked")
            showNotification(isChecked)
            true // return status.
        }

        // SCREENS
        selectScreenPref!!.setOnPreferenceChangeListener { _, newValueOfScreen ->
            Log.d(TAG, "selectScreenPref: $newValueOfScreen")
            val screenPreference = screenPreference(newValueOfScreen.toString())
            scheduleWallpaper(requireContext(), SCHEDULE_TYPE, screenPreference, true, true, WALLPAPER_FREQUENCY)
            true // return status.
        }

        // FREQUENCY
        scheduleFrequencyPref!!.setOnPreferenceChangeListener { _, frequencyArchive ->

            Log.d(TAG, "scheduleFrequencyPref: $frequencyArchive")
            val frequency = scheduleFrequency(frequencyArchive.toString())
            scheduleWallpaper(requireContext(), SCHEDULE_TYPE, SCREEN_PREFERENCE, true, true, frequency)
            true // return status.
        }

        // SCHEDULE_DAILY_WALLPAPER
        scheduleDailyWallpaperPref!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            selectScreenPref.isEnabled = isChecked

            if (!isChecked && (!scheduleArchivePref!!.isEnabled) && (!scheduleFavoritesPref!!.isEnabled) && scheduleDailyWallpaperPref.isChecked)
                scheduleFrequencyPref.isEnabled = false

            scheduleArchivePref?.isEnabled = !isChecked
            scheduleFavoritesPref?.isEnabled = !isChecked && (TOTAL_FAVORITES != 0)
            SCHEDULE_TYPE = SCHEDULE_DAILY_WALLPAPER
            Log.d(TAG, "scheduleDailyWallpaperPref: $isChecked")
            scheduleWallpaper(requireContext(), SCHEDULE_TYPE, SCREEN_PREFERENCE, isChecked, false, WallpaperFrequency.EVERY_DAY)

            scheduleDailyWallpaperPref.isChecked = isChecked
            if (scheduleDailyWallpaperPref.isChecked) scheduleDailyWallpaperPref.summaryOn = "Next Wallpaper is scheduled for ${getTenAMFormat()}" else scheduleDailyWallpaperPref.summaryOff = ""
            true // return status.
        }

        // SCHEDULE_ARCHIVE_WALLPAPER
        scheduleArchivePref!!.setOnPreferenceChangeListener { _, newValue ->
            val isChecked: Boolean = newValue.toString().toBoolean()
            selectScreenPref.isEnabled = isChecked
            scheduleFrequencyPref.isEnabled = isChecked

            scheduleDailyWallpaperPref.isEnabled = !isChecked
            scheduleFavoritesPref?.isEnabled = !isChecked && (TOTAL_FAVORITES != 0)
            Log.d(TAG, "scheduleArchivePref: $isChecked")
            SCHEDULE_TYPE = SCHEDULE_ARCHIVE_WALLPAPER
            scheduleWallpaper(requireContext(), SCHEDULE_TYPE, SCREEN_PREFERENCE, isChecked, false, WALLPAPER_FREQUENCY)

            true // return status.
        }

        // SCHEDULE_FAVORITES_WALLPAPER
        scheduleFavoritesPref!!.setOnPreferenceChangeListener { _, newValue ->

            val isChecked: Boolean = newValue.toString().toBoolean()
            selectScreenPref.isEnabled = isChecked
            scheduleFrequencyPref.isEnabled = isChecked

            scheduleArchivePref.isEnabled = !isChecked
            scheduleDailyWallpaperPref.isEnabled = !isChecked
            Log.d(TAG, "scheduleFavoritesPref: $isChecked")
            SCHEDULE_TYPE = SCHEDULE_FAVORITES_WALLPAPER
            scheduleWallpaper(requireContext(), SCHEDULE_TYPE, SCREEN_PREFERENCE, isChecked, false, WALLPAPER_FREQUENCY)
            true // return status.
        }

        // Set initial state when Fragment starts
        if (scheduleDailyWallpaperPref.isChecked) {
            SCHEDULE_TYPE = SCHEDULE_DAILY_WALLPAPER
            scheduleArchivePref.isEnabled = false
            scheduleFavoritesPref.isEnabled = false
            scheduleFrequencyPref.isEnabled =false
            scheduleDailyWallpaperPref.summaryOn = "Next Wallpaper is scheduled for ${getTenAMFormat()}"
        } else scheduleDailyWallpaperPref.summaryOff = ""

        if (scheduleArchivePref.isChecked) {
            SCHEDULE_TYPE = SCHEDULE_ARCHIVE_WALLPAPER
            scheduleDailyWallpaperPref.isEnabled = false
            scheduleFavoritesPref.isEnabled = false
        }

        if (scheduleFavoritesPref.isChecked) {
            SCHEDULE_TYPE = SCHEDULE_FAVORITES_WALLPAPER
            scheduleDailyWallpaperPref.isEnabled = false
            scheduleArchivePref.isEnabled = false
        }

        if (!scheduleDailyWallpaperPref.isChecked && !scheduleArchivePref.isChecked && !scheduleFavoritesPref.isChecked) {
            scheduleFrequencyPref.isEnabled =false
            selectScreenPref.isEnabled =false
        }

        CoroutineScope(Dispatchers.IO).launch {
            val apodArchiveList = dbArchive.dao.getAllFavoriteArchives(true).map { it.toApodArchive() }
            val favoritesSize = apodArchiveList.size
            withContext(Dispatchers.Main) {
                TOTAL_FAVORITES = favoritesSize
                Log.d(TAG, "Total Favorites: $TOTAL_FAVORITES")
                if (TOTAL_FAVORITES == 0) scheduleFavoritesPref.isEnabled = false
            }
        }
    }
}