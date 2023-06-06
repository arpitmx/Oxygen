package com.ncs.o2.HelperClasses

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.O2Notification
import com.ncs.o2.R
import com.ncs.o2.UI.Notifications.Requests.RequestActivity

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


*/
object LocalNotificationUtil {

    fun showNotification(context: Context, notification: O2Notification) {
        val channelId = "fcm1"
        val channelName = "FCM Channel"
        val notificationId = System.currentTimeMillis().toInt()
        val notificationBuilder : Notification.Builder

        // Create a notification builder
        when (notification.notificationType){


              NotificationType.TASK_REQUEST_RECIEVED_NOTIFICATION ->
              {
                  val intent = Intent(context,RequestActivity::class.java)
                  intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                  val pendingIntent = PendingIntent.getActivity(
                      context,
                      0,
                      intent,
                      PendingIntent.FLAG_IMMUTABLE
                  )

                  notificationBuilder = Notification.Builder(context, channelId)
                .setSmallIcon(R.drawable.round_task_alt_24)
                .setContentTitle(notification.title)
                .setContentText(notification.message)
                .setAutoCancel(false)
                .setColor(Color.GREEN)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)) // Set default notification sound
                .setContentIntent(pendingIntent)
              }
            NotificationType.REQUEST_FAILED_NOTIFICATION ->

            {
                val intent = Intent(context,RequestActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                notificationBuilder = Notification.Builder(context,channelId)
                    .setContentTitle(notification.title)
                    .setContentText(notification.message)
                    .setSmallIcon(R.drawable.baseline_refresh_24)
                    .setColor(Color.RED)
                    .setAutoCancel(false)
                    .setStyle(Notification.BigTextStyle()
                        .bigText(notification.longDesc))
                    .setContentIntent(pendingIntent)

            }

            NotificationType.TASK_CREATION_NOTIFICATION->{

                val intent = Intent(context,RequestActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE
                )

                notificationBuilder = Notification.Builder(context,channelId)
                    .setContentTitle(notification.title)
                    .setContentText(notification.message)
                    .setSmallIcon(R.drawable.baseline_refresh_24)
                    .setColor(Color.RED)
                    .setAutoCancel(false)
                    .setStyle(Notification.BigTextStyle()
                        .bigText(notification.longDesc))
                    .setContentIntent(pendingIntent)

            }

        }



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