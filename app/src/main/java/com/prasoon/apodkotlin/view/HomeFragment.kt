package com.prasoon.apodkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.activity_main.*

class HomeFragment : Fragment() {
    lateinit var viewModel: ApodViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.activity_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Link corresponding ViewModel to View(this)
        viewModel = ViewModelProviders.of(this).get(ApodViewModel::class.java)
        //viewModel.refresh("null")
        viewModel.refresh("2021-01-01")

        swipe_refresh_layout.setOnRefreshListener{
            viewModel.refresh("null")
            swipe_refresh_layout.isRefreshing = false
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        // Observe apod title from viewModel
        viewModel.apodModel.observe(viewLifecycleOwner, Observer { apodModel ->
            apodModel?.let {
                imageViewResult.loadImage(apodModel.hdurl)
                textViewTitle.text = apodModel.title
                textViewMetadataDate.text = apodModel.date
                textViewExplanation.text = apodModel.explanation
            }
        })
    }
}