package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.ApodArchiveDatabase
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.domain.model.ApodArchive
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApodArchives
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SharedArchiveViewModel @Inject constructor(
    application: Application,
    private val getApodArchives: GetApodArchives,
    private val dbArchive: ApodArchiveDatabase,
    private val db: ApodDatabase,
) : AndroidViewModel(application) {
    private val TAG = "NewViewModel"

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val apodArchiveListStateLiveData = MutableLiveData<ApodArchivesListState>()
    var apodArchiveListLiveData = MutableLiveData<List<ApodArchive>?>()
    var apodArchiveListState = ApodArchivesListState()

    // Entry point for view
    fun refreshArchive() {
        Log.d(TAG, "refreshArchive")
        getApodFromArchives()
    }

    private fun getApodFromArchives() {
        viewModelScope.launch {
            getApodArchives.invoke()
                .onEach {
                        result ->
                    when (result) {
                        is Resource.Success -> {
                            apodArchiveListLiveData.postValue(result.data)
                            apodArchiveListState = ApodArchivesListState(apodArchivesList = result.data ?: emptyList(), isLoading = false)
                            apodArchiveListStateLiveData.postValue(apodArchiveListState)
                        }
                        is Resource.Error -> {
                            apodArchiveListState = ApodArchivesListState(apodArchivesList = result.data ?: apodArchiveListState.apodArchivesList, isLoading = false, message = result.message)
                            apodArchiveListStateLiveData.postValue(apodArchiveListState)
                        }
                        is Resource.Loading -> {
                            apodArchiveListState = ApodArchivesListState(isLoading = true)
                            apodArchiveListStateLiveData.postValue(apodArchiveListState)
                        }
                    }
                }.launchIn(viewModelScope)
        }
    }

    // This method is used by both archives and ListFragment to update items when added/removed from favorites
    fun processFavoriteArchivesInDatabase(apodArchiveFromUI: ApodArchive, isFavoriteInDatabase: Boolean) {
        Log.d(TAG, "processFavoriteArchivesInDatabase for ${apodArchiveFromUI.date}: $isFavoriteInDatabase")
        coroutineScope.launch {
            dbArchive.dao.addOrRemoveFavoritesInArchivesDB(apodArchiveFromUI.toApodArchiveEntity((isFavoriteInDatabase)))
            db.dao.updateFavorites(apodArchiveFromUI.date.toIntDate(), isFavoriteInDatabase)
            processFavoritesInUI(apodArchiveFromUI, isFavoriteInDatabase)
        }
    }

    private fun processFavoritesInUI(apodArchiveFromUI: ApodArchive, processFavoriteDB: Boolean) {
        coroutineScope.launch {
            val apodArchiveListUI = apodArchiveListLiveData.value?.toMutableList()

            if (apodArchiveListUI != null) {
                for (i: Int in 0 until apodArchiveListUI.size) {
                    if (apodArchiveListUI[i].date == apodArchiveFromUI.date) {
                        apodArchiveListUI[i].isAddedToFavorites = processFavoriteDB
                    }
                }
            }
            apodArchiveListLiveData.postValue(apodArchiveListUI)
            apodArchiveListStateLiveData.postValue(ApodArchivesListState(apodArchivesList = apodArchiveListUI as List<ApodArchive>, false) )
            Log.d(TAG, "processFavorites for ${apodArchiveListStateLiveData.value}: $processFavoriteDB")

        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
        coroutineScope.cancel() // cancel coroutineScope when component moves away from the viewModel
    }

    val apodFavoritesLiveData = MutableLiveData<MutableList<ApodArchive>>()
    fun refreshList() {
        fetchFavoriteArchivesFromDatabase()
    }

    private fun fetchFavoriteArchivesFromDatabase() {
        coroutineScope.launch {
            val apodArchiveList = dbArchive.dao.getAllFavoriteArchives(true).map { it.toApodArchive() }
            apodFavoritesLiveData.postValue(apodArchiveList as MutableList<ApodArchive>)
        }
    }
}