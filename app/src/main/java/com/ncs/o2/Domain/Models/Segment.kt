package com.ncs.o2.Domain.Models

import com.google.firebase.firestore.Exclude

/*
File : Segment.kt -> com.ncs.o2.Domain.Models
Description : Model class for Segment 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:07 am on 09/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 



*/
data class Segment (
        val SEGMENT_NAME : String="",
        val SEGMENT_ID : String="",
        val DESCRIPTION : String="",
        val TASK_IDS : MutableList<String> = mutableListOf(),
        val CONTRIBUTERS : MutableList<String> = mutableListOf(),
        val SEGMENT_CREATOR : String="",
        val PROJECT_ID: String="",
        @Exclude val CREATION_DATETIME : String="",
        )