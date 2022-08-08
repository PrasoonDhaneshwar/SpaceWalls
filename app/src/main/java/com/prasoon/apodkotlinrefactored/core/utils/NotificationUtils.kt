package com.prasoon.apodkotlinrefactored.core.utils

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.prasoon.apodkotlinrefactored.core.common.Constants

object NotificationUtils {
    fun displayNotification(context: Context, title: String, message: String, indeterminate: Boolean) {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Create channel
            val channel = NotificationChannel(
                Constants.MESSAGE_CHANNEL,
                Constants.TASK_NOTIFICATION,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create notification UI
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            Constants.MESSAGE_CHANNEL
        ).setOngoing(indeterminate)
            .setProgress(0, 0, indeterminate)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.star_on)
            .setOnlyAlertOnce(true)

        // 3. Create the notification
        val notification = notificationBuilder.build()

        // 4. Notify
        notificationManager.notify(Constants.MESSAGE_ID, notification)
    }

    fun cancelNotification(context: Context, title: String, message: String) {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.MESSAGE_ID)
    }

    suspend fun displayNotification(context: Context, title: String, message: String, indeterminate: Boolean, bitmap: Bitmap?) {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Create channel
            val channel = NotificationChannel(
                Constants.MESSAGE_CHANNEL,
                Constants.TASK_NOTIFICATION,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
        val bit = bitmap

        // 2. Create notification UI
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            Constants.MESSAGE_CHANNEL
        ).setProgress(0, 0, indeterminate)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.star_on)
            .setLargeIcon(bitmap)
            .setStyle(NotificationCompat.BigPictureStyle()
                .bigPicture(bit)
                .bigLargeIcon(null))
            .setOnlyAlertOnce(true)

        // 3. Create the notification
        val notification = notificationBuilder.build()

        // 4. Notify
        notificationManager.notify(Constants.MESSAGE_ID, notification)
    }
}