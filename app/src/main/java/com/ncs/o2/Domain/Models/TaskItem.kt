package com.ncs.o2.Domain.Models

import android.graphics.Color
import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude


// This model is being used in the home page
data class TaskItem(
    val title: String = "",
    var id: String="",
    val assignee_id:String="",
    val difficulty: Int = 0,
    val timestamp: Timestamp?=null,
    val completed:Boolean=false,
    val tagList: List<String>,
    val last_updated:Timestamp? = null

    ) {
    @Exclude
    fun getDifficultyColor(): Int {
        when (difficulty) {
            1 -> return Color.GREEN
            2 -> return Color.YELLOW
            3 -> return Color.RED
            else -> return Color.BLACK
        }
    }
    @Exclude
    fun getDifficultyString(): String {
        when (difficulty) {
            1 -> return "E"
            2 -> return "M"
            3 -> return "H"
            else -> return "N/A"
        }
    }
}
