package com.ncs.o2.UI.StartScreen

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.google.firebase.messaging.FirebaseMessaging
import com.ncs.o2.Constants.Errors
import com.ncs.o2.Constants.Pref
import com.ncs.o2.Constants.TestingConfig
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.Domain.Utility.Codes
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.rotateInfinity
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.FirebaseRepository
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.Auth.SignupScreen.SignUpScreenFragment
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.O2Bot.O2Bot
import com.ncs.o2.UI.Tasks.TaskPage.Details.TaskDetailsFragment
import com.ncs.o2.databinding.ActivitySplashScreenBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject


@AndroidEntryPoint
class StartScreen @Inject constructor() : AppCompatActivity() {

    @Inject
    @FirebaseRepository
    lateinit var repository: Repository

    @Inject
    lateinit var db: TasksDatabase
    private val util: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this@StartScreen)
    }

    private val o2Bot: O2Bot by viewModels()


    private val viewModel: StartScreenViewModel by viewModels()

    private val binding: ActivitySplashScreenBinding by lazy {
        ActivitySplashScreenBinding.inflate(layoutInflater)
    }

    companion object {
        val DELAY = 500L
        val DELAY_ACTIVITY_START = 0L
        val TAG = "StartScreen"
    }

    private lateinit var ball: FrameLayout
    private lateinit var valueAnimator: ValueAnimator
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        askNotificationPermission()
        val tasksDAO = db.tasksDao()

