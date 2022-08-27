package com.prasoon.apodkotlinrefactored.data.repository

import android.util.Log
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.data.ApodArchiveDao
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.remote.ApodAPI
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.repository.ApodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

// Step 3.2: REPOSITORY: Create actual implementations in "data" layer
// Have single source of truth. In this case, data will be fetched from api
// and then stored in the database which will be displayed in the UI.
class ApodRepositoryImpl(
    private val api: ApodAPI,
    private val dao: ApodDao,
    private val daoArchive: ApodArchiveDao,
    private var apod: Apod
) : ApodRepository {
    private val TAG = "ApodRepositoryImpl"
    // Step 3.3: REPOSITORY: Implementation of Repository
    override fun getApodCustomDate(date: String): Flow<Resource<Apod>> = flow {
        // Initially, data will be in loading state
        emit(Resource.Loading())

        // Check if data is already present in DB***
        var isDateExistInDB = false
        val job = CoroutineScope(Dispatchers.IO).launch {
            isDateExistInDB = dao.isRowIsExist(date.toIntDate())
        }
        job.join()

        // *** load from it.
        if (isDateExistInDB) {
            apod = dao.getApodFromDatePrimaryKey(date.toIntDate()).toApod()
            Log.d(TAG, "Emit apod from DB:  $apod" )
            // Data loaded
            emit(Resource.Success(data = apod))

        }
        // Otherwise, make a network call and add it into DB
        else {

            // Check for any exceptions
            try {
                val remoteApod = api.getApodCustomDate(BuildConfig.APOD_API_KEY, date)
                dao.insertApod(remoteApod.toApodEntity())   // Update in DB
                daoArchive.insertApod(remoteApod.convertToApodArchiveEntity())   // Update in Archive DB

                // Emit data to UI
                apod = dao.getApodFromDatePrimaryKey(date.toIntDate()).toApod()
                Log.d(TAG, "Emit apod from remote:  $apod")
                emit(Resource.Success(apod))

            } catch (e: HttpException) {
                Log.d(TAG, "Exception occurred: $e")
                if (e.code() == 400) return@flow    // Don't handle bad requests
                emit(Resource.Error(message = "Oops, something went wrong!", data = Apod("", "", "", "", "", "", "")))

            } catch (e: IOException) {
                emit(Resource.Error(message = "Couldn't reach server, please try after sometime", data = Apod("", "", "", "", "", "", "")))
            }
        }
    }
}