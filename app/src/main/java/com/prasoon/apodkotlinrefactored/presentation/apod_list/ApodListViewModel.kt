package com.prasoon.apodkotlinrefactored.presentation.apod_list

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.ApodArchiveDatabase
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import javax.inject.Inject

// Step 22: Create view models and link domain
@HiltViewModel
class ApodListViewModel @Inject constructor(
    val db: ApodDatabase,
    val dbArchive: ApodArchiveDatabase,
    application: Application
): AndroidViewModel(application) {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)


    val apodFavoritesLiveData = MutableLiveData<MutableList<ApodArchive>>()
    val loading = MutableLiveData<Boolean>()

    // Entry point for view
    fun refresh() {
        fetchApodFromDB()
    }

    private fun fetchApodFromDB() {
        // Loading spinner active. Disabled when information is retrieved.
        loading.value = true
        coroutineScope.launch {
            val apodArchiveList = dbArchive.dao.getAllApods(true).map { it.toApodArchive() }
            apodFavoritesLiveData.postValue(apodArchiveList as MutableList<ApodArchive>)
            withContext(Dispatchers.Main) {
                loading.value = false
            }
        }
    }

    fun deleteApodModel(apod: ApodArchive) {
        coroutineScope.launch {
            val apodArchive = dbArchive.dao.delete(apod.toApodArchiveEntity())
            val apodDeleted = db.dao.deleteFromList(apod.date.toIntDate())
            // If all items are deleted from recyclerView, liveData should also be updated
            val apodArchiveList = dbArchive.dao.getAllApods(true).map { it.toApodArchive() }
            apodFavoritesLiveData.postValue(apodArchiveList as MutableList<ApodArchive>)
        }
    }

    // To cancel coroutineScope when component moves away from the viewmodel
    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}