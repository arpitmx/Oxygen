package com.ncs.o2.Data.Room.MessageRepository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.Task

@Dao
interface TeamsMessagesDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAssociation(association: MessageProjectAssociation)

    @Update
    suspend fun update(message: Message)

    @Delete
    suspend fun delete(message: Message)

    @Query("SELECT message_table.* FROM message_table " +
            "INNER JOIN message_project_table ON message_table.messageId = message_project_table.messageId " +
            "WHERE message_project_table.projectId = :projectId")
    fun getMessagesForProject(projectId: String): List<Message>


}
