package com.ncs.o2.Hilt

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.preference.PreferenceManager.getDefaultSharedPreferences
import androidx.navigation.NavInflater
import com.ncs.o2.HelperClasses.Navigator
import com.ncs.o2.O2Application
import com.ncs.o2.Domain.Utility.GlobalUtils
import com.ncs.o2.HelperClasses.PrefManager
import com.ncs.versa.Constants.Endpoints
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
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

    @Provides
    fun providesEasyElements( context : Activity):
            GlobalUtils.EasyElements{
        return GlobalUtils.EasyElements(context)
    }


    @Provides
    fun provideNavigator(context:Activity): Navigator {
        return Navigator(context)
    }


    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return context.getSharedPreferences(Endpoints.SharedPref.SHAREDPREFERENCES, Context.MODE_PRIVATE)
    }

}