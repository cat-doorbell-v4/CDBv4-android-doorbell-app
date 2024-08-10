#!/system/bin/sh
# Enable ADB over Wi-Fi on port 5555
setprop service.adb.tcp.port 5555
stop adbd
start adbd
