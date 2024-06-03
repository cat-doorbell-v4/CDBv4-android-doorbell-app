package com.example.cdbv4_pixel_app

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var catDetectionService: CatDetectionService

    private val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA,
        Manifest.permission.INTERNET
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (!allPermissionsGranted()) {
            requestPermissions()
        } else {
            startServices()
        }

        detectCat()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSIONS_REQUEST_CODE)
    }

    private fun startServices() {
        val intent = Intent(this, SoundDetectionService::class.java)
        startService(intent)
    }

    private fun detectCat() {
        // Logic to detect a cat goes here
        // If a cat is detected:
        catDetectionService.sendCatAlert("Cat detected at the door!")
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 123
    }
}
