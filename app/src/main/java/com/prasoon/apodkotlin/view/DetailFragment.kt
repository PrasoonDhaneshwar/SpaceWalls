package com.prasoon.apodkotlin.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.viewmodel.ApodViewModel
import kotlinx.android.synthetic.main.fragment_detail.*

class DetailFragment : Fragment() {

    // Get arguments back from the nav graph
    private val args: DetailFragmentArgs by navArgs()
    var apodIdDetail = 0
    private lateinit var viewModel: ApodViewModel
    private lateinit var apod: ApodModel

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

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodDetailLoaded.observe(viewLifecycleOwner, Observer { isComplete ->
            Toast.makeText(activity, "Apod Detail loaded", Toast.LENGTH_SHORT).show()
        })

        viewModel.apodDetail.observe(viewLifecycleOwner, Observer { apodDetail ->
            apodDetail?.let {
                  apod = it
                Log.i("DetailFragment", "observeViewModel apodDetail")

                detail_image_view.loadImage(apod.hdurl)
                detail_text_view_title.text = it.title
                detail_text_view_explanation.text = it.explanation
                detail_text_view_date.text = it.date
            }
        })
    }
}