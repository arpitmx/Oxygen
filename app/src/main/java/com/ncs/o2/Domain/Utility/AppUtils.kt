package com.ncs.o2.Domain.Utility

import android.app.Activity
import com.ncs.o2.R

/*
File : AppUtils -> com.ncs.o2.Domain.Utility
Description : App utils  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:26 am on 19/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
object AppUtils {

    fun slideRight(activity: Activity) {
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_right)
    }

    fun slideLeft(activity: Activity) {
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_left)
    }

    fun fadeIn(activity: Activity) {
        activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}