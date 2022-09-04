package com.prasoon.apodkotlinrefactored.presentation.apod_home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.isNotEmpty
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.navigation.NavigationView
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.BOTH_SCREENS
import com.prasoon.apodkotlinrefactored.core.common.Constants.HOME_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.LOCK_SCREEN
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.core.common.Constants.STORAGE_PERMISSION_CODE
import com.prasoon.apodkotlinrefactored.core.common.ScreenPreference
import com.prasoon.apodkotlinrefactored.core.utils.*
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.isRefreshNeededForArchives
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.processHomeApodToArchiveFavorites
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.createBitmapFromCacheFile
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.saveImage
import com.prasoon.apodkotlinrefactored.databinding.FragmentHomeBinding
import com.prasoon.apodkotlinrefactored.databinding.ScreenMenuBinding
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import javax.inject.Inject

@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home),
    NavigationView.OnNavigationItemSelectedListener,
    EasyPermissions.PermissionCallbacks{
    private val TAG = "HomeFragment"
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: ApodViewModel by viewModels()

    private var isAddedToDB = false
    var datePickerString: String? = String()
    var dateFromPendingIntent: String? = null
    var showNotification: Boolean = true

    @Inject
    lateinit var currentApod: Apod

    lateinit var toggle: ActionBarDrawerToggle

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

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

        // Get date from notifications and fetch data from ViewModel
        dateFromPendingIntent = arguments?.getString("date")
        Log.d(TAG, "from pendingIntent: $dateFromPendingIntent")

        // Start with an actual date
        if (!dateFromPendingIntent.isNullOrEmpty()) {
            DateUtils.currentDate = dateFromPendingIntent!!
            showNotification = false    // If Clicked from notification, no need for notifications again
        }
        else if (DateUtils.currentDate.isEmpty())  DateUtils.currentDate = DateUtils.getCurrentDateForInitialization()

        //Log.d(TAG, "viewModel.refresh date from onViewCreated: DateUtils.currentDate: ${DateUtils.currentDate}")

        //viewModel.refresh(DateUtils.currentDate)
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
                    DateUtils.simpleDateFormat = datePickerString
                    binding.homeTextViewDatePicker.text = datePickerString

                    val dateApiFormat = bundle.getString(Constants.CURRENT_DATE_FOR_API)
                    if (dateApiFormat != null) {
                        DateUtils.currentDate = dateApiFormat
                        Log.d(TAG, "viewModel.refresh date from DatePicker: dateApiFormat: $dateApiFormat")
                        viewModel.refresh(dateApiFormat)
                        showNotification = true
                    }
                }
            }
        }

        // Click to refresh
        binding.buttonRetry.setOnClickListener {
            Log.d(TAG, "ButtonRetry refresh for date: ${DateUtils.currentDate}")
            viewModel.refresh(DateUtils.currentDate)
            binding.buttonRetry.visibility = View.INVISIBLE
        }

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
            Log.d(TAG, "imageViewResult")
            if (currentApod.mediaType == "image") {
                val action =
                    HomeFragmentDirections.actionHomeFragmentToViewFragment(currentApod)
                findNavController().navigate(action)
            }
        }

        binding.homeSetWallpaper.setOnClickListener {
            if (currentApod.mediaType == "image") {
                DialogUtils.showBackupDialog(binding.homeImageViewResult, requireContext())
            }
        }

        binding.homeDownloadImage.setOnClickListener {
            // val permissions = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            if (EasyPermissions.hasPermissions(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)) {
                saveImage(requireContext(), currentApod.title, currentApod.date, currentApod.url, currentApod.hdUrl!!)
            } else {
                EasyPermissions.requestPermissions(this, "Grant Storage Permissions to Save Image ",
                    STORAGE_PERMISSION_CODE)
                saveImage(requireContext(), currentApod.title, currentApod.date, currentApod.url, currentApod.hdUrl!!)            }
        }

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
                processHomeApodToArchiveFavorites = true
            } else if (isAddedToDB && !currentApod.mediaType.isEmpty()) {
                viewModel.saveApod(currentApod, false)
                Toast.makeText(activity, "Removed from Favorites!", Toast.LENGTH_SHORT).show()
                binding.homeAddToFavorites.setColorFilter(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.colorRemovedFromFavorites
                    )
                )
                processHomeApodToArchiveFavorites = false
            }
            isAddedToDB = !isAddedToDB
            isRefreshNeededForArchives = true
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodStateLiveData.observe(viewLifecycleOwner) { apodStateLiveData ->
            if (!apodStateLiveData.message.isNullOrEmpty()) {
                Log.d(TAG, "Apod model loading state: ${apodStateLiveData.isLoading}")
                Toast.makeText(context, apodStateLiveData.message, Toast.LENGTH_SHORT).show()
                binding.buttonRetry.visibility = View.VISIBLE
            }
            binding.buttonRetry.visibility = View.INVISIBLE

            currentApod = apodStateLiveData.apod
                DateUtils.currentDate =
                    currentApod.date    // Set the date received from the viewModel

            if (apodStateLiveData.isLoading) {
                Log.d(TAG, "Apod model loading true")
                binding.loader.show()
            } else {
                Log.d(TAG, "Apod model loading false")
                binding.loader.hide()
            }

            if (currentApod.date.isEmpty()) {
                return@observe
            }

            Log.d(TAG, "Apod model received from viewmodel: $currentApod")
            Log.d(TAG, "Web link : ${DateUtils.createApodUrl(currentApod.date)}")
            Log.d(TAG, "Api link : ${DateUtils.createApodUrlApi(currentApod.date)}")

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
                binding.homeSetWallpaper.visibility = View.INVISIBLE

                if (currentApod.url.contains("youtube")) {
                    binding.homeAddToFavorites.visibility = View.VISIBLE
                    val thumbnailUrl =
                        VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(currentApod.url)
                    Log.d(TAG, "YouTube thumbnailUrl: $thumbnailUrl")
                    binding.homeImageViewResult.setImageBitmap(
                        ImageUtils.loadImageUIL(
                            thumbnailUrl,
                            binding.homeImageViewResult,
                            binding.homeProgressImageView,
                            requireContext(),
                            false)
                    )
                } else {
                    // Handling for Apod which is not an image or a YouTube video.
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
                binding.homeSetWallpaper.visibility = View.VISIBLE

                binding.homeImageViewResult.setImageBitmap(
                    ImageUtils.loadImageUIL(
                        currentApod.url,
                        binding.homeImageViewResult,
                        binding.homeProgressImageView,
                        requireContext(),
                    false)
                )

            }
            var url = ""
            if (currentApod.url.contains("youtube")) url = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(currentApod.url)
            else if (currentApod.url.contains("jpg")) url = currentApod.url

            if (url.isNotEmpty() && SHOW_NOTIFICATION && showNotification) {
                CoroutineScope(Dispatchers.IO).launch  {
                    val bitmap = async { createBitmapFromCacheFile(url, requireContext()) }
                    NotificationUtils.displayNotification(requireContext(), currentApod.title, currentApod.date, false, bitmap.await())
                    showNotification = false
                }
            }

            binding.collapsingToolbarLayout.title = currentApod.title
            binding.collapsingToolbarLayout.setCollapsedTitleTypeface(Typeface.DEFAULT_BOLD)
            binding.homeTextViewDatePicker.text = currentApod.date.toSimpleDateFormat()
            val explanationText = currentApod.explanation + if (currentApod.copyright.isNullOrEmpty()) "" else "\nCopyrights©: ${currentApod.copyright}"
            binding.homeTextViewExplanation.text = explanationText
            }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.settings -> {
                Toast.makeText(activity, "settings clicked!", Toast.LENGTH_SHORT).show()
                val action = HomeFragmentDirections.actionHomeFragmentToSettingsFragment()
                findNavController().navigate(action)
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        saveImage(requireContext(), currentApod.title, currentApod.date, currentApod.url, currentApod.hdUrl!!)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            saveImage(requireContext(), currentApod.title, currentApod.date, currentApod.url, currentApod.hdUrl!!)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh(DateUtils.currentDate)
        Log.d(TAG, "viewModel.refresh date from onResume: DateUtils.currentDate: ${DateUtils.currentDate}")
    }
    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.apodStateLiveData.removeObservers(viewLifecycleOwner)
    }
}