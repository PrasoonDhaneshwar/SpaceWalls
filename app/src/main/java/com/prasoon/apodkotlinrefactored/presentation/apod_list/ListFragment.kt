package com.prasoon.apodkotlinrefactored.presentation.apod_list

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.databinding.FragmentListBinding
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list), ListAction {
    private val TAG = "ListFragment"
    private lateinit var binding: FragmentListBinding

    private val apodListAdapter = ApodListAdapter(arrayListOf(), this)
    private val viewModel: ApodListViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Hidden Status Bar
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = FragmentListBinding.bind(view)

        binding.listSwipeRefreshLayout.setOnRefreshListener{
            viewModel.refresh()
            binding.listSwipeRefreshLayout.isRefreshing = false
        }

        binding.listApod.apply {
            layoutManager = LinearLayoutManager(context)
            //layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            adapter = apodListAdapter
        }

        observeViewModel()
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    private fun observeViewModel() {
        viewModel.apodFavoritesLiveData.observe(viewLifecycleOwner) { apodList ->
            binding.listProgress.visibility = View.GONE
            binding.listApod.visibility = View.VISIBLE
            //apodListAdapter.updateApods(apodList.sortedByDescending { it.date.toIntDate() })
            apodListAdapter.updateApodListItems(apodList.sortedByDescending { it.date.toIntDate() })
            if (apodList.size == 0) {
                binding.loadingState.text = "¯\\_(ツ)_/¯"
            } else binding.loadingState.text = ""
        }
    }

    override fun onItemClickDetail(date: String) {
        Log.d(TAG, "onItemClickDetail: $date")
        goToApodModelDetails(date)
    }

    private fun goToApodModelDetails(date: String) {
        val action = ListFragmentDirections.actionListFragmentToDetailFragment(date)
        findNavController().navigate(action)
    }

    override fun onItemClickDeleted(apodArchive: ApodArchive, position: Int): Boolean {
        var isDeleted = false
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Deleting this item...")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes") {p0,p1->
                    Log.d(TAG, "onItemClickDeleted: ${apodArchive.date}")
                    isDeleted = true
                    apodListAdapter.deleteApods(position)
                    viewModel.deleteApodModel(apodArchive)
                    Log.d(TAG, "isDeleted: $isDeleted")
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
        return isDeleted
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}