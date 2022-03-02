package com.prasoon.apodkotlin.services

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.work.*
import com.prasoon.apodkotlin.utils.Constants
import com.prasoon.apodkotlin.utils.DateInput

private val TAG = "WorkScheduler"
object WorkScheduler {
    lateinit var imageDownloaderWorkRequest: OneTimeWorkRequest
    fun scheduleWorkToSaveImage(context: Context, url: String, hdUrl: String, date: String) {
        // todo: no UI logic to be shown here
        Toast.makeText(context, "Starting download...", Toast.LENGTH_SHORT).show()
        val passDataToWorker = Data.Builder()
            .putString(Constants.IMAGE_URL, url)
            .putString(Constants.IMAGE_HD_URL, hdUrl)
            .putString(Constants.CURRENT_DATE, DateInput.currentDate)
            .build()

        // Constraints
        val constraints =
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

        imageDownloaderWorkRequest =
            OneTimeWorkRequestBuilder<ImageDownloadWorker>()
                .setConstraints(constraints)
                .setInputData(passDataToWorker) // Pass data to worker
                .build()
        // Start the worker with enqueue
        WorkManager.getInstance(context).enqueue(imageDownloaderWorkRequest)
    }

    fun getWorkInfoAfterSaveImage(context: Context, lifecycleOwner: LifecycleOwner) {
        WorkManager.getInstance(context)
            .getWorkInfoByIdLiveData(imageDownloaderWorkRequest.id)
            .observe(lifecycleOwner) {
                if (it.state == WorkInfo.State.SUCCEEDED) {
                    // Get data from worker
                    val imageName = it.outputData.getString(Constants.IMAGE_NAME)
                    val storageDirectoryPath =
                        it.outputData.getString(Constants.STORAGE_DIRECTORY_PATH)
                    Log.i(
                        TAG,
                        "doWork result: \n File name: $imageName \n File path: $storageDirectoryPath"
                    )
                    // todo: no UI logic to be shown here
                    Toast.makeText(
                        context,
                        "Saved as $imageName.jpg in $storageDirectoryPath",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}