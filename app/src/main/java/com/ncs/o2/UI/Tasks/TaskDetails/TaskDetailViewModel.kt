package com.ncs.o2.UI.Tasks.TaskDetails

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.JsonObject
import com.ncs.o2.O2Application
import com.ncs.o2.Services.NotificationApiService
import com.ncs.o2.Utility.ExtensionsUtil.gone
import com.ncs.o2.Utility.ExtensionsUtil.snackbar
import com.ncs.o2.Workers.TaskRequestWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import retrofit2.Call
import retrofit2.Response
import java.sql.Time
import java.time.Duration
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
        "cmMiVraYTkyhLGCWh8aorx:APA91bGe-6OkspkpxE9-fpxsOwslGHAlwRxG45gbeg2dxY6MckcpS-PnOl1TQvOVaZ9E90VFtWCBw3qftKJS2DkdYCEgqgGrWxRrjnsbIz4SD0j40oeLbC3OfXRe9ebC38-2xoLMDjmN"
    private val test_fcmtokenEmulator =
        "es4CBA66TeCyvGXjAwNnOi:APA91bFMqBwGPNp-g__CEw3EHSAQabLrVwnTBJnU-zYL1_5t_qnIZX06t96xkoXtBm7m1ZZzyqHzsOlv2-WgMEhnYHLCCJM5x7n8cnoAJb9Em3m5HCWd8t-ueocKX1cpfUopmmL1TVan"


    fun sendNotification(){
        val payloadJsonObject = buildNotificationPayload(test_fcmtokenMI)

        val payloadInputData = Data.Builder()
            .putString(TaskRequestWorker.PAYLOAD_DATA,payloadJsonObject.toString())
            .build()

        val contraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<TaskRequestWorker>()
          //  .setConstraints(contraints)
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