package com.ncs.o2.Services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.common.util.AndroidUtilsLight
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.HelperClasses.NotificationUtil.showNotification
import com.ncs.o2.Models.O2Notification
import com.ncs.o2.R
import com.ncs.o2.UI.Notifications.Requests.RequestActivity
import timber.log.Timber

/*
File : FirebaseMessagingService.kt -> com.ncs.o2
Description : File for FCM Service token 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:12 am on 01/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 



*/
class FCMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Timber.tag("SellerFirebaseService ").i("Refreshed token :: %s", token)
        sendRegistrationToServer(token)
    }

    private fun sendRegistrationToServer(token: String) {
        // TODO : send token to tour server
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Timber.tag("SellerFirebaseService ").i("Message :: %s", message.data["body"])
        val notif = O2Notification(NotificationType.TASK_REQUEST_RECIEVED,"Work Request from armax","#ID12345 has a work request from Armax")
        showNotification(notification = notif, context = applicationContext)

    }


}