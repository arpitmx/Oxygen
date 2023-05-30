package com.ncs.o2

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.ktx.Firebase
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
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG)  {
            Timber.plant(Timber.DebugTree())
        }

    }
}