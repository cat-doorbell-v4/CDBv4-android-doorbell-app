package com.example.cdbv4_pixel_app.services

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.RequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationService(private val context: Context) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://your-api-gateway-url/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ApiService::class.java)

    fun sendNotification(onNotificationSent: () -> Unit) {
        val requestBody = RequestBody.create(
            MediaType.parse("application/json"),
            "{\"message\":\"Cat detected\"}"
        )

        GlobalScope.launch {
            val response = service.sendAlert(requestBody)
            // Handle response if needed
            onNotificationSent()
        }
    }
}
