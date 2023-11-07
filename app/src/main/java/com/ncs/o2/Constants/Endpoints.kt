package com.ncs.versa.Constants

import com.google.firebase.auth.FirebaseAuth
import com.ncs.o2.Domain.Repositories.FirebaseAuthRepository_Factory

object Endpoints {

    const val USERS = "Users"
    const val PROJECTS = "Projects"
    const val USERID = "userid1"


    object SharedPref{
        const val SHAREDPREFERENCES = "O2PREF"
    }


    object Storage{
        const val DP_PATH = "/DP/dp"
    }


    object User {
        val FCM_TOKEN= "FCM_TOKEN"
        val DP_URL = "DP_URL"
        const val PROJECTS = "PROJECTS"
        const val EMAIL = "EMAIL"
        const val USERNAME = "USERNAME"
        const val BIO = "BIO"
        const val DESIGNATION = "DESIGNATION"
        const val ROLE = "ROLE"
        const val PHOTO_ADDED = "PHOTO_ADDED"
        const val DETAILS_ADDED = "DETAILS_ADDED"

    }

    object Project{

        const val ALL_SEGMENT = "ALL_SEGMENT"
        const val TASKS = "TASKS"
        const val SEGMENT = "SEGMENTS"
        const val CONTRIBUTERS = "CONTRIBUTERS"
        const val PROJECTID = "PROJECT_ID"
        const val PROJECTNAME = "PROJECT_NAME"
        const val TAGS="TAGS"

    }

    object Notifications{
        const val NOTIFICATION_TIME_STAMP = "NOTIF_LAST_SEEN"
        const val NOTIFICATIONS = "NOTIFICATIONS"

    }


    object SEGMENT{
        const val TASKS = "TASKS"
        const val ASSIGNEE = "ASSIGNEE"
        const val ASSIGNER = "ASSIGNER"
        const val DEADLINE = "DEADLINE"
        const val STATUS = "STATUS"
        const val TITLE = "TITLE"


        const val SEGMENT_NAME="segment_name"
        const val SEGMENT_ID="segment_id"
        const val DESCRIPTION = "DESC"
        const val CONTRIBUTERS = "contributors"
        const val CREATOR="creator"
        const val CREATOR_ID="creator_id"
        const val PROJECT_ID="project_id"
        const val CREATION_DATETIME="creation_datetime"

    }

    object ROOM{

        object NOTIFICATIONS{
            const val NOTIFICATIONS_TABLE = "notifications"
            const val NOTIFICATIONS_DATABASE = "NOTIFICATION-DB"
        }


    }
}