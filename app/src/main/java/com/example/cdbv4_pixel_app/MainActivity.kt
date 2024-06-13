package com.example.cdbv4_pixel_app

import android.Manifest
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.KeyEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.cdbv4_pixel_app.services.ForegroundService
import com.example.cdbv4_pixel_app.statemachine.StateMachine

/*
Todo: Save a local (circular) log for forensics. Keep on UI?
Todo: Alert when running on battery only
Todo: Alert on excessive temperature
Todo: Send pic from camera when meow identified, but no cat seen
Todo: Is there any way to turn the device buttons off?
Todo: Get AWS log monitoring working
ToDo: Put some unique value (build #?) in UI
*/


class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private lateinit var stateMachine: StateMachine
    private lateinit var wakeLock: PowerManager.WakeLock

    private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.R)
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

        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DoorbellDeviceAdminReceiver::class.java)
        devicePolicyManager.setKeyguardDisabled(componentName, true)

        if (isLockTaskPermitted(this)) {
            startLockTask()
        }

        hideSystemUI()

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        // Acquire WakeLock
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock =
            powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::CatDoorbellWakeLock")
        wakeLock.acquire()

        // Set the activity to show when locked and turn the screen on
        setShowWhenLocked(true)
        setTurnScreenOn(true)

        // Dismiss the keyguard if necessary
        val keyguardManager = getSystemService(KEYGUARD_SERVICE) as KeyguardManager
        keyguardManager.requestDismissKeyguard(this, null)

        // Start the foreground service
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        window.insetsController?.let {
            it.hide(WindowInsets.Type.systemBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_POWER -> {
                // Handle button press here
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN, KeyEvent.KEYCODE_POWER -> {
                // Handle button release here
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }


    private fun isLockTaskPermitted(context: Context): Boolean {
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val packageName = context.packageName
        return devicePolicyManager.isLockTaskPermitted(packageName)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
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

    override fun onResume() {
        super.onResume()
        // Ensure lock task mode is re-enabled when activity resumes
        startLockTask()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::stateMachine.isInitialized) {
            stateMachine.stop()
        }
        if (wakeLock.isHeld) {
            wakeLock.release()
        }
    }
}
