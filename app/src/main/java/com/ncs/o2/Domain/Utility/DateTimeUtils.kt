package com.ncs.o2.Domain.Utility

import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.time.Instant
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


    fun getTimeAgo(timestampInSeconds: Long): String {
        val currentTime = com.google.firebase.Timestamp.now().seconds
        val timeDifference = currentTime - timestampInSeconds

        return when {
            timeDifference <=0 ->"just now"
            timeDifference < 60 -> "about ${timeDifference}s ago"
            timeDifference < 3600 -> "about ${timeDifference / 60}m ago"
            timeDifference < 86400 -> "about ${timeDifference / 3600}h ago"
            timeDifference < 2592000 -> "about ${timeDifference / 86400}d ago"
            timeDifference < 31536000 -> "about ${timeDifference / 2592000}mo ago"
            else -> "about ${timeDifference / 31536000}y ago"
        }
    }
}