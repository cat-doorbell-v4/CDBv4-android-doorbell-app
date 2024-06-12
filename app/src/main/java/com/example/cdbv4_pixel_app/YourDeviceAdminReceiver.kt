package com.example.cdbv4_pixel_app

import android.app.admin.DeviceAdminReceiver
import android.content.Context
import android.content.Intent

class YourDeviceAdminReceiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        // Device admin enabled
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        // Device admin disabled
    }
}
