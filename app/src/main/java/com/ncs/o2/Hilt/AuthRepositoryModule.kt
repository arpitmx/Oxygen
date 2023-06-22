package com.ncs.o2.Hilt


import com.ncs.o2.Domain.Interfaces.AuthRepository
import com.ncs.o2.Domain.Repositories.FirebaseAuthRepository
import com.ncs.o2.Domain.Utility.FirebaseAuthorizationRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
File : RepositoryModule.kt -> com.ncs.o2.Hilt
Description : Module for providing Repositories 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 9:21 am on 07/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
@Module
@InstallIn(SingletonComponent::class)
abstract class AuthRepositoryModule {

    @Singleton
    @Binds
    @FirebaseAuthorizationRepository
    abstract fun bindsAuthRepository(firestoreAuthRepository: FirebaseAuthRepository): AuthRepository


}