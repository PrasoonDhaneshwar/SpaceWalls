package com.prasoon.apodkotlinrefactored

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.prasoon.apodkotlinrefactored.databinding.ActivityMainBinding
import com.prasoon.apodkotlinrefactored.presentation.apod_detail.ApodViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    private lateinit var binding: ActivityMainBinding
    private val viewModel: ApodViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.refresh("2022-01-01")
        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.apodStateLiveData.observe(this) { apodStateLiveData ->
            if (!apodStateLiveData.isLoading) {
                Log.i(TAG, "Values gotten for ${apodStateLiveData.apod}")
            }
        }
    }
}