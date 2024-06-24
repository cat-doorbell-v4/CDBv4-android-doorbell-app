#!/usr/bin/env bash
 export PATH=~/android/platform-tools:${PATH}

# For Pixel 3XL
rel=$(adb -s 192.168.1.21:5555 shell getprop ro.build.version.release)
api=$(adb -s 192.168.1.21:5555 shell getprop ro.build.version.sdk)
patch=$(adb -s 192.168.1.21:5555 shell getprop ro.build.version.security_patch)

echo "Pixel 3XL - Android version: ${rel} SDK: ${api} Patch level:${patch}"

# For Pixel 4XL
rel=$(adb -s 192.168.1.23:5555 shell getprop ro.build.version.release)
api=$(adb -s 192.168.1.23:5555 shell getprop ro.build.version.sdk)
patch=$(adb -s 192.168.1.23:5555 shell getprop ro.build.version.security_patch)

echo "Pixel 4XL - Android version: ${rel} SDK: ${api} Patch level:${patch}"
