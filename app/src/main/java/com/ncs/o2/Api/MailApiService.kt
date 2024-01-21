package com.ncs.o2.Api

import com.google.gson.JsonObject
import com.ncs.o2.Domain.Models.Mail
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST


interface MailApiService {
    @Headers("Content-Type: application/json")
    @POST("/sendmail")
    suspend fun sendOnboardingMail(@Body payload: Mail): Response<JsonObject>
}