package com.ncs.o2.UI.Tasks.TaskPage.Chat

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    @FirebaseRepository val repository: Repository,
) : ViewModel() {

    fun getMessages(
        projectName: String,
        taskId:String,
        resultCallback: (ServerResult<List<Message>>) -> Unit
    ) {
        repository.getMessages(projectName,taskId,resultCallback)
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
    fun getDPUrlThroughRepository(reference: StorageReference): LiveData<ServerResult<String>> {

        return repository.getUserDPUrl(reference)
    }
}