package com.ncs.o2.Domain.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Utility.Version
import com.ncs.o2.HelperClasses.Convertors


/*
File : Message -> com.ncs.o2.Domain.Models
Description : Message data class for chats  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:30 pm on 20/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*//*
File : Message -> com.ncs.o2.Domain.Models
Description : Message data class for chats  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity (@Project : O2 Android)

Creation : 1:30 pm on 20/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

@Version("1")
@Entity(tableName = "message_table")
@TypeConverters(Convertors::class)
data class Message(
    @PrimaryKey(autoGenerate = false)
    val messageId: String,
    val senderId: String,
    val content: String,
    val timestamp: Timestamp?=null,
    val messageType: MessageType,
    val additionalData: Map<String, Any>? = emptyMap(),
)

