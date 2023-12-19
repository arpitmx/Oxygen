package com.ncs.o2.Domain.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Utility.Later
import com.ncs.o2.Domain.Utility.Version
import com.ncs.o2.HelperClasses.Convertors
import com.ncs.versa.Constants.Endpoints

/*
File : Tag.kt -> com.ncs.o2.Models
Description : Model file for Tags  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:52 pm on 04/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
@Later("1.Add priority, tagID")
@Version("2")
@Entity(tableName = Endpoints.ROOM.TASKS.TAGS_TABLE)
@TypeConverters(Convertors::class)
data class Tag(
    val tagText: String,
    val bgColor : String,
    val textColor : String,
    @PrimaryKey(autoGenerate = false)
    val tagID:String="",
    var checked: Boolean = false,
    val last_tag_updated: Timestamp? = Timestamp.now(),
    val projectName:String
    )