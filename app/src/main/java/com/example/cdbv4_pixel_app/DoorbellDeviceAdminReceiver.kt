package com.example.cdbv4_pixel_app

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast

class DoorbellDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val devicePolicyManager =
            context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val componentName = ComponentName(context, DoorbellDeviceAdminReceiver::class.java)
        devicePolicyManager.setLockTaskPackages(componentName, arrayOf(context.packageName))
        Toast.makeText(context, "Device Admin: enabled", Toast.LENGTH_SHORT).show()
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, "Device Admin: disabled", Toast.LENGTH_SHORT).show()
    }
}
