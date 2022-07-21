package com.prasoon.apodkotlinrefactored.presentation.apod_detail

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.use_case.GetApod
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

// Step 5.1: PRESENTATION/UI: Create ViewModels by using the "use cases".
@HiltViewModel
class ApodDetailViewModel @Inject constructor(
    private val getApod: GetApod,
    private val db: ApodDatabase,
    application: Application
) : AndroidViewModel(application) {

    private val TAG = "ApodDetailViewModel"

    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        onError("Exception: ${throwable.localizedMessage} ")
    }
    private val coroutineScopeForDatabase = CoroutineScope(Dispatchers.IO + exceptionHandler)

    val apodDetail = MutableLiveData<Apod>()
    val apodDetailLoaded = MutableLiveData<Boolean>()
    val apodLoadError = MutableLiveData<String?>()
    val loading = MutableLiveData<Boolean>()

    fun refresh(date: String?) {
        getApod(date)
    }

    fun getApodDetailFromDb(date: Int) {
        Log.i(TAG, "getApodDetailFromDb: $date")
        coroutineScopeForDatabase.launch {
            val apod = db.dao.getApodModel(date).toApod()
            apodDetail.postValue(apod)
            withContext(Dispatchers.Main) {
                apodDetailLoaded.value = true
            }
        }
    }

    private fun onError(message: String) {
        // postValue is called from a background thread
        apodLoadError.postValue(message)
        loading.postValue(false)
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.coroutineContext.cancelChildren()
    }
}