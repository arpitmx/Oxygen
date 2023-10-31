package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfilePictureSelectionViewModel  @Inject constructor(
    @FirebaseRepository val repository: Repository,
) : ViewModel() {
    private val _user = MutableLiveData<CurrentUser?>()
    val user = _user

    fun fetchUserInfo() {
        repository.getUserInfo { result ->
            when (result) {
                is ServerResult.Success -> {
                    _user.value = result.data
                }
                else -> {
                }
            }
        }
    }
}