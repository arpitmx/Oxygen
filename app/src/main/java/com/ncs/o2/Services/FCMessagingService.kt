package com.ncs.o2.Services

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.FCMNotification
import com.ncs.o2.Domain.Utility.ExtensionsUtil.isNull
import com.ncs.o2.HelperClasses.NotificationBuilderUtil.showNotification
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.versa.Constants.Endpoints.Notifications as N
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

    override fun onMessageReceived(receivedNotification: RemoteMessage) {
        super.onMessageReceived(receivedNotification)
        if (PrefManager.getUserFCMToken()!="" || PrefManager.getUserFCMToken().isNotEmpty()){
            Timber.tag("SellerFirebaseService ").i("Message :: %s", receivedNotification.data)

            val type = receivedNotification.data[N.TYPE]
            val title = receivedNotification.data[N.TITLE]
            val body = receivedNotification.data[N.BODY]
            val taskID = receivedNotification.data[N.TASKID]

            type?.let {
                if (type == NotificationType.TASK_COMMENT_NOTIFICATION.name){
                    val notif = FCMNotification(
                        notificationType = NotificationType.TASK_COMMENT_NOTIFICATION,
                        title =  title.orEmpty(),
                        message = body.orEmpty(),
                        taskID =  taskID.orEmpty(),
                    )
                    showNotification(notification = notif, context = applicationContext)
                }
                if (type == NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.name){
                    val notif = FCMNotification(
                        notificationType = NotificationType.TASK_COMMENT_MENTION_NOTIFICATION,
                        title =  title.orEmpty(),
                        message = body.orEmpty(),
                        taskID =  taskID.orEmpty(),
                    )

                    showNotification(notification = notif, context = applicationContext)
                }
                if (type == NotificationType.TASK_ASSIGNED_NOTIFICATION.name){
                    val notif = FCMNotification(
                        notificationType = NotificationType.TASK_ASSIGNED_NOTIFICATION,
                        title =  title.orEmpty(),
                        message = body.orEmpty(),
                        taskID =  taskID.orEmpty(),
                    )
                    showNotification(notification = notif, context = applicationContext)
                }
                if (type == NotificationType.WORKSPACE_TASK_UPDATE.name){
                    val notif = FCMNotification(
                        notificationType = NotificationType.WORKSPACE_TASK_UPDATE,
                        title =  title.orEmpty(),
                        message = body.orEmpty(),
                        taskID =  taskID.orEmpty(),
                    )
                    showNotification(notification = notif, context = applicationContext)
                }
                if (type == NotificationType.TASK_CHECKLIST_UPDATE.name){
                    val notif = FCMNotification(
                        notificationType = NotificationType.TASK_CHECKLIST_UPDATE,
                        title =  title.orEmpty(),
                        message = body.orEmpty(),
                        taskID =  taskID.orEmpty(),
                    )
                    showNotification(notification = notif, context = applicationContext)
                }
            }
        }
    }


}