package com.ncs.o2.Data.Room.TasksRepository

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ncs.o2.Domain.Models.Task
import com.ncs.o2.HelperClasses.Convertors

@Database(entities = [Task::class], version = 2)
@TypeConverters(Convertors::class)
abstract class TasksDatabase : RoomDatabase() {
    abstract fun tasksDao(): TasksDao

}