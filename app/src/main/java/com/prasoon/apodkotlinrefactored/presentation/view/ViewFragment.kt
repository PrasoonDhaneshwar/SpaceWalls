package com.prasoon.apodkotlinrefactored.presentation.view

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.utils.DialogUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ShareActionUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentViewBinding
import pub.devrel.easypermissions.EasyPermissions


class ViewFragment : Fragment(R.layout.fragment_view) {
    private val TAG = "ViewFragment"
    private lateinit var binding: FragmentViewBinding

    private val args by navArgs<ViewFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentViewBinding.bind(view)
        val apod = args.apod

        Log.d(TAG, "imageUrl: $apod.url")
        binding.fragmentImageView.setImageBitmap(
            ImageUtils.loadImageUIL(apod.url, binding.fragmentImageView, binding.fragmentViewProgress, requireContext())
        )

        binding.viewToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.viewToolbar.setNavigationOnClickListener(View.OnClickListener { requireActivity().onBackPressed() })

        binding.fragmentDownloadImage.setOnClickListener {
            if (EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ImageUtils.saveImage(requireContext(), apod.title, apod.date, apod.url, apod.hdUrl)
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Storage Permissions to Save Image ",
                    Constants.STORAGE_PERMISSION_CODE
                )
                ImageUtils.saveImage(requireContext(), apod.title, apod.date, apod.url, apod.hdUrl)
            }
        }

        binding.fragmentShareItem.setOnClickListener {
            if (apod.mediaType == "image") {
                ShareActionUtils.performActionIntent(
                    requireContext(),
                    apod.url,
                    Constants.INTENT_ACTION_SEND
                )
            }
        }

        binding.fragmentSetWallpaper.setOnClickListener {
            if (apod.mediaType == "image") {
                DialogUtils.showBackupDialog(binding.fragmentImageView, requireContext())
            }
        }
    }
}