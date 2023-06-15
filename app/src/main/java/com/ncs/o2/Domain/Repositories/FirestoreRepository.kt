package com.ncs.o2.Domain.Repositories

import android.os.Handler
import android.os.Looper
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.ncs.o2.Constants.IDS
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Interfaces.ServerErrorCallback
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.HelperClasses.ServerExceptions
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.tasks.await
import net.datafaker.Faker
import timber.log.Timber
import java.lang.Exception
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
    lateinit var serverErrorCallback : ServerErrorCallback

    fun getTaskPath(task: Task): String {
        return Endpoints.PROJECTS +
                "/${task.PROJECT_ID}" +
                "/${Endpoints.Project.SEGMENT}" +
                "/${task.SEGMENT}" +
                "/${task.SECTION}"+
                "/${task.ID}" +
                "/"

    }

    fun getProjectRef(projectID: String): DocumentReference {
        return firestore.collection(Endpoints.PROJECTS).document(projectID)
    // return Endpoints.PROJECTS + "/${projectID}"
    }

    fun generateRandomID(id:IDS): String {

        val random = Random(System.currentTimeMillis())
        val randomNumber = random.nextInt(10000, 99999)

        when(id){
            IDS.UserID -> return "#U$randomNumber"
            IDS.TaskID -> return "#T$randomNumber"
            IDS.SegmentID -> return "#S$randomNumber"
        }

    }


    fun getProjectPath(projectID: String):String{
        return Endpoints.PROJECTS + "/${projectID}" + "/"
    }

    fun getTasksRepository(projectPath: String, isDuplicate : (List<String>)->Unit){
            firestore.document(projectPath)
                .get(Source.SERVER)
                .addOnSuccessListener { snap->
                    if (snap.exists()){
                        val taskArrayList = snap.get(Endpoints.Project.ALL_TASK_IDS) as List<String>
                        isDuplicate(taskArrayList)

                    }else{
                        Timber.tag(tag = TAG).d("No tasks exists")
                       isDuplicate(listOf())
                    }
                }
                .addOnFailureListener{
                    serverErrorCallback.handleServerException(it.message!!)
                }

    }


   fun uniqueIDfromList(idType: IDS, list: List<String>):String{
       var uniqueID : String

       when(idType){
           IDS.UserID -> {
               do {
                   uniqueID = generateRandomID(idType)
               } while (list.contains(uniqueID))
           }
           IDS.TaskID ->{
               uniqueID = generateRandomID(idType)

           }
           IDS.SegmentID -> {
               uniqueID = generateRandomID(idType)

           }
       }

       return uniqueID
   }


    override fun createUniqueID(idType: IDS, projectID: String, generatedID:(String)->Unit){

        val projectPath = getProjectPath(projectID)
        when(idType){
            IDS.TaskID ->{
                    getTasksRepository(projectPath) { tasksArray->
                            generatedID(uniqueIDfromList(idType,tasksArray))
                }
            }

            IDS.UserID -> {}
            IDS.SegmentID -> {}
        }

    }

    fun createRandomSegmentID(): String {
        val random = Random(System.currentTimeMillis())
        val randomNumber = random.nextInt(1000, 9999)
        return "#S$randomNumber"
    }

    override fun setCallback(callback: ServerErrorCallback) {
        this.serverErrorCallback = callback
    }

//    override fun postTask(task: Task, serverResult: (ServerResult<Int>) -> Unit) {
//        serverResult(ServerResult.Progress)
//        firestore.document(getTaskPath(task))
//            .set(task)
//            .addOnSuccessListener {
//                serverResult(ServerResult.Success(200))
//            }
//            .addOnFailureListener {
//                serverResult(ServerResult.Failure(it))
//            }
//
//    }

    @Later("All task id not working ")
    override suspend fun postTask(task: Task, serverResult: (ServerResult<Int>) -> Unit){

        return try {
        serverResult(ServerResult.Progress)
        firestore.document(getTaskPath(task)).set(task).await()
//        getProjectRef(task.PROJECT_ID)
//            .update(Endpoints.Project.ALL_TASK_IDS,FieldValue.arrayUnion(task.ID))
//            .await()

        val updatedData = hashMapOf<String, Any>(
                "TASKS.${task.ID}" to "${task.SEGMENT}.${task.SECTION}"
            )

        getProjectRef(task.PROJECT_ID)
            .update(updatedData).await()

       serverResult(ServerResult.Success(200))

        }
        catch (exception:Exception) {
            serverResult(ServerResult.Failure(exception))
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

        getProjectRef(projectID).get(Source.SERVER)
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