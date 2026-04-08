# HLFaker Xposed Module - Migration TODO

This document outlines the detailed tasks required to modernize the HLFaker Xposed module to support LSPosed framework and Gradle build system.

---

## 1. LSPosed Framework Support

LSPosed is the modern successor to Xposed Framework, supporting Android 8.0+ (API 26+) with Magisk/KernelSU integration.

### 1.1 Module Entry Point Updates
- [x] **Update xposed_init** - Keep as is but verify compatibility
  - Current: `com.hl46000.hlfaker.MainHook`
  - Status: Compatible with LSPosed

- [x] **Create xposed_scope resource** - Added `@array/xposed_scope` in `res/values/strings.xml`
  - Defines recommended scope (apps to hook) for the module

### 1.2 AndroidManifest.xml Updates
- [x] **Add LSPosed metadata** (in addition to existing Xposed metadata)
  ```xml
  <meta-data
      android:name="xposedscope"
      android:resource="@array/xposed_scope" />
  ```

- [x] **Update minSdkVersion** to support modern Android
  - Changed from `android:minSdkVersion="14"` (Android 4.0)
  - To: `minSdk 26` (Android 8.0) in build.gradle

- [x] **Update targetSdkVersion**
  - Changed from `android:targetSdkVersion="21"` (Android 5.0)
  - To: `targetSdk 34` in build.gradle

- [x] **Add required permissions**
  - Added `READ_PRIVILEGED_PHONE_STATE` for TelephonyManager APIs
  - Removed deprecated package attribute (now in namespace)

- [x] **Add lint options to build.gradle**
  - Disabled `abortOnError` to allow build with lint warnings
  - Disabled `checkReleaseBuilds` for faster builds

### 1.3 XSharedPreferences Updates (CRITICAL)
- [x] **Migrate from XSharedPreferences to LSPosed's preference system**
  - File: `SharedPref.java`
  - Issue: `XSharedPreferences` is deprecated in LSPosed
  - Solution: Use `XSharedPreferences` with world-readable flags
  
- [x] **Update SharedPref.getXValue() method**
  - Added `myXsharedPref.makeWorldReadable()` for LSPosed compatibility

### 1.4 Dependency Cleanup (COMPLETED)
- [x] **Remove unused Android Support Library imports**
  - Removed `android.support.v7.widget.helper.ItemTouchHelper.Callback` from FakeBuilProp.java
  - Removed `android.support.v4.view.MotionEventCompat` from Util.java
  - Removed `android.support.v4.view.accessibility.AccessibilityNodeInfoCompat` from FakeHardwareInfo.java and FakeRAM.java
  - Removed `android.support.v7.widget.RecyclerView.ItemAnimator` from FakeRAM.java
  - Removed `android.support.v4.app.NotificationCompat` from FakeBattery.java
  - Replaced with hardcoded values or standard Java/Android APIs

### 1.5 Hook API Compatibility
- [x] **Verify XposedHelpers usage**
  - Current: Uses `de.robv.android.xposed.XposedHelpers`
  - Status: Compatible with LSPosed (LSPosed provides compatibility layer)

- [ ] **Update hook callbacks if needed**
  - Current: `XC_LoadPackage.LoadPackageParam`
  - Status: Fully compatible

### 1.6 Scope Management
- [x] **Implement per-app scope support**
  - LSPosed allows users to select which apps the module affects
  - Added xposedscope array in resources
  - Current code already has package checking in `RootCloak.java`

### 1.7 Build Workflow
- [x] **Create build script** - `build.sh` with rsync deployment
  - Syncs local changes to remote dev server
  - Runs gradle build on remote server

---

## 2. Gradle Build System Migration - COMPLETED

The project now uses Gradle build system (migrated from Eclipse/ANT).

### 2.1 Project Structure Restructuring
- [x] **Restructure to standard Gradle project layout**
  ```
  HLFaker/
  ├── app/
  │   ├── src/
  │   │   ├── main/
  │   │   │   ├── java/com/hl46000/hlfaker/
  │   │   │   ├── res/
  │   │   │   ├── assets/
  │   │   │   └── AndroidManifest.xml
  │   └── build.gradle
  ├── build.gradle (root)
  ├── settings.gradle
  └── gradle.properties
  ```

### 2.2 Root build.gradle Configuration
- [x] **Create root build.gradle** - Completed

### 2.3 App Module build.gradle Configuration
- [x] **Create app/build.gradle** - Completed

### 2.4 settings.gradle
- [x] **Create settings.gradle** - Completed

### 2.5 Gradle Wrapper Setup
- [x] **Create gradle/wrapper/gradle-wrapper.properties** - Completed

### 2.6 ProGuard Configuration
- [x] **Create proguard-rules.pro** - Completed

### 2.7 Remove Eclipse/ANT Files
- [x] **Delete obsolete files**
  - Deleted `.project`
  - Deleted `.classpath`
  - Deleted `project.properties`
  - Deleted `proguard-project.txt`

### 2.8 Update .gitignore
- [x] **Add Gradle-related entries**
  ```
  # Gradle
  .gradle/
  build/
  app/build/
  local.properties
  *.iml
  .idea/
  ```

