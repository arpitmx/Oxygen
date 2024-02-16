package com.ncs.o2.Domain.Repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ncs.o2.Domain.Interfaces.AuthRepository
import com.ncs.o2.Domain.Models.ServerResult
import com.ncs.o2.Domain.Utility.FirebaseUtils.awaitt
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

/*
File : FirebaseAuthRepository.kt -> com.ncs.o2.Domain.Repositories
Description : Repository for Firebase Auth

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 3:04 pm on 21/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE :
Tasks FUTURE ADDITION : 

*/

class FirebaseAuthRepository @Inject constructor(val firebaseAuth: FirebaseAuth) : AuthRepository {

    override val currentUser: FirebaseUser
        get() = firebaseAuth.currentUser!!

    override suspend fun login(
        email: String,
        password: String
    ): ServerResult<FirebaseUser> {

        Timber.tag("Auth Repository").d("Login try ${email} ${password}")

        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            Timber.tag("Auth Repository").d("Login try - success ${result.user?.email}")
            ServerResult.Success(result.user!!)

        } catch (e: Exception) {
            Timber.tag("Auth Repository").d("Login try - failure ${e.message}")
            e.printStackTrace()
            ServerResult.Failure(e)
        }
    }

    override suspend fun register(
        email: String,
        password: String
    ): ServerResult<FirebaseUser> {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).awaitt()
            return ServerResult.Success(result.user!!)
        } catch (e: Exception) {
            e.printStackTrace()
            ServerResult.Failure(e)
        }
    }

    //result.user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build())?.awaitt()


    override fun logout() {
        firebaseAuth.signOut()
    }
}