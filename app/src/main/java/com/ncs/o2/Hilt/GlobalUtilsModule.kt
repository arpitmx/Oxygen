package com.ncs.o2.Hilt

import com.ncs.o2.Domain.Repositories.FirestoreRepository
import com.ncs.o2.Domain.UseCases.CreateSegmentUseCase
import com.ncs.o2.Domain.UseCases.CreateTaskUseCase
import com.ncs.o2.Domain.UseCases.LoadSectionsUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Calendar
import javax.inject.Singleton

/*
File : GlobalUtils.kt -> com.ncs.o2.Hilt
Description : DI for global utils  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 1:02 am on 15/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 


*/
@InstallIn(SingletonComponent::class)
@Module
object GlobalUtilsModule {

        @Provides
        @Singleton
        fun provideCalendar():Calendar = Calendar.getInstance()


}

