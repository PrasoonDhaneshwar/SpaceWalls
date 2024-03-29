package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.DialogUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.loadImage
import com.prasoon.apodkotlinrefactored.core.utils.ShareActionUtils
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils.getYoutubeThumbnailUrlFromVideoUrl
import com.prasoon.apodkotlinrefactored.databinding.FragmentDetailBinding
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject


@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {
    private val TAG = "DetailFragment"
    private lateinit var binding: FragmentDetailBinding
    private val viewModel: ApodDetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()
    @Inject
    lateinit var currentApod: Apod
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentDetailBinding.bind(view)
        binding.detailToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.detailToolbar.setNavigationOnClickListener (View.OnClickListener { requireActivity().onBackPressed() })

        viewModel.getApodDetailFromDb(args.apodDate)

        binding.detailShareItem.setOnClickListener {
            if (currentApod.mediaType == "image") {
                ShareActionUtils.performActionIntent(
                    requireContext(),
                    currentApod.url,
                    Constants.INTENT_ACTION_SEND
                )
            }
        }

        binding.detailDownloadImage.setOnClickListener {
            if (EasyPermissions.hasPermissions(
                    requireContext(),
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                ImageUtils.saveImage(requireContext(), currentApod.title, currentApod.date, currentApod.url, currentApod.hdUrl)
            } else {
                EasyPermissions.requestPermissions(
                    this,
                    "Grant Storage Permissions to Save Image ",
                    Constants.STORAGE_PERMISSION_CODE
                )
                ImageUtils.saveImage(requireContext(), currentApod.title, currentApod.date, currentApod.url, currentApod.hdUrl)
            }
        }

        binding.detailSetWallpaper.setOnClickListener {
            if (currentApod.mediaType == "image") {
                DialogUtils.showBackupDialog(binding.detailImageView, requireContext())
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodStateLiveData.observe(viewLifecycleOwner) { apodStateLiveData ->
            if (!apodStateLiveData.message.isNullOrEmpty()) {
                Log.d(TAG, "Apod model loading state: ${apodStateLiveData.isLoading}")
                Toast.makeText(context, apodStateLiveData.message, Toast.LENGTH_SHORT).show()
            }
            currentApod = apodStateLiveData.apod

            Log.d(TAG, "observeViewModel currentApod: $currentApod")
            Log.d(TAG, "observeViewModel currentApod date: ${currentApod.date}")

            if (currentApod.mediaType == "video") {
                binding.detailVideoViewButton.visibility = View.VISIBLE
                val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(currentApod.url)
                Log.d(TAG, "observeViewModel currentApod thumbnailUrl: $thumbnailUrl")

                //binding.detailImageView.loadImage(thumbnailUrl, false, binding.detailProgressImageView)
                binding.detailImageView.setImageBitmap(ImageUtils.loadImageUIL(thumbnailUrl, binding.detailImageView, binding.detailProgressImageView, requireContext()))

            } else {
                binding.detailImageView.visibility = View.VISIBLE
                Log.d(TAG, "url: ${currentApod.url}")
                //binding.detailImageView.loadImage(currentApod.url, false, binding.detailProgressImageView)
                binding.detailImageView.setImageBitmap(ImageUtils.loadImageUIL(currentApod.url, binding.detailImageView, binding.detailProgressImageView, requireContext()))
            }

            binding.detailTextViewTitle.text = currentApod.title
            binding.detailTextViewExplanation.text = currentApod.explanation
            if (currentApod.date.isNotEmpty()) binding.detailTextViewDate.text = currentApod.date.toSimpleDateFormat()
            binding.collapsingToolbarLayout.setTitle(currentApod.title)

        }
    }
}