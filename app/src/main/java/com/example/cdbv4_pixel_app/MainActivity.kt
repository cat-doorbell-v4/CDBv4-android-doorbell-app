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
Todo: Alert when running on battery only
Todo: Alert on excessive temperature
Todo: Send status for keep-alive every x min. Create new state for it.
Todo: Set hostnames on each handset
Todo: Modify the alert message to make sense on SMS
Todo: Identify and handle low-light conditions
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
