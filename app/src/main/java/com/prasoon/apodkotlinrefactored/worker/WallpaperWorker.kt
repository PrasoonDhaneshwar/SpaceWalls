package com.prasoon.apodkotlinrefactored.worker

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.DOWNLOAD_IMAGE_MESSAGE_ID
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_ARCHIVE_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_DAILY_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FAVORITES_WALLPAPER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCHEDULE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE_FOR_WORKER
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.generateRandomDate
import com.prasoon.apodkotlinrefactored.core.utils.DateUtils.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.createBitmapFromCacheFile
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.setWallpaper
import com.prasoon.apodkotlinrefactored.core.utils.NotificationUtils.displayNotification
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.local.ApodArchiveDatabase
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
    private val dbArchive: ApodArchiveDatabase,
    ) : CoroutineWorker(appContext, workerParameters) {
    companion object {
        const val TAG = "WallpaperWorker"
        const val WORK_NAME = "WallpaperWorker"
    }
    //private val db by lazy { ApodArchiveDatabase(appContext).dao }
    private val settingPerf: SharedPreferences? = PreferenceManager.getDefaultSharedPreferences(appContext)
    private val notifications = settingPerf!!.getBoolean("notifications", false)
    override suspend fun doWork(): Result {
        //startForegroundService()

        var date = ""

        val scheduleType = inputData.getInt(SCHEDULE_FOR_WORKER, SCHEDULE_DAILY_WALLPAPER)
        val screenPreference = inputData.getInt(SCREEN_PREFERENCE_FOR_WORKER, SCREEN_PREFERENCE)
        when (scheduleType) {
            SCHEDULE_DAILY_WALLPAPER -> {
                date = DateUtils.currentDate.ifEmpty { DateUtils.getCurrentDateForInitialization() }

                supervisorScope {
                    try {
                        val remoteApod = ApodAPI.instance.getApodCustomDate(BuildConfig.APOD_API_KEY, date)
                        val apod = remoteApod.toApodEntity().toApod()
                        Log.d(TAG, "doWork apod: $apod")

                        var url = ""
                        if (apod.url.contains("youtube")) url = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                        else if (apod.url.contains("jpg")) url = apod.url

                        val bitmap: Bitmap?
                        if (url.isNotEmpty()) {
                            bitmap = createBitmapFromCacheFile(url, appContext)
                            Log.d(TAG, "Bitmap: ${bitmap?.height}")

                            if (notifications) {
                                displayNotification(appContext, apod.title, apod.date.toSimpleDateFormat(), false, bitmap)
                            }

                            // Only set wallpaper when url contains an image
                            if (!url.contains("youtube")) {
                                setWallpaper(appContext, null, screenPreference, bitmap)
                            } else {
                                return@supervisorScope
                            }
                        }

                    } catch (e: java.lang.Exception) {
                        Result.failure()
                        Result.retry()
                    }
                }
                return Result.success()
            }

            SCHEDULE_ARCHIVE_WALLPAPER -> {
                date = generateRandomDate()
                processApodArchiveForWorker(date, screenPreference)
            }

            SCHEDULE_FAVORITES_WALLPAPER -> {
                supervisorScope {
                    val apodArchiveList = dbArchive.dao.getAllApods(true).map { it.toApodArchive() }
                    val favoritesSize = apodArchiveList.size
                    if (favoritesSize == 0) {
                        return@supervisorScope
                    } else {
                        val randomIndex = Random.nextInt(apodArchiveList.size);
                        val apodArchive = apodArchiveList[randomIndex]
                        Log.d(TAG, "doWork apodArchive: $apodArchive")
                        val bitmap = createBitmapFromCacheFile(apodArchive.link, appContext)
                        if (notifications) {
                            displayNotification(appContext, apodArchive.title, apodArchive.date.toSimpleDateFormat(), false, bitmap)
                        }
                        // Only set wallpaper when url contains an image
                        if (!apodArchive.link.contains("youtube")) {
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
        Log.d(TAG, "doWork apodArchive: $apodArchive")

        var url = ""
        url = if (apodArchive.link.contains("youtube")) apodArchive.link
        else if (apodArchive.link.contains("jpg")) apodArchive.link
        else return Result.success()

        val bitmap = createBitmapFromCacheFile(url, appContext)

        Log.d(TAG, "Bitmap dimensions -> height x width: ${bitmap?.height} x ${bitmap?.width}")
        Log.d(TAG, "Notifications are: $notifications")

        if (notifications) {
            displayNotification(appContext, apodArchive.title, apodArchive.date.toSimpleDateFormat(), false, bitmap)
        }
        if (!url.contains("youtube")) {
            setWallpaper(appContext, null, screenPreference, bitmap)
        } else {
            return Result.success() // todo: Wallpaper can't be set since it's a YouTube Video
        }
        return Result.success()
    }

    private suspend fun startForegroundService() {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            appContext,
            Constants.DOWNLOAD_IMAGE_CHANNEL
        ).setContentTitle("Astronomy Picture of the Day")
            .setSmallIcon(R.drawable.ic_download)
        setForeground(ForegroundInfo(DOWNLOAD_IMAGE_MESSAGE_ID, notificationBuilder.build()))
    }
}