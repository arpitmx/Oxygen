package com.ncs.o2.Domain.Repositories

import android.content.Intent
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.ncs.o2.Constants.IDType
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Interfaces.ServerErrorCallback
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.HelperClasses.ServerExceptions
import com.ncs.o2.UI.Auth.SignupScreen.ProfilePictureScreen.ProfilePictureSelectionViewModel
import com.ncs.o2.UI.MainActivity
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.ByteArrayOutputStream
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

    private val storageReference = FirebaseStorage.getInstance().reference
    private val TAG: String = FirestoreRepository::class.java.simpleName
    lateinit var serverErrorCallback : ServerErrorCallback
//    private val editor : SharedPreferences.Editor by lazy {
//        pref.edit()
//    }

    fun getTaskPath(task: Task): String {
        return Endpoints.PROJECTS +
                "/${task.project_ID}" +
                "/${Endpoints.Project.SEGMENT}" +
                "/${task.segment}" +
                "/${Endpoints.Project.TASKS}"+
                "/${task.id}" +
                "/"

    }

    fun getNotificationsRef(toUser: String): CollectionReference {
        return firestore.collection(Endpoints.USERS).document(toUser).collection(Endpoints.Notifications.NOTIFICATIONS)
        //Endpoints.USERS+"/${notification.fromUser}"+"/${Endpoints.Notifications.NOTIFICATIONS}"
    }

    fun getNotificationTimeStampPath():String{
//        return Endpoints.USERS +
//                "/${FirebaseAuth.getInstance().currentUser!!.email}"

        return Endpoints.USERS +
                "/userid1"

    }
    override suspend fun updateNotificationTimeStampPath(serverResult: (ServerResult<Int>) -> Unit) {

        val currentTimeStamp = HashMap<String,Any>()
        currentTimeStamp[Endpoints.Notifications.NOTIFICATION_TIME_STAMP] = FieldValue.serverTimestamp()

        return try {
            serverResult(ServerResult.Progress)
            firestore.document(getNotificationTimeStampPath()).update(currentTimeStamp).await()
            serverResult(ServerResult.Success(200))
        } catch (e:Exception){
            serverResult(ServerResult.Failure(e))
        }
    }

    override suspend fun loadNewNotifications(serverResult: (ServerResult<List<Notification>>) -> Unit) {
        TODO("Not yet implemented")
    }

    override suspend fun postNotification(
        notification: Notification,
        serverResult: (ServerResult<Int>) -> Unit
    ) {

        return try {
            serverResult(ServerResult.Progress)
            getNotificationsRef(notification.toUser).add(notification).await()
            serverResult(ServerResult.Success(200))

        }catch (e : Exception){
            serverResult(ServerResult.Failure(e))
        }

    }


    ////////////////////////////// FIREBASE USER DP FUNCTIONALITY //////////////////////////
    override fun uploadUserDP(bitmap: Bitmap):  LiveData<ServerResult<StorageReference>> {
//        serverResult(ServerResult.Progress)

        val liveData = MutableLiveData<ServerResult<StorageReference>>()

        val imageFileName = "${FirebaseAuth.getInstance().currentUser?.email}${Endpoints.Storage.DP_PATH}"
        val imageRef = storageReference.child(imageFileName)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)
        uploadTask.addOnSuccessListener {
            val userData = mapOf(
                "PHOTO_ADDED" to true,
            )
            FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(userData)
                .addOnSuccessListener {
                    liveData.postValue(ServerResult.Success(imageRef))
//                    serverResult(ServerResult.Success(imageRef))
                }
                .addOnFailureListener { e ->
                    liveData.postValue(ServerResult.Failure(e))

//                    serverResult(ServerResult.Failure(e))
                }

        }.addOnFailureListener { exception ->
            liveData.postValue(ServerResult.Failure(exception))

//            serverResult(ServerResult.Failure(exception))
        }

        return liveData
    }

    override fun getUserDPUrl(reference: StorageReference): LiveData<ServerResult<String>> {

        val liveData = MutableLiveData<ServerResult<String>>()

        liveData.postValue(ServerResult.Progress)
        reference.downloadUrl
            .addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                liveData.postValue(ServerResult.Success(imageUrl))
            }
            .addOnFailureListener { exception ->
                liveData.postValue(ServerResult.Failure(exception))
             }
        return liveData
    }
    override fun addImageUrlToFirestore(DPUrl: String): LiveData<Boolean> {
        val liveData = MutableLiveData<Boolean>()
        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().currentUser?.email!!)
            .update("DP_URL", DPUrl)
            .addOnSuccessListener {
                liveData.postValue(true)
            }
            .addOnFailureListener { exception ->
                // Handle failed Firestore update
                liveData.postValue(false)
            }
        return liveData
    }

    ////////////////////////////// FIREBASE USER DP FUNCTIONALITY //////////////////////////

    fun getProjectRef(projectID: String): DocumentReference {
        return firestore.collection(Endpoints.PROJECTS).document(projectID)
    // return Endpoints.PROJECTS + "/${projectID}"
    }

    fun generateRandomID(id:IDType): String {

        val random = Random(System.currentTimeMillis())
        val randomNumber = random.nextInt(10000, 99999)

        when(id){
            IDType.UserID -> return "#U$randomNumber"
            IDType.TaskID -> return "#T$randomNumber"
            IDType.SegmentID -> return "#S$randomNumber"
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
                        val taskMap = snap.get(Endpoints.Project.TASKS) as Map<String, String>
                        val taskArrayList = taskMap.keys.toList()
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




   fun uniqueIDfromList(idType: IDType, list: List<String>):String{
       var uniqueID : String

       when(idType){
           IDType.UserID -> {
               do {
                   uniqueID = generateRandomID(idType)
               } while (list.contains(uniqueID))
           }
           IDType.TaskID ->{
               uniqueID = generateRandomID(idType)

           }
           IDType.SegmentID -> {
               uniqueID = generateRandomID(idType)

           }
       }

       return uniqueID
   }


    override fun createUniqueID(idType: IDType, projectID: String, generatedID:(String)->Unit){

        val projectPath = getProjectPath(projectID)
        when(idType){
            IDType.TaskID ->{
                    getTasksRepository(projectPath) { tasksArray->
                            generatedID(uniqueIDfromList(idType,tasksArray))
                }
            }

            IDType.UserID -> {}
            IDType.SegmentID -> {}
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

    private fun getSegmentRef(task: Task):DocumentReference{
        return firestore.collection(Endpoints.PROJECTS)
            .document(task.project_ID)

    }


    override suspend fun postTask(task: Task, serverResult: (ServerResult<Int>) -> Unit){

        val appendTaskID = hashMapOf<String, Any>("TASKS.${task.id}" to "${task.segment}.TASKS")

        return try {

        serverResult(ServerResult.Progress)
//        firestore.document(getTaskPath(task)).set(task).await()
        getSegmentRef(task).collection(Endpoints.Project.TASKS).document(task.id).set(task).await()
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
                .document(FirebaseAuth.getInstance().currentUser?.email!!)
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



                is ServerResult.Failure -> {
                    projectListCallback(ServerResult.Failure(result.exception))
                }
                ServerResult.Progress -> {
                    projectListCallback(ServerResult.Progress)

                }
                is ServerResult.Success -> {
                    projectListCallback(ServerResult.Success(result.data!!.PROJECTS))
                }
            }

        }


    }

    override fun createSegment(segment: Segment, serverResult: (ServerResult<Int>) -> Unit) {
        return try {

            serverResult(ServerResult.Progress)
            firestore.collection(Endpoints.PROJECTS)
                .document(segment.project_ID).collection(Endpoints.Project.SEGMENT).document(segment.segment_NAME).set(segment)
            serverResult(ServerResult.Success(200))
        }
        catch (exception:Exception) {
            serverResult(ServerResult.Failure(exception))
        }
    }


    override fun checkIfSegmentNameExists(
        fieldName: String,
        projectID: String,
        result: (ServerResult<Boolean>) -> Unit
    ) {

        result(ServerResult.Progress)

        getProjectRef(projectID).collection(Endpoints.Project.SEGMENT).get(Source.SERVER)
            .addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    val fieldValue = document.getString("segment_NAME")
                    if (fieldValue == fieldName) {
                        result(ServerResult.Success(true))
                        return@addOnSuccessListener
                    }
                }
                result(ServerResult.Success(false))
            }
            .addOnFailureListener {
                Timber.tag(TAG).d("Firestore Exception : ${it}")
                result(ServerResult.Failure(it))
            }
    }

    fun getTasks(
        projectName: String,
        segmentName: String,
        sectionName: String,
        result: (ServerResult<List<Task>>) -> Unit
    ) {

        firestore.collection(Endpoints.PROJECTS)
            .document(projectName)
            .collection(Endpoints.Project.TASKS)
            .whereEqualTo("section", sectionName)
            .whereEqualTo("segment",segmentName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val sectionList = mutableListOf<Task>()
                for (document in querySnapshot.documents) {
                    val sectionData = document.toObject(Task::class.java)
                    sectionData?.let { sectionList.add(it) }
                }
                result(ServerResult.Success(sectionList))
            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }
    }
    fun getSegments(projectName: String,result: (ServerResult<List<Segment>>) -> Unit
    ){
      firestore.collection(Endpoints.PROJECTS).document(projectName).collection(Endpoints.Project.SEGMENT)
          .get()
          .addOnSuccessListener { querySnapshot ->
              val segment_list = mutableListOf<Segment>()
              for (document in querySnapshot.documents) {
                  val segments = document.toObject(Segment::class.java)
                  segment_list.add(segments!!)
              }
                  Timber.d("segements",segment_list.toString())
              result(ServerResult.Success(segment_list))
          }
          .addOnFailureListener { exception ->
              result(ServerResult.Failure(exception))
          }
    }


}