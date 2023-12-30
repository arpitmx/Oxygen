package com.ncs.o2.UI.Notifications

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.UI.Notifications.Adapter.NotificationAdapter
import com.ncs.o2.databinding.ActivityNotificationsBinding
import com.ncs.versa.Constants.Endpoints
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

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
            setLastSeenTimeStamp(newLastSeen)
            Timber.tag(TAG).d("Timestamp updated : Last seen timestamp ${newLastSeen}")
        }

    }

    private val onBackPressedCallback: OnBackPressedCallback =
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.fade_in)
                finish()
            }
        }

    private fun setUpView() {
        backBtn = findViewById(R.id.btnBack_notification)

        setUpNotificationRV()

        backBtn.setOnClickThrottleBounceListener {
            onBackPressed()
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
                    notifications = result.data
                    if (notifications.isEmpty()){
                        binding.notificationRV.gone()
                        binding.noNotificationTv.visible()
                    }
                    else{
                        binding.notificationRV.visible()
                        binding.noNotificationTv.gone()
                        adapter = NotificationAdapter(this,PrefManager.getLastSeenTimeStamp(),result.data)
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
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        //finish()
    }


}