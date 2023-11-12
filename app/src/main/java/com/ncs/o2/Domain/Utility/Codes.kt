package com.ncs.o2.Domain.Utility

import com.google.firebase.storage.StorageReference
import kotlin.String

object Codes {


    object STRINGS {

//        var NAME: StorageReference = TODO()
        const val TIME_ZONE_INDIA = "Asia/Kolkata"
        const val INTENT_ERROR_CODE = "errorCode"
        var isMaintaining = ""
        var maintaninDesc = ""
        var segmentText = ""
    }

    object Status {
        const val RESULT_OK = 200
        const val RESULT_FAILED = 400
        const val VALID_INPUT = "Valid"
    }

    sealed class Error {

        abstract val code: String
        abstract val fullcode: String
        abstract val description: String
        abstract val solution: String
        abstract val actionText: String


        class ERR_CODE_NULL : Error() {
            override val code: String = "FAULTY_CODE"
            override val fullcode: String = "Error code : FAULTY_ERROR_CODE"
            override val description: String = "Fault in unknown error code"
            override val solution: String = common_solution
            override val actionText: String = common_action_restart
        }


        sealed class TrueTimeError : Error() {

            //All errors codes in TrueTime will starts with TT1

            class UNINITIALIZED_ERR : TrueTimeError() {
                override val code: String = "TT102"
                override val fullcode: String = "Error code : TT102"
                override val description: String = "TrueTime initialization error"
                override val solution: String = "Check connection and restart the app"
                override val actionText: String = common_action_restart


            }

            class WRONG_TIME_ZONE_ERR : TrueTimeError() {
                override val code: String = "TT103"
                override val fullcode: String = "Error code : TT103"
                override val description: String =
                    "Wrong TimeZone in Time settings"
                override val solution: String =
                    "Change the timezone from time settings, correct is ASIA/KOLKATA (IST)"
                override val actionText: String = common_action_okay


            }
        }


        sealed class NetworkError : Error() {
            //All errors codes in TrueTime will starts with TT1

            class NO_CONNECTION_ERR : NetworkError() {
                override val code: String = "TT101"
                override val fullcode: String = "Error code : TT101"
                override val description: String = "Network connection error"
                override val solution: String = "Check your internet connection and try again."
                override val actionText: String
                    get() = common_action_okay
            }
        }

        companion object {
            //Fault errors codes in TrueTime start with F
            const val common_solution = "Restart the app"
            const val common_action_restart = "Restart Versa"
            const val common_action_okay = "Okay"


        }


    }


}