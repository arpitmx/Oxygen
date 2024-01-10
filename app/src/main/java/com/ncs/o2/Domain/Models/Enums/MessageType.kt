package com.ncs.o2.Domain.Models.Enums

/*
File : MessageType -> com.ncs.o2.Domain.Models.Enums
Description : Enum for Message type 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 2:35 pm on 20/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/


enum class MessageType{
    NORMAL_MSG,  // containing only textual content
    IMAGE_MSG,   // containing only img
    FILE_MSG ;    // containing only file
    companion object {
        fun fromString(value: String): MessageType? {
            return values().find { it.name == value }
        }
    }
}
