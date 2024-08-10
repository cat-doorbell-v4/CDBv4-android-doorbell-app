#!/usr/bin/env bash

# Check if a device number is provided as an argument
if [ -z "$1" ]; then
  echo "Usage: $0 <device-number>"
  exit 1
fi

# Set the device number from the argument
DEVICE_NUMBER=$1

# Set the PATH for Android platform-tools
export PATH=~/android/platform-tools:${PATH}

# Use the provided device number with adb
adb -s $DEVICE_NUMBER shell dpm set-device-owner com.example.cdbv4_pixel_app/.DoorbellDeviceAdminReceiver
