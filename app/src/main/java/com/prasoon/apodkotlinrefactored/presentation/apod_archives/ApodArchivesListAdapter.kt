package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils
import com.prasoon.apodkotlinrefactored.databinding.ItemApodArchiveBinding
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive

//                                                                                        2. Extend holder
//                      4. Populate objects
class ApodArchivesListAdapter(
    var apodDateList: ArrayList<ApodArchive>,
    val actions: ArchiveListAction
) : RecyclerView.Adapter<ApodArchivesListAdapter.ApodViewHolder>() {
    private val TAG = "ApodArchivesListAdapter"

    // 3. Override methods
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApodViewHolder {
        Log.d(TAG, "onCreateViewHolder")
        val apodBinding = ItemApodArchiveBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ApodViewHolder(apodBinding)
    }

    override fun onBindViewHolder(holder: ApodViewHolder, position: Int) {
        holder.bind(apodDateList[position], position)
    }

    override fun getItemCount() = apodDateList.size

    // 1. Create Holder
    // make it inner to access ListAction
    inner class ApodViewHolder(itemApodArchiveBinding: ItemApodArchiveBinding) : RecyclerView.ViewHolder(itemApodArchiveBinding.root) {
        private val itemTitle = itemApodArchiveBinding.itemTitle
        private val itemDate = itemApodArchiveBinding.itemDate
        private val itemImageView = itemApodArchiveBinding.itemApodImage
        private val progress = itemApodArchiveBinding.itemProgressImageView
        private val addToFavorite = itemApodArchiveBinding.addToFavorites

        private var isAddedToDB = false

        // ***Binding between view and data
        fun bind(apod: ApodArchive, position: Int) {
            Log.d(TAG, "bind id: ${apod}")
            itemTitle.text = apod.title
            itemDate.text = apod.date.toSimpleDateFormat()
            itemImageView.setImageBitmap(ImageUtils.loadImageUIL(apod.link, itemImageView, progress, itemImageView.context, true))

            addToFavorite.setOnClickListener {
                if (!isAddedToDB) {
                    Log.d(TAG, "add to favorites: ${apod.date}")
                    actions.onItemAddedToFavorites(apod, position, true)
                    addToFavorite.setImageResource(R.drawable.ic_favorite_fill)

                } else if (isAddedToDB) {
                    Log.d(TAG, "remove from favorites: ${apod.date}")
                    actions.onItemAddedToFavorites(apod, position, false)
                    addToFavorite.setImageResource(R.drawable.ic_baseline_favorite_border)

                }
                isAddedToDB = !isAddedToDB
            }
            if (apod.isAddedToFavorites) {
                isAddedToDB = true
                addToFavorite.setImageResource(R.drawable.ic_favorite_fill)
            } else {
                isAddedToDB = false
                addToFavorite.setImageResource(R.drawable.ic_baseline_favorite_border)
            }
        }
    }

    fun updateApods(newApods: List<ApodArchive>) {
        Log.d(TAG, "updateApods")
        apodDateList.clear()
        apodDateList.addAll(newApods)
        // Alternative for notifyDataSetChanged()
        val start = 0
        val end = newApods.size - 1;
        //notifyItemRangeRemoved(start, end)
        notifyDataSetChanged()
    }

    fun addToFavorites(apod: ApodArchive, position: Int, processFavoriteDB: Boolean) {
        Log.d(TAG, "addToFavorites")
        apodDateList[position].isAddedToFavorites = processFavoriteDB
    }

    // 4. update apod archive list when new information is invoked
    fun updateApodArchiveListItems(newApodArchivesList: List<ApodArchive>) {
        Log.d(TAG, "updateApodArchiveListItems")
        val diffCallback = ArchiveDiffUtilCallback(this.apodDateList, newApodArchivesList)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        this.apodDateList.clear()
        this.apodDateList.addAll(newApodArchivesList)
        diffResult.dispatchUpdatesTo(this)
    }

}