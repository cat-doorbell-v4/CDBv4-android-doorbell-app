#!/usr/bin/env bash
export PATH=~/android/platform-tools:${PATH}

adb -s 9C121FFBA0028E reboot recovery
adb -s 9C121FFBA0028E shell am broadcast -a android.intent.action.MASTER_CLEAR
