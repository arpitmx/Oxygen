package com.ncs.o2.Domain.Models

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ncs.versa.Constants.Endpoints

@Entity(tableName = Endpoints.ROOM.MESSAGES.USERLIST_TABLE)
data class UserInMessage(
    val USERNAME: String? = "",
    @PrimaryKey
    val EMAIL:String="",
    val DP_URL:String?=null,
    val FCM_TOKEN:String?=""
)
