package com.prasoon.apodkotlinrefactored.presentation.apod_list

import androidx.recyclerview.widget.DiffUtil
import com.prasoon.apodkotlinrefactored.domain.model.Apod


class ApodDiffUtilCallback(oldApodList: List<Apod>, newApodList: List<Apod>) :
    DiffUtil.Callback() {
    private val mOldApodList: List<Apod>
    private val mNewApodList: List<Apod>

    init {
        mOldApodList = oldApodList
        mNewApodList = newApodList
    }

    override fun getOldListSize(): Int {
        return mOldApodList.size
    }

    override fun getNewListSize(): Int {
        return mNewApodList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldApodList[oldItemPosition].date === mNewApodList[newItemPosition].date
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldApod: Apod = mOldApodList[oldItemPosition]
        val newApod: Apod = mNewApodList[newItemPosition]
        return oldApod.date == newApod.date
    }
}