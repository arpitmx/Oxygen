package com.ncs.versa.Hilt

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ncs.o2.Domain.Interfaces.AuthRepository
import com.ncs.o2.Domain.Interfaces.Repository
import com.ncs.o2.Domain.Repositories.FirebaseAuthRepository
import com.ncs.o2.Domain.Repositories.FirestoreRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
File : FirebaseModule.kt -> com.ncs.versa.Hilt
Description : This is the hilt module for firebase objects injection 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : Versa Android)

Creation : 9:15 am on 24/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

@InstallIn(SingletonComponent::class)
@Module
object FirebaseModule {


    @Singleton
    @Provides
    fun providesFirebaseAuth() : FirebaseAuth{
        return FirebaseAuth.getInstance()
    }

    @Singleton
    @Provides
    fun providesFirebaseFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    @Singleton
    @Provides
    fun provideFirestoreRepository(firestore: FirebaseFirestore): Repository = FirestoreRepository(firestore)



    @Singleton
    @Provides
    fun provideFirebaseAuthRepository(firebaseAuth: FirebaseAuth): AuthRepository = FirebaseAuthRepository(firebaseAuth)



}