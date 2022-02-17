package com.prasoon.apodkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.prasoon.apodkotlin.model.db.ApodDatabase
import com.prasoon.apodkotlin.model.ApodModel
import kotlinx.coroutines.*

class ListViewModel(application: Application): AndroidViewModel(application) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val db by lazy { ApodDatabase(getApplication()).apodModelDao() }


    val apodModelList = MutableLiveData<List<ApodModel>>()
    val apodLoadError = MutableLiveData<String?>()
    val loading = MutableLiveData<Boolean>()

    // Entry point for view
    fun refresh() {
        fetchApodFromDB()
    }

    private fun fetchApodFromDB() {
        // Loading spinner active. Disabled when information is retrieved.
        loading.value = true
        coroutineScope.launch {
            val apod = db.getAllApods()
            apodModelList.postValue(apod)
            withContext(Dispatchers.Main) {
                loading.value = false
            }
        }
    }

    fun deleteApodModel(apodModel: ApodModel) {
        coroutineScope.launch {
            val apod = db.delete(apodModel)
        }
    }
}