package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.Constants.INTENT_ACTION_SEND
import com.prasoon.apodkotlin.model.Constants.INTENT_ACTION_VIEW
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {
    private val TAG = "DetailFragment"

    // Get arguments back from the nav graph
    private val args: DetailFragmentArgs by navArgs()
    var apodIdDetail = 0
    private lateinit var viewModel: ApodViewModel
    private lateinit var apod: ApodModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ApodViewModel::class.java)
        apodIdDetail = args.apodId

        if (apodIdDetail != 0) {
            viewModel.getApodDetailFromDb(apodIdDetail)
        }

        // Enable scrolling for explanation
        detail_text_view_explanation.setMovementMethod(ScrollingMovementMethod())

        video_view_button.setOnClickListener {
            performActionIntent(requireContext(), apod.url, INTENT_ACTION_VIEW)
        }

        share_item.setOnClickListener {
            performActionIntent(requireContext(), apod.url, INTENT_ACTION_SEND)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodDetailLoaded.observe(viewLifecycleOwner, Observer { isComplete ->
            Toast.makeText(activity, "Apod Detail loaded", Toast.LENGTH_SHORT).show()
        })

        viewModel.apodDetail.observe(viewLifecycleOwner, Observer { apodDetail ->
            apodDetail?.let {
                apod = it
                Log.i(TAG, "observeViewModel apodDetail: $apodDetail")
                Log.i(TAG, "observeViewModel apodDetail id: ${apod.id}")

                if (apod.mediaType.equals("video")) {
                    video_view_button.visibility = View.VISIBLE
                    val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                    Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                    detail_image_view.loadImage(thumbnailUrl, false)

                } else {
                    detail_image_view.visibility = View.VISIBLE
                    Log.i(TAG, "url: ${apod.url}")
                    detail_image_view.loadImage(apod.url, false)
                }

                detail_text_view_title.text = it.title
                detail_text_view_explanation.text = it.explanation
                detail_text_view_date.text = it.date
            }
        })
    }
}