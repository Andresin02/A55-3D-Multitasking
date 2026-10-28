# A55 3D Multitasking v2

Target: Samsung Galaxy A55 SM-A556E, Android 17 / One UI 9.
Access: Termux + Shizuku/Rish, no root.

## Deliverables
- `SwitcherAPK/`: improved Android Studio source. The APK dynamically lists launchable apps and icons, opens selected apps, and offers App info / hide-from-this-view controls. It is a launcher-style multitasking companion, NOT a true system Recents replacement and does not show live snapshots of other apps.
- `SystemUIResearch/`: read-only audit and a firmware analysis checklist. No system files are changed.

## Build APK
1. Open `SwitcherAPK` in Android Studio (recent stable version) with Android SDK Platform 36 and compatible JDK 17.
2. Sync Gradle. The project uses Android Gradle Plugin 8.10.1.
3. Build > Build APK(s).
4. Debug APK output should be under `app/build/outputs/apk/debug/app-debug.apk`.
5. Install normally, then grant no special permissions unless you later add a feature that genuinely needs one.

No compiled APK is included because this environment has no Android SDK/Gradle build tool configured. Do not treat source code as a ready-to-install APK.

## What v2 does
- Enumerates launchable apps through PackageManager.
- Displays app icons and names in horizontally scrollable perspective-like cards.
- Tap opens the selected app.
- Long press opens Android App info.
- Close button hides a card for the current session only (it does not force-stop or kill apps).
- Uses platform animations and light view transforms; this is not a true GPU live-card carousel.

## SystemUI research
Run:
```sh
bash ~/A55_3D_Multitasking_v2/SystemUIResearch/audit_systemui.sh
```
The audit is read-only and saves a report in `$HOME`. Share that report to plan the next step. Shizuku/Rish shell is not root. On stock locked Samsung firmware, directly replacing protected SystemUI is generally not possible with shell access alone. Do not flash rebuilt APKs, disable verified boot, or write to `/system`.

## SystemUI investigation workflow
1. Record exact build fingerprint and `pm path com.android.systemui`.
2. Obtain matching firmware files for that exact build, from a trusted source.
3. Analyze copies offline; retain originals and SHA-256 hashes.
4. Identify Samsung-specific Recents implementation and dependencies. Do not assume AOSP Quickstep files apply.
5. Decide feasibility based on signature checks, bootloader state, SELinux and package protection.
6. Only plan modification after a verified restore path exists. For this no-root device, keep SystemUI work read-only unless a legitimate supported deployment path is established.
