package com.ncs.o2.Hilt

import android.app.Application
import com.ncs.o2.BuildConfig
import com.ncs.o2.Services.NotificationApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

/*
File : ApiModule.kt -> com.ncs.o2.Hilt
Description : Module for providing Api classes 

Author : Alok Ranjan (VC uname : apple)
Link : https://github.com/arpitmx
From : Bitpolarity x Noshbae (@Project : O2 Android)

Creation : 4:26 am on 02/06/23

Todo >
Tasks CLEAN CODE : 
Tasks BUG FIXES : 
Tasks FEATURE MUST HAVE : 
Tasks FUTURE ADDITION : 

*/
@Module
@InstallIn(SingletonComponent::class)
object ApiModule {


    @Provides
    @Singleton
    fun provideOkHTTPClient():OkHttpClient{
        val interceptor = HttpLoggingInterceptor()
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder().addInterceptor(interceptor)
            .addInterceptor(Interceptor { chain: Interceptor.Chain ->
                val request: Request = chain.request()
                chain.proceed(request)
            }).build()
    }


    @Provides
    @Singleton
    fun getApiService (okkHttpClient: OkHttpClient): NotificationApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.FCM_BASE_URL)
            .client(okkHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NotificationApiService::class.java)
    }


}