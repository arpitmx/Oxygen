package com.ncs.o2.UI.Tasks.TaskDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.JsonObject
import com.ncs.o2.Services.NotificationApiService
import com.ncs.o2.Domain.Workers.FCMWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/*
File : TaskDetailViewModel.kt -> com.ncs.o2.UI.Tasks.TaskDetails
Description : ViewModel for task details 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity (@Project : O2 Android)

Creation : 4:41 am on 02/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/

@HiltViewModel
class TaskDetailViewModel @Inject constructor(val notificationApiService: NotificationApiService, val app: Application
) : AndroidViewModel(app) {

    private val test_fcmtokenMI =
        "cQiVebLLTPutKWl2t_13mY:APA91bH-fGRZ06pGDDMx70JwOqB3DI_n-CDbmEzcXGMGSOXrubXSTMx63T11TYFe5WnHT3Tc-wNTcpA7hIY4moZUNzglEjL8pe5Bm21WUh_u5-TaY_mkTxm5BIVlDHfOdTCPz4hL-45F"
    private val test_fcmtokenEmulator =
        "es4CBA66TeCyvGXjAwNnOi:APA91bFMqBwGPNp-g__CEw3EHSAQabLrVwnTBJnU-zYL1_5t_qnIZX06t96xkoXtBm7m1ZZzyqHzsOlv2-WgMEhnYHLCCJM5x7n8cnoAJb9Em3m5HCWd8t-ueocKX1cpfUopmmL1TVan"


    fun sendNotification(){
        val payloadJsonObject = buildNotificationPayload(test_fcmtokenMI)

        val payloadInputData = Data.Builder()
            .putString(FCMWorker.PAYLOAD_DATA,payloadJsonObject.toString())
            .build()

        val contraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<FCMWorker>()
             .setConstraints(contraints)
            .setBackoffCriteria(BackoffPolicy.LINEAR,500L,TimeUnit.MICROSECONDS)
            .setInputData(payloadInputData)
            .build()

        WorkManager.getInstance(app.applicationContext).enqueue(workRequest)

    }


    private fun buildNotificationPayload(token: String): JsonObject {
        val payload = JsonObject()
        payload.addProperty("to", token)
        val data = JsonObject()
        data.addProperty("title", "Work request")
        data.addProperty("body", "Armax wants to work on #1234")
        payload.add("data", data)
        return payload
    }







}