package com.prasoon.apodkotlinrefactored.presentation.settings

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentSettingsBinding


/*
class SettingsFragment : PreferenceFragmentCompat() {
    private val TAG = "SettingsFragment"
    private lateinit var list :List<String>
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
    }
}*/
class SettingsFragment : Fragment(R.layout.fragment_settings) {
    private val TAG = "SettingsFragment"

    private lateinit var binding: FragmentSettingsBinding
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentSettingsBinding.bind(view)

        binding.darkMode.setOnClickListener {
            Log.i(TAG, "darkMode")

        }
        binding.lightMode.setOnClickListener {
            Log.i(TAG, "lightMode")

        }
        binding.red.setOnClickListener {
            Log.i(TAG, "red")

        }
        binding.blue.setOnClickListener {
            Log.i(TAG, "blue")

        }
    }
}