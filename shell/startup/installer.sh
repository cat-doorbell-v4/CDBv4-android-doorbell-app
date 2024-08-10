#!/usr/bin/env bash
adb push enable-wifi-debug.sh /data/adb/service.d/
adb shell chmod +x /data/adb/service.d/enable-wifi-debug.sh
