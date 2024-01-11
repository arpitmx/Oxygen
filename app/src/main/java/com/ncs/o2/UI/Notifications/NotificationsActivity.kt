package com.ncs.o2.UI.Notifications

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Notifications.Adapter.NotificationAdapter
import com.ncs.o2.UI.Tasks.TaskPage.TaskDetailActivity
import com.ncs.o2.databinding.ActivityNotificationsBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity(),NotificationAdapter.OnNotificationClick {

    private val binding: ActivityNotificationsBinding by lazy {
        ActivityNotificationsBinding.inflate(layoutInflater)
    }
    private val utils: GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    private val viewModel: NotificationsViewModel by viewModels()

    private lateinit var adapter: NotificationAdapter
    private lateinit var backBtn: ImageView
    private lateinit var notificationRV: RecyclerView
    private lateinit var notifications: List<Notification>

    companion object {
        const val TAG = "NotificationsActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        setUpView()
        viewModel.fetchNotifications()
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
                overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.fade_in)
                finish()
            }
        }

    private fun setUpView() {
        backBtn = findViewById(R.id.btnBack_notification)

        setUpNotificationRV()

        backBtn.setOnClickThrottleBounceListener {
            startActivity(Intent(this@NotificationsActivity,MainActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.fade_in)
            finish()
        }

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
                    val list=ArrayList<Notification>()
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
        startActivity(Intent(this@NotificationsActivity,MainActivity::class.java))
        overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.fade_in)
        finish()
        super.onBackPressed()
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
            else->{
                toast("Something went wrong")
            }
        }
    }


}