package com.ncs.o2.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Models.ServerResult
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/*
File : MainActivityViewModel.kt -> com.ncs.o2
Description : Viewmodel for Mainactivity 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 7:28 pm on 30/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

@HiltViewModel
class MainActivityViewModel @Inject constructor(private val repository: FirestoreRepository) :
    ViewModel() {

    private val _showprogressLD = MutableLiveData<Boolean>()
    val showprogressLD: LiveData<Boolean> get() = _showprogressLD

    private val _showdialogLD = MutableLiveData<List<String>>()
    val showDialogLD: LiveData<List<String>> get() = _showdialogLD

    private val _projectListLiveData = MutableLiveData<List<String>?>()
    val projectListLiveData: LiveData<List<String>?> get() = _projectListLiveData

    fun fetchUserProjectsFromRepository() {
        repository.fetchUserProjectIDs { result ->
            when (result) {
                is ServerResult.Success -> {
                    _showprogressLD.value = false
                    _projectListLiveData.value = result.data
                }

                is ServerResult.Progress -> {
                    _showprogressLD.value = true
                }

                is ServerResult.Failure -> {

                    _showprogressLD.value = false
                    _showdialogLD.value = listOf("Error", result.exception.message.toString())

                }
            }
        }
    }

}