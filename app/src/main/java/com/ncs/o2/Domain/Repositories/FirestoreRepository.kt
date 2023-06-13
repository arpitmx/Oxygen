package com.ncs.o2.Domain.Repositories

import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.HelperClasses.ServerExceptions
import com.ncs.versa.Constants.Endpoints
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

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

//klinttest

@Suppress("UNCHECKED_CAST")
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) : Repository {

    private val TAG: String = FirestoreRepository::class.java.simpleName

    fun getTaskPath(task: Task): String {
        return Endpoints.PROJECTS +
                "/${task.PROJECTID}" +
                "/${Endpoints.Project.SEGMENT}" +
                "/${task.SEGMENT}" +
                "/${Endpoints.SEGMENT.TASKS}" +
                "/${task.ID}" +
                "/"

    }

    fun getProject(projectID: String): String {
        return Endpoints.PROJECTS + "/${projectID}"
    }

    fun createRandomTaskID(): String {
        val random = Random(System.currentTimeMillis())
        val randomNumber = random.nextInt(1000, 9999)
        return "#T$randomNumber"
    }

    fun createRandomSegmentID(): String {
        val random = Random(System.currentTimeMillis())
        val randomNumber = random.nextInt(1000, 9999)
        return "#S$randomNumber"
    }

    override fun postTask(task: Task, serverResult: (ServerResult<Int>) -> Unit) {
        serverResult(ServerResult.Progress)
        firestore.document(getTaskPath(task))
            .set(task)
            .addOnSuccessListener {
                serverResult(ServerResult.Success(200))
            }
            .addOnFailureListener {
                serverResult(ServerResult.Failure(it))
            }

    }

    override fun getUserInfo(serverResult: (ServerResult<CurrentUser?>) -> Unit) {
        serverResult(ServerResult.Progress)
        Handler(Looper.getMainLooper()).postDelayed({
            var currentUser: CurrentUser?
            firestore.collection(Endpoints.USERS)
                .document(Endpoints.TESTUSERID)
                .get(Source.SERVER)
                .addOnSuccessListener { snap ->
                    if (snap.exists()) {
                        currentUser = snap.toObject(CurrentUser::class.java)

                        Timber.tag(TAG).d(currentUser?.USERNAME)
                        Timber.tag(TAG).d(currentUser?.EMAIL)
                        Timber.tag(TAG).d(currentUser?.PROJECTS.toString())

                        serverResult(ServerResult.Success(currentUser))
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

    override fun createSegment(segment: Segment, serverResult: (ServerResult<Int>) -> Unit) {


    }

    override fun checkIfSegmentNameExists(
        fieldName: String,
        projectID: String,
        result: (ServerResult<Boolean>) -> Unit
    ) {

        result(ServerResult.Progress)

        firestore.document(getProject(projectID)).get(Source.SERVER)
            .addOnSuccessListener { snapshot ->


                if (!snapshot.exists() or (snapshot == null)) {
                    Timber.tag(TAG)
                        .d("Exception : ${ServerExceptions.projectDoesNotExists.exceptionDescription}")
                    result(ServerResult.Failure(ServerExceptions.projectDoesNotExists))
                    return@addOnSuccessListener
                }

                val segmentsMap: Map<String, String> =
                    snapshot.get(Endpoints.Project.ALL_SEGMENT) as Map<String, String>

                if (segmentsMap.isEmpty()) {
                    result(ServerResult.Success(false))
                    Timber.tag(TAG).d("Map is empty")

                } else {
                    val containsValue = segmentsMap.containsValue(fieldName)
                    if (containsValue) {
                        Timber.tag(TAG)
                            .d("Segment name present : ${ServerExceptions.duplicateNameException.exceptionDescription}")
                        result(ServerResult.Success(true))
                    } else {
                        Timber.tag(TAG).d("Segment name original")
                        result(ServerResult.Success(false))
                    }
                }

            }
            .addOnFailureListener {
                Timber.tag(TAG).d("Firestore Exception : ${it}")
                result(ServerResult.Failure(it))
            }
    }


}