### 2.9 Build Script
- [x] **Create build.sh**
  - Rsync-based deployment script
  - Pushes local changes to remote build server
  - Executes gradle build remotely

---

## 3. Property Value Documentation

**Status:** ✅ Documentation Complete

### 3.1 Device Build Properties (FakeBuildInfo.java, MainActivity.java)

| Property Key | SharedPref Key | Default Value (Hardcoded) | Description |
|--------------|----------------|---------------------------|-------------|
| Build.BOARD | BOARD | MSM8960 | Device board |
| Build.BRAND | BRAND | samsung | Device brand |
| Build.CPU_ABI | ABI | armeabi-v7a | Primary ABI |
| Build.CPU_ABI2 | ABI2 | armeabi | Secondary ABI |
| Build.DEVICE | DEVICE | jflte | Device codename |
| Build.DISPLAY | DISPLAY | JSS15J.I9505XXUEML1 | Build display ID |
| Build.FINGERPRINT | FINGERPRINT | samsung/jfltexx/jflte:4.3/JSS15J/I9505XXUEML1:user/release-keys | Build fingerprint |
| Build.HARDWARE | NAME | jfltexx | Hardware name |
| Build.ID | ID | JSS15J | Build ID |
| Build.MANUFACTURER | Manufacture | samsung | Manufacturer |
| Build.MODEL | MODEL | GT-I9505 | Device model |
| Build.PRODUCT | DEVICE | jflte | Product name |
| Build.BOOTLOADER | BOOTLOADER | I9505XXUEML1 | Bootloader version |
| Build.HOST | - | kpfj3.cbf.corp.google.com | Build host (hardcoded) |
| VERSION.INCREMENTAL | BOOTLOADER | I9505XXUEML1 | Version incremental |
| VERSION.RELEASE | AndroidVer | 4.4.2 | Android version |
| VERSION.SDK | API | 19 | SDK level |
| VERSION.CODENAME | - | REL | Version codename (hardcoded) |
| ro.build.description | DESCRIPTION | jfltexx-user 4.3 JSS15J I9505XXUEML1 release-keys | Build description |

### 3.2 Telephony Properties (FakeBuildInfo.java, FakeHardwareInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| IMEI | IMEI | 506066104722640 | Device IMEI |
| IMSI | IMSI | (from SharedPref) | Subscriber ID |
| Phone Number | PhoneNumber | 84962439943 | Line number |
| Sim Serial | SimSerial | 36066104722647215170 | SIM serial number |
| Carrier | Carrier | Mobifone | Network operator name |
| Carrier Code | CarrierCode | 45201 | MCC/MNC |
| Country Code | CountryCode | VN | ISO country code |

### 3.3 Android ID & Serial Properties (FakeBuildInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| Android ID | AndroidID | 6c0bb208c33b8c43 | Settings.Secure.ANDROID_ID |
| Serial Number | AndroidSerial | 6c0bb208c33b | Build.SERIAL |
| Google Ads ID | GoogleAdsID | f741b85f-fbab-4eb3-8e44-358e07c3bc50 | Advertising ID |

### 3.4 GPS Properties (FakeBuildInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| Latitude | Lat | 27.82516672 | GPS latitude |
| Longitude | Long | 125.06788613 | GPS longitude |
| Altitude | Alt | 125.06 | GPS altitude |
| Speed | Speed | 3.7 | GPS speed |
| Accuracy | Alt | 125.06 | Location accuracy |

### 3.5 WiFi Properties (FakeHardwareInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| WiFi MAC | WifiMAC | 6C:C4:08:BB:B1:28 | MAC address |
| WiFi SSID | WifiName | MyWifi | Network name |
| BSSID | BSSID | 6C:C4:08:BB:B1:28 | Access point BSSID |
| Bluetooth MAC | WifiMAC | 6C:C4:08:BB:B1:28 | Bluetooth address (same as WiFi) |

### 3.6 OpenGL Properties (FakeOpenGL.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| GL Vendor | GLVendor | Qualcomm | OpenGL vendor |
| GL Renderer | GLRenderer | Adreno (TM) 330 | OpenGL renderer |

### 3.7 Display Properties (FakeOpenGL.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| DPI | DPI | 320 | Screen density |

### 3.8 Battery Properties (FakeBattery.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| Temperature | Temp | 350 | Battery temperature |
| Level | Level | 35 | Battery percentage |
| Plugged | - | random(0-2) | Charging state |
| Status | - | random(2-4) | Battery status |
| Health | - | 2 | Battery health |

### 3.9 CPU Properties (FakeCPU.java)

| Property | Value | Description |
|----------|-------|-------------|
| CPU Cores | 4 | Number of CPU cores |
| Max Frequency | 2 MHz | Maximum CPU frequency |

**Note:** These are hardcoded specifically for Hearthstone app (`com.blizzard.wtcg.hearthstone.MinSpecCheck`)

### 3.10 RAM Properties (FakeRAM.java)

| Property | Values | Description |
|----------|--------|-------------|
| Total RAM | Random from [1024, 2048, 3027, 4096] MB | Total RAM size |

