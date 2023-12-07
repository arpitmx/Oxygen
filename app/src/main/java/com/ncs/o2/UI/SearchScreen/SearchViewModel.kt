package com.ncs.o2.UI.SearchScreen

import androidx.lifecycle.ViewModel
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel  @Inject constructor(private val firestoreRepository: FirestoreRepository) :  ViewModel() {
    suspend fun getSearchTasked(
        projectName: String,
        assignee:String,
        type:Int,
        state:Int,
        creator:String,
        text:String,
        resultCallback: (ServerResult<List<TaskItem>>) -> Unit
    ) {
        firestoreRepository.getSearchedTasks(projectName = projectName, assignee =assignee, type = type, state = state, text = text, creator = creator, result = resultCallback)

    }
}