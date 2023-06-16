package com.ncs.o2.Domain.Interfaces

import com.ncs.o2.Constants.IDType
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Segment

/*
File : Repository.kt -> com.ncs.o2.Domain.Interfaces
Description : This is the repository interface for extending them to the different Repositories 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 8:57 am on 07/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

interface Repository {


    fun setCallback(callback: ServerErrorCallback)
    fun createUniqueID(idType: IDType, projectID: String, generatedID:(String)->Unit)
    //Task related
    suspend fun postTask(task: Task, serverResult: (ServerResult<Int>)-> Unit)

    //User related
    fun getUserInfo(serverResult: (ServerResult<CurrentUser?>) -> Unit)

    //Project related
    fun fetchUserProjectIDs(projectListCallback: (ServerResult<List<String>>) -> Unit)

    fun createSegment(segment: Segment, serverResult: (ServerResult<Int>) -> Unit)

    fun checkIfSegmentNameExists(fieldName : String, projectID : String, result: (ServerResult<Boolean>) -> Unit)
}