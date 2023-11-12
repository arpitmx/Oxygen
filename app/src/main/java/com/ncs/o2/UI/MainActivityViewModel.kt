package com.ncs.o2.UI

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.UseCases.LoadSectionsUseCase
import com.ncs.o2.Domain.Utility.FirebaseRepository
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
class MainActivityViewModel @Inject constructor(
    @FirebaseRepository val repository: Repository,
    val sectionsUseCase: LoadSectionsUseCase,
    private val firestoreRepository: FirestoreRepository
) :
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
                    _showdialogLD.value = listOf("Errors", result.exception.message.toString())

                }
            }
        }
    }


}