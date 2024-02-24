package com.ncs.o2.Data.Room.MessageRepository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ncs.o2.Domain.Models.Message
import com.ncs.o2.Domain.Models.Tag
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.Domain.Models.UserInMessage
import com.ncs.o2.HelperClasses.Convertors

@Database(entities = [Message::class, UserInMessage::class, Task::class, MessageProjectTaskAssociation::class, MessageProjectAssociation::class], version = 5)
@TypeConverters(Convertors::class)
abstract class MessageDatabase : RoomDatabase() {
    abstract fun usersDao(): UsersDao

    abstract fun messagesDao():MessageDao

    abstract fun teamsMessagesDao():TeamsMessagesDao
}