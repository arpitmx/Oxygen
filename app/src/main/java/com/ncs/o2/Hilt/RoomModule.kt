package com.ncs.versa.Hilt

import android.content.Context
import androidx.room.Room
import com.ncs.o2.Data.Room.MessageRepository.MessageDatabase
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import com.ncs.o2.Data.Room.TasksRepository.TasksDatabase
import com.ncs.versa.Constants.Endpoints
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
File : FirebaseModule.kt -> com.ncs.versa.Hilt
Description : This is the hilt module for firebase objects injection 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : Versa Android)

Creation : 9:15 am on 24/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

@InstallIn(SingletonComponent::class)
@Module
object RoomModule {

    @Singleton
    @Provides
    fun providesNotificationDatabase(@ApplicationContext context : Context) : NotificationDatabase {
        return Room.databaseBuilder(context,
            NotificationDatabase::class.java,
            Endpoints.ROOM.NOTIFICATIONS.NOTIFICATIONS_DATABASE
            )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providesTaksDatabase(@ApplicationContext context : Context) : TasksDatabase {
        return Room.databaseBuilder(context,
            TasksDatabase::class.java,
            Endpoints.ROOM.TASKS.TASKS_DB
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providesMessagesDatabase(@ApplicationContext context : Context) : MessageDatabase {
        return Room.databaseBuilder(context,
            MessageDatabase::class.java,
            Endpoints.ROOM.MESSAGES.MESSAGES_DB
        )
            .fallbackToDestructiveMigration()
            .build()
    }




}