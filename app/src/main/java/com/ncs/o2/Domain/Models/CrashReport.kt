package com.ncs.o2.Domain.Models

data class CrashReport(
    val stackTrace : String,
    val softwareVersion : String,
    val userInfo: String,
    val SDKVersion : String,
    val activity_ProjectID : String,
    val activity_SegmentID : String,
    val crashTimeStamp : String,


)
