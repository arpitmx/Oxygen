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

@Version("4")
@Entity(tableName = Endpoints.ROOM.TASKS.TASKS_TABLE)
@TypeConverters(Convertors::class)
data class Task(

    var title: String = "",
    var description: String = "",
    @PrimaryKey(autoGenerate = false)
    var id: String="",
    var difficulty: Int = 0,
    var priority: Int = 0,
    var status: Int = -1,

    var assignee: String = "",
    val assigner: String = "",
    var moderators:List<String> = listOf(),

    var time_STAMP: Timestamp? =null,
    var duration: String = "",
    var tags: List<String> = listOf(),
    var project_ID: String = "",
    var segment: String = "",
    var section: String = "",

    var type:Int=0,
    val last_updated:Timestamp? = Timestamp.now(),
    val version:Int?=4
    )