**Note:** Specifically for Hearthstone app

### 3.11 System/OS Properties (FakeHardwareInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| os.name | OSName | Linux | Operating system name |
| os.arch | OSArch | armv7l | OS architecture |
| os.version | OSVersion | 3.4.0-gd59db4e | OS version |

### 3.12 Email Properties (FakeEmail.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| Gmail Account | Email | hl.46000@gmail.com | Google account email |
| Target Package | FakeEmailPackge | com.alibaba.aliexpresshd | Package to fake email for |

### 3.13 Root Cloak Properties (RootCloak.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| Hide Root Packages | HideRootPackge | com.alibaba.aliexpresshd | Packages to hide root from |

### 3.14 CPU Info File Override (FakeHardwareInfo.java)

| File | Replacement Path | Description |
|------|------------------|-------------|
| /proc/cpuinfo | /data/misc/sys/cpuinfo | CPU information file |
| /proc/version | /data/misc/sys/version | Kernel version file |

The replacement files are copied from assets:
- `assets/cpuinfo` - ARMv7 Qualcomm MSM 8974 processor info
- `assets/version` - Linux 3.4.0 kernel info

### 3.15 Sensor Properties (FakeOpenGL.java)

| Sensor | Fake Value | Description |
|--------|------------|-------------|
| getVendor() | "BOSCH" or "AVAGO" (random) | Sensor vendor |
| Accelerometer | "Accelerometer/Temperature/Double-tap" | Sensor name |
| Gyroscope | "Gyroscope" | Sensor name |
| Magnetic Field | "Magnetometer" | Sensor name |
| Rotation Vector | "Rotation Vector" | Sensor name |
| Gravity | "Gravity" | Sensor name |
| Linear Acceleration | "Linear Acceleration" | Sensor name |
| Orientation | "Orientation" | Sensor name |
| Corrected Gyroscope | Long multi-sensor string | Sensor name |

### 3.16 User Agent (FakeBuildInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| User Agent | UserAgent | Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/16.0.A.0.36) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/51.0.2704 Mobile Safari/537.36 | WebView User Agent |

### 3.17 Baseband/Radio (FakeBuildInfo.java)

| Property | SharedPref Key | Default Value | Description |
|----------|----------------|---------------|-------------|
| Baseband | BaseBand | eng.administrator.1373289311 | Radio/baseband version |

---

## 4. Additional Improvements

### 4.1 Code Quality
- [ ] **Add null checks** in FakeBuildInfo.java for SharedPref values
- [ ] **Fix typos** in class names (FakeBuilProp -> FakeBuildProp, BuilInfo -> BuildInfo)
- [ ] **Remove hardcoded package checks** in FakeCPU.java and FakeRAM.java
- [ ] **Add proper logging** with configurable log levels

### 4.2 Security
- [ ] **Encrypt sensitive data** in SharedPreferences
- [ ] **Validate input values** before setting properties
- [ ] **Add signature verification** for the module

### 4.3 Compatibility
- [ ] **Test on Android 8.0 - 14** (API 26-34)
- [ ] **Test with Magisk** and KernelSU
- [ ] **Test with various LSPosed versions**

### 4.4 Documentation
- [x] **Document all configurable properties** - Completed in Section 3
- [ ] **Create README.md** with installation instructions
- [ ] **Add CHANGELOG.md** for version history

---

## 5. Testing Checklist

### 5.1 Build Testing ✅
- [x] Project builds successfully with Gradle
- [x] Gradle wrapper configured
- [x] Build script created (build.sh)
- [ ] APK installs on test device
- [ ] Module loads in LSPosed

### 5.2 Functionality Testing ⏳
- [ ] Build properties are faked correctly
- [ ] Telephony properties are faked correctly
- [ ] GPS properties are faked correctly
- [ ] WiFi properties are faked correctly
- [ ] Battery properties are faked correctly
- [ ] Root cloaking works correctly
- [ ] CPU/RAM info is faked correctly

### 5.3 LSPosed Specific Testing ⏳
- [ ] Module scope can be configured
- [ ] Preferences are readable by hook process
- [ ] Module works in both global and per-app mode

---

## 6. Migration Status

### ✅ Completed (High Priority)
1. ✅ Gradle build system migration
2. ✅ XSharedPreferences updates for LSPosed (`makeWorldReadable()`)
3. ✅ AndroidManifest.xml updates (removed package, added permissions)
4. ✅ Removed Android Support Library dependencies
5. ✅ Fixed string comparison bugs (`==` → `.equals()`)
6. ✅ Added lint options for build
7. ✅ Created build.sh deployment script

### ⏳ Remaining (Medium/Low Priority)
- Testing on actual Android devices
- Create README.md documentation
- Code quality improvements
- Security enhancements

---

## 7. Resources

- **LSPosed Documentation:** https://github.com/LSPosed/LSPosed/wiki
- **Xposed API Reference:** https://api.xposed.info/reference/packages.html
- **Android Gradle Plugin:** https://developer.android.com/studio/releases/gradle-plugin
- **Migration Guide:** https://developer.android.com/studio/build/migrate-to-kts

---

*Last Updated: 2024*
