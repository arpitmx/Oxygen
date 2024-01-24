package com.ncs.o2.Domain.Models

import com.google.firebase.firestore.Exclude
import java.io.Serializable

data class Update(
    val DOWNLOAD_SIZE :String = "",
    val RELEASE_NOTES : String = "",
    val UPDATE_URL :   String = "",
    val VERSION_CODE : String = "",
    val VERSION_NAME : String = "",
    val CLEAR_MEMORY : Boolean = false,
) : Serializable
