#!/system/bin/sh

LOG_FILE="/data/local/tmp/99-disable-mac-randomization.log"

echo "Script started at $(date)" > $LOG_FILE

mount -o remount,rw /data >> $LOG_FILE 2>&1
sed -i 's/name="mac_randomization_enabled" value="true"/name="mac_randomization_enabled" value="false"/' /data/misc/wifi/wifi_mac_randomization.xml >> $LOG_FILE 2>&1
mount -o remount,ro /data >> $LOG_FILE 2>&1

echo "Script ended at $(date)" >> $LOG_FILE
