package com.prasoon.apodkotlin.view

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.DateInput
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    lateinit var viewModel: ApodViewModel
    private lateinit var currentApod: ApodModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Link corresponding ViewModel to View(this)
        viewModel = ViewModelProviders.of(this).get(ApodViewModel::class.java)
        viewModel.refresh(DateInput.currentdate)

        swipe_refresh_layout.setOnRefreshListener{
            Log.i(TAG, "swipe_refresh_layout date: ${DateInput.currentdate}")
            viewModel.refresh(DateInput.currentdate)
            swipe_refresh_layout.isRefreshing = false
        }

        // Enable scrolling for explanation
        textViewExplanation.setMovementMethod(ScrollingMovementMethod())

        selectListFragment.setOnClickListener{
            Log.i(TAG, "selectListFragment")

            val action = HomeFragmentDirections.actionHomeFragmentToListFragment()
            // Avoid below for compile time safety
            // findNavController().navigate(R.id.action_homeFragment_to_listFragment)
            findNavController().navigate(action)
        }

        val cal = Calendar.getInstance()

        val dateSetListener = DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            Log.i(TAG, "calendar: $year + $monthOfYear + $dayOfMonth")

            // val myFormat = "dd.MM.yyyy" // mention the format you need
            // val myFormat2 = "dd LLL yyyy HH:mm:ss aaa z" // mention the format you need
            val format = "LLL d, yyyy" // mention the format you need

            val simpleDateFormat = SimpleDateFormat(format, Locale.US)
            textViewDatePicker.text = simpleDateFormat.format(cal.time)

            val monthOfYearString = if (monthOfYear+1 < 10) "0" + (monthOfYear + 1) else (monthOfYear + 1).toString()
            val dayOfMonthString = if (dayOfMonth < 10) "0$dayOfMonth" else dayOfMonth.toString()

            DateInput.currentdate = "$year-$monthOfYearString-$dayOfMonthString"
            Log.i(TAG, "calendar date: ${DateInput.currentdate}")
            viewModel.refresh(DateInput.currentdate)
        }


        selectDateButton.setOnClickListener {
            activity?.let { it1 ->
                val datePickerDialog = DatePickerDialog(
                    it1, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(
                        Calendar.DAY_OF_MONTH
                    )
                )

                // Account for Date Picker to show till today's date.
                val halfDayBefore = 1 * 12 * 60 * 60 * 1000L
                val minimumRange = Calendar.getInstance()
                minimumRange.set(Calendar.YEAR, 1995)
                minimumRange.set(Calendar.MONTH, 5)
                minimumRange.set(Calendar.DAY_OF_MONTH, 16)
                datePickerDialog.datePicker.setMinDate(minimumRange.getTimeInMillis());

                datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - halfDayBefore
                datePickerDialog.show()
            }
        }

        addIntoFavorites.setOnClickListener{
            viewModel.saveApod(currentApod)
        }

        observeViewModel()

    }

    private fun observeViewModel() {
        // Observe apod title from viewModel
        viewModel.apodModel.observe(viewLifecycleOwner, Observer { apodModel ->
            apodModel?.let {
                currentApod = apodModel
                if (currentApod.mediaType.equals("video")) {
                    videoViewButton.visibility = View.VISIBLE
                    Log.i(TAG, "observeViewModel apodDetail id: ${currentApod.url}")
                    Log.i(TAG, "observeViewModel apodDetail: $currentApod")
                    imageViewResult.visibility = View.GONE
                    videoViewResult.visibility = View.VISIBLE
                    var videoId = extractYoutubeId(currentApod.url)
                    //loadVideo(videoId)
                    val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(currentApod.url)
                    Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                    videoViewResult.loadImage(thumbnailUrl, false)

                } else {
                    imageViewResult.visibility = View.VISIBLE
                    videoViewResult.visibility = View.GONE
                    videoViewButton.visibility = View.GONE
                    imageViewResult.loadImage(currentApod.url, false)
                }

                textViewTitle.text = currentApod.title
                textViewMetadataDate.text = currentApod.date
                textViewExplanation.text = currentApod.explanation
            }
        })
    }
}