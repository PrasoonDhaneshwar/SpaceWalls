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
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_apod.view.*

//                                                                          2. Extend holder
//                      4. Populate objects
class ApodListAdapter(
    var apodModels: ArrayList<ApodModel>,
    val actions: ListAction,
    val lifecycle: Lifecycle
) : RecyclerView.Adapter<ApodListAdapter.ApodViewHolder>() {
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
        holder.bind(apodModels[position])
    }

    override fun getItemCount() = apodModels.size

    // 1. Create Holder
    // make it inner to access ListAction
    inner class ApodViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val itemImageView = view.item_apod_image
        private val videoImageView = view.item_apod_video
        private val itemTitle = view.item_title
        private val itemDate = view.item_date
        private val itemDelete = view.item_delete
        private val layout = view.item_layout


        // ***Binding between view and data
        fun bind(apodModel: ApodModel) {
            Log.i(TAG, "bind: $apodModel")
            Log.i(TAG, "bind url: ${apodModel.url}")

            if (apodModel.mediaType.equals("video")) {
                itemImageView.visibility = View.GONE
                videoImageView.visibility = View.VISIBLE
                val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(apodModel.url)
                Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                videoImageView.loadImage(thumbnailUrl, true)

            } else {
                itemImageView.visibility = View.VISIBLE
                videoImageView.visibility = View.GONE
                itemImageView.loadImage(apodModel.url, true)
            }

            itemTitle.text = apodModel.title
            itemDate.text = apodModel.date

            layout.setOnClickListener {
                Log.i(TAG, "layout clicked for: ${apodModel.id}")
                actions.onItemClickDetail(apodModel.id)
            }

            itemDelete.setOnClickListener {
                Log.i(TAG, "delete clicked for: ${apodModel}")
                actions.onItemClickDeleted(apodModel)
            }
        }
    }

    // 4. update apod list when new information is invoked
    fun updateApods(newApods: List<ApodModel>) {
        Log.i(TAG, "updateApods")
        apodModels.clear()
        apodModels.addAll(newApods)
        notifyDataSetChanged()
    }
}