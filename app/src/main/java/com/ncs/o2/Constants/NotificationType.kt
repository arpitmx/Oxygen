package com.ncs.o2.Constants

/*
File : NotificationType.kt -> com.ncs.o2.Constants
Description : This is the type of notifications 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:12 pm on 03/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
enum class NotificationType(val title: String, val priority : Int) {
    REQUEST_FAILED_NOTIFICATION("Request failed", 3),
    TASK_REQUEST_RECIEVED_NOTIFICATION("Work Request",3),
    TASK_CREATION_NOTIFICATION("Task Created",3),
    TASK_COMMENT_MENTION_NOTIFICATION("Mentioned",3),
    TASK_COMMENT_NOTIFICATION("Comment",1) ,
    TASK_ASSIGNED_NOTIFICATION("Task Assigned",3),
    WORKSPACE_TASK_UPDATE("Workspace task updated",3),
    TASK_CHECKLIST_UPDATE("Task checklist updated",3),
    TEAMS_COMMENT_MENTION_NOTIFICATION("Teams_Mentioned",3),
    TEAMS_COMMENT_NOTIFICATION("Teams_Comment",1) ,
    TASK_CHECKPOINT_NOTIFICATION("Task_CheckPoint",1) ,

}