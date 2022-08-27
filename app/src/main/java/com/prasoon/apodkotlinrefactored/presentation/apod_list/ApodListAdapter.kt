package com.prasoon.apodkotlinrefactored.presentation.apod_list

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils.getYoutubeThumbnailUrlFromVideoUrl
import com.prasoon.apodkotlinrefactored.databinding.ItemApodBinding
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import com.prasoon.apodkotlinrefactored.presentation.apod_archives.ArchiveDiffUtilCallback

//                                                                                        2. Extend holder
//                      4. Populate objects
class ApodListAdapter(var apodModelList: ArrayList<ApodArchive>, val actions: ListAction) : RecyclerView.Adapter<ApodListAdapter.ApodViewHolder>() {
    private val TAG = "ApodListAdapter"

    // 3. Override methods
    // todo: view may not be val
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val apodBinding = ItemApodBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApodViewHolder(apodBinding)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        Log.d(TAG, "onBindViewHolder")
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
        fun bind(apodArchive: ApodArchive, position: Int) {
            Log.d(TAG, "bind id: ${apodArchive.date} url: ${apodArchive.link}")

            if (apodArchive.link.contains("youtube")) {
                val thumbnailUrl = apodArchive.link
                Log.d(TAG, "observeViewModel ApodListAdapter thumbnailUrl: $thumbnailUrl")

                //itemImageView.loadImage(thumbnailUrl, true, progress)

                //val context = itemImageView.context
                itemImageView.setImageBitmap(ImageUtils.loadImageUIL(thumbnailUrl, itemImageView, progress, itemImageView.context, true))

            } else {
                itemImageView.visibility = View.VISIBLE
                //itemImageView.loadImage(apod.url, true, progress)
                itemImageView.setImageBitmap(ImageUtils.loadImageUIL(apodArchive.link, itemImageView, progress, itemImageView.context, true))
            }

            itemTitle.text = apodArchive.title
            itemDate.text = apodArchive.date.toSimpleDateFormat()

            layout.setOnClickListener {
                Log.d(TAG, "layout clicked for: ${apodArchive.date}")
                actions.onItemClickDetail(apodArchive.date)
            }

            itemDelete.setOnClickListener {
                Log.d(TAG, "delete clicked for: ${apodArchive.date}")
                val isDeleted = actions.onItemClickDeleted(apodArchive, position)
                if (isDeleted) {
                    deleteApods(position)
                }
            }
        }
    }

    fun updateApods(newApods: List<ApodArchive>) {
        Log.d(TAG, "updateApods")
        apodModelList.clear()
        apodModelList.addAll(newApods)
        // Alternative for notifyDataSetChanged()
        val start = 0
        val end = newApods.size - 1;
        //notifyItemRangeRemoved(start, end)
        notifyDataSetChanged()
    }

    fun deleteApods(position: Int) {
        apodModelList.removeAt(position)
        notifyItemRemoved(position)
        notifyItemRangeChanged(position, itemCount)
        // notifyDataSetChanged()
        Log.d(TAG, "delete item size: ${apodModelList.size}")
    }

    // 4. update apod list when new information is invoked
    fun updateApodListItems(newApodList: List<ApodArchive>) {
        Log.d(TAG, "updateApodListItems")
        val diffCallback = ArchiveDiffUtilCallback(this.apodModelList, newApodList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.apodModelList.clear()
        this.apodModelList.addAll(newApodList)
        diffResult.dispatchUpdatesTo(this)
    }
}