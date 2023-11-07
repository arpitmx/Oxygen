package com.ncs.o2.Constants

import com.google.firebase.storage.StorageReference
import kotlin.String


sealed class Errors {

        object STRINGS {

            var NAME: StorageReference = TODO()
            const val TIME_ZONE_INDIA = "Asia/Kolkata"
            const val INTENT_ERROR_CODE = "errorCode"
        }

        object Status {
            const val RESULT_OK = 200
            const val RESULT_FAILED = 400
            const val VALID_INPUT = "Valid"
        }

        abstract val code: String
        abstract val description: String
        abstract val solution: String
        abstract val actionText: String


        class ERR_CODE_NULL : Errors() {
            override val code: String = "FAULTY_CODE"
            override val description: String = "Fault in unknown error code"
            override val solution: String = common_solution
            override val actionText: String = common_action_restart
        }


        sealed class TrueTimeErrors : Errors() {

            //All errors codes in TrueTime will starts with TT1

            class UNINITIALIZED_ERR : TrueTimeErrors() {
                override val code: String = "TT102"
                override val description: String = "TrueTime initialization error"
                override val solution: String = "Check connection and restart the app"
                override val actionText: String = common_action_restart


            }

            class WRONG_TIME_ZONE_ERR : TrueTimeErrors() {
                override val code: String = "TT103"
                override val description: String =
                    "Wrong TimeZone in Time settings"
                override val solution: String =
                    "Change the timezone from time settings, correct is ASIA/KOLKATA (IST)"
                override val actionText: String = common_action_okay


            }
        }


    sealed class FCMTokenErrors : Errors() {

        object FCM_TOKEN_EXPIRED : FCMTokenErrors() {
            override val code: String = "FCM-001"
            override val description: String = "FCM token has expired."
            override val solution: String = "Generate a new FCM token for the user."
            override val actionText: String = "Refresh Token"
        }

        object FCM_TOKEN_MISSING : FCMTokenErrors() {
            override val code: String = "FCM-002"
            override val description: String = "FCM token is missing."
            override val solution: String = "Generate and save an FCM token for the user."
            override val actionText: String = "Generate Token"
        }

        object FCM_TOKEN_UPDATE_FAILED : FCMTokenErrors() {
            override val code: String = "FCM-003"
            override val description: String = "FCM token update to Firestore failed."
            override val solution: String = "Check network connectivity and try again."
            override val actionText: String = "Retry"
        }
    }


        sealed class AccountErrors: Errors(){
            object ACCOUNT_FIELDS_NULL : AccountErrors(){
                override val code: String ="ACCNT-001"
                override val description: String ="Errors in account, contact developers"
                override val solution: String = "Errors in account field, look for Null fields in database in User's account"
                override val actionText: String = "Send report"
            }


        }

        sealed class NetworkErrors : Errors() {
            //All errors codes in TrueTime will starts with TT1

            object NO_CONNECTION_ERR : NetworkErrors() {
                override val code: String = "NETWRK-001"
                override val description: String = "Check internet connectivity, retry."
                override val solution: String = "Check your internet connection and try again."
                override val actionText: String = "Retry"
            }

            object SERVER_UNAVAILABLE : NetworkErrors() {
                override val code: String = "CON-002"
                override val description: String = "Server is currently unavailable."
                override val solution: String = "Wait for server availability or contact support."
                override val actionText: String = "Contact Support"
            }

            object TIMEOUT_ERROR : NetworkErrors() {
                override val code: String = "CON-003"
                override val description: String = "Request has timed out."
                override val solution: String = "Check your internet connection and retry."
                override val actionText: String = "Retry"
            }
        }

        companion object {
            //Fault errors codes in TrueTime start with F
            const val common_solution = "Restart the app"
            const val common_action_restart = "Restart Versa"
            const val common_action_okay = "Okay"


        }


    }


