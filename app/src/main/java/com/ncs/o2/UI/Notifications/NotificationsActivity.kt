package com.ncs.o2.UI.Notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.PredictiveBackHandler
import androidx.activity.viewModels
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.ExtensionsUtil.gone
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityNotificationsBinding
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class NotificationsActivity : AppCompatActivity() {

    private val binding: ActivityNotificationsBinding by lazy {
        ActivityNotificationsBinding.inflate(layoutInflater)
    }
    private val utils : GlobalUtils.EasyElements by lazy {
        GlobalUtils.EasyElements(this)
    }
    private val viewModel : NotificationsViewModel by viewModels()

    private lateinit var backBtn : ImageView

    companion object{
        const val TAG = "NotificationsActivity"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setUpView()
        updateNotificationLastSeen()
    }

    private fun updateNotificationLastSeen() {
        //viewModel.updateNotificationViewTimeStamp()
        handleUpdateNotification_TimeStamp()
    }

    private fun handleUpdateNotification_TimeStamp() {

        with(PrefManager){
            setNotificationCount(0)
            val newLastSeen = Timestamp.now().seconds
            setLastSeenTimeStamp(newLastSeen)
            Timber.tag(TAG).d("Timestamp updated : Last seen timestamp ${newLastSeen}")
        }

    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            overridePendingTransition(R.anim.slide_in_right, me.shouheng.utils.R.anim.fade_in)
            finish()
        }
    }

    private fun setUpView() {
        backBtn = findViewById(R.id.btnBack_notification)
        backBtn.setOnClickThrottleBounceListener {
            onBackPressed()
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        //finish()
    }



}