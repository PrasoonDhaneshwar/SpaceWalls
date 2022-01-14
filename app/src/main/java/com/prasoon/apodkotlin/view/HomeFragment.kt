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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.utils.loadOrCueVideo
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {
    private val TAG = "HomeFragment"
    lateinit var viewModel: ApodViewModel
    lateinit var date: String
    var isDateSet = false
    private lateinit var currentApod: ApodModel
    private lateinit var youTubePlayerView: YouTubePlayerView

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
        viewModel.refresh("null")

        swipe_refresh_layout.setOnRefreshListener{
            if (!isDateSet) viewModel.refresh("null") else viewModel.refresh(date)
            swipe_refresh_layout.isRefreshing = false
        }

        // Enable scrolling for explanation
        textViewExplanation.setMovementMethod(ScrollingMovementMethod())

        youTubePlayerView = view.findViewById(R.id.videoViewResult)
        lifecycle.addObserver(youTubePlayerView)

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

            date = "$year-$monthOfYearString-$dayOfMonthString"
            Log.i(TAG, "calendar date: $date")
            isDateSet = true
            viewModel.refresh(date)
        }


        selectDateButton.setOnClickListener {
            activity?.let { it1 ->
                val datePickerDialog = DatePickerDialog(
                    it1, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
                )

                // Account for Date Picker to show till today's date.
                val oneDayBefore = 1 * 24 * 60 * 60 * 1000L
                datePickerDialog.datePicker.maxDate = System.currentTimeMillis() - oneDayBefore
                datePickerDialog.show()
            }
        }

        addIntoFavorites.setOnClickListener{
            viewModel.saveApod(currentApod)
        }

        imageViewResult.visibility = View.GONE
        videoViewResult.visibility = View.GONE

        observeViewModel()

/*        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
//                val videoId = extractYoutubeId(currentApod.url)
                Log.i(TAG, "observeViewModel apodDetail videoId: tLC6Sy8f06s")

                youTubePlayer.loadVideo("tLC6Sy8f06s", 0f)
                youTubePlayer.play()
            }
        })*/
    }

    private fun observeViewModel() {
        // Observe apod title from viewModel
        viewModel.apodModel.observe(viewLifecycleOwner, Observer { apodModel ->
            apodModel?.let {
                currentApod = apodModel
                if (currentApod.mediaType.equals("video")) {
                    Log.i(TAG, "observeViewModel apodDetail id: ${currentApod.url}")
                    Log.i(TAG, "observeViewModel apodDetail: $currentApod")
                    imageViewResult.visibility = View.GONE
                    videoViewResult.visibility = View.VISIBLE
                    var videoId = extractYoutubeId(currentApod.url)
                    loadVideo(videoId)

                } else {
                    destroyYoutubeVideo()
                    imageViewResult.visibility = View.VISIBLE
                    videoViewResult.visibility = View.GONE
                    imageViewResult.loadImage(apodModel.url)
                }

                textViewTitle.text = currentApod.title
                textViewMetadataDate.text = currentApod.date
                textViewExplanation.text = currentApod.explanation
            }
        })



/*        viewModel.apodAddedToFavorites.observe(viewLifecycleOwner, Observer { isComplete ->
            Toast.makeText(activity, "Added into favorites", Toast.LENGTH_SHORT).show()
            Log.i(TAG, "Added into favorites")

            viewModel.apodAddedToFavorites.value = false
        })*/
    }

    override fun onDestroy() {
        super.onDestroy()
        youTubePlayerView.release()
    }

    private fun loadVideo(id: String) {
        runBlocking {
            youTubePlayerView.initialize(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    Log.i(TAG, "observeViewModel apodDetail videoId: ${id}")

                    youTubePlayer.loadOrCueVideo(lifecycle, id, 0f)
                    // youTubePlayer.play()
                }
            }, true, IFramePlayerOptions.default)
        }

/*            youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                Log.i(TAG, "observeViewModel apodDetail videoId: ${id}")

                youTubePlayer.loadOrCueVideo(lifecycle, id, 0f)
            }
        })*/
    }

    private fun destroyYoutubeVideo() {
         youTubePlayerView.enableAutomaticInitialization = false
    }
}