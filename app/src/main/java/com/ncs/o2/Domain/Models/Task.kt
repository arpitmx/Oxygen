package com.ncs.o2.Domain.Models

import android.graphics.Color
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.VersionField
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

/*
File : Task.kt -> com.ncs.o2.Models
Description : Task model for Tasks 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:43 pm on 31/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/

// Todo Remove deprecated fields like assigner


data class Task(

    val title: String = "",
    val description: String = "",
    var id: String="",
    val difficulty: Int = 0,
    val links: List<String> = emptyList(),
    val priority: Int = 0,
    val status: Int = -1,

    val assignee: List<String> = emptyList(), // Make it String instead of List,store email
    val assignee_DP_URL: String = "", // Remove and get from Firestore passing assignee email
    val assigner: String = "",  //  (Store email of assigner here instead of name)
    val assigner_email:String="", // Remove
    val deadline: String = "", // Remove
    val contributors:List<String> = listOf(), // Change the name to moderators

    var time_STAMP: Timestamp? =null,
    val duration: String = "",
    val tags: List<String> = listOf(),
    val project_ID: String = "",
    val segment: String = "",
    val section: String = "",
    val completed:Boolean=false,
    val type:Int=0,
    ) {

    @Exclude
    fun getPriorityColor(): Int {
        when (priority) {
            1 -> return Color.GREEN
            2 -> return Color.YELLOW
            3 -> return Color.RED
            4 -> return Color.WHITE
            else -> return Color.BLACK
        }
    }
    @Exclude
    fun getPriorityString(): String {
        when (priority) {
            1 -> return "LOW"
            2 -> return "MEDIUM"
            3 -> return "HIGH"
            4 -> return "CRITICAL"
            else -> return ""
        }
    }

    @Exclude
    fun getDifficultyColor(): Int {
        when (difficulty) {
            1 -> return Color.GREEN
            2 -> return Color.YELLOW
            3 -> return Color.RED
            else -> return Color.BLACK
        }
    }

    @Exclude
    fun getDifficultyString(): String {
        when (difficulty) {
            1 -> return "E"
            2 -> return "M"
            3 -> return "H"
            else -> return "N/A"
        }
    }

    @Exclude
    fun getStatusString(): String {
        when (status) {
            0 -> return "Unassigned"
            1 -> return "Assigned"
            2 -> return "In Progress"
            3 -> return "Finished"
            else -> return "N/A"
        }
    }


}