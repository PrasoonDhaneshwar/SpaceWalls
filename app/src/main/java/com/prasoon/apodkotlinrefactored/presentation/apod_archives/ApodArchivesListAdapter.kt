package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.databinding.ItemApodArchiveBinding
import com.prasoon.apodkotlinrefactored.domain.model.Apod

//                                                                                        2. Extend holder
//                      4. Populate objects
class ApodArchivesListAdapter(
    var apodDateList: ArrayList<String>,
) : RecyclerView.Adapter<ApodArchivesListAdapter.ApodViewHolder>() {
    private val TAG = "ApodArchivesListAdapter"

    // 3. Override methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        Log.i(TAG, "onCreateViewHolder")
        val apodBinding = ItemApodArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApodViewHolder(apodBinding)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        holder.bind(apodDateList[position], position)
    }

    override fun getItemCount() = apodDateList.size

    // 1. Create Holder
    // make it inner to access ListAction
    inner class ApodViewHolder(apodBinding: ItemApodArchiveBinding) : RecyclerView.ViewHolder(apodBinding.root) {
        private val itemDate = apodBinding.itemDate
        private val itemImageView = apodBinding.itemApodImage
        private val progress = apodBinding.itemProgressImageView


        // ***Binding between view and data
        fun bind(apod: String, position: Int) {
            Log.i(TAG, "bind id: ${apod}")
            itemDate.text = apod
            itemImageView.setImageBitmap(ImageUtils.loadImageUIL(apod, itemImageView, progress, itemImageView.context))
        }
    }

    // 4. update apod list when new information is invoked
    fun updateApods(newApods: List<String>) {
        Log.i(TAG, "updateApods")
        apodDateList.clear()
        apodDateList.addAll(newApods)
        // Alternative for notifyDataSetChanged()
        val start = 0
        val end = newApods.size - 1;
        //notifyItemRangeRemoved(start, end)
        // todo: Add diff utils
        notifyDataSetChanged()
    }
}