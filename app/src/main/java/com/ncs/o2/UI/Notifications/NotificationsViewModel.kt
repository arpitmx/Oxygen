package com.ncs.o2.UI.Notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDao
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import io.grpc.Server
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

/*
File : NotificationsViewModel.kt -> com.ncs.o2.UI.Notifications
Description : Viewmodel for notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:52 pm on 01/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
@HiltViewModel
class NotificationsViewModel @Inject constructor
    (@FirebaseRepository val repository: Repository, db: NotificationDatabase) : ViewModel() {

   //Timestamp
    private val _serverResultLiveData = MutableLiveData<ServerResult<Long>>()
    val serverResultLiveData: LiveData<ServerResult<Long>> get() = _serverResultLiveData


    //NotificationList
    private val _notificationsResult = MutableLiveData<ServerResult<List<Notification>>>()
    val notificationsResult: LiveData<ServerResult<List<Notification>>> get() = _notificationsResult


    private val notificationDAO: NotificationDao by lazy {
        db.notificationDao()
    }

    fun updateNotificationViewTimeStamp() {
        CoroutineScope(Dispatchers.IO).launch {
            repository.updateNotificationTimeStampPath { result ->

                when (result) {
                    is ServerResult.Failure -> {
                        _serverResultLiveData.postValue(result)
                        Timber.tag("Notification Viewmodel : Failure : ${result}")
                    }

                    ServerResult.Progress -> {
                        _serverResultLiveData.postValue(result)
                        Timber.tag("Notification Viewmodel : Progress : ${result}")

                    }

                    is ServerResult.Success -> {
                        _serverResultLiveData.postValue(result)
                        Timber.tag("Notification Viewmodel : Success: ${result}")
                    }
                }

            }
        }
    }

    fun fetchNotifications() {
        viewModelScope.launch {
            try {
                _notificationsResult.value = ServerResult.Progress
                val notificationList = CoroutineScope(Dispatchers.IO).async{
                    notificationDAO.getAllNotifications()
                }.await()
                _notificationsResult.value = ServerResult.Success(notificationList)
            } catch (e: Exception) {
                _notificationsResult.value = ServerResult.Failure(e)
            }
        }
    }
}