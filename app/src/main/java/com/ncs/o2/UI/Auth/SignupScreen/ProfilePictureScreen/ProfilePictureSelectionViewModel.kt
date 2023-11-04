package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfilePictureSelectionViewModel @Inject constructor(@FirebaseRepository val repository: Repository) : ViewModel() {

    fun uploadDPthroughRepository(bitmap: Bitmap): LiveData<ServerResult<StorageReference>> {

        return repository.uploadUserDP(bitmap)
    }

    fun getDPUrlThroughRepository(reference: StorageReference): LiveData<ServerResult<String>> {

        return repository.getUserDPUrl(reference)
    }

    fun storeDPUrlToFirestore(imageUrl: String): LiveData<Boolean>{
        return repository.addImageUrlToFirestore(imageUrl)
    }
}