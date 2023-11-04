package com.ncs.o2.Domain.Interfaces

import com.google.firebase.auth.FirebaseUser
import com.ncs.o2.Domain.Models.ServerResult

/*
File : AuthRepository.kt -> com.ncs.o2.Domain.Interfaces
Description : Auth repository interface

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:05 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

interface AuthRepository {

    val currentUser: FirebaseUser?
    suspend fun login(email: String, password: String): ServerResult<FirebaseUser>
    suspend fun register(email: String, password: String): ServerResult<FirebaseUser>
    fun logout()

}