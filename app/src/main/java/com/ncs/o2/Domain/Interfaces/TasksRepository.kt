package com.ncs.o2.Domain.Interfaces

import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.TaskItem

interface TasksRepository {
    suspend fun getTasksItemsForSegment(
        projectName: String,
        segmentName: String,
        sectionName: String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    )

    suspend fun getTasksItemsForState(
        projectName: String,
        state: Int,
        resultCallback: (DBResult<List<Task>>) -> Unit
    )

    suspend fun getTaskbyID(
        projectName: String,
        taskId: String,
        resultCallback: (DBResult<Task>) -> Unit
    )

    suspend fun getSearchedTasksfromDB(
        assignee: String,
        creator: String,
        state: Int,
        type: Int,
        text: String,
        projectName: String,
        segmentName: String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    )

    suspend fun getTagsInProject(
        projectName: String,
        resultCallback: (DBResult<List<Tag>>) -> Unit
    )

    suspend fun getMessagesforTask(
        projectName: String,
        taskID: String,
        resultCallback: (DBResult<List<Message>>) -> Unit
    )

    suspend fun getTeamsMessagesforProject(
        projectName: String,
        channelId:String,
        resultCallback: (DBResult<List<Message>>) -> Unit
    )
}