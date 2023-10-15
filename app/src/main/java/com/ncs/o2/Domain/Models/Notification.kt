package com.ncs.o2.Domain.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.ncs.o2.Constants.NotificationType

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

data class Notification(
    val notificationType: String,
    val taskID : String ,
    val title: String ,
    val message: String,
    val timeStamp : FieldValue,
    val fromUser : String ,
    val toUser : String
)
