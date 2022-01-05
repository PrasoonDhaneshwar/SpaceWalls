package com.prasoon.apodkotlin.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.prasoon.apodkotlin.model.ApodModel

class ListViewModel: ViewModel() {

    val apodModelList = MutableLiveData<List<ApodModel>>()
    val apodLoadError = MutableLiveData<String?>()
    val loading = MutableLiveData<Boolean>()

    // Entry point for view
    fun refresh() {
        fetchApodByDates()
    }

    private fun fetchApodByDates() {
        // Loading spinner active. Disabled when information is retrieved.
        loading.value = true

    }

}