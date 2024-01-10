package com.ncs.o2

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.Timestamp
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Utility.RandomIDGenerator
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDao
import com.ncs.o2.Data.Room.NotificationRepository.NotificationDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import net.datafaker.Faker
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test

/*
File : NotificationDAOTest -> com.ncs.o2
Description : DAO test for Notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity (@Project : O2 Android)

Creation : 9:47 pm on 02/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
class NotificationDAOTest {

    lateinit var notifDB : NotificationDatabase
    lateinit var notifDao : NotificationDao

    @Before
    fun setup(){
        notifDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            NotificationDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        notifDao = notifDB.notificationDao()
        PrefManager.initialize(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun insertNotification_expectedSingleNotificationInsert() = runBlocking{
        val notification  = Notification(
            notificationID = RandomIDGenerator.generateRandomId(),
            notificationType = NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString(),
            taskID = Faker().number().randomDigit().toString(),
            title = "Mention",
            message = Faker().backToTheFuture().quote().toString(),
            timeStamp = Timestamp.now().seconds,
            fromUser = Faker().funnyName().name().toString(),
            toUser = PrefManager.getCurrentUserEmail()
        )

        notifDao.insert(notification = notification)

        val result = notifDao.getNotificationById(notificationID = notification.notificationID)
        Assert.assertEquals(notification.notificationID, result?.notificationID)


    }


    @After
    fun teardown(){
        notifDB.close()
    }


}