package com.ncs.o2.Domain.Models

import com.google.firebase.Timestamp
import com.ncs.o2.Domain.Models.Enums.MessageType
import com.ncs.o2.Domain.Utility.Version


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
data class Message(
    val messageId: String,
    val senderId: String,
    val content: String,
    val timestamp: Timestamp?=null,
    val messageType: MessageType,
    val additionalData: Map<String, Any> = emptyMap()
)

