package com.ncs.o2.Domain.Models

import android.graphics.Color
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
data class Task(
    val TITLE: String = "",
    val DESC: String = "",
    val ID: String = "",
    val DIFFICULTY: Int = 0,
    val LINKS: List<String> = emptyList(),
    val PRIORITY: Int = 0,
    val STATUS: Int = -1,
    val ASSIGNEE: List<String> = emptyList(),
    val ASSIGNER: String = "",
    val DEADLINE: String = "",
    val STARTDATE: String = "",
    val DURATION: String = "",
    val TAGS: List<Tag> = listOf(),
    val PROJECTID: String,
    val SEGMENT: String,
) {

    @Exclude
    fun getPriorityColor(): Int {
        when (PRIORITY) {
            1 -> return Color.GREEN
            2 -> return Color.YELLOW
            3 -> return Color.RED
            else -> return Color.BLACK
        }
    }

    @Exclude
    fun getDifficultyColor(): Int {
        when (DIFFICULTY) {
            1 -> return Color.GREEN
            2 -> return Color.YELLOW
            3 -> return Color.RED
            else -> return Color.BLACK
        }
    }

    @Exclude
    fun getDifficultyString(): String {
        when (DIFFICULTY) {
            1 -> return "E"
            2 -> return "M"
            3 -> return "H"
            else -> return "N/A"
        }
    }

    @Exclude
    fun getStatusString(): String {
        when (STATUS) {
            0 -> return "Unassigned"
            1 -> return "Assigned"
            2 -> return "In Progress"
            3 -> return "Finished"
            else -> return "N/A"
        }
    }


}