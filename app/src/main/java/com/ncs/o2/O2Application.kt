package com.ncs.o2

import android.app.Application
import android.content.Context
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.NotificationsUtils
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.Domain.Workers.FCMWorker
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.Services.NotificationApiService
import dagger.hilt.android.HiltAndroidApp
import io.github.kbiakov.codeview.classifier.CodeProcessor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random


/*

File : O2Application.kt -> com.ncs.o2
Description : Application class for O2 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 7:26 pm on 30/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/

@HiltAndroidApp
class O2Application : Application(), Configuration.Provider{

    private val TAG = O2Application::class.java.simpleName
    @Inject
    lateinit var customWorkerFactory: CustomWorkerFactory
    @Inject
    lateinit var db: TasksDatabase
    @Inject
    lateinit var notificationDB:NotificationDatabase

    @Inject
    @FirebaseRepository
    lateinit var repository: com.ncs.o2.Domain.Interfaces.Repository


    //Todo : Check if initiations taking too long, before production
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)



        //Initiations

        //PrefManager
        PrefManager.initialize(this@O2Application)
        NotificationsUtils.initialize(this@O2Application)


        //CodeViewer
        //CodeProcessor.init(this@O2Application)

        if (BuildConfig.DEBUG)  {
            Timber.plant(Timber.DebugTree())
        }



        PrefManager.putLastCacheUpdateTimestamp(Timestamp.now())

        fcmToken()
        val projectsList=PrefManager.getProjectsList()
        val userID=PrefManager.getCurrentUserEmail()
        for (project in projectsList){
            initializeListner(project)
            initializeTagListner(project)
        }
        if (userID.isNotEmpty()){
            initializeNotificationListner(userID)
        }


    }

    fun isUIThread(): Boolean {
        return Looper.getMainLooper().thread === Thread.currentThread()
    }

    fun handleUncaughtException(thread: Thread?, e: Throwable) {
        e.printStackTrace() // not all Android versions will print the stack trace automatically
        if (isUIThread()) {
         //   invokeLogActivity()
        } else {  //handle non UI thread throw uncaught exception
        //    Handler(Looper.getMainLooper).(Runnable { invokeLogActivity() })

        }
    }
    private fun sendIssueThroughBot(e: Throwable) {
//        val task= Task(
//            title = e.cause.toString(),
//            description = e.stackTraceToString(),
//            id ="#T${RandomIDGenerator.generateRandomTaskId(5)}",
//            difficulty = 2,
//            priority = 2,
//            status = 0,
//            assigner = "oxygenbot@hackncs.in",
//            deadline = "None",
//            project_ID = "NCSOxygen",
//            segment = "Bugs\uD83D\uDC1E", //change segments here //like Design
//            section = "Bugs Found",  //Testing // Completed //Ready //Ongoing
//            assignee_DP_URL = "https://firebasestorage.googleapis.com/v0/b/ncso2app.appspot.com/o/oxygenbot%40hackncs.in%2FDP%2Fdp?alt=media&token=e8c8c439-fa80-4faa-82de-10a5f86dd992",
//            completed = false,
//            duration = Random(System.currentTimeMillis()).nextInt(1,5).toString(),
//            time_STAMP = Timestamp.now(),
//            assigner_email = "slow@gmail.com"
//        )
//
//        CoroutineScope(Dispatchers.Main).launch {
//
//
//            repository.postTask(task) { result ->
//
//                when (result) {
//
//                    is ServerResult.Failure -> {
//                        Timber.d("O2Appxyz : Failure in sending issue!")
//                        Toast.makeText(applicationContext, "Failed", Toast.LENGTH_SHORT).show()
//                    }
//
//                    ServerResult.Progress -> {
//
//                    }
//
//                    is ServerResult.Success -> {
//                        Timber.d("O2Appxyz : Sent Issue!")
//                        Toast.makeText(applicationContext, "Passed", Toast.LENGTH_SHORT).show()
//
//                    }
//
//                }
//            }
//        }
    }

    //Xiami : cmMiVraYTkyhLGCWh8aorx:APA91bGe-6OkspkpxE9-fpxsOwslGHAlwRxG45gbeg2dxY6MckcpS-PnOl1TQvOVaZ9E90VFtWCBw3qftKJS2DkdYCEgqgGrWxRrjnsbIz4SD0j40oeLbC3OfXRe9ebC38-2xoLMDjmN

    private fun fcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(TAG).w(task.exception, "Fetching FCM registration token failed")
                return@addOnCompleteListener
            }
            val token = task.result
            Timber.tag(TAG).d("FCM registration token: %s", token)
            Timber.tag("FCM TOKEN").d("FCM registration %s", token)

        }
    }

    override fun getWorkManagerConfiguration(): Configuration =
        Configuration.Builder()
            .setMinimumLoggingLevel(Log.DEBUG)
            .setWorkerFactory(customWorkerFactory)
            .build()


    class CustomWorkerFactory @Inject constructor(private val notificationApiService: NotificationApiService): WorkerFactory(){
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker
        = FCMWorker(
            appContext,
            workerParameters,
            notificationApiService)
    }

    fun initializeListner(projectName:String){
        CoroutineScope(Dispatchers.Main).launch {


            repository.initilizelistner(projectName = projectName) { result ->

                when (result) {

                    is ServerResult.Failure -> {

                    }

                    ServerResult.Progress -> {

                    }

                    is ServerResult.Success -> {


                    }

                    else -> {}
                }
            }
        }

    }

    fun initializeTagListner(projectName:String){
        CoroutineScope(Dispatchers.Main).launch {

            repository.initilizeTagslistner(projectName = projectName) { result ->

                when (result) {

                    is ServerResult.Failure -> {

                    }

                    ServerResult.Progress -> {

                    }

                    is ServerResult.Success -> {


                    }

                    else -> {}
                }
            }
        }

    }
    fun initializeNotificationListner(userID:String){
        CoroutineScope(Dispatchers.Main).launch {

            repository.initilizeNotificationslistner(userID = userID) { result ->

                when (result) {

                    is ServerResult.Failure -> {

                    }

                    ServerResult.Progress -> {

                    }

                    is ServerResult.Success -> {


                    }

                    else -> {}
                }
            }
        }

    }
}