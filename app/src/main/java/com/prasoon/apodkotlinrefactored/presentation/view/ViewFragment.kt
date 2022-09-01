package com.prasoon.apodkotlinrefactored.presentation.view

import android.Manifest
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ShareActionUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentViewBinding
import com.prasoon.apodkotlinrefactored.databinding.ScreenMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        binding.fragmentImageView.setImageBitmap(ImageUtils.loadImageUIL(apod.url, binding.fragmentImageView, binding.fragmentViewProgress, requireContext(), false))


        binding.viewToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.viewToolbar.setNavigationOnClickListener (View.OnClickListener { requireActivity().onBackPressed() })

        binding.fragmentDownloadImage.setOnClickListener {
            if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ImageUtils.saveImage(requireContext(), apod.title, apod.date, apod.url, apod.hdUrl)
            } else {
                EasyPermissions.requestPermissions(this, "Grant Storage Permissions to Save Image ", Constants.STORAGE_PERMISSION_CODE)
                ImageUtils.saveImage(requireContext(), apod.title, apod.date, apod.url, apod.hdUrl)
            }
        }

        binding.fragmentShareItem.setOnClickListener {
            if (apod.mediaType == "image") {
                ShareActionUtils.performActionIntent(requireContext(), apod.url, Constants.INTENT_ACTION_SEND)
            }
        }

        binding.fragmentSetWallpaper.setOnClickListener {
            if (apod.mediaType == "image") {
                showBackupDialog(binding.fragmentImageView, requireContext())
                }
        }
    }
    private fun showBackupDialog(imageView: ImageView, context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)
        Log.d(TAG, "showBackupDialog")

        val mBinding = ScreenMenuBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(mBinding.root)

        if (!mBinding.root.isNotEmpty()) bottomSheetDialog.dismiss()

        bottomSheetDialog.show()
        mBinding.homeScreen.setOnClickListener {
            Log.d(TAG, "Change wallpaper on home screen: ${Constants.HOME_SCREEN}")
            setWallpaperFromBottomSheetDialog(context, imageView, ScreenPreference.HOME_SCREEN)
            bottomSheetDialog.dismiss()
        }
        mBinding.lockScreen.setOnClickListener {
            Log.d(TAG, "Change wallpaper on lock screen: ${Constants.LOCK_SCREEN}")
            setWallpaperFromBottomSheetDialog(context, imageView, ScreenPreference.LOCK_SCREEN)
            bottomSheetDialog.dismiss()
        }
        mBinding.bothScreens.setOnClickListener {
            Log.d(TAG, "Change wallpaper on both screens: $BOTH_SCREENS")
            setWallpaperFromBottomSheetDialog(context, imageView, ScreenPreference.BOTH_SCREENS)
            bottomSheetDialog.dismiss()
        }
    }

    private fun setWallpaperFromBottomSheetDialog(context: Context, imageView: ImageView, screenPreference: ScreenPreference) {
        var isSetWallpaper: Boolean
        CoroutineScope(Dispatchers.IO).launch {
            isSetWallpaper = ImageUtils.setWallpaper(context, imageView, screenPreference.value, null)
            withContext(Dispatchers.Main){
                if (isSetWallpaper) Toast.makeText(context, "Wallpaper set successfully on ${screenPreference.title}", Toast.LENGTH_SHORT).show()
                else Toast.makeText(requireContext(), "Unable to set Wallpaper. Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
}