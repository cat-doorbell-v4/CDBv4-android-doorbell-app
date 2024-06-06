package com.example.cdbv4_pixel_app.services

import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraManager
import androidx.core.content.ContextCompat

class FlashlightService(private val context: Context) {
    private val cameraManager: CameraManager by lazy {
        ContextCompat.getSystemService(context, CameraManager::class.java) as CameraManager
    }

    @Volatile
    private var isTorchOn = false

    @Synchronized
    fun turnOn() {
        try {
            if (!isTorchOn) {
                cameraManager.setTorchMode(getCameraId(), true)
                isTorchOn = true
            }
        } catch (e: CameraAccessException) {
            // Handle exception
        }
    }

    @Synchronized
    fun turnOff() {
        try {
            if (isTorchOn) {
                cameraManager.setTorchMode(getCameraId(), false)
                isTorchOn = false
            }
        } catch (e: CameraAccessException) {
            // Handle exception
        }
    }

    private fun getCameraId(): String {
        return cameraManager.cameraIdList[0] // Assumes the first camera is the one to use
    }
}
