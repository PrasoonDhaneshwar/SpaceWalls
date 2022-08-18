package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import androidx.recyclerview.widget.DiffUtil
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive


class ArchiveDiffUtilCallback(oldApodArchiveList: List<ApodArchive>, newApodArchiveList: List<ApodArchive>) :
    DiffUtil.Callback() {
    private val mOldApodArchiveList: List<ApodArchive>
    private val mNewApodArchiveList: List<ApodArchive>

    init {
        mOldApodArchiveList = oldApodArchiveList
        mNewApodArchiveList = newApodArchiveList
    }

    override fun getOldListSize(): Int {
        return mOldApodArchiveList.size
    }

    override fun getNewListSize(): Int {
        return mNewApodArchiveList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldApodArchiveList[oldItemPosition].date === mNewApodArchiveList[newItemPosition].date
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldApodArchive: ApodArchive = mOldApodArchiveList[oldItemPosition]
        val newApodArchive: ApodArchive = mNewApodArchiveList[newItemPosition]
        return oldApodArchive.date == newApodArchive.date
    }
}