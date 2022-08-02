package com.prasoon.apodkotlinrefactored.presentation.apod_home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.getFileSizeOfUrl
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.getFileSizeOfUrlCoroutines
import com.prasoon.apodkotlinrefactored.core.utils.Resource
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.data.local.entity.ApodEntity
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

// Step 5.1: PRESENTATION/UI: Create ViewModels by using the "use cases".
@HiltViewModel
class ApodViewModel @Inject constructor(
    private val getApod: GetApod,
    private val db: ApodDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "ApodViewModel"

    // Initialize states
    private var apodState = ApodState()
    val apodStateLiveData = MutableLiveData<ApodState>()         // To be OBSERVED from the UI

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    fun refresh(date: String?) {
        Log.i(TAG, "refresh for date: $date")
        getApod(date)
    }

    // Fill each case of apod received
    private fun getApod(date: String?) {

        coroutineScope.launch {
            getApod.invoke(date = date)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            apodState =
                                ApodState(apod = result.data ?: apodState.apod, isLoading = false)
                            apodStateLiveData.postValue(apodState)
                            //if (!apodState.apod.hdUrl.isNullOrEmpty()) size = getFileSizeOfUrlCoroutines(apodState.apod.hdUrl!!)
                        }
                        is Resource.Error -> {
                            apodState =
                                ApodState(apod = result.data ?: apodState.apod, isLoading = false, message = result.message)
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

        /*var size: Long?
        coroutineScope.launch {
            size = getFileSizeOfUrl("https://apod.nasa.gov/apod/image/2206/V838Mon_Hubble_2238.jpg")
            withContext(Dispatchers.Main) {
                Log.i("ApodRepositoryImpl", "size of image:  ${size!! /1024} kB")
            }
        }*/
    }


    fun saveApod(apod: Apod, processFavoriteDB: Boolean) {
        Log.i(TAG, "saveApod")
        coroutineScope.launch {
            db.dao.addIntoDB(apod.toApodEntity(processFavoriteDB))
        }
    }

    sealed class UIEvent {
        data class ShowSnackbar(val message: String) : UIEvent()
    }
}