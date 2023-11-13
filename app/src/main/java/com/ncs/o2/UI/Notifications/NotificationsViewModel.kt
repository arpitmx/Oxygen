package com.ncs.o2.UI.Notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    (@FirebaseRepository val repository: Repository) : ViewModel()
{
    private val _serverResultLiveData = MutableLiveData<ServerResult<Long>>()
    val serverResultLiveData: LiveData<ServerResult<Long>> get() = _serverResultLiveData


    fun updateNotificationViewTimeStamp(){
        CoroutineScope(Dispatchers.IO).launch{
            repository.updateNotificationTimeStampPath { result->

                when(result){
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

}