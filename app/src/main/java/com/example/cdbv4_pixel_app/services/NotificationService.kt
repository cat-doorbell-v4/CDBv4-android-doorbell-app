package com.example.cdbv4_pixel_app.services

import android.content.Context
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationService(
    private val context: Context,
    private val onNotificationSent: (Boolean) -> Unit
) {

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ApiService::class.java)

    fun sendNotification(endpoint: String, hostname: String) {
        GlobalScope.launch {
            val response = service.sendAlert(endpoint, hostname)

            // Handle response if needed
            onNotificationSent(response.isSuccessful)
        }
    }
}
