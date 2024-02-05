package com.ncs.o2.UI.Assigned

import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.WorkspaceTaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignedViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository,private val taskRepository: TaskRepository) : ViewModel(){

    var sectionName: String? = null


    fun getUserTasksId(
        sectionName:String,
        projectName: String,
        resultCallback: (ServerResult<List<WorkspaceTaskItem>?>) -> Unit
    ) {
        firestoreRepository.getUserTasks(sectionName,projectName,resultCallback)
    }

    fun getTasksForID(
        projectName: String,
        taskID:String,
        resultCallback: (DBResult<Task>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTaskbyID(projectName, taskID, resultCallback)
        }
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
    fun getTasksbyIdFromDB(
        projectName: String,
        taskId:String,
        resultCallback: (DBResult<Task>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTaskbyID(projectName,taskId,resultCallback)
        }
    }




}