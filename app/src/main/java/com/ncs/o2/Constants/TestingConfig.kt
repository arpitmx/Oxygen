package com.ncs.o2.Constants

import com.ncs.o2.UI.Auth.AuthScreenActivity
import com.ncs.o2.UI.MainActivity
import com.ncs.o2.UI.Testing.TestingActivity

/*
File : TestingConfig -> com.ncs.o2.Constants
Description : Class for testing 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:21 am on 14/10/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
object TestingConfig {

    val isTesting:Boolean = false
    val testingMode:TestModes= TestModes.ADD_TASKS
    val activity  = TestingActivity::class.java


    enum class TestModes{
        ADD_NOTIFICATIONS,
        ADD_TASKS,
        ADD_NOTIFICATION_ROOM,
        FETCH_LATEST_NOTIFICATION_FIRESTORE,
    }

}