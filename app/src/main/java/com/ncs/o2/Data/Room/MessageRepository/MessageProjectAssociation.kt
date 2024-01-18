package com.ncs.o2.Data.Room.MessageRepository

import androidx.room.Entity

@Entity(tableName = "message_project_table", primaryKeys = ["messageId", "projectId"])
data class MessageProjectAssociation(
    val messageId: String,
    val projectId: String,
)