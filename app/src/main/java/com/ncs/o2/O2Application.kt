package com.ncs.o2

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout.TabGravity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.iid.FirebaseInstanceIdReceiver
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import com.ncs.o2.BuildConfig

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
class O2Application : Application(){

    private val TAG = O2Application::class.java.simpleName
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)  {
            Timber.plant(Timber.DebugTree())
        }

        fcmToken()
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
}