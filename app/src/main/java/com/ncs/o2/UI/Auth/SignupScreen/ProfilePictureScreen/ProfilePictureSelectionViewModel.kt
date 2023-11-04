package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfilePictureSelectionViewModel @Inject constructor(@FirebaseRepository val repository: Repository) : ViewModel() {

    fun uploadDPthroughRepository(bitmap: Bitmap): LiveData<StorageReference>{

        return repository.uploadUserDP(bitmap)
    }

    fun getDPUrlTHroughRepository(reference: StorageReference): LiveData<String>{

        return repository.getUserDPUrl(reference)
    }

    fun storeDPUrlToFirestore(imageUrl: String): LiveData<Boolean>{
        return repository.addImageUrlToFirestore(imageUrl)
    }
}