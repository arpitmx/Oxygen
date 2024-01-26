package com.ncs.o2.Domain.Models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import com.ncs.o2.Domain.Utility.Later

/*
File : Contributor.kt -> com.ncs.o2.Models
Description : Model for Contributor 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:36 am on 05/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :

*/
@Later("Add name")
data class User (
        val firebaseID : String? = null,
        val profileDPUrl : String? = null,
        val profileIDUrl : String?= null,
        val post: String? = null,
        val username : String? = null,
        val role:Int? = null,
        val timestamp:Timestamp? =null,
        val designation:String?= null,
        val fcmToken: String?= null,
        var email : String?= null,
        var fullName:String?=null,
        @Exclude var isChecked : Boolean = false,
        )
