package com.ncs.o2.UI.Tasks.TaskPage.Chat

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Repositories.TaskRepository
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @FirebaseRepository val repository: Repository,
    val taskRepository: TaskRepository,
    val app: Application
) : AndroidViewModel(app) {

    @Inject
    lateinit var msgDB:MessageDatabase

    var CHAT_WINDOW_OPTION_BOX_STATUS : Boolean = false



    fun getMessages(
        projectName: String,
        taskId:String,
        resultCallback: (ServerResult<List<Message>>) -> Unit
    ) {
        repository.getMessages(projectName,taskId,resultCallback)
    }

    fun getNewMessages(
    projectName: String,
    taskId:String,
    resultCallback: (ServerResult<List<Message>>) -> Unit
    ) {
        repository.getNewMessages(projectName,taskId,resultCallback)
    }

    fun getTeamsMessages(
        projectName: String,
        resultCallback: (ServerResult<List<Message>>) -> Unit
    ) {
        repository.getTeamsMessages(projectName,resultCallback)
    }

    fun getNewTeamsMessages(
        projectName: String,
        resultCallback: (ServerResult<List<Message>>) -> Unit
    ) {
        repository.getNewTeamsMessages(projectName,resultCallback)
    }
    fun getUserbyId(
        user_id:String,
        resultCallback: (ServerResult<UserInMessage>) -> Unit
    ) {
        repository.getMessageUserInfobyId(id = user_id,resultCallback)
    }
    fun uploadImage(bitmap: Bitmap,projectId:String,taskId:String): LiveData<ServerResult<StorageReference>> {
        return repository.postImage(bitmap,projectId,taskId)
    }
    fun uploadImageFromTeams(bitmap: Bitmap,projectId:String): LiveData<ServerResult<StorageReference>> {
        return repository.postTeamsImage(bitmap,projectId)
    }
    fun getDPUrlThroughRepository(reference: StorageReference): LiveData<ServerResult<String>> {
        return repository.getUserDPUrl(reference)
    }

    fun addNotificationToFirebase(user_id: String,notification: Notification,resultCallback: (ServerResult<Int>) -> Unit){
        repository.insertNotification(userID=user_id, notification = notification, result = resultCallback)
    }
    fun getMessagesforProjectandTask(
        projectName: String,
        taskId:String,
        resultCallback: (DBResult<List<Message>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getMessagesforTask(projectName,taskId,resultCallback)
        }

    }

    fun getTeamsMessagesforProject(
        projectName: String,
        resultCallback: (DBResult<List<Message>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTeamsMessagesforProject(projectName,resultCallback)
        }

    }




}