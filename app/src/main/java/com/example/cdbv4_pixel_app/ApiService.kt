package com.example.cdbv4_pixel_app

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("https://h3togpa399.execute-api.us-east-1.amazonaws.com/prod1/ring")
    suspend fun sendAlert(@Body requestBody: RequestBody): Response<ResponseBody>
}
