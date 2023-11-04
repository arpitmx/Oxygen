package com.ncs.o2.UI.Tasks.TaskDetails

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.gson.JsonObject
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Services.NotificationApiService
import com.ncs.o2.Domain.Workers.FCMWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import net.datafaker.Faker
import timber.log.Timber
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
class TaskDetailViewModel @Inject
constructor(val notificationApiService: NotificationApiService,
            @FirebaseRepository val repository: Repository, val app: Application
) : AndroidViewModel(app) {

    companion object{
        val TAG = "TaskDetailViewModel"
    }

    private val _notificationStatusLiveData = MutableLiveData<ServerResult<Int>>()
    val notificationStatusLiveData: LiveData<ServerResult<Int>> = _notificationStatusLiveData

    suspend fun sendNotificationToReceiverFirebase(notification: Notification, fcmToken: String){

        repository.postNotification(notification){ result ->
            when(result){
                is ServerResult.Failure -> {
                    Timber.tag(TAG).d("Failure : ${result.exception.printStackTrace()}")
                    _notificationStatusLiveData.postValue(result)
                }
                ServerResult.Progress -> {
                    Timber.tag(TAG).d("In progress")
                    _notificationStatusLiveData.postValue(result)
                }
                is ServerResult.Success -> {

                    Timber.tag(TAG).d("Saving to firebase success : ${result.data}")
                    sendFCMNotification(fcmToken)

                    Timber.tag(TAG).d("Sending FCM Notification")
                    _notificationStatusLiveData.postValue(result)

                }
            }
        }
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
        data.addProperty("body", Faker().bigBangTheory().quote().toString())
        payload.add("data", data)
        return payload
    }







}