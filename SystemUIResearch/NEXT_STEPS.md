# SystemUI analysis checklist — no root

## Current state
The audit script is read-only. Run it and retain its output and the exact build fingerprint. Do not patch or replace the installed package yet.

## After the audit
1. Find the exact matching Samsung firmware for the recorded build fingerprint.
2. Obtain SystemUI and related framework/resource APKs from that exact firmware package.
3. Record SHA-256 hashes and preserve pristine copies.
4. Inspect package manifest, resources, overlays, dependencies, and Recents-related classes offline. Samsung One UI may differ substantially from AOSP Quickstep.
5. Assess platform signature checks, privileged permissions, SELinux, bootloader state, verified boot and a tested restore path before proposing any deployment.
6. With a locked stock bootloader and Shizuku/Rish only, direct replacement of protected SystemUI is generally not feasible. Do not use `pm install` on a rebuilt system package, write to `/system`, disable verified boot or flash modified images.
7. A realistic no-root deliverable is the companion APK. Native Recents replacement remains an investigation until a supported route is proven.
