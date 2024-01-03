package com.ncs.versa.Constants

object Endpoints {

    const val USERS = "Users"
    const val PROJECTS = "Projects"
    const val USERID = "userid1"
    const val new_changes_des = "Certainly! In order to provide you with a more personalized description, could you please specify what kind of description you are looking for? Are you interested in a description of a person, a place, an event, or something else? Additionally, if you have any specific details or preferences in mind, feel free to share them, so I can tailor the description to your needs.```xml    <application>        <meta-data          android:name=\"firebase_performance_logcat_enabled\"          android:value=\"true\" />    </application>```"


    object SharedPref {
        const val SHAREDPREFERENCES = "O2PREF"
    }


    object CodeViewer {
        val LANG = "LANG"
        const val CODE = "CODE"

    }

    object Storage {
        const val DP_PATH = "/DP/dp"
        const val IMAGE_PATH = "/icon/img"
    }

    object TaskDetails{

        const val EMPTY_MODERATORS = "None"

    }


    object User {

        val FCM_TOKEN = "FCM_TOKEN"
        val DP_URL = "DP_URL"
        const val PROJECTS = "PROJECTS"
        const val EMAIL = "EMAIL"
        const val USERNAME = "USERNAME"
        const val BIO = "BIO"
        const val DESIGNATION = "DESIGNATION"
        const val ROLE = "ROLE"
        const val PHOTO_ADDED = "PHOTO_ADDED"
        const val DETAILS_ADDED = "DETAILS_ADDED"
        const val NOTIFICATION_TIME_STAMP = "NOTIFICATION_LAST_SEEN"
        const val TIMESTAMP="TIMESTAMP"
        const val LAST_NOTIFICATION_UPDATED="LAST_NOTIFICATION_UPDATED"
    }

    object Workspace {
        const val WORKSPACE = "WORKSPACE"
        const val STATUS = "status"
        const val ID = "id"
        const val PROJECT_ID="project_id"
    }

    object Project {

        const val ALL_SEGMENT = "ALL_SEGMENT"
        const val TASKS = "TASKS"
        const val MESSAGES="MESSAGES"
        const val SEGMENT = "SEGMENTS"
        const val CONTRIBUTERS = "CONTRIBUTERS"
        const val PROJECTID = "PROJECT_ID"
        const val PROJECTNAME = "PROJECT_NAME"
        const val TAGS = "TAGS"
        const val CHECKLIST="CHECKLIST"
        const val LAST_UPDATED="last_updated"
        const val LAST_TAG_UPDATED="last_tag_updated"
    }



    object Notifications {
        val TASKID: String = "TASKID"
        const val NOTIFICATION_LAST_SEEN = "NOTIFICATION_LAST_SEEN"
        const val NOTIFICATION_COUNT = "NOTIFICATION_COUNT"
        const val LATEST_NOTIFICATION_TIME_STAMP = "LATEST_NOTIFICATION_TIME_STAMP"
        const val NOTIFICATIONS = "NOTIFICATIONS"
        const val TIMESTAMP = "timeStamp"

        const val notificationID: String = "notificationID"
        const val notificationType: String = "notificationType"


        const val taskID: String = "taskID"
        const val title: String = "title"
        const val message: String = "message"

        const val fromUser: String = "fromUser"
        const val toUser: String = "toUser"
        const val timeStamp: String = "timeStamp"
        const val lastUpdated:String="lastUpdated"
        const val TO: String = "to"
        const val TITLE: String = "title"
        const val BODY: String = "body"
        const val DATA: String = "data"
        const val TYPE: String = "type"
        const val project_id:String = "projectID"

        object Groups{
            const val COMMENT_NOTIF_GROUP: String = "Comment_Notification_Group"

        }

        object Types {
            const val REQUEST_FAILED_NOTIFICATION = 1
            const val TASK_REQUEST_RECIEVED_NOTIFICATION = 2
            const val TASK_CREATION_NOTIFICATION = 3
            const val TASK_COMMENT_MENTION_NOTIFICATION = 4
            const val UNKNOWN_TYPE_NOTIFICATION = -1
            const val TASK_ASSIGNED_NOTIFICATION = 5
            const val WORKSPACE_TASK_UPDATED=6
            const val TASK_CHECKLIST_UPDATED=7
        }

    }


    object SEGMENT {
        const val TASKS = "TASKS"
        const val ASSIGNEE = "ASSIGNEE"
        const val ASSIGNER = "ASSIGNER"
        const val DEADLINE = "DEADLINE"
        const val STATUS = "STATUS"
        const val TITLE = "TITLE"


        const val SEGMENT_NAME = "segment_name"
        const val SEGMENT_ID = "segment_id"
        const val DESCRIPTION = "DESC"
        const val CONTRIBUTERS = "contributors"
        const val CREATOR = "creator"
        const val CREATOR_ID = "creator_id"
        const val PROJECT_ID = "project_id"
        const val CREATION_DATETIME = "creation_datetime"

    }

    object ROOM {

        object NOTIFICATIONS {
            const val NOTIFICATIONS_TABLE = "notifications"
            const val NOTIFICATIONS_DATABASE = "NOTIFICATION-DB"
        }
        object MESSAGES{
            const val USERLIST_TABLE="users_in_messages"
            const val USERLIST_DB="users_in_messages_db"
        }
        object TASKS{
            const val TASKS_TABLE="tasks"
            const val TAGS_TABLE="tags"
            const val TASKS_DB="tasks_db"
        }



    }


}