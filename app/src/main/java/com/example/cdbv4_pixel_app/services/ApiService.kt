package com.example.cdbv4_pixel_app.services

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @POST("{endpoint}")
    suspend fun sendAlert(
        @Path("endpoint") endpoint: String,
        @Body hostname: String
    ): Response<Any>
}
