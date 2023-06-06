package com.ncs.o2.Domain.Models

import java.lang.Exception

/*
File : ServerResult.kt -> com.ncs.o2.Models
Description : This is the sealed class for server results  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:20 am on 31/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
sealed class ServerResult<out T> {
    data class Success<out T>(val data : T) : ServerResult<T>()
    object Progress : ServerResult<Nothing>()
    data class Failure(val exception: Exception) : ServerResult<Nothing>()
}