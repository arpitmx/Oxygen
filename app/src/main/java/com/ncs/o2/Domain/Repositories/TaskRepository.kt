package com.ncs.o2.Domain.Repositories

import com.ncs.o2.Data.Room.TasksRepository.TasksDao
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.o2.Domain.Interfaces.TasksRepository
import com.ncs.o2.Domain.Models.DBResult
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Models.Task
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TaskRepository @Inject constructor(private val db: TasksDatabase):TasksRepository {
    val taskDao=db.tasksDao()
    override suspend fun getTasksItemsForSegment(
        projectName: String,
        segmentName: String,
        sectionName: String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val tasks = taskDao.getTasksforSegments(projectName, segmentName, sectionName)
                withContext(Dispatchers.Main) {
                    resultCallback(DBResult.Success(tasks))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultCallback(DBResult.Failure(e))
                }
            }
        }
    }

    override suspend fun getTaskbyID(
        projectName: String,
        taskId:String,
        resultCallback: (DBResult<Task>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val tasks = taskDao.getTasksbyId(tasksId = taskId, projectId = projectName)
                withContext(Dispatchers.Main) {
                    resultCallback(DBResult.Success(tasks!!))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultCallback(DBResult.Failure(e))
                }
            }
        }
    }

    override suspend fun getSearchedTasksfromDB(
        assignee: String,
        creator: String,
        state: Int,
        type: Int,
        text: String,
        projectName: String,
        segmentName: String,
        resultCallback: (DBResult<List<Task>>) -> Unit
    ) {
        withContext(Dispatchers.IO) {
            try {
                val tasks = taskDao.getSearchedTasks(
                    projectName = projectName,
                    type=type,
                    state=state,
                    creator = creator,
                    assignee = assignee,
                    text = text,
                    segmentName=segmentName
                )
                withContext(Dispatchers.Main) {
                    resultCallback(DBResult.Success(tasks))
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    resultCallback(DBResult.Failure(e))
                }
            }
        }
    }

}