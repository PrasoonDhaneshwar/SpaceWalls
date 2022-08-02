package com.prasoon.apodkotlinrefactored.presentation.apod_archives

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.core.common.DateInput
import com.prasoon.apodkotlinrefactored.core.utils.Resource
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApodArchives
import com.prasoon.apodkotlinrefactored.presentation.apod_home.ApodState
import com.prasoon.apodkotlinrefactored.presentation.apod_home.ApodViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

// Step 22: Create view models and link domain
@HiltViewModel
class ApodArchivesViewModel @Inject constructor(
    application: Application,
    private val getApodArchives: GetApodArchives
) : AndroidViewModel(application) {

    var apodArchivesListState = ApodArchivesListState()
    val apodArchivesListLiveData = MutableLiveData<ApodArchivesListState>()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    // Entry point for view
    fun refresh() {
        getApodFromArchives()
    }

    private fun getApodFromArchives() {
        coroutineScope.launch {
            getApodArchives.invoke()
                .onEach {
                        result ->
                    when (result) {
                        is Resource.Success -> {
                            apodArchivesListState =
                                ApodArchivesListState(apodArchivesList = result.data ?: emptyList())
                            apodArchivesListLiveData.postValue(apodArchivesListState)
                        }
                        is Resource.Error -> {
                            apodArchivesListState =
                                ApodArchivesListState(
                                    apodArchivesList = result.data
                                        ?: apodArchivesListState.apodArchivesList,
                                    isLoading = false,
                                    message = result.message
                                )
                            apodArchivesListLiveData.postValue(apodArchivesListState)
                        }
                        is Resource.Loading -> {
                            apodArchivesListState =
                                ApodArchivesListState(isLoading = true, message = "Unknown error occurred")
                            apodArchivesListLiveData.postValue(apodArchivesListState)
                            ApodViewModel.UIEvent.ShowSnackbar(result.message ?: "Unknown error occurred")
                        }
                    }
                }.launchIn(viewModelScope)
                }
        }

    // To cancel coroutineScope when component moves away from the viewModel
    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}