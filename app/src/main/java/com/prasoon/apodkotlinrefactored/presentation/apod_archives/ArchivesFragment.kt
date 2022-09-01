package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.isRefreshNeededForArchives
import com.prasoon.apodkotlinrefactored.databinding.FragmentArchivesBinding
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ArchivesFragment : Fragment(R.layout.fragment_archives), ArchiveListAction {
    private val TAG = "ArchivesFragment"
    private lateinit var binding: FragmentArchivesBinding
    private val apodListAdapter = ApodArchivesListAdapter(arrayListOf(), this)

    private val viewModel: SharedArchiveViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.apply {
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
        Log.d(TAG, "onViewCreated isRefreshed $isRefreshNeededForArchives")

        binding = FragmentArchivesBinding.bind(view)

        binding.listApod.apply {
            setHasFixedSize(true)
             //layoutManager = LinearLayoutManager(context)
            layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            //layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = apodListAdapter
        }

        binding.listApod.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!recyclerView.canScrollVertically(1) && dy != 0) {
                    Log.d(TAG, "addOnScrollListener")
                    viewModel.refreshArchive()
                }
            }
        })

        binding.listSwipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshArchive()
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
        viewModel.apodArchiveListStateLiveData.observe(viewLifecycleOwner) { archiveList ->
            Log.d(TAG, "apodArchivesListLiveData: $archiveList")
            if (archiveList.isLoading) binding.loader.show()

            if (!archiveList.isLoading) {
                binding.loader.hide()
                //apodListAdapter.updateApods(archiveList.apodArchivesList)
                apodListAdapter.updateApodArchiveListItems(archiveList.apodArchivesList)
            }
            if (!archiveList.message.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "Unexpected error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onItemClickDetail(date: String) {
        Log.d(TAG, "onItemClickDetail: $date")
    }

    override fun onItemAddedOrRemovedFromFavorites(apodArchiveFromUI: ApodArchive, position: Int, processFavoriteDB: Boolean): Boolean {
        apodListAdapter.addToFavorites(position, processFavoriteDB)  // Update in each item of adapter
        viewModel.processFavoriteArchivesInDatabase(apodArchiveFromUI, processFavoriteDB)                 // Update in DB
        return true
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshArchive()

        if (isRefreshNeededForArchives)
            apodListAdapter.updateItemFromHomeToArchive(DateUtils.currentDate, DateUtils.processHomeApodToArchiveFavorites)
        isRefreshNeededForArchives = false
    }
}