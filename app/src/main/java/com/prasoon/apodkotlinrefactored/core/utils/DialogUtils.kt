package com.prasoon.apodkotlinrefactored.core.utils

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.isNotEmpty
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.databinding.ScreenMenuBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object DialogUtils {
    private val TAG = "DialogUtils"

    fun showBackupDialog(imageView: ImageView, context: Context) {
        val bottomSheetDialog = BottomSheetDialog(context)
        Log.d(TAG, "showBackupDialog")

        val mBinding = ScreenMenuBinding.inflate(LayoutInflater.from(context))
        bottomSheetDialog.setContentView(mBinding.root)

        if (!mBinding.root.isNotEmpty()) bottomSheetDialog.dismiss()

        bottomSheetDialog.show()
        mBinding.homeScreen.setOnClickListener {
            setWallpaperFromBottomSheetDialog(context, imageView, ScreenPreference.HOME_SCREEN)
            bottomSheetDialog.dismiss()
        }
        mBinding.lockScreen.setOnClickListener {
            setWallpaperFromBottomSheetDialog(context, imageView, ScreenPreference.LOCK_SCREEN)
            bottomSheetDialog.dismiss()
        }
        mBinding.bothScreens.setOnClickListener {
            setWallpaperFromBottomSheetDialog(context, imageView, ScreenPreference.BOTH_SCREENS)
            bottomSheetDialog.dismiss()
        }
    }

    private fun setWallpaperFromBottomSheetDialog(context: Context, imageView: ImageView, screenPreference: ScreenPreference) {
        Log.d(TAG, "Change wallpaper on ${screenPreference.title}")
        var isSetWallpaper: Boolean
        CoroutineScope(Dispatchers.IO).launch {
            isSetWallpaper = ImageUtils.setWallpaper(context, imageView, screenPreference.value, null)
            withContext(Dispatchers.Main){
                if (isSetWallpaper) Toast.makeText(context, "Wallpaper set successfully on ${screenPreference.title}", Toast.LENGTH_SHORT).show()
                else Toast.makeText(context, "Unable to set Wallpaper. Please try again", Toast.LENGTH_SHORT).show()
            }
        }
    }
}