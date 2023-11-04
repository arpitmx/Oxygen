package com.ncs.o2.Domain.Models

/*
File : User.kt -> com.ncs.o2.Models
Description : User data class for Firestore user 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:40 am on 31/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/
data class CurrentUser (
            val EMAIL: String = "",
            val USERNAME: String = "",
            val PROJECTS: List<String> = listOf(),
            val BIO:String="",
            val DESIGNATION:String="",
            val ROLE:Long=1,
)