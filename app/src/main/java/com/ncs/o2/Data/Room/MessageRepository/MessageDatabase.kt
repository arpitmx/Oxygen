package com.ncs.o2.Data.Room.MessageRepository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ncs.o2.Domain.Models.UserInMessage

@Database(entities = [UserInMessage::class], version = 1, exportSchema = false)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao
}