package com.ncs.o2.Hilt

import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.UseCases.CreateTaskUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/*
File : UserCasesModule.kt -> com.ncs.o2.Hilt
Description : Module for use cases 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 7:32 pm on 06/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/

@InstallIn(SingletonComponent::class)
@Module
object UserCasesModule {

    @Singleton
    @Provides
    fun provideCreateTaskUseCase(repository: FirestoreRepository):CreateTaskUseCase{
        return CreateTaskUseCase(repository)
    }
}