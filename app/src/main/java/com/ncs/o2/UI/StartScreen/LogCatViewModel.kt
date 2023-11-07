package com.ncs.o2.UI.StartScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import androidx.lifecycle.viewModelScope
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/*
File : LogCatViewModel -> com.ncs.o2.UI.StartScreen
Description : Viewmodel for logcat 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 6:03 pm on 04/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
@HiltViewModel
class LogCatViewModel @Inject
constructor(@FirebaseRepository val repository: Repository) : ViewModel(){
    fun logCatOutput() = liveData(viewModelScope.coroutineContext + Dispatchers.IO) {
        Runtime.getRuntime().exec("logcat -c")
        Runtime.getRuntime().exec("logcat")
            .inputStream
            .bufferedReader()
            .useLines { lines -> lines.forEach { line -> emit(line) }
            }
    }

    fun checkMaintenanceThroughRepository(): LiveData<maintainceCheck> {
        return repository.maintenanceCheck()
    }

    fun updateFCMToken() {

    }
}