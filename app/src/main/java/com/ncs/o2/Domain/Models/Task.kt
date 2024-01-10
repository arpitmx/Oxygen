package com.ncs.o2.Domain.Models

import android.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.android.gms.common.internal.safeparcel.SafeParcelable.VersionField
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.ncs.o2.Domain.Utility.Version
import com.ncs.o2.HelperClasses.Convertors
import com.ncs.versa.Constants.Endpoints
import java.sql.Time

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

@Version("3")
@Entity(tableName = Endpoints.ROOM.TASKS.TASKS_TABLE)
@TypeConverters(Convertors::class)
data class Task(

    val title: String = "",
    val description: String = "",
    @PrimaryKey(autoGenerate = false)
    var id: String="",
    val difficulty: Int = 0,
    val priority: Int = 0,
    val status: Int = -1,

    val assignee: String = "",
    val assigner: String = "",
    var moderators:List<String> = listOf(),

    var time_STAMP: Timestamp? =null,
    val duration: String = "",
    var tags: List<String> = listOf(),
    val project_ID: String = "",
    val segment: String = "",
    val section: String = "",
    val completed:Boolean=false,
    val type:Int=0,
    val last_updated:Timestamp? = Timestamp.now(),
    val version:Int?=3
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