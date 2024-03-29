package com.ncs.o2

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.NotificationsUtils
import com.ncs.o2.Domain.Workers.NotificationsFCMWorker
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.Api.NotificationApiService
import com.ncs.o2.HelperClasses.CrashReporting.CrashMan
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject


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
class O2Application : Application(), Configuration.Provider, LifecycleEventObserver{

    private val TAG = "O2App"
    private val fragment = "Fragment123 State: "
    private val activity = "Activity123 State : "

    @Inject
    lateinit var customWorkerFactory: CustomWorkerFactory

    private val activityManager by lazy {
        getSystemService(ACTIVITY_SERVICE) as ActivityManager
    }

    @Inject
    @FirebaseRepository
    lateinit var repository: com.ncs.o2.Domain.Interfaces.Repository



    //Todo : Check if initiations taking too long, before production
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        //Initiations
        initStateLogger()

        //PrefManager
        PrefManager.initialize(this@O2Application)
        NotificationsUtils.initialize(this@O2Application)

        //CrashMan
//        setUpCrashMan()



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

    private fun setUpCrashMan() {
        Thread.setDefaultUncaughtExceptionHandler(CrashMan(context = this@O2Application))
    }

    private fun initStateLogger() {

        FragmentActivity().supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks(){

            override fun onFragmentPreAttached(fm: FragmentManager, f: Fragment, context: Context) {
                Timber.tag(fragment).d("%s preAttached", f.javaClass.simpleName)
            }

            override fun onFragmentAttached(fm: FragmentManager, f: Fragment, context: Context) {
                Timber.tag(fragment).d("%s attached", f.javaClass.simpleName)
            }

            override fun onFragmentPreCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                Timber.tag(fragment).d("%s preCreated", f.javaClass.simpleName)
            }

            override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                Timber.tag(fragment).d("%s created", f.javaClass.simpleName)
            }

            override fun onFragmentActivityCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
                Timber.tag(fragment).d("%s fragmentActivityCreated", f.javaClass.simpleName)
            }

            override fun onFragmentViewCreated(fm: FragmentManager, f: Fragment, v: View, savedInstanceState: Bundle?) {
                Timber.tag(fragment).d("%s viewCreated", f.javaClass.simpleName)
            }

            override fun onFragmentStarted(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s started", f.javaClass.simpleName)
            }

            override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s resumed", f.javaClass.simpleName)
            }

            override fun onFragmentPaused(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s paused", f.javaClass.simpleName)
            }

            override fun onFragmentStopped(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s stopped", f.javaClass.simpleName)
            }

            override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
                Timber.tag(fragment).d("%s saveInstanceState", f.javaClass.simpleName)
            }

            override fun onFragmentViewDestroyed(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s viewDestroyed", f.javaClass.simpleName)
            }

            override fun onFragmentDestroyed(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s destroyed", f.javaClass.simpleName)
            }

            override fun onFragmentDetached(fm: FragmentManager, f: Fragment) {
                Timber.tag(fragment).d("%s detached", f.javaClass.simpleName)
            }
        }, true )



        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks{
            override fun onActivityCreated(act: Activity, p1: Bundle?) {
                Timber.tag(activity).d("%s created", act.javaClass.simpleName)
            }

            override fun onActivityStarted(act: Activity) {
                Timber.tag(activity).d("%s started", act.javaClass.simpleName)
            }

            override fun onActivityResumed(act: Activity) {
                Timber.tag(activity).d("%s resumed", act.javaClass.simpleName)

            }

            override fun onActivityPaused(act: Activity) {
                Timber.tag(activity).d("%s paused", act.javaClass.simpleName)

            }

            override fun onActivityStopped(act: Activity) {
                Timber.tag(activity).d("%s stopped", act.javaClass.simpleName)
            }

            override fun onActivitySaveInstanceState(act: Activity, p1: Bundle) {
                Timber.tag(activity).d("%s saveInstanceState", act.javaClass.simpleName)

            }

            override fun onActivityDestroyed(act: Activity) {
                Timber.tag(activity).d("%s destroyed", act.javaClass.simpleName)

            }
        })

    }

    fun getBackstack(){
        val appTasks = activityManager.appTasks
        for (appTask in appTasks) {
            val taskInfo = appTask.taskInfo
            val baseActivity = taskInfo.baseActivity
            Timber.d("Activity in backstack: " + baseActivity!!.shortClassName)
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


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                Timber.tag(TAG).d("onCreate")
            }
            Lifecycle.Event.ON_RESUME -> {
                Timber.tag(TAG).d("onResume")

            }
            Lifecycle.Event.ON_START -> {
                Timber.tag(TAG).d("onStart")
            }
            Lifecycle.Event.ON_PAUSE -> {
                Timber.tag(TAG).d("onPause")
            }
            Lifecycle.Event.ON_STOP -> {
                Timber.tag(TAG).d("onStop")
            }
            Lifecycle.Event.ON_DESTROY -> {
                Timber.tag(TAG).d("onDestroy")
            }
            Lifecycle.Event.ON_ANY -> {
                Timber.tag(TAG).d("onAny")
            }
        }
    }



    private fun fcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(TAG).w(task.exception, "Fetching FCM registration token failed")
                return@addOnCompleteListener
            }
            val token = task.result
            Timber.tag(TAG).d("FCM registration token: %s", token)
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
        = NotificationsFCMWorker(
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