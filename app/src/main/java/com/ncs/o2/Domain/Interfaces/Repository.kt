package com.ncs.o2.Domain.Interfaces

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Constants.IDType
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserInfo
import com.ncs.o2.UI.StartScreen.maintainceCheck

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
    fun createUniqueID(idType: IDType, projectID: String, generatedID: (String) -> Unit)

    //Task related
    suspend fun postTask(task: Task, serverResult: (ServerResult<Int>) -> Unit)

    suspend fun addTask(task: Task)
    suspend fun postTags(tag: Tag, projectName: String, serverResult: (ServerResult<Int>) -> Unit)
    suspend fun fetchProjectTags(
        projectName: String,
        projectListCallback: (ServerResult<List<Tag>>) -> Unit
    )


    //User related

    fun getTagbyId(
        id: String,
        projectName: String,
        result: (ServerResult<Tag>) -> Unit
    )


    suspend fun getTasksbyId(
        id: String,
        projectName: String,
    ): ServerResult<Task>


    fun getUserInfo(serverResult: (ServerResult<CurrentUser?>) -> Unit)
    fun getUserInfobyId(id: String, serverResult: (ServerResult<User?>) -> Unit)
    fun getSection(projectName: String, segmentName: String, result: (ServerResult<List<*>>) -> Unit)

    fun getUserInfoEditProfile(serverResult: (ServerResult<UserInfo?>) -> Unit)
    fun editUserInfo(userInfo: UserInfo, serverResult: (ServerResult<UserInfo?>) -> Unit)

    //Project related
    fun fetchUserProjectIDs(projectListCallback: (ServerResult<List<String>>) -> Unit)

    fun createSegment(segment: Segment, serverResult: (ServerResult<Int>) -> Unit)

    //Notifications Related
    suspend fun updateNotificationTimeStampPath(serverResult: (ServerResult<Long>) -> Unit)
    suspend fun getNewNotifications(
        lastSeenTimeStamp: Long,
        serverResult: (ServerResult<List<Notification>>) -> Unit
    )

    suspend fun postNotification(
        notification: Notification,
        serverResult: (ServerResult<Int>) -> Unit
    )

    suspend fun setFCMToken(token: String, serverResult: (ServerResult<Int>) -> Unit)
    suspend fun getNotificationLastSeenTimeStamp(serverResult: (ServerResult<Long>) -> Unit)


    fun maintenanceCheck(): LiveData<maintainceCheck>

    // User DP Related
    fun getUserDPUrl(reference: StorageReference): LiveData<ServerResult<String>>
    fun uploadUserDP(bitmap: Bitmap): LiveData<ServerResult<StorageReference>>
    fun addImageUrlToFirestore(DPUrl: String): LiveData<Boolean>

    fun checkIfSegmentNameExists(
        fieldName: String,
        projectID: String,
        result: (ServerResult<Boolean>) -> Unit
    )
}