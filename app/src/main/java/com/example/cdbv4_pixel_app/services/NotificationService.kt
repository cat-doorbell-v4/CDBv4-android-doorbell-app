package com.example.cdbv4_pixel_app.services

import android.content.Context
import android.os.Build
import android.util.Log
import com.example.cdbv4_pixel_app.Constants
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class NotificationService(
    private val context: Context,
    private val onNotificationSent: (Boolean) -> Unit
) {
    private val TAG = "NotificationService"

    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service = retrofit.create(ApiService::class.java)

    private fun getDeviceName(): String {
        val model = Build.MODEL
        Log.d(TAG, "Device model: $model")
        return Constants.DEVICE_NAMES[model] ?: model
    }

    fun sendNotification(endpoint: String) {
        val deviceName = getDeviceName()
        Log.i(TAG, "Device Name: $deviceName")

        GlobalScope.launch {
            try {
                val response = service.sendAlert(endpoint, deviceName)
                // Handle response if needed
                onNotificationSent(response.isSuccessful)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to send notification", e)
                onNotificationSent(false)
            }
        }
    }
}
