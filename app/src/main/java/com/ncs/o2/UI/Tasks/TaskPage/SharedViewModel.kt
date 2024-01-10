package com.ncs.o2.UI.Tasks.TaskPage

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import timber.log.Timber
import java.util.Stack

/*
File : SharedViewModel -> com.ncs.o2.UI.Tasks.TaskPage
Description : Shared view model for common data between Chat, details  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 5:51 pm on 29/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :
*/
class SharedViewModel : ViewModel(){

    private val _notificationReceiverLiveData = MutableLiveData<MutableList<String>>()
    val notificationReceiverLiveData: LiveData<MutableList<String>> get() = _notificationReceiverLiveData


    fun getList(): MutableList<String> {
        return _notificationReceiverLiveData.value.orEmpty().distinct().toMutableList()
    }

    fun pushReceiver(token : String){
        val currentList = _notificationReceiverLiveData.value.orEmpty().toMutableList()
        currentList.add(token)
        _notificationReceiverLiveData.value = currentList

        Timber.tag("SharedViewModel").d("Pushed : $token")
    }

    fun removeItemFromList(item: String) {
        val currentList = _notificationReceiverLiveData.value.orEmpty().toMutableList()
        currentList.remove(item)
        _notificationReceiverLiveData.value = currentList

        Timber.tag("SharedViewModel").d("Popped : $item")

    }
}