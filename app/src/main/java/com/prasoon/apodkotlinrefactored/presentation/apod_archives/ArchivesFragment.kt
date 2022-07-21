package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.databinding.FragmentDetailBinding

class ArchivesFragment : Fragment(R.layout.fragment_detail) {
    private val TAG = "DetailFragment"
    private lateinit var binding: FragmentDetailBinding


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)

        val imageUrl = null
        Log.i(TAG, "imageUrl: $imageUrl")
        // binding.fragmentImageView.loadImage(imageUrl, true, binding.fragmentViewProgress)
    }
}