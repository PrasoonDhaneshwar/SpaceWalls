package com.prasoon.apodkotlin.view

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlin.R
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.viewmodel.ListAction
import kotlinx.android.synthetic.main.item_apod.view.*

//                                                                                        2. Extend holder
//                      4. Populate objects
class ApodListAdapter(var apodModelList: ArrayList<ApodModel>, val actions: ListAction) : RecyclerView.Adapter<ApodListAdapter.ApodViewHolder>() {
    private val TAG = "ApodListAdapter"

    // 3. Override methods
    // todo: view may not be val
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        Log.i("ApodListAdapter", "onCreateViewHolder")
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_apod, parent, false)
        return ApodViewHolder(view)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        Log.i("ApodListAdapter", "onBindViewHolder")
        holder.bind(apodModelList[position], position)
    }

    override fun getItemCount() = apodModelList.size

    // 1. Create Holder
    // make it inner to access ListAction
    inner class ApodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemImageView = view.item_apod_image
        private val itemTitle = view.item_title
        private val itemDate = view.item_date
        private val itemDelete = view.item_delete
        private val layout = view.item_layout
        private val progress = view.item_progress_image_view

        // ***Binding between view and data
        fun bind(apodModel: ApodModel, position: Int) {
            Log.i(TAG, "bind id: ${apodModel.id}")
            Log.i(TAG, "bind url: ${apodModel.url}")

            if (apodModel.mediaType == "video") {
                val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(apodModel.url)
                Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                itemImageView.loadImage(thumbnailUrl, true, progress)
            } else {
                itemImageView.visibility = View.VISIBLE
                itemImageView.loadImage(apodModel.url, true, progress)
            }

            itemTitle.text = apodModel.title
            itemDate.text = apodModel.date

            layout.setOnClickListener {
                Log.i(TAG, "layout clicked for: ${apodModel.id}")
                actions.onItemClickDetail(apodModel.id)
            }

            itemDelete.setOnClickListener {
                Log.i(TAG, "delete clicked for: ${apodModel.id}")
                val isDeleted = actions.onItemClickDeleted(apodModel, position)
                if (isDeleted) {
                    deleteApods(position)
                }
            }
        }
    }

    // 4. update apod list when new information is invoked
    fun updateApods(newApods: List<ApodModel>) {
        Log.i(TAG, "updateApods")
        apodModelList.clear()
        apodModelList.addAll(newApods)
        notifyDataSetChanged()
    }

    fun deleteApods(position: Int) {
        apodModelList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        // notifyDataSetChanged()
        Log.i(TAG, "delete item size: ${apodModelList.size}")
    }
}