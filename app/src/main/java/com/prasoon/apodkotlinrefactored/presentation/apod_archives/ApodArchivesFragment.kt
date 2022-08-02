package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.DateInput
import com.prasoon.apodkotlinrefactored.databinding.FragmentArchivesBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class ApodArchivesFragment : Fragment(R.layout.fragment_archives) {
    private val TAG = "ArchivesFragment"
    private lateinit var binding: FragmentArchivesBinding
    private val apodListAdapter = ApodArchivesListAdapter(arrayListOf())

    private val viewModel: ApodArchivesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentArchivesBinding.bind(view)
        viewModel.refresh()

        binding.listApod.apply {
            setHasFixedSize(true)
            // layoutManager = LinearLayoutManager(context)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = apodListAdapter
        }

        binding.listApod.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy != 0) {
                    viewModel.refresh()
                }
            }
        })

        binding.listSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            binding.listSwipeRefreshLayout.isRefreshing = false
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodArchivesListLiveData.observe(viewLifecycleOwner) { apodList ->
            Log.i(TAG, "apodArchivesListLiveData: $apodList")

            if (!apodList.isLoading)
                apodListAdapter.updateApods(apodList.apodArchivesList)
            if (!apodList.message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }
}