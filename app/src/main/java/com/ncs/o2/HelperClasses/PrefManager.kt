package com.ncs.o2.HelperClasses

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Models.CurrentUser
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

    val selectedPosition = MutableLiveData<Int>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor : SharedPreferences.Editor
    fun initialize(context: Context) {
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



    // Project related

    fun setcurrentProject(project: String?) {
        val existingProject = sharedPreferences.getString("project", null)
        if (existingProject != null) {
            editor.putString("project", project)
        } else {
            editor.putString("project", project)
        }
        editor.apply()
    }
    fun setRadioButton(position:Int) {
        val exisitingRadioButton = sharedPreferences.getInt("position", -1)
        if (exisitingRadioButton != -1) {
            editor.putInt("position", position)
        } else {
            editor.putInt("position", position)
        }
        editor.apply()
    }
    fun getcurrentRadioButton():Int {
        return sharedPreferences.getInt("position",0)
    }
    fun getcurrentProject():String {
        return sharedPreferences.getString("project","NCSOxygen")!!
    }
    fun setcurrentsegment(segment: String?) {
        val existingSegment = sharedPreferences.getString("segment", null)
        if (existingSegment != null) {
            editor.putString("segment", segment)
        } else {
            editor.putString("segment", segment)
        }
        editor.apply()
    }
    fun getcurrentsegment():String {
        return sharedPreferences.getString("segment","Development")!!
    }
    fun setcurrentUserdetails(user:CurrentUser){
        editor.putString(Endpoints.User.USERNAME,user.USERNAME)
        editor.putString(Endpoints.User.EMAIL,user.EMAIL)
        editor.putString(Endpoints.User.BIO,user.BIO)
        editor.putString(Endpoints.User.DESIGNATION,user.DESIGNATION)
        editor.putInt(Endpoints.User.ROLE,user.ROLE)
        editor.apply()
    }
    fun getcurrentUserdetails():CurrentUser{

        val username = sharedPreferences.getString(Endpoints.User.USERNAME, "")
        val email = sharedPreferences.getString(Endpoints.User.EMAIL, "")
        val bio = sharedPreferences.getString(Endpoints.User.BIO, "")
        val designation = sharedPreferences.getString(Endpoints.User.DESIGNATION, "")
        val role = sharedPreferences.getInt(Endpoints.User.ROLE, 0)
        return CurrentUser(EMAIL =  email!!,USERNAME = username!!, BIO = bio!!, DESIGNATION = designation!!, ROLE = role!!)
    }
    fun lastaddedproject(project:String){
        val lastproject = sharedPreferences.getString("last_project", null)
        if (lastproject != null) {
            editor.putString("last_project", project)
        } else {
            editor.putString("last_project", project)
        }
        editor.apply()
    }
    fun getlastaddedproject():String {
        return sharedPreferences.getString("last_project","NCSOxygen")!!
    }



}