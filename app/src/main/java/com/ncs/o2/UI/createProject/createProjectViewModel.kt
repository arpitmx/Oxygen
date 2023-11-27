package com.ncs.o2.UI.createProject

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class createProjectViewModel @Inject constructor(@FirebaseRepository val repository: Repository) : ViewModel() {

    fun uploadIconthroughRepository(bitmap: Bitmap, project_id: String): LiveData<ServerResult<StorageReference>> {
        return repository.uploadProjectIcon(bitmap, project_id)
    }

    fun getIconUrlThroughRepository(reference: StorageReference): LiveData<ServerResult<String>> {

        return repository.getProjectIconUrl(reference)
    }

    fun storeIconUrlToFirestore(imageUrl: String, projectName: String): LiveData<Boolean>{
        return repository.addProjectImageUrlToFirestore(imageUrl, projectName)
    }
}