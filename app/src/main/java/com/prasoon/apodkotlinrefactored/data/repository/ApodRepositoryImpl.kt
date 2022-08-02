package com.prasoon.apodkotlinrefactored.data.repository

import android.util.Log
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toIntDate
import com.prasoon.apodkotlinrefactored.core.utils.Resource
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.remote.ApodAPI
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.repository.ApodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import retrofit2.http.HTTP
import java.io.IOException

// Step 3.2: REPOSITORY: Create actual implementations in "data" layer
// Have single source of truth. In this case, data will be fetched from api
// and then stored in the database which will be displayed in the UI.
class ApodRepositoryImpl(
    private val api: ApodAPI,
    private val dao: ApodDao
) : ApodRepository {
    // Step 3.3: REPOSITORY: Implementation of Repository
    override fun getApodCustomDate(date: String?): Flow<Resource<Apod>> = flow {
        // Initially, data will be in loading state
        emit(Resource.Loading())
        // If data is already present in cache, load from it.
        val apod = date?.let { dao.getApodFromDatePrimaryKey(it.toIntDate())?.toApod() ?: Apod("", "", "", "", "", "", "") }

        // Data loaded
        emit(Resource.Loading(data = apod))
        var currentDateReceived = String()

        // Check for any exceptions
        try {
            Log.i("ApodRepositoryImpl", "date:  $date")
            Log.i("ApodRepositoryImpl", "apod:  $apod")
            val remoteApod = api.getApodCustomDate(BuildConfig.APOD_API_KEY, date)
            dao.delete(remoteApod.toApodEntity())   // Delete old data from DB
            dao.insertApod(remoteApod.toApodEntity())   // And Update it in DB
            //Log.i("ApodRepositoryImpl", "remoteApod:  ${remoteApod}")
            //Log.i("ApodRepositoryImpl", "remoteApod.toApodEntity():  ${remoteApod.toApodEntity()}")
            currentDateReceived = if (date.isNullOrEmpty()) remoteApod.date else date

        } catch (e: HttpException) {
            Log.i("ApodRepositoryImpl", "exception occurred: $e")
            if (e.code() == 400) return@flow    // Don't handle bad requests
            emit(
                Resource.Error(
                    message = "Oops, something went wrong!",
                    data = apod
                )
            )

        } catch (e: IOException) {
            emit(
                Resource.Error(
                    message = "Couldn't reach server, please try after sometime",
                    data = apod
                )
            )
        }
        // Emit data to UI
        // Current date needs to be updated, since fetching from DB from newApod (ONLY in case of empty date sent)
        val newApod: Apod =
            dao.getApodFromDatePrimaryKey(currentDateReceived.toIntDate())?.toApod() ?: Apod("", "", "", "", "", "", "")
        Log.i("ApodRepositoryImpl", "newApod:  ${newApod}" )

        emit(Resource.Success(newApod))
    }
}