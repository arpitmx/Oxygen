package com.ncs.o2.Data.Room.TasksRepository

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ncs.o2.Domain.Models.Tag

@Dao
interface TagsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: Tag)

    @Update
    suspend fun update(tag: Tag)

    @Delete
    suspend fun delete(tag: Tag)

    @Query("SELECT * FROM tags WHERE tagID = :tagId")
    suspend fun getTagbyId(tagId:String): Tag?

    @Query("SELECT * FROM tags WHERE projectName = :projectName")
    suspend fun getTagsInProject(projectName:String): List<Tag>?

    @Query("SELECT * FROM tags")
    suspend fun getAllTag(): List<Tag>?
}