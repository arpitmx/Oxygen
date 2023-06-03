package com.ncs.o2.HelperClasses

import android.annotation.SuppressLint
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.ncs.o2.R

/*
File : NotificationUtil.kt -> com.ncs.o2.HelperClasses
Description : Helper for notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 2:59 am on 01/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/object NotificationUtil {

    fun showNotification(context: Context, title: String, message: String, targetActivity: Class<*>) {
        val channelId = "fcm1"
        val channelName = "FCM Channel"
        val notificationId = System.currentTimeMillis().toInt()

        val intent = Intent(context, targetActivity)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )


        // Create a notification builder

        val notificationBuilder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.round_task_alt_24)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(false)
            .setColor(Color.RED)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set default notification sound
            .setContentIntent(pendingIntent)


        // Create a notification manager
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Check if running on Android Oreo or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create a notification channel
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH,

            ).apply {
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
            }

            // Create the notification channel
            notificationManager.createNotificationChannel(channel)
        }

        // Show the notification
        notificationManager.notify(notificationId, notificationBuilder.build())
    }
}