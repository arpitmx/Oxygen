package com.ncs.o2.Workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.HelperClasses.NotificationUtil
import com.ncs.o2.Models.O2Notification
import com.ncs.o2.Models.ServerResult
import com.ncs.o2.Services.NotificationApiService
import com.ncs.o2.UI.Notifications.Requests.RequestActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import timber.log.Timber
import java.io.IOException
import java.lang.Exception
import java.net.ConnectException
import java.net.UnknownHostException
import java.util.concurrent.TimeoutException
import javax.inject.Inject

/*
File : FCMWorker.kt -> com.ncs.o2.Workers
Description : Worker class for Sending FCM data 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:37 pm on 01/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

@HiltWorker
class TaskRequestWorker @AssistedInject constructor(
            @Assisted context: Context,
            @Assisted workerParameters: WorkerParameters,
            @Assisted val notificationApiService: NotificationApiService,
        ) : CoroutineWorker(context, workerParameters)  {

    private val _resultLiveData =  MutableLiveData<ServerResult<String>>()
    val resultLiveData:LiveData<ServerResult<String>> get() = _resultLiveData

    private fun getJsonPayload(str:String):JsonObject{
        return Gson().fromJson(str,JsonObject::class.java)
    }


    override suspend fun doWork(): Result {

        return try {


            val payloadJson: JsonObject = getJsonPayload(inputData.getString(PAYLOAD_DATA)!!)
            val response = notificationApiService.sendNotification(payloadJson).execute()
            if (response.isSuccessful) {
                Timber.tag(TAG).d("Successful : ${response.body()}")
                _resultLiveData.postValue(ServerResult.Success("Success"))
                Result.success()
            } else {
                Timber.tag(TAG).d("Retrying : ${response.body()}")
                _resultLiveData.postValue(ServerResult.Progress)
                Result.retry()
            }

        } catch (e: UnknownHostException){
            Timber.tag(TAG).d("Retrying Connection : ${e.message}")
            _resultLiveData.postValue(ServerResult.Failure(e))
            Result.retry()
        }
        catch (e: ConnectException){
            Timber.tag(TAG).d("Retrying Connection : ${e.message}")
            _resultLiveData.postValue(ServerResult.Failure(e))
            Result.retry()
        }

        catch (e:TimeoutException){
            Timber.tag(TAG).d("Request timeout exception: ${e.message}")
            _resultLiveData.postValue(ServerResult.Failure(e))
            Result.retry()
        }
         catch (e:Exception){
            Timber.tag(TAG).d("Failed : ${e.message}")
            _resultLiveData.postValue(ServerResult.Failure(e))
             val failedNotif = O2Notification(
                 NotificationType.REQUEST_FAILED,
                 "Request Sending Failed for #12345","Click here for resending request",
                 "Cause due to -> ${e.message}")
            NotificationUtil.showNotification(notification = failedNotif, context = applicationContext)
            Result.failure()
        }
    }


    companion object{
        const val PAYLOAD_DATA = "PAYLOAD_DATA"
        const val TAG = "TaskRequestWorker"
    }

}


//        notificationApiService.sendNotification(payload)
//            .enqueue(object : retrofit2.Callback<JsonObject> {
//                override fun onResponse(
//                    call: Call<JsonObject>,
//                    response: Response<JsonObject>
//                ) {
//                    if (response.isSuccessful) {
////                        binding.titleTv.snackbar("Request Sent")
////                        binding.requestText.text = "Wait for response..."
////                        binding.progressBar.gone()
////                        binding.requestButton.isClickable = false
//                        Result.success()
//                    } else {
//                        // binding.titleTv.snackbar("Request Failed, ${response.code()}")
//                        Result.retry()
//                    }
//                }
//
//                override fun onFailure(call: Call<JsonObject>, t: Throwable) {
//                    //  binding.titleTv.snackbar("Request Failed, Retry")
//                    Result.failure()
//                }
//            })