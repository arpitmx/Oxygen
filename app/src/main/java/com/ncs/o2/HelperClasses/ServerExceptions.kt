package com.ncs.o2.HelperClasses

/*
File : ServerExceptions.kt -> com.ncs.o2.HelperClasses
Description : Exceptions for server result 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 12:18 pm on 11/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
object ServerExceptions : Exception(){

    object duplicateNameException : Exception(){
        val exceptionDescription: String = "Duplicate Name exception"
        val exceptionCode : Int = 1000
    }

    object projectDoesNotExists : Exception(){
        val exceptionDescription: String = "Project Does not exists"
        val exceptionCode : Int = 1200
    }
    object segement_created : Exception(){
        val exceptionDescription: String = "Segment created successfully"
        val exceptionCode : Int = 1400
    }
}