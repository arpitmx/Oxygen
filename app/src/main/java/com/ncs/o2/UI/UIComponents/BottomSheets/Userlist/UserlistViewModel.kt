package com.ncs.o2.UI.UIComponents.BottomSheets.Userlist

import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.UseCases.CreateSegmentUseCase
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserlistViewModel @Inject constructor(
    @FirebaseRepository val repository: Repository,
) : ViewModel() {

    fun getContDetails(
        id: String,
        resultCallback: (ServerResult<User?>) -> Unit
    ) {
        repository.getUserInfobyId(id,resultCallback)
    }
}