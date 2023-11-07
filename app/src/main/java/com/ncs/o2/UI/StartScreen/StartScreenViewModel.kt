package com.ncs.o2.UI.StartScreen

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Services.NotificationApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import net.datafaker.providers.base.Bool
import javax.inject.Inject

@HiltViewModel
class StartScreenViewModel @Inject
constructor(@FirebaseRepository val repository: Repository) : ViewModel() {

    companion object{
        val TAG = "TaskDetailViewModel"
    }

    private val _maintainenceStatusLiveData = MutableLiveData<maintainceCheck?>()
    val maintainenceStatusLiveData: LiveData<maintainceCheck?> = _maintainenceStatusLiveData

    fun checkMaintenanceThroughRepository(): LiveData<maintainceCheck>{
        return repository.maintenanceCheck()
    }
}