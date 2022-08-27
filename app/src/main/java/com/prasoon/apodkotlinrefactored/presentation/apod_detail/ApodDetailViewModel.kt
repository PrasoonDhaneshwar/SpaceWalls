package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApod
import com.prasoon.apodkotlinrefactored.presentation.apod_home.ApodState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

// Step 5.1: PRESENTATION/UI: Create ViewModels by using the "use cases".
@HiltViewModel
class ApodDetailViewModel @Inject constructor(
    private val db: ApodDatabase,
    application: Application,
    private val getApod: GetApod,
    ) : AndroidViewModel(application) {

    private val TAG = "ApodDetailViewModel"

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    var isDateExistInDB = false
    // Initialize states
    private var apodState = ApodState()
    val apodStateLiveData = MutableLiveData<ApodState>()         // To be OBSERVED from the UI

    fun getApodDetailFromDb(date: String) {
        Log.d(TAG, "getApodDetailFromDb: $date")
        coroutineScope.launch {
            val job = CoroutineScope(Dispatchers.IO).launch {
                isDateExistInDB = db.dao.isRowIsExist(date.toIntDate())
            }
            job.join()

            if (isDateExistInDB) {
                val apod = db.dao.getApodFromDatePrimaryKey(date.toIntDate()).toApod()

                apodState = ApodState(apod = apod, isLoading = false)
                apodStateLiveData.postValue(apodState)

            } else {
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
                                    ApodState(apod = result.data ?: apodState.apod, isLoading = false, message = result.message)
                                apodStateLiveData.postValue(apodState)
                            }
                            is Resource.Loading -> {
                                apodState = ApodState(isLoading = true)
                                apodStateLiveData.postValue(apodState)
                            }
                        }
                    }.launchIn(viewModelScope)
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        coroutineScope.cancel()
        viewModelScope.coroutineContext.cancelChildren()
    }
}