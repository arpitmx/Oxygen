package com.ncs.o2.UI.Tasks.TaskPage

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
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Repositories.TaskRepository
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Api.NotificationApiService
import com.ncs.o2.Domain.Workers.FCMWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            @FirebaseRepository val repository: Repository, val app: Application, private val firestoreRepository: FirestoreRepository,
            val taskRepository: TaskRepository
) : AndroidViewModel(app) {

    companion object{
        val TAG = "TaskDetailViewModel"
    }

    private val _notificationStatusLiveData = MutableLiveData<ServerResult<Int>>()
    val notificationStatusLiveData: LiveData<ServerResult<Int>> = _notificationStatusLiveData
    var task: Task? = null

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
        //data.addProperty("body", Faker().bigBangTheory().quote().toString())
        payload.add("data", data)
        return payload
    }


    suspend fun getTasksById(id: String, projectName: String): ServerResult<Task> {
        return firestoreRepository.getTasksbyId(id, projectName = projectName)
    }




    fun getTagsbyId(
        id:String,
        projectName: String,
        resultCallback: (ServerResult<Tag>) -> Unit
    ) {
        firestoreRepository.getTagbyId(id,projectName, resultCallback)
    }
    fun getUserbyId(
        id:String,
        resultCallback: (ServerResult<User?>) -> Unit
    ) {
        firestoreRepository.getUserInfobyId(id,resultCallback)
    }

    suspend fun updateTask(taskID:String,projectName: String,NewAssignee:String,OldAssignee:String):ServerResult<Boolean>{
        return firestoreRepository.updateTask(id = taskID, projectName = projectName, newAssignee = NewAssignee, oldAssignee =  OldAssignee)
    }

    suspend fun updateTaskSummary(task: Task,newSummary:String):ServerResult<Boolean>{
        return firestoreRepository.updateTaskSummary(task = task,newSummary=newSummary)
    }

    suspend fun updateState(taskID:String,userID:String,newState:String,projectName: String):ServerResult<Boolean>{
        return firestoreRepository.updateState(id = taskID, userID = userID,newState=newState, projectName = projectName)
    }
    suspend fun updatePriority(taskID:String,newPriority:String,projectName: String):ServerResult<Boolean>{
        return firestoreRepository.updatePriority(id = taskID, newPriority = newPriority, projectName = projectName)
    }
    suspend fun updateType(taskID:String,newType:String,projectName: String):ServerResult<Boolean>{
        return firestoreRepository.updateType(id = taskID, newType = newType, projectName = projectName)
    }
    suspend fun updateDifficulty(taskID:String,newDifficulty:String,projectName: String):ServerResult<Boolean>{
        return firestoreRepository.updateDifficulty(id = taskID, newDifficulty = newDifficulty, projectName = projectName)
    }
    suspend fun updateDuration(taskID:String,newDuration:String,projectName: String):ServerResult<Boolean>{
        return firestoreRepository.updateDration(id = taskID, newDuration = newDuration, projectName = projectName)
    }
    suspend fun updateModerators(taskID:String,projectName: String,moderator:String):ServerResult<Unit>{
        return firestoreRepository.updateModerator(id = taskID, projectName = projectName,moderator=moderator)
    }

    suspend fun updateSection(taskID:String,projectName: String,newSection:String):ServerResult<Boolean>{
        return firestoreRepository.updateSection(taskId = taskID, projectName = projectName,newSection=newSection)
    }
    suspend fun updateTitle(taskID:String,projectName: String,newTitle:String):ServerResult<Boolean>{
        return firestoreRepository.updateTitle(taskId = taskID, projectName = projectName,newTitle=newTitle)
    }
    suspend fun addNewModerators(taskID:String,projectName: String,moderator:MutableList<String>,unselected:MutableList<String>):ServerResult<Boolean>{
        return firestoreRepository.addNewModerator(id = taskID, projectName = projectName, newModerators =moderator,unselected=unselected)
    }

    suspend fun updateTags(newTags:List<String>,projectName: String,taskID: String):ServerResult<Boolean>{
        return firestoreRepository.updateTags(newTags = newTags, projectName = projectName, taskId = taskID)
    }

    fun getTaskbyIdFromDB(
        projectName: String,
        taskId:String,
        resultCallback: (DBResult<Task>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTaskbyID(projectName,taskId,resultCallback)
        }
    }

    fun addNotificationToFirebase(user_id: String,notification: Notification,resultCallback: (ServerResult<Int>) -> Unit){
        repository.insertNotification(userID=user_id, notification = notification, result = resultCallback)
    }


}