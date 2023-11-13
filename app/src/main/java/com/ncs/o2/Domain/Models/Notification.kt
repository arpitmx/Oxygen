package com.ncs.o2.Domain.Models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ncs.versa.Constants.Endpoints
import java.util.Date

/*
File : ReceiveNotification -> com.ncs.o2.Domain.Models
Description : Data class for recieving notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:37 am on 15/10/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

@Entity(tableName = Endpoints.ROOM.NOTIFICATIONS.NOTIFICATIONS_TABLE)
data class Notification(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "notificationID")
    val notificationID: String,

    @ColumnInfo(name = "notificationType")
    val notificationType: String,

    @ColumnInfo(name = "taskID")
    val taskID: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "message")
    val message: String,

    @ColumnInfo(name = "fromUser")
    val fromUser: String,

    @ColumnInfo(name = "toUser")
    val toUser: String,

    @ColumnInfo(name = "timeStamp")
    val timeStamp: Long
)
