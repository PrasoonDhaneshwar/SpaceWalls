package com.prasoon.apodkotlin.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.prasoon.apodkotlin.R
import kotlinx.android.synthetic.main.fragment_list.*

class ListFragment : Fragment() {
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

        // Long click listener example
        /*
        selectSecondActivity.setOnLongClickListener{
            val action = HomeFragmentDirections.actionHomeFragmentToListFragment()
            // Avoid below for compile time safety
            // findNavController().navigate(R.id.action_homeFragment_to_listFragment)
            findNavController().navigate(action)
            true
        }*/

    }
}