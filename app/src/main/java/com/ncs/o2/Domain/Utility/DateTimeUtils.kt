package com.ncs.o2.Domain.Utility

import java.text.SimpleDateFormat
import java.util.Date

/*
File : DateTimeUtils -> com.ncs.o2.Domain.Utility
Description : Utility for date and time conversions 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 7:08 pm on 02/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
object DateTimeUtils {
    fun formatTime(seconds: Long, dateFormat: String = "yyyy-MM-dd HH:mm:ss"): String {
        val milliseconds = seconds * 1000L
        val date = Date(milliseconds)
        val sdf = SimpleDateFormat(dateFormat)
        return sdf.format(date)
    }
}