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

    @Query("SELECT * FROM tasks WHERE project_ID = :projectId and status =:state")
    suspend fun getTasksInProjectforState(projectId:String,state: Int): List<Task>

    @Query("SELECT * FROM tasks WHERE project_ID = :projectId and segment = :segmentName and section = :sectionName")
    suspend fun getTasksforSegments(projectId: String,segmentName:String,sectionName:String):List<Task>

    @Query("SELECT * FROM tasks WHERE project_ID = :projectId and segment = :segmentName and section = :sectionName")
    fun getTasks(projectId: String,segmentName:String,sectionName:String):List<Task>

    @Query("SELECT * FROM tasks WHERE project_ID = :projectName " +
            "AND (:type = 0 OR type = :type) " +
            "AND (:state = 0 OR status = :state) " +
            "AND (:creator = '' OR assigner = :creator) " +
            "AND (:assignee = '' OR assignee = :assignee) " +
            "AND (title LIKE '%' || :text || '%' OR description LIKE '%' || :text || '%' OR id LIKE '%' || :text || '%' )"+
            "AND (segment LIKE '%' || :segmentName)"


    )
    suspend fun getSearchedTasks(
        projectName: String,
        type: Int,
        state: Int,
        creator: String,
        assignee: String,
        text: String,
        segmentName: String,
    ): List<Task>

}
