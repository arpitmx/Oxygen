package com.ncs.o2.HelperClasses

import android.content.Context
import android.content.Intent

/*
File : Navigator.kt -> com.ncs.o2.HelperClasses
Description : Navigator helps in navigation 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:03 pm on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
class Navigator constructor(val context: Context) {
    fun startSingleTopActivity(target:Class<*>){
        val intent = Intent(context, target)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}