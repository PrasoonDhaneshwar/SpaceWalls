package com.prasoon.apodkotlin.view

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.viewmodel.ListAction
import com.prasoon.apodkotlin.viewmodel.ListViewModel
import kotlinx.android.synthetic.main.fragment_list.*

class ListFragment : Fragment(), ListAction {
    private val TAG = "ListFragment"

    private val apodListAdapter = ApodListAdapter(arrayListOf(), this, lifecycle)
    private lateinit var viewModel: ListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loading_view.visibility = View.GONE
        list_swipe_refresh_layout.setOnRefreshListener{

            list_swipe_refresh_layout.isRefreshing = false
        }

        apod_list.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = apodListAdapter
        }

        viewModel = ViewModelProviders.of(this).get(ListViewModel::class.java)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.apodModelList.observe(viewLifecycleOwner, Observer {apodList ->
            loading_view.visibility = View.GONE
            apod_list.visibility = View.VISIBLE
            apodListAdapter.updateApods(apodList.sortedByDescending{ it.id})
        })
    }

    private fun goToApodModelDetails(id: Int) {
        val action = ListFragmentDirections.actionListFragmentToDetailFragment(id)
        findNavController().navigate(action)
    }

    override fun onItemClickDetail(id: Int) {
        Log.i(TAG, "onItemClickDetail: $id")
        goToApodModelDetails(id)
    }

    override fun onItemClickDeleted(apodModel: ApodModel) {
        activity?.let {
            AlertDialog.Builder(it)
                .setTitle("Deleting this item...")
                .setMessage("Are you sure?")
                .setPositiveButton("Yes") {p0,p1->
                    Log.i(TAG, "onItemClickDeleted: ${apodModel.id}")
                    viewModel.deleteApodModel(apodModel)
                    viewModel.refresh()
                }
                .setNegativeButton("Cancel", null)
                .create()
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }
}