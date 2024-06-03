package com.example.cdbv4_pixel_app.services

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST(Constants.BASE_URL)
    suspend fun sendAlert(@Body requestBody: com.example.cdbv4_pixel_app.services.RequestBody): Response<ResponseBody>
}
