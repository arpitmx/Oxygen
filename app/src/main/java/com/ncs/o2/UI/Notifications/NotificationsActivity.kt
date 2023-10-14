package com.ncs.o2.UI.Notifications

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.window.OnBackInvokedDispatcher
import androidx.activity.OnBackPressedCallback
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickSingleTimeBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.toast
import com.ncs.o2.R
import com.ncs.o2.databinding.ActivityNotificationsBinding

class NotificationsActivity : AppCompatActivity() {

    private val binding: ActivityNotificationsBinding by lazy {
        ActivityNotificationsBinding.inflate(layoutInflater)
    }

    private lateinit var backBtn : ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        setUpView()
    }

    private val onBackPressedCallback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
            finish()
        }
    }

    private fun setUpView() {
        backBtn = findViewById(R.id.btnBack_notification)
        backBtn.setOnClickThrottleBounceListener {
            onBackPressed()
        }

    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
        //finish()
    }



}