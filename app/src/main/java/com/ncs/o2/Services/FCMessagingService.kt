package com.ncs.o2.Services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.HelperClasses.LocalNotificationUtil.showNotification
import com.ncs.o2.Domain.Models.FCNotification
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
        val notif = FCNotification(NotificationType.TASK_REQUEST_RECIEVED_NOTIFICATION,"Work Request from armax","#ID12345 has a work request from Armax")
        showNotification(notification = notif, context = applicationContext)

    }


}