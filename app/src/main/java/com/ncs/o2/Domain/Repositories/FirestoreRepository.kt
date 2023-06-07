package com.ncs.o2.Domain.Repositories

import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.lang.StringBuilder
import javax.inject.Inject

/*
File : FirestoreRepository.kt -> com.ncs.o2.UI
Description : This is the class for firestore repository 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:15 pm on 30/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : Repository {

    private val TAG: String = FirestoreRepository::class.java.simpleName

    fun getTaskPath(task:Task):String{
        return Endpoints.PROJECTS +
                "/${task.PROJECTID}" +
                "/${Endpoints.Project.SEGMENT}"+
                "/${task.SEGMENT}"+
                "/${Endpoints.SEGMENT.TASKS}"+
                "/${task.ID}"+
                "/"

    }

    override fun postTask(task: Task, serverResult: (ServerResult<Int>)-> Unit){
        serverResult(ServerResult.Progress)
        firestore.document(getTaskPath(task))
            .set(task)
            .addOnSuccessListener {
                serverResult(ServerResult.Success(200))
            }
            .addOnFailureListener{
                serverResult(ServerResult.Failure(it))
            }


    }

  override fun getUserInfo(serverResult: (ServerResult<User?>) -> Unit) {
        serverResult(ServerResult.Progress)
        Handler(Looper.getMainLooper()).postDelayed({
            var user: User?
            firestore.collection(Endpoints.USERS)
                .document(Endpoints.TESTUSERID)
                .get(Source.SERVER)
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        user = snap.toObject(User::class.java)

                        Timber.tag(TAG).d(user?.USERNAME)
                        Timber.tag(TAG).d(user?.EMAIL)
                        Timber.tag(TAG).d(user?.PROJECTS.toString())

                        serverResult(ServerResult.Success(user))
                    }
                }
                .addOnFailureListener { error ->
                    Timber.tag(TAG).d("failed %s", error.stackTrace)
                    serverResult(ServerResult.Failure(error))
                }
        }, 1000)


    }

    override fun fetchUserProjectIDs(projectListCallback: (ServerResult<List<String>>) -> Unit) {
        getUserInfo { result ->

            when (result) {
                is ServerResult.Success -> {
                    projectListCallback(ServerResult.Success(result.data!!.PROJECTS))
                }

                is ServerResult.Progress -> {
                    projectListCallback(ServerResult.Progress)

                }

                is ServerResult.Failure -> {
                    projectListCallback(ServerResult.Failure(result.exception))
                }

            }


//            Timber.tag(TAG).d(user?.PROJECTS.toString())
//            if (user!=null){
//                ServerResult.Success(user.PROJECTS)
//            }else {
//                ServerResult.Failure()
//            }

        }


    }

}