package com.example.cdbv4_pixel_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cdbv4_pixel_app.statemachine.StateMachine

/*
Todo: Save a local (circular) log for forensics. Keep on UI?
Todo: Alert when running on battery only
Todo: Alert on excessive temperature
Todo: Identify and handle low-light conditions
Todo: Send pic from camera when meow identified, but no cat seen
Todo: Make sure app starts on reboot and also goes to the foreground
Todo: Write significant events to the UI in the form of a circular log
Todo: Is there any way to turn the device buttons off?
Todo: Get log monitoring working
 */


class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private lateinit var stateMachine: StateMachine
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (hasPermissions()) {
            Log.i(TAG, "We have permissions")
            initializeStateMachine()
        } else {
            Log.i(TAG, "We do not yet have permissions")
            requestPermissions()
        }
    }

    private fun hasPermissions(): Boolean {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        return permissions.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    private fun initializeStateMachine() {
        stateMachine = StateMachine(this)
        stateMachine.start()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.i(TAG, "All required permissions are granted")
                initializeStateMachine()
            } else {
                Log.e(TAG, "Permissions NOT granted! Exiting")
                // Handle the case where permissions are not granted
                // Optionally show a message to the user and close the app
                finish() // Optionally close the app if permissions are not granted
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::stateMachine.isInitialized) {
            stateMachine.stop()
        }
    }
}
