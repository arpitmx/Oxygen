package com.ncs.o2.UI.Assigned

import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.WorkspaceTaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AssignedViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository) : ViewModel(){


    fun getUserTasksId(
        sectionName:String,
        resultCallback: (ServerResult<List<WorkspaceTaskItem>?>) -> Unit
    ) {
        firestoreRepository.getUserTasks(sectionName,resultCallback)
    }

    fun getTasksItembyId(
        projectName:String,
        id:String,
        resultCallback: (ServerResult<TaskItem>) -> Unit
    ) {
        firestoreRepository.getTaskItembyId(projectName = projectName, id = id,
            result = resultCallback
        )
    }


}