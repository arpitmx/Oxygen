package com.ncs.o2.UI.CreateTask

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Constants.IDType
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Interfaces.ServerErrorCallback
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.UseCases.CreateTaskUseCase
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    (val createTaskUseCase: CreateTaskUseCase, @FirebaseRepository val repository: Repository) : ViewModel(), ServerErrorCallback
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

//        fun publishTask(task: Task, result: (ServerResult<Int>) -> Unit) {
//
//            repository.createUniqueID(idType = IDType.TaskID, task.project_ID) { taskID ->
//
//                CoroutineScope(Dispatchers.IO).launch {
//                    task.id = taskID
//                    repository.postTask(task) { repoResult ->
//                        Timber.tag(CreateTaskUseCase.TAG).d(repoResult.toString())
//                        result(repoResult)
//                    }
//
//                }
//            }
//        }

        fun addTaskThroughRepository(task: Task) {
            viewModelScope.launch {
                repository.addTask(task)
            }
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
        fun getTagsbyId(
            id:String,
            projectName: String,
            resultCallback: (ServerResult<Tag>) -> Unit
        ) {
            repository.getTagbyId(id,projectName, resultCallback)
        }

        override fun handleServerException(exceptionMessage: String) {
            Timber.tag(TAG).d("Exception : $exceptionMessage")
            _progressLiveData.postValue(false)
                _serverExceptionLiveData.postValue(exceptionMessage)
        }


        companion object{
            const val TAG = "CreateTaskViewModel"
        }

        fun uploadImagethroughRepository(bitmap: Bitmap, taskID:String): LiveData<ServerResult<StorageReference>> {
            return repository.uploadImage(bitmap, taskID)
        }
        fun getImageUrlThroughRepository(reference: StorageReference): LiveData<ServerResult<String>> {
            return repository.getImageUrl(reference)
        }
    }