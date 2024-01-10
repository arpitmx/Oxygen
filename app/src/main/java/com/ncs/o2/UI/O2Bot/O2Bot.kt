package com.ncs.o2.UI.O2Bot

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

/*
File : O2Bot -> com.ncs.o2.UI.O2Bot
Description : Bot file for O2 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 2:56 am on 06/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

@HiltViewModel
class O2Bot @Inject constructor(@FirebaseRepository val repository: Repository): ViewModel() {


    private val _serverResultLiveData = MutableLiveData<ServerResult<Int>>()
    val serverResultLiveData: LiveData<ServerResult<Int>> get() = _serverResultLiveData

    fun botPostBug(title : String, description : String){

        val task= Task(
            title = description,
            description = title,
            id ="#T${RandomIDGenerator.generateRandomTaskId(5)}",
            difficulty = 2,
            priority = 2,
            status = 0,
            assigner = "oxygenbot@hackncs.in",
            project_ID = "NCSOxygen",
            segment = "Bugs\uD83D\uDC1E", //change segments here //like Design
            section = "Bugs Found",  //Testing // Completed //Ready //Ongoing
            duration = Random(System.currentTimeMillis()).nextInt(1,5).toString(),
            time_STAMP = Timestamp.now(),
        )

        CoroutineScope(Dispatchers.Main).launch {


            repository.postTask(task, mutableListOf()) { result ->

                when (result) {

                    is ServerResult.Failure -> {
                        _serverResultLiveData.postValue(ServerResult.Failure(result.exception))
                    }

                    ServerResult.Progress -> {
                        _serverResultLiveData.postValue(ServerResult.Progress)

                    }

                    is ServerResult.Success -> {
                        _serverResultLiveData.postValue(ServerResult.Success(200))

                    }

                }
            }
        }
    }



}