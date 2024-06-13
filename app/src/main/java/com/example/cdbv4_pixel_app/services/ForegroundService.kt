package com.example.cdbv4_pixel_app.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.cdbv4_pixel_app.R
import com.example.cdbv4_pixel_app.statemachine.StateMachine

class ForegroundService : Service() {
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var stateMachine: StateMachine

    override fun onCreate() {
        super.onCreate()
        val notificationChannelId = "CAT_DOORBELL_CHANNEL"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                notificationChannelId,
                "Cat Doorbell Service",
                NotificationManager.IMPORTANCE_MIN
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chan)
        }

        val notification: Notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("")
            .setContentText("")
            .setSmallIcon(R.drawable.transparent_icon) // Use the transparent icon
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .build()

        startForeground(1, notification)

        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "CatDoorbell::WakeLock")
        wakeLock.acquire()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Start your cat listening logic here if needed
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock.release()
        stateMachine.stop()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
