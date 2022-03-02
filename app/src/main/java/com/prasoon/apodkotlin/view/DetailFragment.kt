package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.utils.Constants.INTENT_ACTION_SEND
import com.prasoon.apodkotlin.utils.Constants.INTENT_ACTION_VIEW
import com.prasoon.apodkotlin.utils.loadImage
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_detail.*

@AndroidEntryPoint
class DetailFragment : Fragment() {
    private val TAG = "DetailFragment"

    // Get arguments back from the nav graph
    private val args: DetailFragmentArgs by navArgs()
    var apodIdDetail = 0
    private val viewModel: ApodViewModel by viewModels()
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
        apodIdDetail = args.apodId

        if (apodIdDetail != 0) {
            viewModel.getApodDetailFromDb(apodIdDetail)
        }

        // Enable scrolling for explanation
        detail_text_view_explanation.setMovementMethod(ScrollingMovementMethod())

        detail_video_view_button.setOnClickListener {
            performActionIntent(requireContext(), apod.url, INTENT_ACTION_VIEW)
        }

        detail_share_item.setOnClickListener {
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

                if (apod.mediaType == "video") {
                    detail_video_view_button.visibility = View.VISIBLE
                    val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                    Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                    detail_image_view.loadImage(thumbnailUrl, false, detail_progress_image_view)

                } else {
                    detail_image_view.visibility = View.VISIBLE
                    Log.i(TAG, "url: ${apod.url}")
                    detail_image_view.loadImage(apod.url, false, detail_progress_image_view)
                }

                detail_text_view_title.text = it.title
                detail_text_view_explanation.text = it.explanation
                detail_text_view_date.text = it.date
            }
        })
    }
}