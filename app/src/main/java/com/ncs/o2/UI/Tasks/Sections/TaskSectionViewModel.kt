package com.ncs.o2.UI.Tasks.Sections

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
class TaskSectionViewModel @Inject constructor(private val firestoreRepository: FirestoreRepository) : ViewModel(){

    fun getTasksItemsForSegment(
        projectName: String,
        segmentName: String,
        sectionName: String,
        resultCallback: (ServerResult<List<TaskItem>>) -> Unit
    ) {
        firestoreRepository.getTasksItem(projectName, segmentName, sectionName, resultCallback)
    }
    fun getUserbyId(
        id:String,
        resultCallback: (ServerResult<User?>) -> Unit
    ) {
        firestoreRepository.getUserInfobyId(id,resultCallback)
    }

}