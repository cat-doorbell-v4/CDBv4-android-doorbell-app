package com.example.cdbv4_pixel_app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.provider.Settings
import android.util.Log

class LaunchService : Service() {

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            val settingsIntent = Intent(Settings.ACTION_SETTINGS)
            settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(settingsIntent)
            Log.d("LaunchService", "Launched Settings from service")
        } catch (e: Exception) {
            Log.e("LaunchService", "Error launching Settings: ${e.message}")
        }

        // Stop the service once the task is done
        stopSelf()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
