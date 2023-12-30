package com.ncs.o2.Domain.Utility

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.JsonObject
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Workers.FCMWorker
import com.ncs.versa.Constants.Endpoints.Notifications as N
import java.util.concurrent.TimeUnit

/*
File : NotificationsUtils -> com.ncs.o2.Domain.Utility
Description : Utils for notifications  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:35 pm on 29/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :
*/

@SuppressLint("StaticFieldLeak")
object NotificationsUtils {

    lateinit var workManager: WorkManager
    fun initialize(context: Context) {
        workManager = WorkManager.getInstance(context)
    }

    fun sendFCMNotification(fcmToken: String, notification: Notification) {

        FirebaseAuth.getInstance().currentUser?.let {

            val payloadJsonObject = buildNotificationPayload(fcmToken, notification)
            payloadJsonObject?.let {
                val payloadInputData = Data.Builder()
                    .putString(FCMWorker.PAYLOAD_DATA, payloadJsonObject.toString())
                    .build()

                val contraints = Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()

                val workRequest = OneTimeWorkRequestBuilder<FCMWorker>()
                    .setConstraints(contraints)
                    .setBackoffCriteria(BackoffPolicy.LINEAR, 500L, TimeUnit.MICROSECONDS)
                    .setInputData(payloadInputData)
                    .build()

                workManager.enqueue(workRequest)

            }
        }
    }




    private fun buildNotificationPayload(token: String, notification: Notification): JsonObject? {

        if (notification.notificationType == NotificationType.TASK_COMMENT_NOTIFICATION.name) {

            val payload = JsonObject()
            val data = JsonObject()

            payload.addProperty(N.TO, token)
            data.addProperty(N.TITLE, notification.title)
            data.addProperty(N.BODY, notification.message)
            data.addProperty(N.TYPE, notification.notificationType)
            data.addProperty(N.TASKID, notification.taskID)

            payload.add(N.DATA, data)

            return payload
        }

        if (notification.notificationType == NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.name) {

            val payload = JsonObject()
            val data = JsonObject()

            payload.addProperty(N.TO, token)
            data.addProperty(N.TITLE, notification.title)
            data.addProperty(N.BODY, notification.message)
            data.addProperty(N.TYPE, notification.notificationType)
            data.addProperty(N.TASKID, notification.taskID)

            payload.add(N.DATA, data)

            return payload
        }

        if (notification.notificationType == NotificationType.TASK_ASSIGNED_NOTIFICATION.name) {

            val payload = JsonObject()
            val data = JsonObject()

            payload.addProperty(N.TO, token)
            data.addProperty(N.TITLE, notification.title)
            data.addProperty(N.BODY, notification.message)
            data.addProperty(N.TYPE, notification.notificationType)
            data.addProperty(N.TASKID, notification.taskID)

            payload.add(N.DATA, data)

            return payload
        }

        if (notification.notificationType == NotificationType.WORKSPACE_TASK_UPDATE.name) {

            val payload = JsonObject()
            val data = JsonObject()

            payload.addProperty(N.TO, token)
            data.addProperty(N.TITLE, notification.title)
            data.addProperty(N.BODY, notification.message)
            data.addProperty(N.TYPE, notification.notificationType)
            data.addProperty(N.TASKID, notification.taskID)

            payload.add(N.DATA, data)

            return payload
        }

        if (notification.notificationType == NotificationType.TASK_CHECKLIST_UPDATE.name) {

            val payload = JsonObject()
            val data = JsonObject()

            payload.addProperty(N.TO, token)
            data.addProperty(N.TITLE, notification.title)
            data.addProperty(N.BODY, notification.message)
            data.addProperty(N.TYPE, notification.notificationType)
            data.addProperty(N.TASKID, notification.taskID)

            payload.add(N.DATA, data)

            return payload
        }


        return null
    }

}