package com.ncs.o2.Domain.Models

import com.google.firebase.Timestamp

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
        val segment_NAME : String="",
        val segment_ID : String="",
        val description : String="",
        val contributers : MutableList<String> = mutableListOf(),
        val segment_CREATOR : String="",
        val segment_CREATOR_ID : String="",
        val project_ID: String="",
        val creation_DATETIME : Timestamp?=null,
        )