package com.prasoon.apodkotlin.services

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.prasoon.apodkotlin.utils.Constants.CURRENT_DATE
import com.prasoon.apodkotlin.utils.Constants.IMAGE_HD_URL
import com.prasoon.apodkotlin.utils.Constants.IMAGE_NAME
import com.prasoon.apodkotlin.utils.Constants.IMAGE_URL
import com.prasoon.apodkotlin.utils.Constants.STORAGE_DIRECTORY_PATH
import com.prasoon.apodkotlin.utils.NotificationUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.net.URL
import java.util.*

private val TAG = "ImageDownloadWorker"
class ImageDownloadWorker(context: Context, parameters: WorkerParameters) : Worker(context, parameters) {

    override fun doWork(): Result {
        Log.i(TAG, "doWork executed")

        // Receive data from activity
        val url = inputData.getString(IMAGE_URL)
        val hdUrl = inputData.getString(IMAGE_HD_URL)
        val date = inputData.getString(CURRENT_DATE)

        Log.i(TAG, "doWork starting with thread id: ${Thread.currentThread().id} " +
                ", url: $url, hdUrl: $hdUrl, date: $date")
        val downloadResult = saveImage(applicationContext, url!!, hdUrl!!, date!!)

        return Result.success(downloadResult)
    }

    private fun saveImage(context: Context, url: String, hdUrl: String, date: String): Data {
        val imageName = "APOD_" + date.replace("-", "")
        val imageUrl = if (hdUrl.isEmpty()) URL(url) else URL(hdUrl)
        NotificationUtils.displayNotification(context, "Downloading APOD", date)

        val bitmap = BitmapFactory.decodeStream(imageUrl.openConnection().getInputStream())
        val storageDirectoryPath: String

        val fos: OutputStream?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val contentValues = ContentValues()
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "$imageName.jpg")
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            val imageUri =
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            fos = Objects.requireNonNull(imageUri)?.let { resolver.openOutputStream(it) }
            storageDirectoryPath =
                contentValues.get(MediaStore.MediaColumns.RELATIVE_PATH).toString()
        } else {
            val imagesDir: String =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                    .toString()
            storageDirectoryPath = imagesDir
            val image = File(imagesDir, "$imageName.jpg")
            fos = FileOutputStream(image)
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
        Objects.requireNonNull(fos)?.close()
        NotificationUtils.cancelNotification(context, "Downloading APOD finished", date)

        return Data.Builder()
            .putString(IMAGE_NAME, imageName)
            .putString(STORAGE_DIRECTORY_PATH, storageDirectoryPath)
            .build()
    }


}