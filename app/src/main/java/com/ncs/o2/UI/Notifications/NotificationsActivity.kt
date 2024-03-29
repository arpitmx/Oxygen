package com.ncs.o2.UI.Notifications

import android.app.Activity
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.performShakeHapticFeedback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.NetworkChangeReceiver
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.HelperClasses.ShakeDetector
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Notifications.Adapter.NotificationAdapter
import com.ncs.o2.UI.Report.ShakeDetectedActivity
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.UI.Teams.ChannelHolderActivity
import com.ncs.o2.databinding.ActivityNotificationsBinding
import com.ncs.versa.Constants.Endpoints
import com.ncs.versa.HelperClasses.BounceEdgeEffectFactory
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity(),NotificationAdapter.OnNotificationClick,NetworkChangeReceiver.NetworkChangeCallback {

    private val binding: ActivityNotificationsBinding by lazy {
        ActivityNotificationsBinding.inflate(layoutInflater)
    }
    private val utils: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }

    private lateinit var shakeDetector: ShakeDetector
    val list:MutableList<Notification> = mutableListOf()

    private val networkChangeReceiver = NetworkChangeReceiver(this,this)
    private val viewModel: NotificationsViewModel by viewModels()

    private lateinit var adapter: NotificationAdapter
    private lateinit var backBtn: ImageView
    private lateinit var notificationRV: RecyclerView
    private lateinit var notifications: List<Notification>
    private val intentFilter by lazy{
        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
    }
    companion object {
        const val TAG = "NotificationsActivity"
    }
    var type:String?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        registerReceiver(true)


        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        val projectID=intent.getStringExtra("projectID")
        val taskID=intent.getStringExtra("taskID")
        type=intent.getStringExtra("type")
        val channelID=intent.getStringExtra("channel_name")
        Log.d("notificaions",projectID.toString())
        Log.d("notificaions",taskID.toString())

        if (projectID!=null && taskID!=null && type!=null){
            when(type){
                NotificationType.TASK_CHECKPOINT_NOTIFICATION.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskID)
                    intent.putExtra("index", "2")
                    intent.putExtra("type", "notifications")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.TASK_COMMENT_NOTIFICATION.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskID)
                    intent.putExtra("index", "1")
                    intent.putExtra("type", "notifications")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskID)
                    intent.putExtra("index", "1")
                    intent.putExtra("type", "notifications")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.TEAMS_COMMENT_NOTIFICATION.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, ChannelHolderActivity::class.java)
                    Log.d("teamsChannelID",channelID.toString())
                    intent.putExtra("channel_name", channelID)
                    intent.putExtra("type", "channel")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.TEAMS_COMMENT_MENTION_NOTIFICATION.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, ChannelHolderActivity::class.java)
                    Log.d("teamsChannelID",channelID.toString())
                    intent.putExtra("channel_name", channelID)
                    intent.putExtra("type", "channel")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.TASK_ASSIGNED_NOTIFICATION.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskID)
                    intent.putExtra("index", "0")
                    intent.putExtra("type", "notifications")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.WORKSPACE_TASK_UPDATE.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskID)
                    intent.putExtra("index", "0")
                    intent.putExtra("type", "notifications")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                NotificationType.TASK_CHECKLIST_UPDATE.name->{
                    finish()
                    updateProjectCache(projectID)
                    val intent = Intent(this, TaskDetailActivity::class.java)
                    intent.putExtra("task_id", taskID)
                    intent.putExtra("index", "2")
                    intent.putExtra("type", "notifications")

                    startActivity(intent)
                    this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
                }
                else->{
                    toast("Something went wrong")
                }
            }
        }
        setUpView()
        viewModel.fetchNotifications()
        setSelectedButtonColor(binding.all)
    }

    private fun initShake(){
        val shakePref=PrefManager.getShakePref()
        Log.d("shakePref",shakePref.toString())
        if (shakePref){

            val sensi=PrefManager.getShakeSensitivity()
            when(sensi){
                1->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultLightSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                2->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultMediumSensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
                3->{
                    shakeDetector = ShakeDetector(this, Endpoints.defaultHeavySensi,onShake = {
                        performShakeHapticFeedback()
                        takeScreenshot(this)
                    })
                }
            }
        }
    }
    private fun updateProjectCache(projectName:String){
        PrefManager.setcurrentProject(projectName)
        val segments=PrefManager.getProjectSegments(projectName)
        if (segments.isNotEmpty()){
            PrefManager.setcurrentsegment(segments[0].segment_NAME)
            PrefManager.putsectionsList(segments[0].sections.distinct())
        }
        else{
            PrefManager.setcurrentsegment("Select Segment")
        }
        val list = PrefManager.getProjectsList()
        var position:Int=0
        for (i in 0 until list.size){
            if (list[i]==projectName){
                position=i
            }
        }
        PrefManager.setcurrentProject(projectName)
        PrefManager.setRadioButton(position)
        PrefManager.selectedPosition.value = position
    }

    private fun updateNotificationLastSeen() {
        //viewModel.updateNotificationViewTimeStamp()
        handleUpdateNotification_TimeStamp()
    }

    private fun handleUpdateNotification_TimeStamp() {

        with(PrefManager) {
            val newLastSeen = Timestamp.now().seconds
            setNotificationCount(0)
            setProjectTimeStamp(getcurrentProject(),newLastSeen)
            setLastSeenTimeStamp(newLastSeen)
            Timber.tag(TAG).d("Timestamp updated : Last seen timestamp ${newLastSeen}")
        }

    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                startActivity(Intent(this@NotificationsActivity,MainActivity::class.java))
                overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
                finish()
            }
        }

    private fun setUpView() {
        backBtn = findViewById(R.id.btnBack_notification)

        setUpNotificationRV()

        backBtn.setOnClickThrottleBounceListener {
            startActivity(Intent(this@NotificationsActivity,MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
            finish()
        }

        binding.all.setOnClickThrottleBounceListener {
            setSelectedButtonColor(binding.all)
            list.clear()
            setUpNotificationRV()
        }

        binding.mentions.setOnClickThrottleBounceListener {
            setSelectedButtonColor(binding.mentions)
            val newList=list.filter { it.notificationType==NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.name || it.notificationType==NotificationType.TEAMS_COMMENT_MENTION_NOTIFICATION.name}
            setUpRV(newList.toMutableList())
        }

        binding.workspace.setOnClickThrottleBounceListener {
            setSelectedButtonColor(binding.workspace)
            val newList=list.filter { it.notificationType==NotificationType.WORKSPACE_TASK_UPDATE.name}
            setUpRV(newList.toMutableList())
        }

        binding.assigned.setOnClickThrottleBounceListener {
            setSelectedButtonColor(binding.assigned)
            val newList=list.filter { it.notificationType==NotificationType.TASK_ASSIGNED_NOTIFICATION.name}
            setUpRV(newList.toMutableList())
        }

        binding.checklist.setOnClickThrottleBounceListener {
            setSelectedButtonColor(binding.checklist)
            val newList=list.filter { it.notificationType==NotificationType.TASK_CHECKLIST_UPDATE.name  }
            setUpRV(newList.toMutableList())
        }
    }

    private fun setSelectedButtonColor(button: AppCompatButton) {
        val list= listOf(
            binding.all,binding.mentions,binding.workspace,binding.assigned,binding.checklist
        ).toMutableList()
        list.remove(button)

        button.setBackgroundResource(R.drawable.item_bg_curve_selected)
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, R.color.primary_bg),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(ContextCompat.getColor(this, R.color.primary_bg))
        for (element in list){
            setUnSelectedButtonColor(element)
        }
    }


    private fun setUnSelectedButtonColor(button: AppCompatButton){
        button.setBackgroundDrawable(resources.getDrawable(R.drawable.item_bg_curve))
        val drawable: Drawable? = button.compoundDrawables[2]?.mutate()
        drawable?.colorFilter = PorterDuffColorFilter(
            ContextCompat.getColor(this, R.color.better_white),
            PorterDuff.Mode.SRC_IN
        )
        button.setCompoundDrawablesWithIntrinsicBounds(null, null, drawable, null)
        button.setTextColor(resources.getColor(R.color.better_white))
    }


    private fun setUpRV(list: MutableList<Notification>){
        notificationRV = binding.notificationRV
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        notificationRV.layoutManager = layoutManager
        notificationRV.edgeEffectFactory = BounceEdgeEffectFactory()

        adapter = NotificationAdapter(this,PrefManager.getProjectTimeStamp(PrefManager.getcurrentProject()),list,this)
        adapter.notifyDataSetChanged()
        notificationRV.adapter = adapter
    }

    private fun setUpNotificationRV() {

        notificationRV = binding.notificationRV
        val layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        notificationRV.layoutManager = layoutManager

        viewModel.notificationsResult.observe(this) { result ->
            when (result) {
                is ServerResult.Failure -> {

                    binding.progress.gone()
                    utils.singleBtnDialog(
                        "Failure",
                        "Failed loading notifications : ${result.exception.message}",
                        "Okay"
                    ) { finish() }
                }

                ServerResult.Progress -> {
                    binding.progress.visible()
                }

                is ServerResult.Success -> {
                    binding.progress.gone()
                    Log.d("notificationDB",result.data.toString())

                    for (notification in result.data){
                        if (notification.projectID==PrefManager.getcurrentProject()){
                            list.add(notification)
                        }
                    }
                    Log.d("notificationDB",list.toString())
                    if (list.isEmpty()){
                        binding.notificationRV.gone()
                        binding.noNotificationTv.visible()
                    }
                    else{
                        binding.notificationRV.visible()
                        binding.noNotificationTv.gone()
                        adapter = NotificationAdapter(this,PrefManager.getProjectTimeStamp(PrefManager.getcurrentProject()),list,this)
                        adapter.notifyDataSetChanged()
                        notificationRV.adapter = adapter
                        FirebaseFirestore.getInstance().collection(Endpoints.USERS)
                            .document(PrefManager.getCurrentUserEmail())
                            .update("NOTIFICATION_LAST_SEEN",Timestamp.now().seconds)
                        Handler(Looper.getMainLooper()).postDelayed({
                            handleUpdateNotification_TimeStamp()
                        },100)
                    }

                }
            }
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        if (type!=null){
            startActivity(Intent(this@NotificationsActivity,MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
        }
        else{
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.slide_out_right)
        }

    }

    override fun onClick(notification: Notification) {
        val type=notification.notificationType

        when(type){
            NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.name->{
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task_id", notification.taskID)
                intent.putExtra("index", "1")
                startActivity(intent)
                this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            NotificationType.TASK_ASSIGNED_NOTIFICATION.name->{
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task_id", notification.taskID)
                intent.putExtra("index", "0")
                startActivity(intent)
                this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            NotificationType.WORKSPACE_TASK_UPDATE.name->{
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task_id", notification.taskID)
                intent.putExtra("index", "0")
                startActivity(intent)
                this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            NotificationType.TASK_CHECKLIST_UPDATE.name->{
                val intent = Intent(this, TaskDetailActivity::class.java)
                intent.putExtra("task_id", notification.taskID)
                intent.putExtra("index", "2")
                startActivity(intent)
                this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            NotificationType.TEAMS_COMMENT_MENTION_NOTIFICATION.name->{
                finish()
                val intent = Intent(this, ChannelHolderActivity::class.java)
                Log.d("channele name",notification.channelID)
                intent.putExtra("channel_name", notification.channelID)
                intent.putExtra("type", "channel")

                startActivity(intent)
                this.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
            }
            else->{
                toast("Something went wrong")
            }
        }
    }
    private var receiverRegistered = false

    fun registerReceiver(flag : Boolean){
        if (flag){
            if (!receiverRegistered) {
                registerReceiver(networkChangeReceiver,intentFilter)
                receiverRegistered = true
            }
        }else{
            if (receiverRegistered){
                unregisterReceiver(networkChangeReceiver)
                receiverRegistered = false
            }
        }
    }
    override fun onStart() {
        super.onStart()
        registerReceiver(true)
    }

    override fun onStop() {
        super.onStop()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onPause() {
        super.onPause()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        registerReceiver(false)
        if (PrefManager.getShakePref() && this::shakeDetector.isInitialized){
            shakeDetector.unregisterListener()
        }
    }
    override fun onOnlineModePositiveSelected() {
        PrefManager.setAppMode(Endpoints.ONLINE_MODE)
        utils.restartApp()
    }

    override fun onOfflineModePositiveSelected() {
        startActivity(intent)
        PrefManager.setAppMode(Endpoints.OFFLINE_MODE)
    }

    override fun onOfflineModeNegativeSelected() {
        networkChangeReceiver.retryNetworkCheck()
    }
    override fun onResume() {
        super.onResume()
        if (PrefManager.getShakePref()){
            initShake()
            shakeDetector.registerListener()
        }
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkChangeReceiver, intentFilter)
    }

    fun takeScreenshot(activity: Activity) {
        Log.e("takeScreenshot", activity.localClassName)
        val rootView = activity.window.decorView.rootView
        rootView.isDrawingCacheEnabled = true
        val bitmap = rootView.drawingCache
        val currentTime = Timestamp.now().seconds
        val filename = "screenshot_$currentTime.png"
        val internalStorageDir = activity.filesDir
        val file = File(internalStorageDir, filename)
        try {
            val fos = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
            fos.flush()
            fos.close()
            rootView.isDrawingCacheEnabled = false
            moveToReport(filename)

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    fun moveToReport(filename: String) {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("filename", filename)
        intent.putExtra("type","report")
        startActivity(intent)
    }

    fun moveToShakeSettings() {
        val intent = Intent(this, ShakeDetectedActivity::class.java)
        intent.putExtra("type","settings")
        startActivity(intent)
    }


}