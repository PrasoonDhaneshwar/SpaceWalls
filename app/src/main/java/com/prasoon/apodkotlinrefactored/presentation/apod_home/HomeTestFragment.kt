package com.prasoon.apodkotlinrefactored.presentation.apod_home

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.navigation.NavigationView
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.DateInput
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toSimpleDateFormat
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

    private var isAddedToDB = false
    var datePickerString: String? = String()

    @Inject
    lateinit var currentApod: Apod


    lateinit var toggle: ActionBarDrawerToggle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeNewBinding.bind(view)

        toggle = ActionBarDrawerToggle(
            activity,
            binding.drawerLayout,
            /*binding.homeToolbar,*/
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        binding.navView.setNavigationItemSelectedListener(this)
        binding.navView.bringToFront()     // Needed for buttons to be clickable

        // Start with an actual date
        val date =
            if (DateInput.currentDate.isEmpty()) DateInput.getCurrentDateForInitialization() else DateInput.currentDate
        viewModel.refresh(date)

        binding.overviewFloatingActionButton.setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            val supportFragmentManager = requireActivity().supportFragmentManager
            // Show the dialog
            datePickerFragment.show(supportFragmentManager, "DatePickerFragment")

            supportFragmentManager.setFragmentResultListener(
                "REQUEST_KEY",
                viewLifecycleOwner
            ) { resultKey, bundle ->
                if (resultKey == "REQUEST_KEY") {

                    datePickerString = bundle.getString(Constants.SELECTED_SIMPLE_DATE_FORMAT)
                    DateInput.simpleDateFormat = datePickerString
                    binding.homeTextViewDatePicker.text = datePickerString

                    val dateApiFormat = bundle.getString(Constants.CURRENT_DATE_FOR_API)
                    if (dateApiFormat != null) {
                        DateInput.currentDate = dateApiFormat
                    }
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

/*        binding.homeScheduleWallpaper.setOnClickListener {
            Log.i(TAG, "imageViewResult")
            if (currentApod.mediaType == "image") {
                //todo
            }
        }*/

        binding.homeAddToFavorites.setOnClickListener {
            if (!isAddedToDB && !currentApod.mediaType.isEmpty()) {
                viewModel.saveApod(currentApod, true)
                Toast.makeText(activity, "Added to Favorites!", Toast.LENGTH_SHORT).show()
                binding.homeAddToFavorites.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorAddToFavorites
                    )
                )
            } else if (isAddedToDB && !currentApod.mediaType.isEmpty()) {
                viewModel.saveApod(currentApod, false)
                Toast.makeText(activity, "Removed from Favorites!", Toast.LENGTH_SHORT).show()
                binding.homeAddToFavorites.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorRemovedFromFavorites
                    )
                )

            }
            isAddedToDB = !isAddedToDB
        }

        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        val date =
            if (datePickerString.isNullOrEmpty() && DateInput.currentDate.isEmpty()) DateInput.getCurrentDateForInitialization() else DateInput.currentDate

        viewModel.refresh(date)
    }

    private fun observeViewModel() {
        viewModel.apodStateLiveData.observe(viewLifecycleOwner) { apodStateLiveData ->
            if (!apodStateLiveData.message.isNullOrEmpty()) {
                Log.i(TAG, "Apod model loading state: ${apodStateLiveData.isLoading}")
                Toast.makeText(context, apodStateLiveData.message, Toast.LENGTH_SHORT).show()
            } else if (!apodStateLiveData.isLoading) {
                currentApod = apodStateLiveData.apod
                DateInput.currentDate =
                    currentApod.date    // Set the date received from the viewModel

                Log.i(TAG, "Apod model received from viewmodel: $currentApod")
                Log.i(TAG, "Web link : ${DateInput.createApodUrl(currentApod.date)}")
                Log.i(TAG, "Api link : ${DateInput.createApodUrlApi(currentApod.date)}")

                if (currentApod.addToFavoritesUI) {
                    binding.homeAddToFavorites.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorAddToFavorites
                        )
                    )
                    isAddedToDB = true
                } else {
                    binding.homeAddToFavorites.setColorFilter(
                        ContextCompat.getColor(
                            requireContext(),
                            R.color.colorRemovedFromFavorites
                        )
                    )
                    isAddedToDB = false
                }

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
                    binding.homeImageViewResult.setImageBitmap(
                        ImageUtils.loadImageUIL(
                            currentApod.url,
                            binding.homeImageViewResult,
                            binding.homeProgressImageView,
                            requireContext()
                        )
                    )
                }


                binding.collapsingToolbarLayout.setTitle(currentApod.title)
                binding.collapsingToolbarLayout.setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
                //binding.homeTextViewTitle.text = currentApod.title
                if (currentApod.date.isNotEmpty()) binding.homeTextViewDatePicker.text =
                    currentApod.date.toSimpleDateFormat()
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