package com.prasoon.apodkotlinrefactored.presentation.view

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils
import com.prasoon.apodkotlinrefactored.core.utils.DialogUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ShareActionUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentViewBinding
import com.prasoon.apodkotlinrefactored.databinding.ScreenMenuBinding
import com.prasoon.apodkotlinrefactored.worker.AlertReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pub.devrel.easypermissions.EasyPermissions
import java.util.Calendar


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
            ImageUtils.loadImageUIL(
                apod.url,
                binding.fragmentImageView,
                binding.fragmentViewProgress,
                requireContext(),
                false
            )
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