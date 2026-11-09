package com.prasoon.apodkotlinrefactored.worker

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.generateRandomDate
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toIntDate
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.createBitmapFromCacheFile
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.setWallpaper
import com.prasoon.apodkotlinrefactored.core.utils.NotificationUtils.displayNotification
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.local.ApodArchiveDatabase
import com.prasoon.apodkotlinrefactored.data.local.ApodDatabase
import com.prasoon.apodkotlinrefactored.data.remote.ApodAPI
import com.prasoon.apodkotlinrefactored.domain.repository.ApodArchivesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.supervisorScope
import kotlin.random.Random

@HiltWorker
class WallpaperWorker @AssistedInject constructor(
    @Assisted val appContext: Context,
    @Assisted workerParameters: WorkerParameters,
    private val archiveRepository: ApodArchivesRepository,
    private val api: ApodAPI,
    private val dbArchive: ApodArchiveDatabase,
    private val db: ApodDatabase,
    ) : CoroutineWorker(appContext, workerParameters) {
    companion object {
        const val TAG = "WallpaperWorker"
        const val WORK_NAME = "WallpaperWorker"
    }
    private val settingPerf: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(appContext)
    private val notifications = settingPerf!!.getBoolean("notifications", false)
    override suspend fun doWork(): Result {
        var date = ""

        val scheduleType = inputData.getInt(SCHEDULE_FOR_WORKER, SCHEDULE_DAILY_WALLPAPER)
        val screenPreference = inputData.getInt(SCREEN_PREFERENCE_FOR_WORKER, SCREEN_PREFERENCE)
        when (scheduleType) {
            SCHEDULE_DAILY_WALLPAPER -> {
                date = DateUtils.currentDate.ifEmpty { DateUtils.getCurrentDateForInitialization() }

                return processApodArchiveForWorker(date, screenPreference)
            }

            SCHEDULE_ARCHIVE_WALLPAPER -> {
                date = generateRandomDate()
                return processApodArchiveForWorker(date, screenPreference)
            }

            SCHEDULE_FAVORITES_WALLPAPER -> {
                supervisorScope {
                    val apodArchiveList = dbArchive.dao.getAllFavoriteArchives(true).map { it.toApodArchive() }
                    val favoritesSize = apodArchiveList.size
                    if (favoritesSize == 0) {
                        return@supervisorScope
                    } else {
                        val randomIndex = Random.nextInt(apodArchiveList.size);
                        val apodArchive = apodArchiveList[randomIndex]
                        Log.d(TAG, "doWork apodArchive: $apodArchive")
                        val bitmap = createBitmapFromCacheFile(apodArchive.link, appContext)
                        if (notifications) {
                            if (apodArchive.link.contains("youtube") || apodArchive.link.contains(".gif")) {
                                displayNotification(appContext, "Can not set wallpaper for YouTube or web content\n"+ apodArchive.title, apodArchive.date, false, bitmap)
                            } else {
                                displayNotification(appContext, "Wallpaper is set for: " + apodArchive.title, apodArchive.date, false, bitmap)
                            }
                        }
                        // Only set wallpaper when url contains an image
                        if ((apodArchive.link.contains("jpeg") || apodArchive.link.contains("jpg") || apodArchive.link.contains("png")) && !apodArchive.link.contains("youtube")) {
                            setWallpaper(appContext, null, screenPreference, bitmap)
                        } else {
                            return@supervisorScope
                        }
                    }
                }
            }
        }
        return Result.success()
    }

    private suspend fun processApodArchiveForWorker(date: String, screenPreference: Int): Result {
        val apodArchive = archiveRepository.fetchArchiveFromDate(date)
        // Network failure, or today's page isn't published yet
        if (apodArchive.link.isEmpty()) return Result.retry()
        Log.d(TAG, "doWork apodArchive: $apodArchive")
        dbArchive.dao.insertApodArchive(apodArchive.toApodArchiveEntity())
        val bitmap = createBitmapFromCacheFile(apodArchive.link, appContext)

        Log.d(TAG, "Bitmap dimensions -> height x width: ${bitmap?.height} x ${bitmap?.width}")
        Log.d(TAG, "Notifications are: $notifications")

        if (notifications) {
            if (apodArchive.link.contains("youtube")) {
                displayNotification(appContext, "Can not set wallpaper for YouTube content.\n"+ apodArchive.title, apodArchive.date, false, bitmap)
            } else if(apodArchive.link.contains("jpeg") || apodArchive.link.contains("jpg") || apodArchive.link.contains("png")) {
                displayNotification(appContext, "Wallpaper is set: " + apodArchive.title, apodArchive.date, false, bitmap)
            }
        }
        if ((apodArchive.link.contains("jpeg") || apodArchive.link.contains("jpg") || apodArchive.link.contains("png")) && bitmap != null && !apodArchive.link.contains("youtube")) {
            setWallpaper(appContext, null, screenPreference, bitmap)
            dbArchive.dao.updateApodArchiveImage(date.toIntDate(), bitmap)
            dbArchive.dao.setWallpaperField(date.toIntDate(), true)
        } else {
            displayNotification(appContext, "Can not set wallpaper for Web content."+ apodArchive.title, apodArchive.date, false, bitmap)
        }
        return Result.success()
    }
}