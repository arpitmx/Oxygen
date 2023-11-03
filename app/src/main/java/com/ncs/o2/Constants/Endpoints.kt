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

    object User {
        const val PROJECTS = "PROJECTS"
        const val EMAIL = "email"
        const val USERNAME = "username"
        const val BIO = "bio"
        const val DESIGNATION = "designation"
        const val ROLE = "role"

    }

    object Project{

        const val ALL_SEGMENT = "ALL_SEGMENT"
        const val TASKS = "TASKS"
        const val SEGMENT = "SEGMENTS"
        const val CONTRIBUTERS = "CONTRIBUTERS"
        const val PROJECTID = "PROJECT_ID"
        const val PROJECTNAME = "PROJECT_NAME"

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
}