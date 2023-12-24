package com.ncs.o2.Data.Room.NotificationRepository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ncs.o2.Domain.Models.Notification

/*
File : NotificationDatabase -> com.ncs.o2.Room.NotificationsDB
Description : Database for Notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 5:54 am on 02/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
@Database(entities = [Notification::class], version = 1)
abstract class NotificationDatabase : RoomDatabase() {

    // Step 2: Define an abstract function to get the NotificationDao
    abstract fun notificationDao(): NotificationDao

    // You can define other DAOs for additional entities here if needed.
}