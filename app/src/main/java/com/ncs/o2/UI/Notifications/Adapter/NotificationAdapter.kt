package com.ncs.o2.UI.Notifications.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.ncs.o2.Constants.NotificationType
import com.ncs.o2.Domain.Models.Notification
import com.ncs.o2.Domain.Utility.DateTimeUtils
import com.ncs.o2.Domain.Utility.ExtensionsUtil.invisible
import com.ncs.o2.Domain.Utility.ExtensionsUtil.setOnClickThrottleBounceListener
import com.ncs.o2.Domain.Utility.ExtensionsUtil.visible
import com.ncs.o2.R
import com.ncs.o2.databinding.NotificationMentionItemBinding
import com.ncs.versa.Constants.Endpoints.Notifications.Types as T


/*
File : NotificationAdapter -> com.ncs.o2.UI.Notifications
Description : Adapter for notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:51 am on 18/11/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
class NotificationAdapter(
    private val context: Context,
    private val lastSeenTimeStamp: Long,
    private var notifications: List<Notification>,
    private val notificationClick: OnNotificationClick
) :
    RecyclerView.Adapter<ViewHolder>() {


    init {
        notifications = notifications.sortedByDescending { it.timeStamp }
    }


    private fun getItemTypeInt(notificationType: String): Int {
        when (notificationType) {

            NotificationType.REQUEST_FAILED_NOTIFICATION.toString() -> {
                return T.REQUEST_FAILED_NOTIFICATION
            }

            NotificationType.TASK_REQUEST_RECIEVED_NOTIFICATION.toString() -> {
                return T.TASK_REQUEST_RECIEVED_NOTIFICATION
            }

            NotificationType.TASK_CREATION_NOTIFICATION.toString() -> {
                return T.TASK_CREATION_NOTIFICATION
            }

            NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString() -> {
                return T.TASK_COMMENT_MENTION_NOTIFICATION
            }
            NotificationType.TASK_ASSIGNED_NOTIFICATION.toString() -> {
                return T.TASK_ASSIGNED_NOTIFICATION
            }
            NotificationType.WORKSPACE_TASK_UPDATE.toString() -> {
                return T.WORKSPACE_TASK_UPDATED
            }
            NotificationType.TASK_CHECKLIST_UPDATE.toString() -> {
                return T.TASK_CHECKLIST_UPDATED
            }
            else -> {
                return T.UNKNOWN_TYPE_NOTIFICATION
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItemTypeInt(notifications[position].notificationType)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            T.TASK_COMMENT_MENTION_NOTIFICATION -> {
                val binding = NotificationMentionItemBinding.inflate(inflater, parent, false)
                ActivityMentionNotificationVH(binding)
            }
            T.TASK_ASSIGNED_NOTIFICATION -> {
                val binding = NotificationMentionItemBinding.inflate(inflater, parent, false)
                ActivityAssignedNotificationVH(binding)
            }
            T.WORKSPACE_TASK_UPDATED -> {
                val binding = NotificationMentionItemBinding.inflate(inflater, parent, false)
                ActivityWorkspaceUpdatedNotificationVH(binding)
            }
            T.TASK_CHECKLIST_UPDATED -> {
                val binding = NotificationMentionItemBinding.inflate(inflater, parent, false)
                ActivityCheckLstUpdatedNotificationVH(binding)
            }


            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val notification = notifications[position]

        when (notification.notificationType) {
            NotificationType.TASK_COMMENT_MENTION_NOTIFICATION.toString() -> {
                (holder as ActivityMentionNotificationVH).bind(notification)
            }
            NotificationType.TASK_ASSIGNED_NOTIFICATION.toString() -> {
                (holder as ActivityAssignedNotificationVH).bind(notification)
            }
            NotificationType.WORKSPACE_TASK_UPDATE.toString() -> {
                (holder as ActivityWorkspaceUpdatedNotificationVH).bind(notification)
            }
            NotificationType.TASK_CHECKLIST_UPDATE.toString() -> {
                (holder as ActivityCheckLstUpdatedNotificationVH).bind(notification)
            }
        }
    }

    override fun getItemCount(): Int = notifications.size

    private inner class ActivityMentionNotificationVH(val binding: NotificationMentionItemBinding) :
        ViewHolder(binding.root) {

        fun bind(notification: Notification) {

            binding.taskId.text = notification.taskID
            binding.durationTv.text = DateTimeUtils.getTimeAgo(notification.timeStamp)
            binding.msgTv.text = notification.message

            if (notification.timeStamp > lastSeenTimeStamp) {
                binding.newNotifMark.visible()
            } else {
                binding.newNotifMark.invisible()
            }
            binding.parent.setOnClickThrottleBounceListener{
                notificationClick.onClick(notification)
            }
        }


    }
    private inner class ActivityAssignedNotificationVH(val binding: NotificationMentionItemBinding) :
        ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.difficulty.setBackgroundResource(R.drawable.label_cardview_green)
            binding.difficulty.text="@Assigned"
            binding.difficulty.setTextColor(context.resources.getColor(R.color.darkbg_main))
            binding.taskId.text = notification.taskID
            binding.durationTv.text = DateTimeUtils.getTimeAgo(notification.timeStamp)
            binding.msgTv.text = notification.message

            if (notification.timeStamp > lastSeenTimeStamp) {
                binding.newNotifMark.visible()
            } else {
                binding.newNotifMark.invisible()
            }
            binding.parent.setOnClickThrottleBounceListener{
                notificationClick.onClick(notification)
            }

        }


    }

    private inner class ActivityWorkspaceUpdatedNotificationVH(val binding: NotificationMentionItemBinding) :
        ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.difficulty.setBackgroundResource(R.drawable.label_cardview_yellow)
            binding.difficulty.text="@Workspace Update"
            binding.difficulty.setTextColor(context.resources.getColor(R.color.darkbg_main))
            binding.taskId.text = notification.taskID
            binding.durationTv.text = DateTimeUtils.getTimeAgo(notification.timeStamp)
            binding.msgTv.text = notification.message

            if (notification.timeStamp > lastSeenTimeStamp) {
                binding.newNotifMark.visible()
            } else {
                binding.newNotifMark.invisible()
            }
            binding.parent.setOnClickThrottleBounceListener{
                notificationClick.onClick(notification)
            }

        }


    }

    private inner class ActivityCheckLstUpdatedNotificationVH(val binding: NotificationMentionItemBinding) :
        ViewHolder(binding.root) {

        fun bind(notification: Notification) {
            binding.difficulty.setBackgroundResource(R.drawable.label_cardview_red)
            binding.difficulty.text="@Checklist Update"
            binding.difficulty.setTextColor(context.resources.getColor(R.color.better_white))
            binding.taskId.text = notification.taskID
            binding.durationTv.text = DateTimeUtils.getTimeAgo(notification.timeStamp)
            binding.msgTv.text = notification.message

            if (notification.timeStamp > lastSeenTimeStamp) {
                binding.newNotifMark.visible()
            } else {
                binding.newNotifMark.invisible()
            }
            binding.parent.setOnClickThrottleBounceListener{
                notificationClick.onClick(notification)
            }

        }


    }

    interface OnNotificationClick{
        fun onClick(notification: Notification)
    }
}