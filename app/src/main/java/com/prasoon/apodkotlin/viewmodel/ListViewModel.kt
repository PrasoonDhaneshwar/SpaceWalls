package com.prasoon.apodkotlin.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.db.ApodDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(
    val db: ApodDao,
    application: Application): AndroidViewModel(application) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


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

    // To cancel coroutineScope when component moves away from the viewmodel
    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}