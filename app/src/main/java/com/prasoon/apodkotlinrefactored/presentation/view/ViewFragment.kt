package com.prasoon.apodkotlinrefactored.presentation.view

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.generateRandomDate
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ShareActionUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentViewBinding
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import pub.devrel.easypermissions.EasyPermissions


class ViewFragment : Fragment(R.layout.fragment_view) {
    private val TAG = "ViewFragment"
    private lateinit var binding: FragmentViewBinding

    private val args by navArgs<ViewFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentViewBinding.bind(view)
        val apod = args.apod

        val url = apod.url
        val hdUrl = apod.hdUrl
        val date = apod.date
        Log.d(TAG, "imageUrl: $url")
        binding.fragmentImageView.setImageBitmap(ImageUtils.loadImageUIL(url, binding.fragmentImageView, binding.fragmentViewProgress, requireContext(), false))


        binding.viewToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.viewToolbar.setNavigationOnClickListener (View.OnClickListener { requireActivity().onBackPressed() })

        binding.fragmentDownloadImage.setOnClickListener {
            if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ImageUtils.saveImage(requireContext(), url, hdUrl, date)
            } else {
                EasyPermissions.requestPermissions(this, "Grant Storage Permissions to Save Image ", Constants.STORAGE_PERMISSION_CODE)
                ImageUtils.saveImage(requireContext(), url, hdUrl, date)
            }
        }

        binding.fragmentShareItem.setOnClickListener {
            if (apod.mediaType == "image") {
                ShareActionUtils.performActionIntent(requireContext(), apod.url, Constants.INTENT_ACTION_SEND)
            }
        }

        binding.fragmentSetWallpaper.setOnClickListener {
            if (apod.mediaType == "image") {
                ImageUtils.setWallpaper(requireContext(), binding.fragmentImageView, BOTH_SCREENS, null)
            }
        }
    }
}