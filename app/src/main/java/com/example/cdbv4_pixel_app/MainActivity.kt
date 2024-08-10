package com.example.cdbv4_pixel_app

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.cdbv4_pixel_app.services.ForegroundService
import com.example.cdbv4_pixel_app.services.LaunchService
import com.example.cdbv4_pixel_app.services.LogcatService
import com.example.cdbv4_pixel_app.statemachine.StateMachine


/*
Todo: Make remotely accessible (i.e auto-set full-time wifi access)
Todo: Convert to AWS IoT device(s)
Todo: Alert when running on battery only
Todo: Alert on excessive temperature
Todo: Send pic from camera when meow identified, but no cat seen (ie false alarms)
*/

class MainActivity : AppCompatActivity() {
    private val PERMISSIONS_REQUEST_CODE = 100
    private lateinit var stateMachine: StateMachine
    private lateinit var wakeLock: PowerManager.WakeLock
    private lateinit var gestureDetector: GestureDetector
    private lateinit var wifiManager: WifiManager


    private val TAG = "MainActivity"

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Request necessary permissions
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                0
            )
        } else {
            disableMacRandomization()
        }
        startLogging()
        checkAndRequestPermissions()
        adminBackdoorUnlock()
        if (isLockTaskPermitted(this)) {
            startLockTask()
        }
        acquireWakeLock()
        setForeground()
        disableKeyguard()
        // Prevent the screen from turning off
        setTurnScreenOn(true)
        setShowWhenLocked(true)
        initializeStateMachine()

        // Set Wi-Fi configuration
        setWifiConfiguration(Constants.WIFI_SSID, Constants.WIFI_PASSWORD)
    }

    private fun setWifiConfiguration(ssid: String, password: String) {
        val wifiConfig = WifiConfiguration()
        wifiConfig.SSID = String.format("\"%s\"", ssid)
        wifiConfig.preSharedKey = String.format("\"%s\"", password)

        val netId = wifiManager.addNetwork(wifiConfig)
        if (netId == -1) {
            Log.e(TAG, "Failed to add network configuration!")
            return
        }

        wifiManager.disconnect()
        wifiManager.enableNetwork(netId, true)
        wifiManager.reconnect()
    }

    private fun disableMacRandomization() {
        // Ensure WiFi is enabled
        if (!wifiManager.isWifiEnabled) {
            wifiManager.isWifiEnabled = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // Android 10+
            try {
                val configuredNetworks = wifiManager.configuredNetworks
                val currentNetwork = wifiManager.connectionInfo.ssid

                for (config in configuredNetworks) {
                    if (config.SSID == currentNetwork) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // Android 11+
                            try {
                                val setMacRandomizationSettingMethod = config.javaClass.getMethod(
                                    "setMacRandomizationSetting", Int::class.javaPrimitiveType
                                )
                                setMacRandomizationSettingMethod.invoke(
                                    config,
                                    0
                                ) // 0 corresponds to WifiConfiguration.RANDOMIZATION_NONE
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else { // Use reflection for older methods
                            try {
                                val field =
                                    config.javaClass.getDeclaredField("macRandomizationSetting")
                                field.isAccessible = true
                                field.setInt(
                                    config,
                                    0
                                ) // 0 corresponds to WifiConfiguration.RANDOMIZATION_NONE
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                        wifiManager.updateNetwork(config)
                        wifiManager.reconnect()
                        break
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun adminBackdoorUnlock() {
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                Log.d("AdminBackdoor", "Double tap detected")

                // Exit kiosk mode
                try {
                    runOnUiThread {
                        stopLockTask()
                        Log.d("AdminBackdoor", "Exited kiosk mode")
                    }

                    // Delay launching the Service to ensure lock task has stopped
                    Handler(Looper.getMainLooper()).postDelayed({
                        try {
                            // Start a service to launch Settings
                            val serviceIntent = Intent(this@MainActivity, LaunchService::class.java)
                            startService(serviceIntent)
                            Log.d("AdminBackdoor", "Started LaunchService")
                        } catch (e: Exception) {
                            Log.e("AdminBackdoor", "Error starting service: ${e.message}")
                        }
                    }, 500) // Delay by 500 milliseconds

                } catch (e: Exception) {
                    Log.e("AdminBackdoor", "Error exiting kiosk mode: ${e.message}")
                }

                return true
            }

            override fun onDoubleTapEvent(e: MotionEvent): Boolean {
                Log.d("AdminBackdoor", "Double tap event detected")
                return super.onDoubleTapEvent(e)
            }
        })

        // Set an onTouchListener on the root view to listen for touch events
        val contentView: View = findViewById(android.R.id.content)
        contentView.setOnTouchListener { v, event ->
            val result = gestureDetector.onTouchEvent(event)
            // Call performClick if the touch event is a click
            if (result) {
                v.performClick()
            }
            result
        }

        // Set a click listener to satisfy the accessibility warning
        contentView.setOnClickListener {
            // Handle click if needed
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        } else {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            applicationContext.startActivity(intent)
        }
    }

    private fun startLogging() {
        // Start the LogcatService
        val serviceIntent = Intent(this, LogcatService::class.java)
        startService(serviceIntent)
        Log.i(TAG, "LogcatService started")
    }

    private fun stopLogging() {
        val serviceIntent = Intent(this, LogcatService::class.java)
        stopService(serviceIntent)
    }

    private fun screenOn() {
        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        filter.addAction(Intent.ACTION_SCREEN_ON)
        val receiver = ScreenReceiver()
        registerReceiver(receiver, filter)

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setForeground() {
        // Start the foreground service
        val serviceIntent = Intent(this, ForegroundService::class.java)
        startForegroundService(serviceIntent)
    }

    private fun acquireWakeLock() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)

        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "MyApp::MyWakelockTag"
        )
        wakeLock.acquire()
    }

    private fun disableKeyguard() {
        val devicePolicyManager =
            getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(this, DoorbellDeviceAdminReceiver::class.java)
        devicePolicyManager.setKeyguardDisabled(componentName, true)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
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

    private fun initializeStateMachine() {
        stateMachine = StateMachine(this)
        stateMachine.start()
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), 1)
        } else {
            Log.d(TAG, "All permissions are already granted")
            setWifiConfiguration(
                Constants.WIFI_SSID,
                Constants.WIFI_PASSWORD
            ) // Set Wi-Fi config if permissions are granted
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Log.d(TAG, "All requested permissions granted")
                setWifiConfiguration(
                    Constants.WIFI_SSID,
                    Constants.WIFI_PASSWORD
                ) // Set Wi-Fi config if permissions are granted
            } else {
                Log.d(TAG, "Some permissions are denied")
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
        stopLogging()
    }
}
