package com.example.cdbv4_pixel_app

import android.content.Context
import android.provider.Settings

object DeviceUtils {
    fun getDeviceName(context: Context): String? {
        return Settings.Global.getString(context.contentResolver, Settings.Global.DEVICE_NAME)
    }
}
