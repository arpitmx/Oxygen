package com.ncs.o2.Constants

object SwitchFunctions {

    fun getNumStateFromStringState(stringState:String) : Int{
        return when(stringState){
            "Submitted" -> 1
            "Open" -> 2
            "Working" -> 3
            "Review" -> 4
            "Completed" -> 5
            else -> 0
        }
    }
    fun getStringStateFromNumState(numState:Int):String{
        return when (numState) {
            1 -> "Submitted"
            2 -> "Open"
            3 -> "Working"
            4 -> "Review"
            5 -> "Completed"
            6 -> "Open"
            else -> "Undefined"
        }
    }
    fun getNumPriorityFromStringPriority(stringPriority:String) : Int{
        return when(stringPriority){
            "Low" -> 1
            "Medium" -> 2
            "High" -> 3
            "Critical" -> 4
            else -> -1
        }
    }
    fun getStringPriorityFromNumPriority(numPriority:Int):String{
         return when (numPriority) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            4 -> "Critical"
            else -> "Undefined"
        }
    }
    fun getNumTypeFromStringType(stringType:String) : Int{
        return when(stringType){
            "Bug" -> 1
            "Feature" -> 2
            "Feature request" -> 3
            "Task" -> 4
            "Exception" -> 5
            "Security" -> 6
            "Performance" -> 7
            else -> 0
        }
    }
    fun getStringTypeFromNumType(numType:Int):String{
        return when (numType) {
            1 -> "Bug"
            2 -> "Feature"
            3 -> "Feature request"
            4 -> "Task"
            5 -> "Exception"
            6 -> "Security"
            7 -> "Performance"
            else -> "Undefined"
        }
    }
    fun getNumDifficultyFromStringDifficulty(stringDifficulty:String) : Int{
        return when(stringDifficulty){
            "Easy" -> 1
            "Medium" -> 2
            "Hard" -> 3
            else -> -1
        }
    }
    fun getStringDifficultyFromNumDifficulty(numDifficulty:Int):String{
        return when (numDifficulty) {
            1 -> "Easy"
            2 -> "Medium"
            3 -> "Hard"
            else -> "Undefined"
        }
    }
}