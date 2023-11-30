package com.ncs.o2.Domain.Utility

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.JsonObject
import com.ncs.o2.Domain.Workers.FCMWorker
import net.datafaker.Faker
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
    fun initialize( context: Context){
        workManager = WorkManager.getInstance(context)
    }

    fun sendFCMNotification(fcmToken : String){

        val payloadJsonObject = buildNotificationPayload(fcmToken)

        val payloadInputData = Data.Builder()
            .putString(FCMWorker.PAYLOAD_DATA,payloadJsonObject.toString())
            .build()

        val contraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<FCMWorker>()
            .setConstraints(contraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR,500L, TimeUnit.MICROSECONDS)
            .setInputData(payloadInputData)
            .build()

        workManager.enqueue(workRequest)

    }


    private fun buildNotificationPayload(token: String): JsonObject {
        val payload = JsonObject()
        val data = JsonObject()


        payload.addProperty("to", token)

        data.addProperty("title", "Work request")
        data.addProperty("body", Faker().bigBangTheory().quote().toString())

        payload.add("data", data)

        return payload
    }

}