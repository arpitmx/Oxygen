package com.ncs.o2.Domain.Models

import com.ncs.o2.Constants.NotificationType

/*
File : Notification.kt -> com.ncs.o2.Models
Description : Notification data class 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 10:53 pm on 03/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

data class FCMNotification constructor(
    val notificationType: NotificationType,
    val title: String = "",
    val desc: String = "",
    val message: String = "",
    val taskID : String = "",
    val projectID:String="",
    val channelId:String=""
)