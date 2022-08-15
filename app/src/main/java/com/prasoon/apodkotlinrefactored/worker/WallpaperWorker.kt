package com.prasoon.apodkotlinrefactored.worker

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.prasoon.apodkotlinrefactored.BuildConfig
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants
import com.prasoon.apodkotlinrefactored.core.common.Constants.DOWNLOAD_IMAGE_MESSAGE_ID
import com.prasoon.apodkotlinrefactored.core.common.Constants.SCREEN_PREFERENCE
import com.prasoon.apodkotlinrefactored.core.common.Constants.SHOW_NOTIFICATION
import com.prasoon.apodkotlinrefactored.core.common.DateInput
import com.prasoon.apodkotlinrefactored.core.common.DateInput.toSimpleDateFormat
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.createBitmapFromCacheFile
import com.prasoon.apodkotlinrefactored.core.utils.ImageUtils.setWallpaper
import com.prasoon.apodkotlinrefactored.core.utils.NotificationUtils.displayNotification
import com.prasoon.apodkotlinrefactored.core.utils.VideoUtils
import com.prasoon.apodkotlinrefactored.data.remote.ApodAPI
import kotlinx.coroutines.supervisorScope

class WallpaperWorker(
    val context: Context,
    workerParameters: WorkerParameters,
) : CoroutineWorker(context, workerParameters) {
    companion object {
        val TAG = "WallpaperWorker"
        const val WORK_NAME = "WallpaperWorker"
    }

    override suspend fun doWork(): Result {
        //startForegroundService()

        val date = DateInput.currentDate.ifEmpty { DateInput.getCurrentDateForInitialization() }
        supervisorScope {

            try {
                val remoteApod = ApodAPI.instance.getApodCustomDate(BuildConfig.APOD_API_KEY, date)
                val apod = remoteApod.toApodEntity().toApod()
                Log.i(TAG, "doWork apod: $apod")

                var url = ""
                if (apod.url.contains("youtube")) url = VideoUtils.getYoutubeThumbnailUrlFromVideoUrl(apod.url)
                else if (apod.url.contains("jpg")) url = apod.url

                val bitmap: Bitmap
                if (url.isNotEmpty()) {
                    bitmap = createBitmapFromCacheFile(url, context)
                    Log.i(TAG, "Bitmap: ${bitmap.height}")

                    if (SHOW_NOTIFICATION) {
                        displayNotification(context, apod.title, apod.date.toSimpleDateFormat(), false, bitmap)
                    }

                    // Only set wallpaper when url contains an image
                    if (!url.contains("youtube")) {
                        setWallpaper(context, null, SCREEN_PREFERENCE, bitmap)
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

    private suspend fun startForegroundService() {
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            Constants.DOWNLOAD_IMAGE_CHANNEL
        ).setContentTitle("Astronomy Picture of the Day")
            .setSmallIcon(R.drawable.ic_download)
        setForeground(ForegroundInfo(DOWNLOAD_IMAGE_MESSAGE_ID, notificationBuilder.build()))
    }
}