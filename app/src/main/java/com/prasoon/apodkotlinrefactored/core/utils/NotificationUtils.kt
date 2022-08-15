package com.prasoon.apodkotlinrefactored.core.utils


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import androidx.core.app.NotificationCompat
import com.prasoon.apodkotlinrefactored.MainActivity
import com.prasoon.apodkotlinrefactored.R
import com.prasoon.apodkotlinrefactored.core.common.Constants

object NotificationUtils {
    val TAG = "NotificationUtils"
    // When image is downloaded
    fun displayNotification(context: Context, title: String, message: String, indeterminate: Boolean) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Create channel
            val channel = NotificationChannel(
                Constants.DOWNLOAD_IMAGE_CHANNEL,
                Constants.DOWNLOAD_IMAGE_TASK_NOTIFICATION,
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // 2. Create notification UI
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            Constants.DOWNLOAD_IMAGE_CHANNEL
        ).setOngoing(indeterminate)
            .setProgress(0, 0, indeterminate)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_download)
            .setOnlyAlertOnce(true)

        // 3. Create the notification
        val notification = notificationBuilder.build()

        // 4. Notify
        notificationManager.notify(Constants.DOWNLOAD_IMAGE_MESSAGE_ID, notification)
    }

    fun cancelNotification(context: Context, title: String, message: String) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.DOWNLOAD_IMAGE_MESSAGE_ID)
    }

    // For Daily Wallpaper
    fun displayNotification(context: Context, title: String, message: String, indeterminate: Boolean, bitmap: Bitmap?) {
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // 1. Create channel
            val channel = NotificationChannel(Constants.DAILY_WALLPAPER_CHANNEL, Constants.DAILY_WALLPAPER_TASK_NOTIFICATION, NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        // Extra: Set onclick events to the notification
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent: PendingIntent
        pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                context,
                0,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            PendingIntent.getActivity(
                context,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        // 2. Create notification UI
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(
            context,
            Constants.DAILY_WALLPAPER_CHANNEL
        ).setProgress(0, 0, indeterminate)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_download)
            .setLargeIcon(bitmap)
            .setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(bitmap)
                    .bigLargeIcon(null)
            )
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)

        // 3. Create the notification
        val notification = notificationBuilder.build()

        // 4. Notify
        notificationManager.notify(Constants.DAILY_WALLPAPER_MESSAGE_ID, notification)
    }
}