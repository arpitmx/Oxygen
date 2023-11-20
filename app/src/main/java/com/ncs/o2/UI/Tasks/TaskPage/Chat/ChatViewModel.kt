package com.ncs.o2.UI.Tasks.TaskPage.Chat

import androidx.lifecycle.ViewModel
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
}