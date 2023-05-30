package com.ncs.o2.Hilt

import android.content.Context
import com.ncs.o2.Utility.GlobalUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Scope
import javax.inject.Singleton

/*
File : UtilModule.kt -> com.ncs.o2.Hilt
Description : Hilt module for utils  

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 2:02 am on 31/05/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/

@Module
@InstallIn(ActivityComponent::class)
object UtilModule {

    @Singleton
    @Provides
    fun providesEasyElements(context : Context):
            GlobalUtils.EasyElements{
        return GlobalUtils.EasyElements(context)
    }


}