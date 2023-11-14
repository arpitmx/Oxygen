package com.ncs.o2.Domain.Repositories

import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserInfo
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.FirebaseUtils.awaitt
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.UI.StartScreen.maintainceCheck
import com.ncs.versa.Constants.Endpoints
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
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
    lateinit var serverErrorCallback: ServerErrorCallback
//    private val editor : SharedPreferences.Editor by lazy {
//        pref.edit()
//    }

    fun getTaskPath(task: Task): String {
        return Endpoints.PROJECTS +
                "/${task.project_ID}" +
                "/${Endpoints.Project.SEGMENT}" +
                "/${task.segment}" +
                "/${Endpoints.Project.TASKS}" +
                "/${task.id}" +
                "/"

    }

    fun getNotificationsRef(toUser: String): CollectionReference {
        return firestore.collection(Endpoints.USERS).document(toUser)
            .collection(Endpoints.Notifications.NOTIFICATIONS)
        //Endpoints.USERS+"/${notification.fromUser}"+"/${Endpoints.Notifications.NOTIFICATIONS}"
    }

    fun getNotificationTimeStampPath(): String {
        return Endpoints.USERS +
                "/${FirebaseAuth.getInstance().currentUser!!.email}"

    }

    override suspend fun updateNotificationTimeStampPath(serverResult: (ServerResult<Long>) -> Unit) {

        val currentTimeStamp = HashMap<String, Any>()
        val currentTime = com.google.firebase.Timestamp.now().seconds
        currentTimeStamp[Endpoints.Notifications.NOTIFICATION_LAST_SEEN] = currentTime

        return try {
            serverResult(ServerResult.Progress)
            firestore.document(getNotificationTimeStampPath()).update(currentTimeStamp).await()
            serverResult(ServerResult.Success(currentTime))
        } catch (e: Exception) {
            serverResult(ServerResult.Failure(e))
        }
    }


    override suspend fun getNotificationLastSeenTimeStamp(serverResult: (ServerResult<Long>) -> Unit) {
        return try {
            serverResult(ServerResult.Progress)
            val lastSeenTimeStamp = firestore.document(getNotificationTimeStampPath()).get().await()
                .getLong(Endpoints.User.NOTIFICATION_TIME_STAMP)
            serverResult(ServerResult.Success(lastSeenTimeStamp!!))

        } catch (e: Exception) {
            serverResult(ServerResult.Failure(e))
        }
    }

    override fun maintenanceCheck(): LiveData<maintainceCheck> {
        val liveData = MutableLiveData<maintainceCheck>()

        firestore.collection("AppConfig")
            .document("maintenance")
            .get(Source.SERVER)
            .addOnSuccessListener { data ->
                if (data.exists()) {
                    val maintanenceChecks = data.data?.get("isMaintaining").toString()
                    val maintainceDesc = data.data?.get("Description").toString()

                    Codes.STRINGS.isMaintaining = maintanenceChecks
                    Codes.STRINGS.maintaninDesc = maintainceDesc


                    val checks = data.toObject(maintainceCheck::class.java)
                    liveData.postValue(checks!!)

                }
            }
            .addOnFailureListener {
                Timber.tag("checks").d("failed")
            }

        return liveData
    }

    override suspend fun getNewNotifications(
        lastSeenTimeStamp: Long,
        serverResult: (ServerResult<List<Notification>>) -> Unit
    ) {

        return try {
            serverResult(ServerResult.Progress)

            val notificationsCollection = firestore.collection(Endpoints.USERS)
                .document(FirebaseAuth.getInstance().currentUser!!.email!!)
                .collection(Endpoints.Notifications.NOTIFICATIONS)

            val query = notificationsCollection
                .orderBy(Endpoints.Notifications.TIMESTAMP, Query.Direction.DESCENDING)
                .whereGreaterThan(Endpoints.Notifications.TIMESTAMP, lastSeenTimeStamp)
                .get(Source.SERVER)
                .await()

            val newNotifications = CoroutineScope(Dispatchers.IO).async {
                query.documents.map { documentSnapshot ->


                    val notificationID: String =
                        documentSnapshot.getString(Endpoints.Notifications.notificationID)!!
                    val notificationType: String =
                        documentSnapshot.getString(Endpoints.Notifications.notificationType)!!
                    val taskID: String =
                        documentSnapshot.getString(Endpoints.Notifications.taskID)!!
                    val title: String = documentSnapshot.getString(Endpoints.Notifications.title)!!
                    val message: String =
                        documentSnapshot.getString(Endpoints.Notifications.message)!!
                    val fromUser: String =
                        documentSnapshot.getString(Endpoints.Notifications.fromUser)!!
                    val toUser: String =
                        documentSnapshot.getString(Endpoints.Notifications.toUser)!!
                    val timeStamp: Long =
                        documentSnapshot.getLong(Endpoints.Notifications.timeStamp)!!

                    Notification(
                        notificationID = notificationID,
                        notificationType = notificationType,
                        taskID = taskID,
                        title = title,
                        message = message,
                        fromUser = fromUser,
                        toUser = toUser,
                        timeStamp = timeStamp
                    )
                }
            }.await()

            serverResult(ServerResult.Success(newNotifications))

        } catch (e: Exception) {
            serverResult(ServerResult.Failure(e))
        }

    }

    override suspend fun postNotification(
        notification: Notification,
        serverResult: (ServerResult<Int>) -> Unit
    ) {

        return try {
            serverResult(ServerResult.Progress)
            getNotificationsRef(notification.toUser).add(notification).await()
            serverResult(ServerResult.Success(200))

        } catch (e: Exception) {
            serverResult(ServerResult.Failure(e))
        }

    }


    ////////////////////////////// FIREBASE USER DP FUNCTIONALITY //////////////////////////
    override fun uploadUserDP(bitmap: Bitmap): LiveData<ServerResult<StorageReference>> {

        val liveData = MutableLiveData<ServerResult<StorageReference>>()
        val imageFileName =
            "${FirebaseAuth.getInstance().currentUser?.email}${Endpoints.Storage.DP_PATH}"
        val imageRef = storageReference.child(imageFileName)
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 30, baos)
        val data = baos.toByteArray()
        val uploadTask = imageRef.putBytes(data)


        uploadTask.addOnSuccessListener {
            val userData = mapOf(
                "PHOTO_ADDED" to true,
            )

            firestore.collection("Users")
                .document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(userData)
                .addOnSuccessListener {
                    liveData.postValue(ServerResult.Success(imageRef))
                }
                .addOnFailureListener { e ->
                    liveData.postValue(ServerResult.Failure(e))
                }

        }.addOnFailureListener { exception ->
            liveData.postValue(ServerResult.Failure(exception))

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
        firestore.collection("Users")
            .document(FirebaseAuth.getInstance().currentUser?.email!!)
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

    fun generateRandomID(id: IDType): String {

        val random = Random(System.currentTimeMillis())
        val randomNumber = random.nextInt(10000, 99999)

        when (id) {
            IDType.UserID -> return "#U$randomNumber"
            IDType.TaskID -> return "#T$randomNumber"
            IDType.SegmentID -> return "#S$randomNumber"
        }
    }


    fun getProjectPath(projectID: String): String {
        return Endpoints.PROJECTS + "/${projectID}" + "/"
    }

    fun getTasksRepository(projectPath: String, isDuplicate: (List<String>) -> Unit) {
        firestore.document(projectPath)
            .get(Source.SERVER)
            .addOnSuccessListener { snap ->

                if (snap.exists()) {
                    val taskMap = snap.get(Endpoints.Project.TASKS) as Map<String, String>
                    val taskArrayList = taskMap.keys.toList()
                    isDuplicate(taskArrayList)

                } else {
                    Timber.tag(tag = TAG).d("No tasks exists")
                    isDuplicate(listOf())
                }
            }
            .addOnFailureListener {
                serverErrorCallback.handleServerException(it.message!!)
            }

    }


    fun uniqueIDfromList(idType: IDType, list: List<String>): String {
        var uniqueID: String

        when (idType) {
            IDType.UserID -> {
                do {
                    uniqueID = generateRandomID(idType)
                } while (list.contains(uniqueID))
            }

            IDType.TaskID -> {
                uniqueID = generateRandomID(idType)

            }

            IDType.SegmentID -> {
                uniqueID = generateRandomID(idType)

            }
        }

        return uniqueID
    }


    override suspend fun setFCMToken(token: String, serverResult: (ServerResult<Int>) -> Unit) {

        val userData = mapOf(
            Endpoints.User.FCM_TOKEN to token,
        )

        return try {

            serverResult(ServerResult.Progress)
            firestore.collection(Endpoints.USERS)
                .document(FirebaseAuth.getInstance().currentUser?.email!!)
                .update(userData).awaitt()
            serverResult(ServerResult.Success(200))

        } catch (e: Exception) {
            serverResult(ServerResult.Failure(e))
        }

    }

    override fun getTagbyId(
        id: String,
        projectName: String,
        result: (ServerResult<Tag>) -> Unit
    ) {

        firestore.collection(Endpoints.PROJECTS)
            .document(projectName)
            .collection(Endpoints.Project.TAGS)
            .whereEqualTo("tagID", id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]

                    val tagText = document.getString("tagText")
                    val tagID = document.getString("tagID")
                    val textColor = document.getString("textColor")!!
                    val bgColor = document.getString("bgColor")
                    val tag = Tag(
                        tagText = tagText!!,
                        tagID = tagID,
                        textColor = textColor,
                        bgColor = bgColor!!,
                    )
                    result(ServerResult.Success(tag))
                } else {
                    result(ServerResult.Failure(Exception("Document not found for title: $id")))
                }
            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }
    }


    override fun createUniqueID(idType: IDType, projectID: String, generatedID: (String) -> Unit) {

        val projectPath = getProjectPath(projectID)
        when (idType) {
            IDType.TaskID -> {
                getTasksRepository(projectPath) { tasksArray ->
                    generatedID(uniqueIDfromList(idType, tasksArray))
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

    private fun getSegmentRef(task: Task): DocumentReference {
        return firestore.collection(Endpoints.PROJECTS)
            .document(task.project_ID)

    }


    override suspend fun postTask(task: Task, serverResult: (ServerResult<Int>) -> Unit) {

        val appendTaskID = hashMapOf<String, Any>("TASKS.${task.id}" to "${task.segment}.TASKS")

        return try {

            serverResult(ServerResult.Progress)
            //firestore.document(getTaskPath(task)).set(task).await()
            getSegmentRef(task).collection(Endpoints.Project.TASKS).document(task.id).set(task)
                .await()

            serverResult(ServerResult.Success(200))

        } catch (exception: Exception) {
            serverResult(ServerResult.Failure(exception))
        }
    }


    override suspend fun addTask(task: Task) {
        try {
            firestore.collection(Endpoints.PROJECTS).document(PrefManager.getcurrentProject())
                .collection(Endpoints.Project.TASKS).document(task.id).set(task)
                .await()

        } catch (e: Exception) {
            // Handle exceptions appropriately
            Log.e("addTaskChecking", "Error adding user to Firestore", e)
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

    override fun getUserInfoEditProfile(serverResult: (ServerResult<UserInfo?>) -> Unit) {

        serverResult(ServerResult.Progress)

        var userInfo: UserInfo?
        firestore.collection(Endpoints.USERS)
            .document(FirebaseAuth.getInstance().currentUser?.email!!)
            .get(Source.SERVER)
            .addOnSuccessListener { snap ->

                if (snap.exists()) {
                    userInfo = snap.toObject(UserInfo::class.java)

                    Timber.tag(TAG).d(userInfo?.USERNAME)
                    Timber.tag(TAG).d(userInfo?.DESIGNATION)
                    Timber.tag(TAG).d(userInfo?.BIO)
                    Timber.tag(TAG).d(userInfo?.DP_URL)

                    serverResult(ServerResult.Success(userInfo))
                }
            }
            .addOnFailureListener { error ->
                Timber.tag(TAG).d("failed %s", error.stackTrace)
                serverResult(ServerResult.Failure(error))
            }
    }

    override fun editUserInfo(
        userInfo: UserInfo,
        serverResult: (ServerResult<UserInfo?>) -> Unit
    ) {

        serverResult(ServerResult.Progress)

        val userUpdate = mapOf(
            "USERNAME" to userInfo.USERNAME,
            "BIO" to userInfo.BIO,
            "DESIGNATION" to userInfo.DESIGNATION,
            "DP_URL" to userInfo.DP_URL
        )

        firestore.collection(Endpoints.USERS)
            .document(FirebaseAuth.getInstance().currentUser?.email!!)
            .update(userUpdate)
            .addOnSuccessListener { snap ->
                serverResult(ServerResult.Success(userInfo))
            }
            .addOnFailureListener { error ->
                Timber.tag(TAG).d("failed %s", error.stackTrace)
                serverResult(ServerResult.Failure(error))
            }

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
                .document(segment.project_ID).collection(Endpoints.Project.SEGMENT)
                .document(segment.segment_NAME).set(segment)
            serverResult(ServerResult.Success(200))
        } catch (exception: Exception) {
            serverResult(ServerResult.Failure(exception))
        }
    }


    // TODO : Use Where Query
    override fun checkIfSegmentNameExists(
        fieldName: String,
        projectID: String,
        result: (ServerResult<Boolean>) -> Unit
    ) {

        result(ServerResult.Progress)

        getProjectRef(projectID).collection(Endpoints.Project.SEGMENT).get(Source.SERVER)
            .addOnSuccessListener { snapshot ->

                CoroutineScope(Dispatchers.IO).launch {
                    for (document in snapshot.documents) {
                        val fieldValue = document.getString("segment_NAME")
                        if (fieldValue == fieldName) {
                            result(ServerResult.Success(true))
                            return@launch
                        }
                    }

                    result(ServerResult.Success(false))
                }

            }
            .addOnFailureListener {
                Timber.tag(TAG).d("Firestore Exception : ${it}")
                result(ServerResult.Failure(it))
            }
    }


    override fun getSection(
        projectName: String,
        segmentName: String,
        result: (ServerResult<List<*>>) -> Unit
    ) {

        firestore.collection(Endpoints.PROJECTS).document(projectName)
            .collection(Endpoints.Project.SEGMENT).document(segmentName)
            .get()
            .addOnSuccessListener {
//                val section_list = mutableListOf<String>()
                if (it.exists()) {
                    val section_list = it.get("sections") as List<*>
                    result(ServerResult.Success(section_list))
                }
            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }
    }

    fun getSegments(
        projectName: String, result: (ServerResult<List<Segment>>) -> Unit
    ) {
        firestore.collection(Endpoints.PROJECTS).document(projectName)
            .collection(Endpoints.Project.SEGMENT)
            .get(Source.SERVER)
            .addOnSuccessListener { querySnapshot ->
                val segment_list = mutableListOf<Segment>()

                CoroutineScope(Dispatchers.IO).launch {
                    for (document in querySnapshot.documents) {
                        val segments = document.toObject(Segment::class.java)
                        segment_list.add(segments!!)
                    }

                    withContext(Dispatchers.Main) {
                        Timber.d("segements", segment_list.toString())
                        result(ServerResult.Success(segment_list))
                    }

                }


            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
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
            .whereEqualTo("segment", segmentName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val sectionList = mutableListOf<Task>()

                CoroutineScope(Dispatchers.IO).launch {
                    for (document in querySnapshot.documents) {
                        val sectionData = document.toObject(Task::class.java)
                        sectionData?.let { sectionList.add(it) }
                    }
                }

                result(ServerResult.Success(sectionList))

            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }
    }


    fun getTasksItem(
        projectName: String,
        segmentName: String,
        sectionName: String,
        result: (ServerResult<List<TaskItem>>) -> Unit
    ) {

        firestore.collection(Endpoints.PROJECTS)
            .document(projectName)
            .collection(Endpoints.Project.TASKS)
            .whereEqualTo("section", sectionName)
            .whereEqualTo("segment", segmentName)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val sectionList = mutableListOf<TaskItem>()
                var assignerID: String
                for (document in querySnapshot.documents) {

                    val title = document.getString("title")
                    val id = document.getString("id")
                    val difficulty = document.get("difficulty")!!
                    val duration = document.getString("duration")
                    var time = document.get("time_STAMP") as Timestamp

                    if (time == null) {
                        time = Timestamp.now()
                    }

                    val completed = document.getBoolean("completed")
                    if (document.getString("assigner_email") != null) {
                        assignerID = document.getString("assigner_email")!!
                    } else {
                        assignerID = "mohit@mail.com"
                    }
                    val assignee_DP_URL = document.getString("assignee_DP_URL")

                    val taskItem = TaskItem(
                        title = title!!,
                        id = id!!,
                        difficulty = difficulty.toString().toInt(),
                        duration = duration!!,
                        timestamp = time,
                        completed = completed.toString().toBoolean(),
                        assignee_DP_URL = assignee_DP_URL!!,
                        assignee_id = assignerID,
                    )
                    sectionList.add(taskItem)
                }
                result(ServerResult.Success(sectionList))
            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }
    }

    override suspend fun fetchProjectTags(
        projectName: String,
        result: (ServerResult<List<Tag>>) -> Unit
    ) {
        firestore.collection(Endpoints.PROJECTS).document(projectName)
            .collection(Endpoints.Project.TAGS)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val tags_list = mutableListOf<Tag>()
                for (document in querySnapshot.documents) {
                    val tag = document.toObject(Tag::class.java)
                    tags_list.add(tag!!)
                }
                result(ServerResult.Success(tags_list))
            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }

    }

    fun getContributors(projectName: String, result: (ServerResult<List<String>>) -> Unit) {
        firestore.collection(Endpoints.PROJECTS).document(projectName)
            .get()
            .addOnSuccessListener { data ->
//                val section_list = mutableListOf<String>()
                if (data.exists()) {
                    val contributor_list = data.get("contributors") as List<String>
                    result(ServerResult.Success(contributor_list))
                }
            }
            .addOnFailureListener { exception ->
                result(ServerResult.Failure(exception))
            }
    }

    override fun getUserInfobyId(id: String, serverResult: (ServerResult<User?>) -> Unit) {

        firestore.collection(Endpoints.USERS)
            .whereEqualTo("EMAIL", id)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val document = querySnapshot.documents[0]
                    val firebaseID = document.getString("EMAIL")
                    val profileDPUrl = document.getString("DP_URL")
                    val name = document.getString("USERNAME")!!
                    var time = document.get("TIMESTAMP") as Timestamp?
                    if (time.isNull) {
                        time = Timestamp.now()
                    }

                    val designation = document.getString("DESIGNATION")
                    val user = User(
                        firebaseID = firebaseID!!,
                        profileDPUrl = profileDPUrl,
                        username = name,
                        timestamp = time,
                        designation = designation!!
                    )
                    serverResult(ServerResult.Success(user))
                } else {
                    serverResult(ServerResult.Failure(Exception("Document not found for title: $id")))
                }
            }
            .addOnFailureListener { exception ->
                serverResult(ServerResult.Failure(exception))
            }
    }

    override suspend fun postTags(
        tag: Tag,
        projectName: String,
        serverResult: (ServerResult<Int>) -> Unit
    ) {


        return try {

            serverResult(ServerResult.Progress)
            firestore.collection(Endpoints.PROJECTS)
                .document(projectName).collection(Endpoints.Project.TAGS).document(tag.tagID!!)
                .set(tag)
                .await()

            serverResult(ServerResult.Success(200))

        } catch (exception: Exception) {
            serverResult(ServerResult.Failure(exception))
        }

    }

    override suspend fun getTasksbyId(
        id: String,
        projectName: String,
    ): ServerResult<Task> {

        return try {

            val task =
                firestore.collection(Endpoints.PROJECTS)
                    .document(projectName)
                    .collection(Endpoints.Project.TASKS)
                    .whereEqualTo("id", id)
                    .get().await()

            val snapShot = task

            if (!snapShot.isEmpty) {

                val document = snapShot.documents[0]
                val taskData = document.toObject(Task::class.java)

                taskData?.let {
                    return ServerResult.Success(it)
                } ?: ServerResult.Failure(Exception("Document not found for title: $id"))

            } else {
                return ServerResult.Failure(Exception("Document not found for title: $id"))
            }

        } catch (e: Exception) {
            return ServerResult.Failure(e)
        }

    }


}

