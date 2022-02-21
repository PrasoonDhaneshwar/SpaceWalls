package com.prasoon.apodkotlin.utils

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationUtils {
    fun displayNotification(context: Context, title: String, message: String) {
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
        ).setOngoing(true)
            .setProgress(0, 0, true)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.star_on)

        // 3. Create the notification
        val notification = notificationBuilder.build()

        // 4. Notify
        notificationManager.notify(Constants.MESSAGE_ID, notification)
    }

    fun cancelNotification(context: Context, title: String, message: String) {
        val notificationManager: NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(Constants.MESSAGE_ID)
    }
}