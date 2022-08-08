package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toIntDate
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.loadImage
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils.getYoutubeThumbnailUrlFromVideoUrl
import com.prasoon.apodkotlinrefactored.databinding.FragmentDetailBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {
    private val TAG = "DetailFragment"
    private lateinit var binding: FragmentDetailBinding

    private val viewModel: ApodDetailViewModel by viewModels()

    private val args: DetailFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)
        binding.detailToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.detailToolbar.setNavigationOnClickListener (View.OnClickListener { requireActivity().onBackPressed() })

        val apodDateSelected = args.apodDate.toIntDate()
        viewModel.getApodDetailFromDb(apodDateSelected)

        //binding.collapsingToolbarLayout.setTitle(" ");


        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodDetailLoaded.observe(viewLifecycleOwner) { isComplete ->
            Toast.makeText(activity, "Apod Detail loaded", Toast.LENGTH_SHORT).show()
        }

        viewModel.apodDetail.observe(viewLifecycleOwner){ apodDetail ->
            apodDetail?.let {apod->

                Log.i(TAG, "observeViewModel apodDetail: $apodDetail")
                Log.i(TAG, "observeViewModel apodDetail date: ${apod.date}")

                if (apod.mediaType == "video") {
                    binding.detailVideoViewButton.visibility = View.VISIBLE
                    val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                    Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                    binding.detailImageView.loadImage(thumbnailUrl, false, binding.detailProgressImageView)

                } else {
                    binding.detailImageView.visibility = View.VISIBLE
                    Log.i(TAG, "url: ${apod.url}")
                    //binding.detailImageView.loadImage(apod.url, false, binding.detailProgressImageView)

                    binding.detailImageView.setImageBitmap(
                        ImageUtils.loadImageUIL(
                            apod.url,
                            binding.detailImageView,
                            binding.detailProgressImageView,
                            requireContext()
                        ))

                }

                binding.detailTextViewTitle.text = apod.title
                binding.detailTextViewExplanation.text = apod.explanation
                if (apod.date.isNotEmpty()) binding.detailTextViewDate.text = apod.date.toSimpleDateFormat()
                binding.collapsingToolbarLayout.setTitle(apod.title)
            }
        }
    }
}