package com.ncs.o2.Domain.UseCases

import com.ncs.o2.Constants.IDType
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.FirebaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/*
File : CreateTaskUseCase.kt -> com.ncs.o2.UseCases
Description : UseCase for Creating task 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:37 pm on 06/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

class CreateTaskUseCase @Inject constructor(
    @FirebaseRepository val repository: Repository
) {

    fun getTaskID() {
    }

    fun publishTask(task: Task, result: (ServerResult<Int>) -> Unit) {

        repository.createUniqueID(idType = IDType.TaskID, task.project_ID) { taskID ->

            CoroutineScope(Dispatchers.IO).launch {
                task.id = taskID
                repository.postTask(task) { repoResult ->
                    Timber.tag(TAG).d(repoResult.toString())
                    result(repoResult)
                }

            }
        }


    }

    fun notifyTaskCreationResultLocal() {

    }

    fun notifyProjectUsersAboutTaskCreation() {

    }


    companion object {
        const val TAG = "CreateTaskUseCase"
    }


}