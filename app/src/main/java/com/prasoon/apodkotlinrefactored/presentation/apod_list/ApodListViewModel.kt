package com.prasoon.apodkotlinrefactored.presentation.apod_list

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

// Step 22: Create view models and link domain
@HiltViewModel
class ApodListViewModel @Inject constructor(
    val db: ApodDatabase,
    application: Application
): AndroidViewModel(application) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    val apodModelList = MutableLiveData<List<Apod>>()
    val loading = MutableLiveData<Boolean>()

    // Entry point for view
    fun refresh() {
        fetchApodFromDB()
    }

    private fun fetchApodFromDB() {
        // Loading spinner active. Disabled when information is retrieved.
        loading.value = true
        coroutineScope.launch {
            val apod = db.dao.getAllApods(true).map { it.toApod() }
            apodModelList.postValue(apod)
            withContext(Dispatchers.Main) {
                loading.value = false
            }
        }
    }

    fun deleteApodModel(apod: Apod) {
        coroutineScope.launch {
            val apod = db.dao.delete(apod.toApodEntity())
        }
    }

    // To cancel coroutineScope when component moves away from the viewmodel
    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}