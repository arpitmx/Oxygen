package com.ncs.o2.UI.Tasks.Sections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Repositories.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.datafaker.providers.base.Bool
import javax.inject.Inject

/*
File : TaskSectionViewModel.kt -> com.ncs.o2.UI.Tasks.Sections
Description : View model for task section 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:59 pm on 13/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 




*/
@HiltViewModel
class TaskSectionViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository, private val taskRepository: TaskRepository) : ViewModel(){


    var sectionName: String? = null

    var isReturning : Boolean = false

    var scrollPosition : Int = 0


    private val _currentSegment = MutableLiveData<String>()
    val currentSegment: LiveData<String> get() = _currentSegment

    var currentSelected="all"

    fun getTasksItemsForSegment(
        projectName: String,
        segmentName: String,
        sectionName: String,
        resultCallback: (ServerResult<List<TaskItem>>) -> Unit
    ) {
       CoroutineScope(Dispatchers.Main).launch {
           firestoreRepository.getTasksItem(projectName, segmentName, sectionName, resultCallback)
       }
    }
    fun getTasksForSegmentFromDB(
        projectName: String,
        segmentName: String,
        sectionName: String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTasksItemsForSegment(projectName, segmentName, sectionName, resultCallback)
        }
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
    fun getTasksForStateFromDB(
        projectName: String,
        state:Int,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTasksItemsForState(projectName, state, resultCallback)
        }
    }
    fun getTagsInProject(
        projectName:String,
        resultCallback: (DBResult<List<Tag>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTagsInProject(projectName, resultCallback)
        }
    }
    fun getTasksForSegment(
        projectName: String,
        segmentName: String,
        sectionName: String,
        resultCallback: (ServerResult<List<Task>>) -> Unit
    ) {
        firestoreRepository.getTasks(projectName, segmentName, sectionName, resultCallback)
    }
    fun getUserbyId(
        id:String,
        resultCallback: (ServerResult<User?>) -> Unit
    ) {
        firestoreRepository.getUserInfobyId(id,resultCallback)
    }

    fun getTasksforSegmentsforAssignee(
        projectName: String,
        assignee:String,
        state:Int,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTasksItemsForStatewithAssignee(projectName,assignee, state,resultCallback)
        }
    }

    fun getTasksforModerators(
        projectName: String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTasksInProject(projectName,resultCallback)
        }
    }

    fun getTasksOpenedBy(
        projectName: String,
        assigneer:String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            taskRepository.getTasksItemsOpenedBy(projectName,assigneer,resultCallback)
        }
    }

    fun updateCurrentSegment(newSegment: String) {
        _currentSegment.value = newSegment
    }






}