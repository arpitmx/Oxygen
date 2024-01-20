package com.ncs.o2.Domain.Models

import com.google.firebase.Timestamp

data class Channel(
    val channel_name : String="",
    val channel_id : String="",
    val channel_desc:String="",
    val timestamp : Timestamp?=null,
    val creator:String="",
)
