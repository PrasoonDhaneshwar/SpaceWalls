package com.prasoon.apodkotlin.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.ApodInterface
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.activity_main.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val TAG = "MainActivity"
private const val BASE_URL = "https://api.nasa.gov/"


class MainActivity : AppCompatActivity() {
    lateinit var viewModel: ApodViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        // Link corresponding ViewModel to View(this)
        viewModel = ViewModelProviders.of(this).get(ApodViewModel::class.java)
        viewModel.refresh()


        swipe_refresh_layout.setOnRefreshListener{
            viewModel.refresh()
            swipe_refresh_layout.isRefreshing = false
        }
        observeViewModel()
    }

    private fun observeViewModel() {
        // Observe apod title from viewModel
        viewModel.apodModel.observe(this, Observer { apodModel ->
            apodModel?.let {
                imageViewResult.loadImage(apodModel.hdurl)
                textViewTitle.text = apodModel.title
                textViewMetadataDate.text = apodModel.date
                textViewExplanation.text = apodModel.explanation
            }
        })
    }
}