package com.example.cdbv4_pixel_app.services

import android.content.Context
import android.hardware.camera2.CameraManager

class FlashlightService(private val context: Context) {

    private val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    private val cameraId = cameraManager.cameraIdList[0] // Assuming the first camera

    fun turnOn() {
        cameraManager.setTorchMode(cameraId, true)
    }

    fun turnOff() {
        cameraManager.setTorchMode(cameraId, false)
    }
}
