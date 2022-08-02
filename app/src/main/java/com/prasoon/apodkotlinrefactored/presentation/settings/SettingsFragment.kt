package com.prasoon.apodkotlinrefactored.presentation.settings

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.prasoon.apodkotlinrefactored.R


class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "SettingsFragment"
    private lateinit var list :List<String>
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}