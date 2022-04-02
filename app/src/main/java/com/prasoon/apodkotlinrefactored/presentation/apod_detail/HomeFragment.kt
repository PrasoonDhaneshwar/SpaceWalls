package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.databinding.FragmentHomeBinding
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment: Fragment(R.layout.fragment_home) {
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: ApodViewModel by viewModels()

    @Inject
    lateinit var currentApod: Apod

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        viewModel.refresh("2022-01-01")
        observeViewModel()

    }

    private fun observeViewModel() {
        viewModel.apodStateLiveData.observe(viewLifecycleOwner) { apodStateLiveData ->
            if (!apodStateLiveData.isLoading) {
                Log.i(TAG, "Values gotten for ${apodStateLiveData.apod}")
            }
        }
    }
}