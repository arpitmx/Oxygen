package com.ncs.o2.Data.Room.MessageRepository

import androidx.room.Entity

@Entity(tableName = "message_project_task_table", primaryKeys = ["messageId", "projectId", "taskId"])
data class MessageProjectTaskAssociation(
    val messageId: String,
    val projectId: String,
    val taskId: String
)
