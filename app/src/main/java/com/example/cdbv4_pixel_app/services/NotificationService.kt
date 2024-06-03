package com.example.cdbv4_pixel_app.services

import android.content.Context

class NotificationService(private val context: Context) {

    fun sendNotification(onNotificationSent: () -> Unit) {
        // Implement notification sending logic via AWS API Gateway
        // Call onNotificationSent() once the notification is sent
    }
}
