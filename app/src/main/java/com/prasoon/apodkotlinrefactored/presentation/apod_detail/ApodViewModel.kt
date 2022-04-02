package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.core.util.Resource
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

// Step 5.1: PRESENTATION/UI: Create ViewModels by using the "use cases".
@HiltViewModel
class ApodViewModel @Inject constructor(
    private val getApod: GetApod
) : ViewModel() {

    // Initialize states
    private var apodState = ApodState()
    val apodStateLiveData = MutableLiveData<ApodState>()         // To be OBSERVED from the UI

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun refresh(date: String) {
        getApod(date)
    }

    // Fill each case of apod received
    fun getApod(date: String) {

        coroutineScope.launch {
            getApod.invoke(date = date)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            apodState =
                                ApodState(apod = result.data ?: apodState.apod, isLoading = false)
                            apodStateLiveData.postValue(apodState)
                        }
                        is Resource.Error -> {
                            apodState =
                                ApodState(apod = result.data ?: apodState.apod, isLoading = false)
                            apodStateLiveData.postValue(apodState)
                        }
                        is Resource.Loading -> {
                            apodState = ApodState(isLoading = true)
                            apodStateLiveData.postValue(apodState)
                            UIEvent.ShowSnackbar(result.message ?: "Unknown error occurred")
                        }
                    }
                }.launchIn(viewModelScope)      // Launch coroutine in a viewModelScope
        }
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: String) : UIEvent()
    }
}