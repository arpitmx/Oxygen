package com.ncs.o2.Domain.Interfaces

/*
File : ServerErrorCallback.kt -> com.ncs.o2.Domain.Interfaces
Description : Interface for error handling from server  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 6:32 am on 15/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

interface ServerErrorCallback {
    fun handleServerException(exceptionMessage : String)
}
