package com.prasoon.apodkotlinrefactored.data.repository

import android.graphics.BitmapFactory
import android.util.Log
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.common.Resource
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.ApodArchiveDao
import com.prasoon.apodkotlinrefactored.data.ApodDao
import com.prasoon.apodkotlinrefactored.data.remote.ApodAPI
import com.prasoon.apodkotlinrefactored.domain.model.Apod
import com.prasoon.apodkotlinrefactored.domain.repository.ApodRepository
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import java.net.URL

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

            // Store bitmap in DB
            if (apod.imageBitmapUI == null) {
                CoroutineScope(Dispatchers.IO).launch {
                    var bitmapUrl = ""
                    if (apod.url.contains("youtube")) {
                        bitmapUrl = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                    } else if(apod.url.contains("jpeg") || apod.url.contains("jpg") || apod.url.contains("png") || apod.url.contains("gif")) {
                        bitmapUrl = apod.url
                    } else return@launch
                    val bitmap =
                        BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                            withContext(Dispatchers.IO) {
                                val cleanUrl = if (!apod.url.startsWith("https://") && !apod.url.startsWith("http://"))
                                    "https:$apod.link" else apod.url
                                URL(cleanUrl).openConnection()
                            }.getInputStream()
                        })
                    if (bitmap != null) {
                        daoArchive.updateApodArchiveImage(date.toIntDate(), bitmap)
                        dao.updateApodImage(date.toIntDate(), bitmap)
                    }
                }
            }
        }
        // Otherwise, make a network call and add it into DB
        else {

            // Check for any exceptions
            try {
                val remoteApod = api.getApodCustomDate(BuildConfig.APOD_API_KEY, date)
                dao.insertApod(remoteApod.toApodEntity())   // Update in DB
                val apodArchiveEntity = remoteApod.convertToApodArchiveEntity()
                daoArchive.insertApodArchive(apodArchiveEntity)   // Update in Archive DB

                if (apod.url.isNotEmpty()) {
                    // Store bitmap in DB
                    CoroutineScope(Dispatchers.IO).launch {
                        var bitmapUrl = ""
                        if (apod.url.contains("youtube")) {
                            Log.d(TAG, "bitmapUrl: $bitmapUrl")
                            bitmapUrl = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                        } else if(apod.url.contains("jpeg") || apod.url.contains("jpg") || apod.url.contains("png") || apod.url.contains("gif")) {
                            Log.d(TAG, "bitmapUrl: $bitmapUrl")
                            bitmapUrl = apod.url
                        } else return@launch
                        Log.d(TAG, "bitmapUrl: $bitmapUrl")

                        val bitmap =
                            BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                                withContext(Dispatchers.IO) {
                                    URL(bitmapUrl).openConnection()
                                }.getInputStream()
                            })
                        if (bitmap != null) {
                            daoArchive.updateApodArchiveImage(date.toIntDate(), bitmap)
                            dao.updateApodImage(date.toIntDate(), bitmap)
                        }
                        bitmapUrl = ""
                    }
                }

                // Emit data to UI
                if (dao.getApodFromDatePrimaryKey(date.toIntDate()) != null) {
                    apod = dao.getApodFromDatePrimaryKey(date.toIntDate()).toApod()
                    Log.d(TAG, "Emit apod from remote: $apod")
                    emit(Resource.Success(apod))
                }
            } catch (e: HttpException) {
                Log.d(TAG, "Exception occurred: $e")
                if (e.code() == 400) return@flow    // Don't handle bad requests
                if (e.code() == 404) {
                    Log.d(TAG, "404 occurred")
                    emit(Resource.Error(message = "No data available for $date", data = Apod(date, "", "No data available for ${date.toSimpleDateFormat()}.\nPlease try for another date", "", "", "", "")))
                }
                else {
                    emit(Resource.Error(message = "Oops, something went wrong!", data = Apod("", "", "", "", "", "", "")))
                }

            } catch (e: IOException) {
                emit(Resource.Error(message = "Couldn't reach server, please try after sometime", data = Apod("", "", "", "", "", "", "")))
            }
        }
    }
}