package com.prasoon.apodkotlin.view

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.prasoon.apodkotlin.R
import kotlinx.android.synthetic.main.fragment_view.*

class ViewFragment : Fragment(R.layout.fragment_view) {
    private val TAG = "ViewFragment"

    private val args by navArgs<ViewFragmentArgs>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val imageUrl = args.imageUrl
        Log.i(TAG, "imageUrl: $imageUrl")
        fragment_image_view.loadImage(imageUrl, true, fragment_view_progress)
    }
}