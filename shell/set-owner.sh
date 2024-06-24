#!/usr/bin/env bash

 export PATH=~/android/platform-tools:${PATH}

 adb -s 9C121FFBA0028E shell dpm set-device-owner com.example.cdbv4_pixel_app/.DoorbellDeviceAdminReceiver
