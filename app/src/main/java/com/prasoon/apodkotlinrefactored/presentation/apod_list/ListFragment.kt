package com.prasoon.apodkotlinrefactored.presentation.apod_list

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants.LIST_VIEW_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SELECTED_LIST_DB_FAVORITES
import com.prasoon.apodkotlinrefactored.core.common.Constants.SELECTED_LIST_VIEW
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.databinding.FragmentListBinding
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import com.prasoon.apodkotlinrefactored.presentation.apod_archives.SharedArchiveViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListFragment : Fragment(R.layout.fragment_list), ListAction {
    private val TAG = "ListFragment"
    private lateinit var binding: FragmentListBinding

    private val apodListAdapter = ApodListAdapter(arrayListOf(), this)
    private val viewModel: SharedArchiveViewModel by activityViewModels()
    private lateinit var listPreference: SharedPreferences
    private lateinit var listPreferencesEditor: SharedPreferences.Editor
    var selectedListView = false
    var selectedListDbFavorites = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        listPreference = requireContext().getSharedPreferences(LIST_VIEW_PREFERENCE, Context.MODE_PRIVATE)
        listPreferencesEditor = listPreference.edit()
        selectedListView = listPreference.getBoolean(SELECTED_LIST_VIEW, true)
        selectedListDbFavorites = listPreference.getBoolean(SELECTED_LIST_DB_FAVORITES, true)
        Log.d(TAG, "onCreate selectedListView: $selectedListView, selectedListDbFavorites: $selectedListDbFavorites")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        binding = FragmentListBinding.bind(view)

        val menuListDbItem = binding.toolbar.menu.getItem(0)
        val menuListViewItem = binding.toolbar.menu.getItem(1)

        if (selectedListDbFavorites) {
            binding.toolbar.title = "Favorites"
            menuListDbItem.title = "Wallpaper History"
            menuListDbItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_view_list)
            observeViewModelFavorites()
        } else {
            binding.toolbar.title = "Wallpaper History"
            menuListDbItem.title = "Favorites"
            menuListDbItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_wallpaper_history)
            observeViewModelArchive()
        }

        binding.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                // these ids should match the item ids from menu file
                R.id.favorites -> {
                    Log.d(TAG, "Favorites menu item is clicked: $selectedListDbFavorites")

                    if (selectedListDbFavorites) {
                        observeViewModelFavorites()
                        binding.toolbar.title = "Favorites"
                        menuListDbItem.title = "Wallpaper History"
                        menuListDbItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_baseline_view_list)
                    } else {
                        observeViewModelArchive()
                        binding.toolbar.title = "Wallpaper History"
                        menuListDbItem.title = "Favorites"
                        menuListDbItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_wallpaper_history)
                    }

                    listPreferencesEditor.putBoolean(SELECTED_LIST_DB_FAVORITES, selectedListDbFavorites)
                    listPreferencesEditor.apply()
                    selectedListDbFavorites = !selectedListDbFavorites
                    true
                }

                R.id.listview -> {
                    Log.d(TAG, "selectedGrid: $selectedListView")

                    if (selectedListView) {
                        binding.listApod.layoutManager = LinearLayoutManager(context)
                        menuListViewItem.title = "Grid view"
                        menuListViewItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_view_grid)
                    }
                    else {
                        binding.listApod.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                        menuListViewItem.title = "List view"
                        menuListViewItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_view_carousel)
                    }
                    listPreferencesEditor.putBoolean(SELECTED_LIST_VIEW, selectedListView)
                    listPreferencesEditor.apply()
                    selectedListView = !selectedListView
                    true
                }
                else -> false
            }
        }

        binding.listSwipeRefreshLayout.setOnRefreshListener{
            if (selectedListDbFavorites) viewModel.refreshWallpaperHistory() else viewModel.refreshList()
            binding.listSwipeRefreshLayout.isRefreshing = false
        }

        Log.d(TAG, "onViewCreated selectedGrid: $selectedListView")
        if (selectedListView) {
            menuListViewItem.title = "Grid view"
            menuListViewItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_view_grid)
            binding.listApod.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = apodListAdapter
            }
        }
        else {
            menuListViewItem.title = "List view"
            menuListViewItem.icon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_view_carousel)
            binding.listApod.apply {
                layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                adapter = apodListAdapter
            }
        }
        selectedListView = !selectedListView
        selectedListDbFavorites = !selectedListDbFavorites
    }

    override fun onDetach() {
        super.onDetach()
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        selectedListView = !selectedListView
        selectedListDbFavorites = !selectedListDbFavorites
    }

    private fun observeViewModelFavorites() {
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

    private fun observeViewModelArchive() {
        viewModel.apodWallpaperHistoryLiveData.observe(viewLifecycleOwner) { apodWallpaperHistoryList ->
            binding.listProgress.visibility = View.GONE
            binding.listApod.visibility = View.VISIBLE
            //apodListAdapter.updateApods(apodList.sortedByDescending { it.date.toIntDate() })
            apodListAdapter.updateApodListItems(apodWallpaperHistoryList.sortedByDescending { it.date.toIntDate() })
            if (apodWallpaperHistoryList.size == 0) {
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
                    viewModel.processFavoriteArchivesInDatabase(apodArchive, false)
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
        viewModel.refreshList()
        viewModel.refreshWallpaperHistory()
    }
}