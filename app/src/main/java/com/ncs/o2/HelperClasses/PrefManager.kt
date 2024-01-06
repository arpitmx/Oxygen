package com.ncs.o2.HelperClasses

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.versa.Constants.Endpoints
import java.util.Date

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
    val list=MutableLiveData<List<String>>()
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(Endpoints.SharedPref.SHAREDPREFERENCES, Context.MODE_PRIVATE)
        editor= sharedPreferences.edit()
    }


    //Notification View Timestamp

    fun saveTimestamp(timestamp: Timestamp) {
        sharedPreferences.edit().putString(Endpoints.Notifications.NOTIFICATION_LAST_SEEN, timestamp.toString()).apply()
    }

    fun getTimestamp(): String {
        return sharedPreferences.getString(Endpoints.Notifications.NOTIFICATION_LAST_SEEN, "NONE")!!
    }



    fun clearTimestamp() {
        sharedPreferences.edit().remove(Endpoints.Notifications.NOTIFICATION_LAST_SEEN).apply()
    }



    //DP related

    fun setDpUrl(url:String?){
        if (url != null){
            editor.putString(Endpoints.User.DP_URL, url)
        }

        editor.apply()
    }

    fun getDpUrl(): String? {
        return sharedPreferences.getString(Endpoints.User.DP_URL, null)
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
        editor.putString(Endpoints.User.DP_URL,user.DP_URL)
        editor.putString(Endpoints.User.FCM_TOKEN,user.FCM_TOKEN)
        editor.putString(Endpoints.User.DESIGNATION,user.DESIGNATION)
        editor.putLong(Endpoints.User.ROLE,user.ROLE)
        editor.apply()
    }

    fun setUserRole(role:Long){
        if (role != null){
            editor.putLong(Endpoints.User.ROLE, role)
        }
        editor.apply()
    }
    fun getcurrentUserdetails():CurrentUser{

        val username = sharedPreferences.getString(Endpoints.User.USERNAME, "")
        val email = sharedPreferences.getString(Endpoints.User.EMAIL, "")
        val bio = sharedPreferences.getString(Endpoints.User.BIO, "")
        val designation = sharedPreferences.getString(Endpoints.User.DESIGNATION, "")
        val role = sharedPreferences.getLong(Endpoints.User.ROLE, 0)
        val fcm= sharedPreferences.getString(Endpoints.User.FCM_TOKEN,"")
        return CurrentUser(EMAIL =  email!!,USERNAME = username!!, BIO = bio!!, DESIGNATION = designation!!, ROLE = role, FCM_TOKEN = fcm!!)
    }

    fun setCurrentUserTimeStamp(timestamp: Timestamp){
        val timestampInMillis = timestamp.toDate().time
        editor.putLong(Endpoints.User.TIMESTAMP, timestampInMillis)
        editor.apply()
    }

    fun getCurrentUserTimeStamp():Timestamp {
        return Timestamp(Date(sharedPreferences.getLong(Endpoints.User.TIMESTAMP, 0L)))
    }


    fun getCurrentUserEmail():String{
        return getcurrentUserdetails().EMAIL
    }


    fun getUserFCMToken(): String {
        return getcurrentUserdetails().FCM_TOKEN
    }

    fun setUserFCMToken(token : String){
        editor.putString(Endpoints.User.FCM_TOKEN,token)
        editor.apply()
    }

    fun setLastSeenTimeStamp(timestamp: Long){
        editor.putLong(Endpoints.Notifications.NOTIFICATION_LAST_SEEN,timestamp)
        editor.apply()
    }

    fun getLastSeenTimeStamp():Long{
        return sharedPreferences.getLong(Endpoints.Notifications.NOTIFICATION_LAST_SEEN,0)
    }


    fun setNotificationCount(count: Int){
        editor.putInt(Endpoints.Notifications.NOTIFICATION_COUNT, count)
        editor.apply()
    }

    fun getNotificationCount():Int{
        return sharedPreferences.getInt(Endpoints.Notifications.NOTIFICATION_COUNT,0)
    }


    fun setLatestNotificationTimeStamp(timestamp: Long){
        editor.putLong(Endpoints.Notifications.LATEST_NOTIFICATION_TIME_STAMP,timestamp)
        editor.apply()
    }

    fun getLatestNotificationTimeStamp():Long{
        return sharedPreferences.getLong(Endpoints.Notifications.LATEST_NOTIFICATION_TIME_STAMP,0)
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

    fun setSegmentdetails(segment: Segment){
        val gson = Gson()
        val contributersJson = gson.toJson(segment.contributers)
        editor.putString(Endpoints.SEGMENT.SEGMENT_NAME,segment.segment_NAME)
        editor.putString(Endpoints.SEGMENT.SEGMENT_ID,segment.segment_ID)
        editor.putString(Endpoints.SEGMENT.DESCRIPTION,segment.description)
        editor.putString(Endpoints.SEGMENT.CONTRIBUTERS, contributersJson)
        editor.putString(Endpoints.SEGMENT.CREATOR,segment.segment_CREATOR)
        editor.putString(Endpoints.SEGMENT.CREATOR_ID,segment.segment_CREATOR_ID)
        editor.putString(Endpoints.SEGMENT.PROJECT_ID,segment.project_ID)
        editor.apply()
    }
    fun getSegmentDetails():Segment{

        val segment_name = sharedPreferences.getString(Endpoints.SEGMENT.SEGMENT_NAME, "")
        val segment_id = sharedPreferences.getString(Endpoints.SEGMENT.SEGMENT_ID, "")
        val desc = sharedPreferences.getString(Endpoints.SEGMENT.DESCRIPTION, "")
        val contributorsJson = sharedPreferences.getString(Endpoints.SEGMENT.CONTRIBUTERS, null)
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        val creator = sharedPreferences.getString(Endpoints.SEGMENT.CREATOR, "")
        val creator_id = sharedPreferences.getString(Endpoints.SEGMENT.CREATOR_ID, "")
        val project_id = sharedPreferences.getString(Endpoints.SEGMENT.PROJECT_ID, "")

        return Segment(segment_NAME = segment_name!!, segment_ID = segment_id!!, description = desc!!, contributers = gson.fromJson(contributorsJson, type), segment_CREATOR = creator!!, segment_CREATOR_ID = creator_id!!, project_ID = project_id!!)
    }
    fun putsectionsList(sections:List<String>){
        val gson = Gson()
        val sectionsJson = gson.toJson(sections)
        editor.putString("sections", sectionsJson)
        editor.apply()
    }
    fun getsectionsList():List<String>{
        val sectionsJson = sharedPreferences.getString("sections",
           null
        )
        if (sectionsJson != null) {
            val gson = Gson()
            val type = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(sectionsJson, type)
        }else{
            return listOf("Ongoing Progress", "Ready for Test", "Testing", "Completed")
        }
    }
    fun putProjectsList(projects:List<String>){
        val gson=Gson()
        val projectsJson=gson.toJson(projects)
        editor.putString("projects",projectsJson)
        editor.apply()
    }

    fun putLastCacheUpdateTimestamp(timestamp: Timestamp){
        editor.putLong("last_cache_update_timestamp", timestamp.seconds)
        editor.apply()
    }
    fun getLastCacheUpdateTimestamp(): Timestamp {
        val timestampInSeconds = sharedPreferences.getLong("last_cache_update_timestamp", 0)
        return Timestamp(timestampInSeconds, 0)
    }
    fun putLastTAGCacheUpdateTimestamp(timestamp: Timestamp){
        editor.putLong("last_tag_cache_update_timestamp", timestamp.seconds)
        editor.apply()
    }
    fun getLastTAGCacheUpdateTimestamp(): Timestamp {
        val timestampInSeconds = sharedPreferences.getLong("last_tag_cache_update_timestamp", 0)
        return Timestamp(timestampInSeconds, 0)
    }
    fun putLastNotificationCacheUpdateTimestamp(timestamp: Long){
        editor.putLong("last_notification_cache_update_timestamp", timestamp)
        editor.apply()
    }
    fun getLastNotificationCacheUpdateTimestamp(): Long {
        val timestampInSeconds = sharedPreferences.getLong("last_notification_cache_update_timestamp", 0)
        return timestampInSeconds
    }
    fun getProjectsList():List<String> {
        val projectsJson = sharedPreferences.getString("projects", null)
        if (projectsJson != null) {
            val gson = Gson()
            val type = object : TypeToken<List<String>>() {}.type
            return gson.fromJson(projectsJson, type)
        } else {
            return listOf("NCSOxygen")
        }
    }
    fun putDraftTask(task: Task){
        val gson=Gson()
        val taskJson=gson.toJson(task)
        editor.putString("task",taskJson)
        editor.apply()
    }
    fun getDraftTask(): Task? {
        val taskJson = sharedPreferences.getString("task", null)
        val gson = Gson()
        return if (taskJson != null) {
            gson.fromJson(taskJson, Task::class.java)
        } else {
            Task()
        }
    }
    fun putDraftCheckLists(list: List<CheckList>){
        val gson=Gson()
        val taskJson=gson.toJson(list)
        editor.putString("DraftCheckLists",taskJson)
        editor.apply()
    }
    fun getDraftCheckLists(): List<CheckList>? {
        val taskJson = sharedPreferences.getString("DraftCheckLists", null)
        val gson = Gson()
        return if (taskJson != null) {
            val type = object : TypeToken<List<CheckList>>() {}.type
            gson.fromJson(taskJson, type)
        } else {
            emptyList()
        }
    }




}