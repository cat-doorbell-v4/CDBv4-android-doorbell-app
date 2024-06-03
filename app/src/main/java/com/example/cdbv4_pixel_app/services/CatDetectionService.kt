package com.example.cdbv4_pixel_app.services

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CatDetectionService {

    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://h3togpa399.execute-api.us-east-1.amazonaws.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun sendCatAlert(message: String) {
        val requestBody = RequestBody(message = message, timestamp = System.currentTimeMillis())

        GlobalScope.launch(Dispatchers.IO) {
            try {
                val response = apiService.sendAlert(requestBody)
                if (response.isSuccessful) {
                    // Handle successful response
                    println("Alert sent successfully!")
                } else {
                    // Handle error response
                    println("Failed to send alert: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                // Handle exception
                println("Exception occurred: ${e.message}")
            }
        }
    }
}
