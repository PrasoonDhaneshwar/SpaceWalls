package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.databinding.FragmentArchivesBinding
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*

@AndroidEntryPoint
class ArchivesFragment : Fragment(R.layout.fragment_archives), ArchiveListAction {
    private val TAG = "ArchivesFragment"
    private lateinit var binding: FragmentArchivesBinding
    private val apodListAdapter = ApodArchivesListAdapter(arrayListOf(), this)

    private val viewModel: ApodArchivesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        binding = FragmentArchivesBinding.bind(view)
        viewModel.refresh()

        binding.listApod.apply {
            setHasFixedSize(true)
             layoutManager = LinearLayoutManager(context)
            //layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            //layoutManager = GridLayoutManager(requireContext(), 2)
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

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    private fun observeViewModel() {
        viewModel.apodArchivesListLiveData.observe(viewLifecycleOwner) { apodList ->
            Log.i(TAG, "apodArchivesListLiveData: $apodList")
            if (apodList.isLoading) binding.loader.show()

            if (!apodList.isLoading) {
                binding.loader.hide()
                //apodListAdapter.updateApods(apodList.apodArchivesList)
                apodListAdapter.updateApodArchiveListItems(apodList.apodArchivesList)
            }
            if (!apodList.message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClickDetail(date: String) {
        Log.i(TAG, "onItemClickDetail: $date")
    }

    override fun onItemAddedToFavorites(apodModel: ApodArchive, position: Int, processFavoriteDB: Boolean): Boolean {
        apodListAdapter.addToFavorites(apodModel, position, processFavoriteDB)  // Update in each item of adapter
        viewModel.saveApodArchive(apodModel, processFavoriteDB)                 // Update in DB
        return true
    }
}