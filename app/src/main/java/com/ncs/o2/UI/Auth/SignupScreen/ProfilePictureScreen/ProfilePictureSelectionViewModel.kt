package com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.UI.Tasks.TaskDetails.TaskDetailViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
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