package com.ncs.o2.Domain.Models

import android.graphics.Color
import com.ncs.o2.Domain.Utility.Later

/*
File : Tag.kt -> com.ncs.o2.Models
Description : Model file for Tags  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 11:52 pm on 04/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
@Later("1.Add priority, tagID")
data class Tag(
    val tagText: String,
    val bgColor : String,
    val textColor : String,
)