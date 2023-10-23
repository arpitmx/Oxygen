package com.ncs.o2.HelperClasses

import android.content.Context
import android.content.SharedPreferences
import com.google.firebase.Timestamp
import com.ncs.versa.Constants.Endpoints

/*
File : SharedPrefHelper -> com.ncs.o2.HelperClasses
Description : Helper for SharedPreferences 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:16 pm on 14/10/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :
*/


object PrefManager {


    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    fun initialize(context: Context){
        sharedPreferences = context.getSharedPreferences(Endpoints.SharedPref.SHAREDPREFERENCES, Context.MODE_PRIVATE)
        editor= sharedPreferences.edit()
    }


    //Notification View Timestamp
    fun saveTimestamp(timestamp: Timestamp) {
        sharedPreferences.edit().putString(Endpoints.Notifications.NOTIFICATION_TIME_STAMP, timestamp.toString()).apply()
    }

    fun getTimestamp(): String {
        return sharedPreferences.getString(Endpoints.Notifications.NOTIFICATION_TIME_STAMP, "NONE")!!
    }

    fun clearTimestamp() {
        sharedPreferences.edit().remove(Endpoints.Notifications.NOTIFICATION_TIME_STAMP).apply()
    }
}