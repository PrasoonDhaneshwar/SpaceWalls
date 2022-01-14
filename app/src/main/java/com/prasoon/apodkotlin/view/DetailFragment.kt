package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {
    private val TAG = "DetailFragment"

    // Get arguments back from the nav graph
    private val args: DetailFragmentArgs by navArgs()
    var apodIdDetail = 0
    private lateinit var viewModel: ApodViewModel
    private lateinit var apod: ApodModel

    private lateinit var youTubePlayerView: YouTubePlayerView

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

        detail_image_view.visibility = View.GONE
        detail_youtube_video_view.visibility = View.GONE

        observeViewModel()

        youTubePlayerView = view.findViewById(R.id.detail_youtube_video_view)
        lifecycle.addObserver(youTubePlayerView)
    }

    private fun observeViewModel() {
        viewModel.apodDetailLoaded.observe(viewLifecycleOwner, Observer { isComplete ->
            Toast.makeText(activity, "Apod Detail loaded", Toast.LENGTH_SHORT).show()
        })

        viewModel.apodDetail.observe(viewLifecycleOwner, Observer { apodDetail ->
            apodDetail?.let {
                apod = it
                Log.i(TAG, "observeViewModel apodDetail id: ${apodDetail.id}")
                Log.i(TAG, "observeViewModel apodDetail: $apodDetail")

                if (apod.mediaType.equals("video")) {
                    detail_image_view.visibility = View.GONE
                    detail_youtube_video_view.visibility = View.VISIBLE
                    youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                        override fun onReady(youTubePlayer: YouTubePlayer) {
                            val videoId = extractYoutubeId(apod.url)
                            youTubePlayer.loadVideo(videoId, 0f)
                        }
                    })
                } else {
                    detail_image_view.visibility = View.VISIBLE
                    detail_youtube_video_view.visibility = View.GONE
                    Log.i(TAG, "url: ${apodDetail.url}")
                    detail_image_view.loadImage(apod.url)
                }

                detail_text_view_title.text = it.title
                detail_text_view_explanation.text = it.explanation
                detail_text_view_date.text = it.date
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        youTubePlayerView.release()
    }
}