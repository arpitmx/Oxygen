package com.ncs.o2.Data.Room.MessageRepository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Models.UserInMessage

@Dao
interface UsersDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user:UserInMessage)

    @Query("SELECT * FROM users_in_messages")
    suspend fun getAllUsers(): List<UserInMessage>

    @Query("SELECT * FROM users_in_messages WHERE EMAIL=:email")
    suspend fun getUserbyId(email: String): UserInMessage?

}