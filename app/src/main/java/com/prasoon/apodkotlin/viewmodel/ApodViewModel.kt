package com.prasoon.apodkotlin.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.prasoon.apodkotlin.BuildConfig
import com.prasoon.apodkotlin.model.ApodDatabase
import com.prasoon.apodkotlin.model.ApodModel
import com.prasoon.apodkotlin.model.ApodService
import kotlinx.coroutines.*


class ApodViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ApodViewModel"

    // -----------------Room Database Setup---------------
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private val db by lazy { ApodDatabase(getApplication()).apodModelDao() }

    // apodDetail and apodDetailLoaded is for DetailFragment
    val apodDetail = MutableLiveData<ApodModel?>()
    val apodDetailLoaded = MutableLiveData<Boolean>()


    fun getApodDetailFromDb(id: Int) {
        Log.i(TAG, "getApodDetailFromDb: $id")
        coroutineScope.launch {
            val apod = db.getApodModel(id)
            apodDetail.postValue(apod)
            withContext(Dispatchers.Main) {
                apodDetailLoaded.value = true
            }
        }
    }

    fun saveApod(apodModel: ApodModel) {
        Log.i(TAG, "saveApod")
        coroutineScope.launch {
            db.insertApod(apodModel)
        }
    }

    // -----------------Retrofit Setup---------------
    val apodService = ApodService.getApodFromInterface()
    var job: Job? = null
    val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
        onError("Exception: ${throwable.localizedMessage} ")
    }

    val apodModel = MutableLiveData<ApodModel>()
    val apodLoadError = MutableLiveData<String?>()
    val apodDateList = MutableLiveData<List<String>>()
    val loading = MutableLiveData<Boolean>()

    // Entry point for view
    fun refresh(date: String?) {
        getApodDatesFromDb()
        fetchApodByCustomDate(date)
        //fetchApodByCurrentDateDummy()
    }

    fun getApodDatesFromDb() {
        Log.i(TAG, "getApodDatesFromDb")
        coroutineScope.launch {
            apodDateList.postValue(db.getAllApodDates())
        }
    }

    private fun fetchApodByCurrentDate() {
        // Loading spinner active. Disabled when information is retrieved.
        loading.value = true
        // -----------------Retrofit Setup---------------
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            // Get the response from Retrofit api
            val response = apodService.getApodCurrentDate(BuildConfig.APOD_API_KEY)
            // When response is received, post it to the main thread
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    apodModel.value = response.body()
                    apodLoadError.value = null
                    loading.value = false
                } else {
                    onError("Error: ${response.message()}")
                }
            }
        }
    }

    private fun fetchApodByCustomDate(date: String?) {
        // Loading spinner active. Disabled when information is retrieved.
        // setValue should be called from the main thread
        loading.value = true
        // -----------------Retrofit Setup---------------
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            // Get the response from Retrofit api
            val response = apodService.getApodCustomDate(BuildConfig.APOD_API_KEY, date)
            // When response is received, post it to the main thread
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    apodModel.value = response.body()
                    apodLoadError.value = null
                    loading.value = false
                } else {
                    onError("Error: ${response.message()}")
                }
            }
        }
    }

    private fun onError(message: String) {
        // postValue is called from a background thread
        apodLoadError.postValue(message)
        loading.postValue(false)
    }

/*    private fun fetchApodByCurrentDateDummy() {
        val dummydata = generateDummyApod()
        apodModel.value = dummydata
    }

    // https://api.nasa.gov/planetary/apod?api_key=XqN37uhbQmRUqsm2nTFk4rsugtM2Ibe0YUS9HDE3&date=2021-01-01
    private fun generateDummyApod(): ApodModel {
        val apodmodel= ApodModel("2021-01-01",
            "The South Celestial Pole is easy to spot in star trail images of the southern sky. " +
                    "The extension of Earth's axis of rotation to the south, it's at the center of all the southern star trail arcs. " +
                    "In this starry panorama streching about 60 degrees across deep southern skies the South Celestial Pole is somewhere " +
                    "near the middle though, flanked by bright galaxies and southern celestial gems. Across the top of the frame are the stars " +
                    "and nebulae along the plane of our own Milky Way Galaxy. Gamma Crucis, a yellowish giant star heads the Southern Cross near top" +
                    " center, with the dark expanse of the Coalsack nebula tucked under the cross arm on the left. Eta Carinae and the reddish glow of the " +
                    "Great Carina Nebula shine along the galactic plane near the right edge. At the bottom are the Large and Small Magellanic clouds, external " +
                    "galaxies in their own right and satellites of the mighty Milky Way. A line from Gamma Crucis through the blue star at the bottom of the southern " +
                    "cross, Alpha Crucis, points toward the South Celestial Pole, but where exactly is it? Just look for south pole star Sigma Octantis. Analog to Polaris " +
                    "the north pole star, Sigma Octantis is little over one degree fom the the South Celestial pole.",
            "https://apod.nasa.gov/apod/image/2101/2020_12_16_Kujal_Jizni_Pol_1500px-3.png",
            "image",
            "v1",
            "Galaxies and the South Celestial Pole",
            "https://apod.nasa.gov/apod/image/2101/2020_12_16_Kujal_Jizni_Pol_1500px-3.jpg")
        return apodmodel
    }*/
}