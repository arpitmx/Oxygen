package com.ncs.o2.Data.Room.TasksRepository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ncs.o2.Domain.Models.Task

@Dao
interface TasksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("SELECT * FROM tasks WHERE id = :tasksId and project_ID = :projectId")
    suspend fun getTasksbyId(tasksId:String,projectId:String): Task?

    @Query("SELECT * FROM tasks")
    suspend fun getAllTasks(): List<Task>

    @Query("SELECT * FROM tasks WHERE project_ID = :projectId")
    suspend fun getTasksInProject(projectId:String): List<Task>?

}
