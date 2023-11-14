package com.ncs.o2.Domain.Models

import android.graphics.Color
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
    val assignee: List<String> = emptyList(),
    val assigner: String = "",
    val assigner_email:String="",
    val deadline: String = "",
    var time_STAMP: Timestamp? =null,
//    val timestamp: FieldValue= FieldValue.serverTimestamp(),
    val duration: String = "",
    val tags: List<String> = listOf(),
    val project_ID: String = "",
    val segment: String = "",
    val section: String = "",
    val assignee_DP_URL: String = "",
    val completed:Boolean=false
    ) {

    @Exclude
    fun getPriorityColor(): Int {
        when (priority) {
            1 -> return Color.GREEN
            2 -> return Color.YELLOW
            3 -> return Color.RED
            else -> return Color.BLACK
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