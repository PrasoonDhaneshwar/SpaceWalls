package com.prasoon.apodkotlinrefactored.presentation.apod_home

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.DateInput
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.loadImage
import com.prasoon.apodkotlinrefactored.core.utils.ShareActionUtils
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentHomeNewBinding
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeTestFragment : Fragment(R.layout.fragment_home_new),
NavigationView.OnNavigationItemSelectedListener {
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeNewBinding
    private val viewModel: ApodViewModel by viewModels()

    @Inject
    lateinit var currentApod: Apod

    var apodDateListDb: List<String> = listOf()

    lateinit var toggle: ActionBarDrawerToggle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeNewBinding.bind(view)

        toggle = ActionBarDrawerToggle(
            activity,
            binding.drawerLayout,
            binding.homeToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.bringToFront()     // Needed for buttons to be clickable

        // Start with an empty date
        viewModel.refresh(DateInput.currentDate)

        binding.homeSelectDateButton.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            val supportFragmentManager = requireActivity().supportFragmentManager
            // Show the dialog
            datePickerFragment.show(supportFragmentManager, "DatePickerFragment")

            supportFragmentManager.setFragmentResultListener(
                "REQUEST_KEY",
                viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == "REQUEST_KEY") {

                    val date = bundle.getString(Constants.SELECTED_SIMPLE_DATE_FORMAT)
                    DateInput.simpleDateFormat = date
                    binding.homeTextViewDatePicker.text = date

                    val dateApiFormat = bundle.getString(Constants.CURRENT_DATE_FOR_API)
                    DateInput.currentDate = dateApiFormat
                    viewModel.refresh(dateApiFormat)
                }
            }
        }

/*        // Swipe to refresh
        binding.homeSwipeRefreshLayout.setOnRefreshListener {
            Log.i(TAG, "Swipe refresh for date: ${DateInput.currentDate}")
            viewModel.refresh(DateInput.currentDate)
            binding.homeSwipeRefreshLayout.isRefreshing = false
        }*/

        binding.homeVideoViewButton.setOnClickListener {
            ShareActionUtils.performActionIntent(
                requireContext(),
                currentApod.url,
                Constants.INTENT_ACTION_VIEW
            )
        }

        binding.homeShareItem.setOnClickListener {
            if (currentApod.mediaType == "image") {
                ShareActionUtils.performActionIntent(
                    requireContext(),
                    currentApod.url,
                    Constants.INTENT_ACTION_SEND
                )
            }
        }

        binding.homeImageViewResult.setOnClickListener {
            Log.i(TAG, "imageViewResult")
            if (currentApod.mediaType == "image") {
                val action =
                    HomeTestFragmentDirections.actionHomeTestFragmentToViewFragment(currentApod.hdUrl!!)
                findNavController().navigate(action)
            }
        }

        binding.homeSetWallpaper.setOnClickListener {
            if (currentApod.mediaType == "image") {
                ImageUtils.setWallpaper(requireContext(), binding.homeImageViewResult)
            }
        }

        binding.homeScheduleWallpaper.setOnClickListener {
            Log.i(TAG, "imageViewResult")
            if (currentApod.mediaType == "image") {
                //todo
            }
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodStateLiveData.observe(viewLifecycleOwner) { apodStateLiveData ->
            if (!apodStateLiveData.isLoading && !apodStateLiveData.message.isNullOrEmpty()) {
                Log.i(TAG, "Apod model loading state: ${apodStateLiveData.isLoading}")
                Toast.makeText(context, apodStateLiveData.message, Toast.LENGTH_SHORT).show()
            } else {
                currentApod = apodStateLiveData.apod
                DateInput.currentDate =
                    currentApod.date    // Set the date received from the viewModel

                Log.i(TAG, "Apod model received from viewmodel: $currentApod")
                Log.i(TAG, "Web link : ${DateInput.createApodUrl(currentApod.date)}")
                Log.i(TAG, "Api link : ${DateInput.createApodUrlApi(currentApod.date)}")

                // VIDEO
                if (currentApod.mediaType == "video") {
                    // Fit center for maintaining YouTube video's aspect ratio
                    binding.homeImageViewResult.scaleType = ImageView.ScaleType.FIT_CENTER

                    binding.homeVideoViewButton.visibility = View.VISIBLE
                    binding.homeDownloadImage.visibility = View.INVISIBLE
                    binding.homeAddToFavorites.visibility = View.INVISIBLE

                    if (currentApod.url.contains("youtube")) {
                        binding.homeAddToFavorites.visibility = View.VISIBLE
                        val thumbnailUrl =
                            VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(currentApod.url)
                        Log.i(TAG, "YouTube thumbnailUrl: $thumbnailUrl")
                        binding.homeImageViewResult.loadImage(
                            thumbnailUrl,
                            false,
                            binding.homeProgressImageView
                        )
                    } else {
                        // Handling for Apod which is not and image or a YouTube video.
                        // Open link with browser
                        ShareActionUtils.performActionIntent(
                            requireContext(),
                            currentApod.url,
                            Constants.INTENT_ACTION_VIEW
                        )
                        // todo - Reset to visible needed for next date set?
                        binding.homeProgressImageView.visibility = View.INVISIBLE

                        binding.homeImageViewResult.setImageResource(R.drawable.handle_another_app)
                    }
                }
                // IMAGE
                else {
                    // Fit center crop to fit aspect ratio of imageview
                    binding.homeImageViewResult.scaleType = ImageView.ScaleType.CENTER_CROP
                    binding.homeDownloadImage.visibility = View.VISIBLE
                    binding.homeImageViewResult.visibility = View.VISIBLE
                    binding.homeVideoViewButton.visibility = View.INVISIBLE
                    binding.homeAddToFavorites.visibility = View.VISIBLE
                    //binding.homeImageViewResult.loadImage(currentApod.url, false, binding.homeProgressImageView)
                    binding.homeImageViewResult.setImageBitmap(
                        ImageUtils.loadImageUIL(
                            currentApod.hdUrl,
                            binding.homeImageViewResult,
                            binding.homeProgressImageView,
                            requireContext()
                        )
                    )
                    //binding.homeImageViewResult.loadImageUILImageView(currentApod.url, binding.homeProgressImageView, requireContext())
                    //binding.homeImageViewResult.loadImageUILImageViewTest(currentApod.url, binding.homeProgressImageView, requireContext())
                }


                binding.collapsingToolbarLayout.setTitle(currentApod.title)
                binding.collapsingToolbarLayout.setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
                //binding.homeTextViewTitle.text = currentApod.title
                // binding.homeTextViewMetadataDate.text = currentApod.date
                binding.homeTextViewExplanation.text = currentApod.explanation
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                Toast.makeText(activity, "settings clicked!", Toast.LENGTH_SHORT).show()
                val action = HomeTestFragmentDirections.actionHomeTestFragmentToSettingsFragment()
                findNavController().navigate(action)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}