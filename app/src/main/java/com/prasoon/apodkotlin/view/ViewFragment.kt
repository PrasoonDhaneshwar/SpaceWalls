package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
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