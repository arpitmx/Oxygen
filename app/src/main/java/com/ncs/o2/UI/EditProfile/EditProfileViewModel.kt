package com.ncs.o2.UI.EditProfile

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.UserInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val repository: Repository
):ViewModel() {

    val _getUserDetails = MutableLiveData<ServerResult<UserInfo>>()
    val _editUserDetails = MutableLiveData<ServerResult<UserInfo>>()


    fun getUserDetails()= viewModelScope.launch(Dispatchers.IO){

        _getUserDetails.postValue(ServerResult.Progress)

        try {
            if (hasInternetConnection()){
                repository.getUserInfoEditProfile(){ result ->
                    when (result) {
                        is ServerResult.Success -> {
                            // Handle success
                            val data = result.data
                            _getUserDetails.postValue(ServerResult.Success(data!!))
                        }
                        is ServerResult.Failure -> {
                            // Handle error
                            val message = result.exception
                            _getUserDetails.postValue(ServerResult.Failure(message))
                        }
                        else -> {
                            Unit
                        }
                    }
                }

            }
        } catch (t: Throwable){
            when(t){
                is IOException -> _getUserDetails.postValue(ServerResult.Failure(t))
            }
        }
    }

    fun editUserDetails(userInfo: UserInfo)= viewModelScope.launch(Dispatchers.IO){

        _editUserDetails.postValue(ServerResult.Progress)

        try {
            if (hasInternetConnection()){
                repository.editUserInfo(userInfo){ result ->
                    when (result) {
                        is ServerResult.Success -> {
                            // Handle success
                            val data = result.data
                            _editUserDetails.postValue(ServerResult.Success(data!!))
                        }
                        is ServerResult.Failure -> {
                            // Handle error
                            val message = result.exception
                            _editUserDetails.postValue(ServerResult.Failure(message))
                        }
                        else -> {
                           Unit
                        }
                    }
                }

            }
        } catch (t: Throwable){
            when(t){
                is IOException -> _editUserDetails.postValue(ServerResult.Failure(t))
            }
        }
    }

    private fun hasInternetConnection(): Boolean{

        // TODO check internet connection
        return true
    }
}

