package com.ncs.o2.Api

import com.google.gson.JsonObject
import com.ncs.o2.BuildConfig
import retrofit2.Response

import retrofit2.http.Body
import retrofit2.http.Headers

import retrofit2.http.POST


interface NotificationApiService {
    @Headers("Authorization: key=" + BuildConfig.FCM_SERVER_KEY, "Content-Type: application/json")
    @POST("fcm/send")
   suspend fun sendNotification(@Body payload: JsonObject?): Response<JsonObject>
}