package com.ncs.o2.HelperClasses

import android.app.DownloadManager
import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.ncs.o2.Domain.Models.Channel
import com.ncs.o2.Domain.Models.CheckList
import com.ncs.o2.Domain.Models.CurrentUser
import com.ncs.o2.Domain.Models.Segment
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.User
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.Domain.Models.state.SegmentItem
import com.ncs.versa.Constants.Endpoints
import java.sql.Time
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
    private val projectTimestampMapKey = "project_timestamp_map"
    private val lastTaskTimestampMapKey = "last_task_timestamp_map"
    private val lastTagTimestampMapKey = "last_tag_timestamp_map"
    private val lastTeamsTimestampMapKey = "last_teams_timestamp_map"
    private val lastSegmentsTimestampMapKey = "last_segments_timestamp_map"
    private val lastChannelsTimestampMapKey = "last_channels_timestamp_map"

    private val projectIconUrlMapKey = "project_icon_url_map"
    private val projectDeeplinkMapKey = "project_deeplink_map"


    private lateinit var projectTimestampMap: MutableMap<String, Long>
    private lateinit var LastTaskTimestampMap: MutableMap<String, Timestamp>
    private lateinit var LastTagTimestampMap: MutableMap<String, Timestamp>
    private lateinit var LastTeamsTimestampMap: MutableMap<String, Timestamp>
    private lateinit var LastSegmentsTimestampMap: MutableMap<String, Timestamp>
    private lateinit var LastChannelTimestampMap: MutableMap<String, Timestamp>

    private lateinit var ProjectIconUrlMap: MutableMap<String, String>
    private lateinit var ProjectDeepLinkMap: MutableMap<String, String>


    private val projectIdTaskIdMapKey = "project_id_task_id_map"
    private val projectIdChannelIdMapKey = "project_id_channel_id_map"

    private lateinit var projectIdTaskIdMap: MutableMap<String, Timestamp>
    private lateinit var projectIdChannelIdMap: MutableMap<String, Timestamp>

    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(Endpoints.SharedPref.SHAREDPREFERENCES, Context.MODE_PRIVATE)
        editor= sharedPreferences.edit()
        val savedHashMapString = sharedPreferences.getString(projectTimestampMapKey, null)
        projectTimestampMap = savedHashMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Long>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()
        val savedProjectIdTaskIdMapString = sharedPreferences.getString(projectIdTaskIdMapKey, null)
        projectIdTaskIdMap = savedProjectIdTaskIdMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()
        val savedLastTaskTimeStampMapString = sharedPreferences.getString(lastTaskTimestampMapKey, null)
        LastTaskTimestampMap = savedLastTaskTimeStampMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()
        val savedLastTagTimeStampMapString = sharedPreferences.getString(lastTagTimestampMapKey, null)
        LastTagTimestampMap = savedLastTagTimeStampMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

        val savedLastTeamsTimeStampMapString = sharedPreferences.getString(lastTeamsTimestampMapKey, null)
        LastTeamsTimestampMap = savedLastTeamsTimeStampMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

        val savedLastSegmentsTimeStampMapString = sharedPreferences.getString(
            lastSegmentsTimestampMapKey, null)
        LastSegmentsTimestampMap = savedLastSegmentsTimeStampMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

        val savedProjectIconUrlMapString = sharedPreferences.getString(
            projectIconUrlMapKey, null)
        ProjectIconUrlMap = savedProjectIconUrlMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, String>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

        val savedProjectIdChannelIdMapString = sharedPreferences.getString(projectIdChannelIdMapKey, null)
        projectIdChannelIdMap = savedProjectIdChannelIdMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

        val savedLastChannelsTimeStampMapString = sharedPreferences.getString(
            lastChannelsTimestampMapKey, null)
        LastChannelTimestampMap = savedLastChannelsTimeStampMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, Timestamp>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

        val savedProjectDeeplinkMapString = sharedPreferences.getString(
            projectDeeplinkMapKey, null)
        ProjectDeepLinkMap = savedProjectDeeplinkMapString?.let {
            try {
                Gson().fromJson(it, object : TypeToken<MutableMap<String, String>>() {}.type)
            } catch (e: JsonSyntaxException) {
                mutableMapOf()
            }
        } ?: mutableMapOf()

    }



    fun setProjectTimeStamp(projectName: String, timestamp: Long) {
        projectTimestampMap[projectName] = timestamp
        saveHashMapToPreferences()
    }

    fun getProjectTimeStamp(projectName: String): Long {
        return projectTimestampMap[projectName] ?: 0
    }

    private fun saveHashMapToPreferences() {
        val hashMapString = Gson().toJson(projectTimestampMap)
        editor.putString(projectTimestampMapKey, hashMapString)
        editor.apply()
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

    fun setDownloadedUpdateUri(uri : Uri?){
        editor.putString("UPDATE_URI", uri.toString())
        editor.apply()
    }

  fun getDownloadedUpdateUri(): Uri? {
        val uriString = sharedPreferences.getString("UPDATE_URI", null)
        return if (uriString != null) Uri.parse(uriString) else null
    }

    fun setDownloadID(downloadID: Long){
        editor.putLong("DOWNLOAD_ID", downloadID)
        editor.apply()
    }

    fun getDownloadID(): Long {
        return sharedPreferences.getLong("DOWNLOAD_ID",-1L)
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
        return sharedPreferences.getString("segment",Endpoints.defaultSegment)!!
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
    fun saveProjectSegments(projectName: String, segments: List<SegmentItem>) {
        val gson = Gson()
        val segmentsJson = gson.toJson(segments)
        editor.putString("project_$projectName", segmentsJson)
        editor.apply()
    }

    fun getProjectSegments(projectName: String): List<SegmentItem> {
        val segmentsJson = sharedPreferences.getString("project_$projectName", null)
        val gson = Gson()
        val type = object : TypeToken<List<SegmentItem>>() {}.type
        return if (segmentsJson != null) {
            gson.fromJson(segmentsJson, type)
        } else {
            emptyList()
        }
    }
    fun setTaskTimestamp(projectId: String, taskId: String, timestamp: Timestamp) {
        val id = "$projectId$taskId"
        projectIdTaskIdMap[id] = timestamp
        saveProjectIdTaskIdMapToPreferences()
    }


    fun getTaskTimestamp(projectId: String, taskId: String): Timestamp {
        val id = "$projectId$taskId"
        return projectIdTaskIdMap[id] ?: Timestamp(0, 0)
    }

    private fun saveProjectIdTaskIdMapToPreferences() {
        val projectIdTaskIdMapString = Gson().toJson(projectIdTaskIdMap)
        editor.putString(projectIdTaskIdMapKey, projectIdTaskIdMapString)
        editor.apply()
    }


    fun getReadCount(): Int {
        return sharedPreferences.getInt("readCount", 0)
    }

    fun setReadCount(count: Int) {
        val oldCount= getReadCount()
        editor.putInt("readCount", oldCount+count)
        editor.apply()
    }
    fun resetReadCount() {
        editor.putInt("readCount", 0)
        editor.apply()
    }
    fun getAllTimeReadCount(): Int {
        return sharedPreferences.getInt("allTimeReadCount", 0)
    }

    fun setAllTimeReadCount(count: Int) {
        val oldCount= getAllTimeReadCount()
        editor.putInt("allTimeReadCount", oldCount+count)
        editor.apply()
    }

    fun setLastTaskTimeStamp(projectName: String, timestamp: Timestamp) {
        LastTaskTimestampMap[projectName] = timestamp
        saveLastTaskTimeStampHashMapToPreferences()
    }

    fun getLastTaskTimeStamp(projectName: String): Timestamp {
        return LastTaskTimestampMap[projectName] ?: Timestamp(0, 0)
    }
    private fun saveLastTaskTimeStampHashMapToPreferences() {
        val hashMapString = Gson().toJson(LastTaskTimestampMap)
        editor.putString(lastTaskTimestampMapKey, hashMapString)
        editor.apply()
    }

    fun setLastTagTimeStamp(projectName: String, timestamp: Timestamp) {
        LastTagTimestampMap[projectName] = timestamp
        saveLastTagTimeStampHashMapToPreferences()
    }

    fun getLastTagTimeStamp(projectName: String): Timestamp {
        return LastTagTimestampMap[projectName] ?: Timestamp(0, 0)
    }
    private fun saveLastTagTimeStampHashMapToPreferences() {
        val hashMapString = Gson().toJson(LastTagTimestampMap)
        editor.putString(lastTagTimestampMapKey, hashMapString)
        editor.apply()
    }
    fun putProjectsTasksRetrieved(key: String, stringList: List<String>) {
        val gson = Gson()
        val json = gson.toJson(stringList)
        editor.putString(key, json)
        editor.apply()
    }

    fun getStringList(key: String): List<String> {
        val jsonString = sharedPreferences.getString(key, null)
        return if (jsonString != null) {
            try {
                val gson = Gson()
                val type = object : TypeToken<List<String>>() {}.type
                gson.fromJson(jsonString, type)
            } catch (e: JsonSyntaxException) {
                emptyList()
            }
        } else {
            emptyList()
        }
    }
    fun getAppMode(): String? {
        return sharedPreferences.getString("appMode", Endpoints.ONLINE_MODE)
    }

    fun setAppMode(mode:String) {
        editor.putString("appMode", mode)
        editor.apply()
    }

     fun hasOfflineDialogBeenShown(): Boolean {
        return sharedPreferences.getBoolean("offlineDialogShown", false)
    }

     fun setOfflineDialogShown(isshown:Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("offlineDialogShown", isshown)
        editor.apply()
    }
    fun setLastTeamsTimeStamp(projectName: String, timestamp: Timestamp) {
        LastTeamsTimestampMap[projectName] = timestamp
        saveLastTeamsTimeStampHashMapToPreferences()
    }

    fun getLastTeamsTimeStamp(projectName: String): Timestamp {
        return LastTeamsTimestampMap[projectName] ?: Timestamp(0, 0)
    }
    private fun saveLastTeamsTimeStampHashMapToPreferences() {
        val hashMapString = Gson().toJson(LastTeamsTimestampMap)
        editor.putString(lastTeamsTimestampMapKey, hashMapString)
        editor.apply()
    }
    fun setLastSegmentsTimeStamp(projectName: String, timestamp: Timestamp) {
        LastSegmentsTimestampMap[projectName] = timestamp
        saveLastSegmentsTimeStampHashMapToPreferences()
    }
    fun getLastSegmentsTimeStamp(projectName: String): Timestamp {
        return LastSegmentsTimestampMap[projectName] ?: Timestamp(0, 0)
    }
    private fun saveLastSegmentsTimeStampHashMapToPreferences() {
        val hashMapString = Gson().toJson(LastSegmentsTimestampMap)
        editor.putString(lastSegmentsTimestampMapKey, hashMapString)
        editor.apply()
    }
    fun setProjectIconUrl(projectName: String, url:String) {
        ProjectIconUrlMap[projectName] = url
        saveProjectIconUrlHashMapToPreferences()
    }
    fun getProjectIconUrl(projectName: String): String {
        return ProjectIconUrlMap[projectName] ?: ""
    }
    private fun saveProjectIconUrlHashMapToPreferences() {
        val hashMapString = Gson().toJson(ProjectIconUrlMap)
        editor.putString(projectIconUrlMapKey, hashMapString)
        editor.apply()
    }
    fun setChannelTimestamp(projectId: String, channelID: String, timestamp: Timestamp) {
        val id = "$projectId$channelID"
        projectIdChannelIdMap[id] = timestamp
        saveProjectIdChannelIdMapToPreferences()
    }


    fun getChannelTimestamp(projectId: String, channelID: String): Timestamp {
        val id = "$projectId$channelID"
        return projectIdChannelIdMap[id] ?: Timestamp(0, 0)
    }

    private fun saveProjectIdChannelIdMapToPreferences() {
        val projectIdChannelIdMapString = Gson().toJson(projectIdChannelIdMap)
        editor.putString(projectIdChannelIdMapKey, projectIdChannelIdMapString)
        editor.apply()
    }

    fun setLastChannelTimeStamp(projectName: String, timestamp: Timestamp) {
        LastChannelTimestampMap[projectName] = timestamp
        saveLastChannelTimeStampHashMapToPreferences()
    }
    fun getLastChannelTimeStamp(projectName: String): Timestamp {
        return LastChannelTimestampMap[projectName] ?: Timestamp(0, 0)
    }
    private fun saveLastChannelTimeStampHashMapToPreferences() {
        val hashMapString = Gson().toJson(LastChannelTimestampMap)
        editor.putString(lastChannelsTimestampMapKey, hashMapString)
        editor.apply()
    }
    fun saveProjectChannels(projectName: String, channels: List<Channel>) {
        val gson = Gson()
        val channelsJson = gson.toJson(channels)
        editor.putString("project_channels_$projectName", channelsJson)
        editor.apply()
    }

    fun getProjectChannels(projectName: String): List<Channel> {
        val channelsJson = sharedPreferences.getString("project_channels_$projectName", null)
        val gson = Gson()
        val type = object : TypeToken<List<Channel>>() {}.type
        return if (channelsJson != null) {
            gson.fromJson(channelsJson, type)
        } else {
            emptyList()
        }
    }
    fun saveProjectFavourites(projectName: String, favs: List<String>) {
        val gson = Gson()
        val channelsJson = gson.toJson(favs)
        editor.putString("project_favs_$projectName", channelsJson)
        editor.apply()
    }

    fun getProjectFavourites(projectName: String): List<String> {
        val favsJson = sharedPreferences.getString("project_favs_$projectName", null)
        val gson = Gson()
        val type = object : TypeToken<List<String>>() {}.type
        return if (favsJson != null) {
            gson.fromJson(favsJson, type)
        } else {
            emptyList()
        }
    }
    fun setProjectDeepLink(projectName: String, link:String) {
        ProjectDeepLinkMap[projectName] = link
        saveProjectDeepLinkHashMapToPreferences()
    }
    fun getProjectDeepLink(projectName: String): String {
        return ProjectDeepLinkMap[projectName] ?: ""
    }
    private fun saveProjectDeepLinkHashMapToPreferences() {
        val hashMapString = Gson().toJson(ProjectDeepLinkMap)
        editor.putString(projectDeeplinkMapKey, hashMapString)
        editor.apply()
    }

}