package com.ncs.o2.UI.CreateTask

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.o2.Domain.Interfaces.ServerErrorCallback
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.UseCases.CreateTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/*
File : CreateTaskViewModel.kt -> com.ncs.o2.UI.CreateTask
Description : ViewModel for creating task activity 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:34 pm on 06/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

@HiltViewModel
class CreateTaskViewModel @Inject constructor
    (val createTaskUseCase: CreateTaskUseCase) : ViewModel(), ServerErrorCallback
    {

        private val _serverExceptionLiveData = MutableLiveData<String>()
        val serverExceptionLiveData: LiveData<String> get() = _serverExceptionLiveData


        private val _progressLiveData = MutableLiveData<Boolean>()
        val progressLiveData: LiveData<Boolean> get() = _progressLiveData

        private val _successLiveData = MutableLiveData<Boolean>()
        val successLiveData: LiveData<Boolean> get() = _successLiveData


        init {
            createTaskUseCase.repository.setCallback(this)
        }

        fun createTask(task : Task){
          viewModelScope.launch{
              try {
                  createTaskUseCase.publishTask(task){ publishResult->
                      when(publishResult){
                          is ServerResult.Failure -> {
                              Timber.tag(TAG).d("Server result -> Failure : ${publishResult.exception.message}")
                              _progressLiveData.postValue(false)
                              _successLiveData.postValue(false)
                              _serverExceptionLiveData.postValue(publishResult.exception.message)
                          }
                          is ServerResult.Progress -> {
                              Timber.tag(TAG).d("Server result -> Progress ")
                              _progressLiveData.postValue(true)
                          }
                          is ServerResult.Success -> {
                              if (publishResult.data==200){
                                  Timber.tag(TAG).d("Server result -> Failure : ${publishResult.data}")
                                  _progressLiveData.postValue(false)
                                  _successLiveData.postValue(true)
                              }
                          }
                      }
                  }
              }
              catch (exception : Exception){
                  Timber.tag(TAG).d("Server result -> Exception : ${exception.message}")
                  _progressLiveData.postValue(false)
                  _serverExceptionLiveData.postValue(exception.message)
              }
          }

        }

        override fun handleServerException(exceptionMessage: String) {
            Timber.tag(TAG).d("Exception : $exceptionMessage")
            _progressLiveData.postValue(false)
                _serverExceptionLiveData.postValue(exceptionMessage)
        }


        companion object{
            const val TAG = "CreateTaskViewModel"
        }
    }