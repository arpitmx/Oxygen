package com.ncs.o2.Data.Room.NotificationRepository

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ncs.o2.Domain.Models.Notification

/*
File : NotificationDAO -> com.ncs.o2.Room.NotificationsDB
Description : Interface for Notification DAO 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity (@Project : O2 Android)

Creation : 5:42 am on 02/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION :
*/

@Dao
interface NotificationDao {
    @Insert
    suspend fun insert(notification: Notification)

    @Update
    suspend fun update(notification: Notification)

    @Delete
    suspend fun delete(notification: Notification)

    @Query("SELECT * FROM notifications WHERE notificationID = :notificationID")
    suspend fun getNotificationById(notificationID: String): Notification?

    @Query("SELECT * FROM notifications")
    suspend fun getAllNotifications(): List<Notification>

}