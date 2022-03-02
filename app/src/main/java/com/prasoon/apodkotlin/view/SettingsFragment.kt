package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.preference.PreferenceFragmentCompat
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.utils.loadImage
import kotlinx.android.synthetic.main.fragment_view.*

class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "SettingsFragment"

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}