//     setUpViews(TestingConfig.isTesting)

    }



    lateinit var scaleAnimation: ScaleAnimation

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setBallAnimator()
            setUpViews(TestingConfig.isTesting)
        } else {
            util.singleBtnDialog(
                title = "Notification Permission required",
                msg = "Notification permission is required for better functioning of the app, you can always allow permissions from phone settings",
                btnText = "I Understand",
                positive = {
                    setBallAnimator()
                    setUpViews(TestingConfig.isTesting)
                })
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                setBallAnimator()
                setUpViews(TestingConfig.isTesting)
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                util.twoBtnDialog(
                    title = "Notification Permission required",
                    msg = "Notification permission is required for better functioning of the app",
                    positiveBtnText = "OK",
                    positive = {
                        requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    },
                    negativeBtnText = "Cancel",
                    negative = {
                        setBallAnimator()
                        setUpViews(TestingConfig.isTesting)
                    })
            } else {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            setBallAnimator()
            setUpViews(TestingConfig.isTesting)
        }
    }

    private fun setBallTouchAnimator() {

        ball = binding.fragContainer
        ball.rotateInfinity(this)
        val maxsize = 12f
        val change = 3f



        ball.setOnClickThrottleBounceListener {

            this.performHapticFeedback()
            val scale = ball.scaleX

            scaleAnimation = ScaleAnimation(
                scale,
                scale + change,
                scale,
                scale + change,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            )
            ball.scaleX = scale + change
            ball.scaleY = scale + change

            scaleAnimation.duration = 200
            scaleAnimation.fillAfter = true
            ball.startAnimation(scaleAnimation)
            ball.rotateInfinity(this)

            // Check if the target scale has been reached

            // Check if the target scale has been reached
            if (ball.getScaleX() >= maxsize) {
                // Start the new activity
                startMainActivity()
            }
        }

    }

    private fun setBallAnimator() {

        ball = binding.fragContainer

        ball.rotateInfinity(this)
        val maxsize = 15f

        valueAnimator = ValueAnimator.ofFloat(2f, maxsize)
        valueAnimator.setDuration(300L)

        valueAnimator.addUpdateListener {

            val scale = valueAnimator.animatedValue as Float
            ball.setScaleX(scale)
            ball.setScaleY(scale)

        }

        valueAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animator: Animator) {
                binding.progress.gone()
            }

            override fun onAnimationEnd(animator: Animator) {
                startMainActivity()
            }

            override fun onAnimationCancel(animator: Animator) {
            }

            override fun onAnimationRepeat(animator: Animator) {
            }
        })
    }


    private fun startMainActivity() {

        viewModel.checkMaintenanceThroughRepository().observe(this) {

            if (Codes.STRINGS.isMaintaining != null) {
                if (Codes.STRINGS.isMaintaining == "true") {
                    Log.d("maintainenceCheck", "maintaining")
                    val intent = Intent(this, MaintainingScreen::class.java)
                    startActivity(intent)
                    finishAffinity()
                } else {
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    overridePendingTransition(
                        R.anim.fadein,
                        R.anim.fadeout
                    )
                    finishAffinity()
                }
            }
        }
    }


    private fun showBallError(error: Errors, logs: Exception) {

        Handler(Looper.getMainLooper()).postDelayed({
            val tintColor = ContextCompat.getColor(this, R.color.redx)
            binding.ball.setColorFilter(tintColor)

            util.showActionSnackbar(binding.root, error.description, 150000, error.actionText) {


                val tintColor = ContextCompat.getColor(this, R.color.pureblack)

                if (error.actionText.equals("Send report")) {
                    val title: String = error.description
                    val desc: String = "📍${error.code} \n\n${error.solution}" +
                            "\n${error.description}\n" +
                            "\nPosted on: \n1. API level 31" +
                            "\n2. Android logs : \n${logs.printStackTrace()} "

                    o2Bot.botPostBug(title, desc)

                    o2Bot.serverResultLiveData.observe(this) {
                        when (it) {
                            is ServerResult.Failure -> {
                                Toast.makeText(this, "Failed to submit issue", Toast.LENGTH_SHORT)
                                    .show()
                                binding.ball.setColorFilter(tintColor)
                                setUpProcesses()
                            }

                            ServerResult.Progress -> {

                            }

                            is ServerResult.Success -> {
                                Toast.makeText(this, "Issue submitted", Toast.LENGTH_SHORT).show()
                                finishAffinity()
                            }
                        }
                    }
                } else if (error.code.contains("NOTIF")) {

                    binding.ball.setColorFilter(tintColor)
                    setUpNotifications()

                } else {

                    binding.ball.setColorFilter(tintColor)

                    setUpNotifications()
                }

            }
        }, 500)
    }


    private fun setUpViews(isTesting: Boolean) {

        if (isTesting) {

            startActivity(Intent(this, TestingConfig.activity))
            finishAffinity()

        } else {

            if (FirebaseAuth.getInstance().currentUser != null) {
                setUpProcesses()
            } else {
                Handler(Looper.myLooper()!!).postDelayed({
                    val intent = Intent(this, AuthScreenActivity::class.java)
                    intent.putExtra("isDetailsAdded", "false")
                    intent.putExtra("isPhotoAdded", "false")
                    intent.putExtra("showchooser", "true")
                    startActivity(intent)
                    finishAffinity()
                }, DELAY)

            }
        }


    }

    private fun setUpProcesses() {

        // throw RuntimeException("This is a test crash")

        FirebaseFirestore.getInstance().collection(Endpoints.USERS)
            .document(FirebaseAuth.getInstance().currentUser?.email!!).get(Source.SERVER)
            .addOnCompleteListener { task ->

                if (!task.isSuccessful || task.isNull) {

                    showBallError(Errors.NetworkErrors.NO_CONNECTION_ERR, task.exception!!)
                    val exception = task.exception
                    exception?.printStackTrace()

                    return@addOnCompleteListener
                }

                val document = task.result

                if (!document.exists()) {
                    showBallError(Errors.AccountErrors.ACCOUNT_FIELDS_NULL, task.exception!!)
                    return@addOnCompleteListener
                }

                val isDetailsAdded = document.getBoolean(Endpoints.User.DETAILS_ADDED)
                val isPhotoAdded = document.getBoolean(Endpoints.User.PHOTO_ADDED)
                val role = document.get(Endpoints.User.ROLE)
                val timestamp = document.getTimestamp(Endpoints.User.TIMESTAMP)
                PrefManager.setUserRole(role.toString().toLong())
                PrefManager.setCurrentUserTimeStamp(Timestamp.now())
                val dp_url = document.getString(Endpoints.User.DP_URL)
                val dp_url_pref = PrefManager.getDpUrl()
                val notification_timestamp = document.getLong("NOTIFICATION_LAST_SEEN")
                if (!notification_timestamp.isNull) {
                    PrefManager.setLastSeenTimeStamp(notification_timestamp!!)
                    PrefManager.setProjectTimeStamp(PrefManager.getcurrentProject(),notification_timestamp)
                }
                if (dp_url_pref == null) {
                    PrefManager.setDpUrl(dp_url)
                } else if (dp_url_pref != dp_url) {
                    Timber.tag(TAG).d("New DP is avaialable : ${dp_url}")
                    PrefManager.setDpUrl(dp_url)
                }

                PrefManager.setDpUrl(dp_url)

                if (isDetailsAdded == null) {
                    showBallError(
                        Errors.AccountErrors.ACCOUNT_FIELDS_NULL,
                        Exception("No details added")
                    )
                    return@addOnCompleteListener
                }

                if (isDetailsAdded == false) {

                    val intent = Intent(this, AuthScreenActivity::class.java)
                    intent.putExtra("isDetailsAdded", "false")
                    intent.putExtra("showchooser", "false")
                    startActivity(intent)
                    finishAffinity()

                } else if (isPhotoAdded == false) {
                    val intent = Intent(this, AuthScreenActivity::class.java)
                    intent.putExtra("isPhotoAdded", "false")
                    intent.putExtra("showchooser", "false")
                    startActivity(intent)
                    finishAffinity()

                }


                setUpFCMTokenIfRequired(document = document)
                setUpProjectsList(document = document)
                val projectsList = PrefManager.getProjectsList()
                for (projects in projectsList) {
                    setUpTasks(projects)
                    setUpTags(projects)
                }


            }
            .addOnFailureListener { exception ->

                when (exception) {
                    is FirebaseNetworkException -> {
                        showBallError(Errors.NetworkErrors.NO_CONNECTION_ERR, exception)
                    }

                    is FirebaseAuthException -> {
                        showBallError(Errors.AccountErrors.ACCOUNT_FIELDS_NULL, exception)
                    }
                }

                Timber.tag(TAG).d(exception.stackTraceToString())
            }


    }

    private fun stopAnimAndStartActivity() {
        Handler(Looper.getMainLooper()).postDelayed(
            {
                ball.animation.cancel()
                ball.post {
                    valueAnimator.start()
                }
            }, DELAY
        )
    }

    private fun setUpTasks(projectName: String) {
        val dao = db.tasksDao()
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val taskResult = withContext(Dispatchers.IO) {
                    viewModel.getTasksinProject(projectName)
                }

                Timber.tag(TaskDetailsFragment.TAG).d("Fetched task result : ${taskResult}")

                when (taskResult) {

                    is ServerResult.Failure -> {
                    }

                    is ServerResult.Progress -> {
                    }

                    is ServerResult.Success -> {

                        val tasks = taskResult.data
                        for (task in tasks) {
                            dao.insert(task)
                        }


                    }

                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)


            }

        }
    }

    private fun setUpTags(projectName: String) {
        val dao = db.tagsDao()
        CoroutineScope(Dispatchers.Main).launch {
            try {

                val tagResult = withContext(Dispatchers.IO) {
                    viewModel.getTagsinProject(projectName)
                }

                Timber.tag(TaskDetailsFragment.TAG).d("Fetched Tag result : ${tagResult}")

                when (tagResult) {

                    is ServerResult.Failure -> {
                    }

                    is ServerResult.Progress -> {
                    }

                    is ServerResult.Success -> {

                        val tags = tagResult.data
                        for (tag in tags) {
                            dao.insert(tag)
                        }
                        setUpNotifications()
                    }
                }

            } catch (e: Exception) {

                Timber.tag(TaskDetailsFragment.TAG).e(e)


            }

        }
    }

    private fun setUpNotifications() {
        viewModel.setUpNewNotifications()
        viewModel.serverResultLiveData.observe(this) { result ->

            when (result) {
                is ServerResult.Failure -> {
                    showBallError(
                        Errors.NotificationErrors.NOTIFICATION_FETCH_FAILED,
                        result.exception
                    )
                }

                ServerResult.Progress -> {
                    //util.showSnackbar(binding.root,"Fetching Notifications", 500)
                }

                is ServerResult.Success -> {

                    val newNotificationsList = result.data
                    if (newNotificationsList.isNotEmpty()) {

                        viewModel.pushNewNotificationsToRoom(newNotificationsList) { pushResult ->

                            when (pushResult) {
                                is ServerResult.Failure -> {
                                    showBallError(
                                        Errors.NotificationErrors.NOTIFICATION_SAVE_FAILED,
                                        pushResult.exception
                                    )
                                }

                                ServerResult.Progress -> {
                                }

                                is ServerResult.Success -> {
                                    // Toast.makeText(this, "You have ${pushResult.data} new notifications", Toast.LENGTH_SHORT).show()

                                    util.showSnackbar(binding.root, "O2 is ready", 1000)
                                    stopAnimAndStartActivity()
                                }

                            }
                        }


                    } else {
                        util.showSnackbar(binding.root, "O2 is ready", 1000)
                        stopAnimAndStartActivity()
                    }

                }
            }

        }
    }

    private fun setUpProjectsList(document: DocumentSnapshot) {
        val projectsList = document.get("PROJECTS") as List<String>
        PrefManager.putProjectsList(projectsList)

    }


    private fun setUpFCMTokenIfRequired(document: DocumentSnapshot) {

        val fcmToken = document.getString(Endpoints.User.FCM_TOKEN) ?: ""

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Timber.tag(SignUpScreenFragment.TAG)
                    .w(task.exception, "Fetching FCM registration token failed")
                return@addOnCompleteListener
            }

            val actualFCM = task.result
            if (fcmToken != actualFCM) {
                PrefManager.setUserFCMToken(actualFCM)
                updateFCMToken(actualFCM)
            }

        }

    }

    private fun updateFCMToken(actualFCM: String?) {
        CoroutineScope(Dispatchers.Main).launch {
            repository.setFCMToken(actualFCM!!) { result ->
                when (result) {
                    is ServerResult.Failure -> {
                        showBallError(
                            Errors.FCMTokenErrors.FCM_TOKEN_UPDATE_FAILED,
                            result.exception
                        )
                    }

                    ServerResult.Progress -> {

                    }

                    is ServerResult.Success -> {
                        util.showSnackbar(binding.root, "FCM token updated", 5000)
                    }
                }
            }
        }

    }


}