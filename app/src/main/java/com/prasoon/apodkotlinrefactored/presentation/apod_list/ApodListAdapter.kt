package com.prasoon.apodkotlinrefactored.presentation.apod_list

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.loadImage
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils.getYoutubeThumbnailUrlFromVideoUrl
import com.prasoon.apodkotlinrefactored.databinding.ItemApodBinding
import com.prasoon.apodkotlinrefactored.domain.model.Apod

//                                                                                        2. Extend holder
//                      4. Populate objects
class ApodListAdapter(
    var apodModelList: ArrayList<Apod>,
    val actions: ListAction,
) : RecyclerView.Adapter<ApodListAdapter.ApodViewHolder>() {
    private val TAG = "ApodListAdapter"

    // 3. Override methods
    // todo: view may not be val
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        Log.i(TAG, "onCreateViewHolder")
        val apodBinding = ItemApodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApodViewHolder(apodBinding)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        Log.i(TAG, "onBindViewHolder")
        holder.bind(apodModelList[position], position)
    }

    override fun getItemCount() = apodModelList.size

    // 1. Create Holder
    // make it inner to access ListAction
    inner class ApodViewHolder(apodBinding: ItemApodBinding) : RecyclerView.ViewHolder(apodBinding.root) {
        private val itemImageView = apodBinding.itemApodImage
        private val itemTitle = apodBinding.itemTitle
        private val itemDate = apodBinding.itemDate
        private val itemDelete = apodBinding.itemDelete
        private val layout = apodBinding.itemLayout
        private val progress = apodBinding.itemProgressImageView

        // ***Binding between view and data
        fun bind(apod: Apod, position: Int) {
            Log.i(TAG, "bind id: ${apod.date}")
            Log.i(TAG, "bind url: ${apod.url}")

            if (apod.mediaType == "video") {
                val thumbnailUrl = getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                Log.i(TAG, "observeViewModel apodDetail thumbnailUrl: $thumbnailUrl")

                //itemImageView.loadImage(thumbnailUrl, true, progress)

                //val context = itemImageView.context
                itemImageView.setImageBitmap(ImageUtils.loadImageUIL(thumbnailUrl, itemImageView, progress, itemImageView.context))


            } else {
                itemImageView.visibility = View.VISIBLE
                //itemImageView.loadImage(apod.url, true, progress)
                itemImageView.setImageBitmap(ImageUtils.loadImageUIL(apod.url, itemImageView, progress, itemImageView.context))

            }

            itemTitle.text = apod.title
            itemDate.text = apod.date.toSimpleDateFormat()

            layout.setOnClickListener {
                Log.i(TAG, "layout clicked for: ${apod.date}")
                actions.onItemClickDetail(apod.date)
            }

            itemDelete.setOnClickListener {
                Log.i(TAG, "delete clicked for: ${apod.date}")
                val isDeleted = actions.onItemClickDeleted(apod, position)
                if (isDeleted) {
                    deleteApods(position)
                }
            }
        }
    }

    // 4. update apod list when new information is invoked
    fun updateApods(newApods: List<Apod>) {
        Log.i(TAG, "updateApods")
        apodModelList.clear()
        apodModelList.addAll(newApods)
        // Alternative for notifyDataSetChanged()
        val start = 0
        val end = newApods.size - 1;
        //notifyItemRangeRemoved(start, end)
        // todo: Add diff utils
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