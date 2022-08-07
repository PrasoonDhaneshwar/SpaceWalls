package com.prasoon.apodkotlinrefactored.presentation.view

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.databinding.FragmentViewBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.net.UnknownHostException


class ViewFragment : Fragment(R.layout.fragment_view) {
    private val TAG = "ViewFragment"
    private lateinit var binding: FragmentViewBinding

    private val args by navArgs<ViewFragmentArgs>()

    lateinit var document : Document
    lateinit var element : Elements
    lateinit var png : String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentViewBinding.bind(view)

        val imageUrl = args.imageUrl
        Log.i(TAG, "imageUrl: $imageUrl")
        binding.fragmentImageView.setImageBitmap(
            ImageUtils.loadImageUIL(
                imageUrl,
                binding.fragmentImageView,
                binding.fragmentViewProgress,
            requireContext()
        ))


        binding.viewToolbar.setNavigationIcon(R.drawable.ic_back)
        binding.viewToolbar.setNavigationOnClickListener (View.OnClickListener { requireActivity().onBackPressed() })

        CoroutineScope(Dispatchers.IO).launch {
            try {
                document = Jsoup.connect("https://apod.nasa.gov/apod/ap220726.html").get()
                element = document.select("center").select("b")
                //png = document.select("iframe[width]").attr("src")
                png = document.select("div[id=\"center\"] strong").toString()
                //png = document.select("iframe[src]").attr("src")


            } catch (e: UnknownHostException) {
                e.printStackTrace();
            }
            withContext(Dispatchers.Main) {
                Log.d(TAG, "words png: $png")
                Log.d(TAG, "words element: ${element.first()?.text()}")
                Log.d(TAG,"words png modified: https://apod.nasa.gov/apod/$png")
            }
        }
    }
}