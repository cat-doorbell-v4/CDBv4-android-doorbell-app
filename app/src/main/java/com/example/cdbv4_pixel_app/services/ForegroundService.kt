package com.example.cdbv4_pixel_app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder

class ForegroundService : Service() {

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "CatDoorbellServiceChannel",
                "Cat Doorbell Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, "CatDoorbellServiceChannel")
                .setContentTitle("Cat Doorbell Running")
                .setContentText("Listening for your cat...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)  // Use a built-in icon
                .build()
        } else {
            Notification.Builder(this)
                .setContentTitle("Cat Doorbell Running")
                .setContentText("Listening for your cat...")
                .setSmallIcon(android.R.drawable.ic_menu_info_details)  // Use a built-in icon
                .build()
        }

        startForeground(1, notification)

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
