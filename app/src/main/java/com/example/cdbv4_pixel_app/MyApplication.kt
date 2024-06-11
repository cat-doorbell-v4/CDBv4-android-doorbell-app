package com.example.cdbv4_pixel_app

import android.app.Application

class MyApplication : Application() {

    companion object {
        var deviceName: String? = null
    }

    override fun onCreate() {
        super.onCreate()
        deviceName = DeviceUtils.getDeviceName(this)
    }
}
