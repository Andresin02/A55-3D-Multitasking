#!/data/data/com.termux/files/usr/bin/bash
# Read-only inventory. No writes to Android system or settings.
set -u
R="$HOME/.rish_private/run_rish"
[ -x "$R" ] || R="$HOME/rish"
if [ ! -x "$R" ]; then
  echo "ERROR: Rish not found at ~/.rish_private/run_rish or ~/rish"
  exit 1
fi
OUT="$HOME/a55_systemui_audit_$(date +%Y%m%d_%H%M%S).txt"
{
  echo "===== A55 SYSTEMUI READ-ONLY AUDIT ====="
  date
  sh "$R" -c '
    echo "-- identity --"; id
    echo "-- build fingerprint --"
    getprop ro.product.model
    getprop ro.product.device
    getprop ro.build.version.release
    getprop ro.build.version.sdk
    getprop ro.build.display.id
    getprop ro.build.fingerprint
    echo "-- SystemUI paths --"
    pm path com.android.systemui
    echo "-- package metadata --"
    dumpsys package com.android.systemui | grep -E "versionName=|versionCode=|codePath=|pkgFlags=|privateFlags=" | head -n 40
    echo "-- boot security --"
    getprop ro.boot.verifiedbootstate
    getprop ro.boot.flash.locked
    getenforce
    echo "-- available paths --"
    for p in /system/priv-app/SystemUI /system_ext/priv-app/SystemUI /product/priv-app/SystemUI; do
      [ ! -e "$p" ] || ls -ld "$p"
    done
  '
} 2>&1 | tee "$OUT"
echo "Saved report: $OUT"
