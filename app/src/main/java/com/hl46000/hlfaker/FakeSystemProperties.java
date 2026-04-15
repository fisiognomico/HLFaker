package com.hl46000.hlfaker;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * FakeSystemProperties - Hooks android.os.SystemProperties to return fake values
 * for emulator detection properties.
 * 
 * This class intercepts SystemProperties.get() calls and returns values that
 * hide emulator characteristics from detection mechanisms.
 */
public class FakeSystemProperties {
    
    // Default values for system properties
    private static final String DEFAULT_QEMU = "0";
    private static final String DEFAULT_HARDWARE = "jfltexx";
    private static final String DEFAULT_CHARACTERISTICS = "default";
    private static final String DEFAULT_SECURE = "1";
    private static final String DEFAULT_DEBUGGABLE = "0";
    private static final String DEFAULT_NATIVE_BRIDGE = "";
    private static final String DEFAULT_INIT_SVC_QEMU = "";
    private static final String DEFAULT_SERIALNO = "6c0bb208c33b";
    private static final String DEFAULT_BOOTIMAGE_FINGERPRINT = "samsung/jfltexx/jflte:4.3/JSS15J/I9505XXUEML1:user/release-keys";
    
    // USB state defaults (linked to battery plugged state)
    private static final String DEFAULT_USB_PLUGGED = "mtp,adb";
    private static final String DEFAULT_USB_UNPLUGGED = "";
    
    public FakeSystemProperties(LoadPackageParam loadPkgParam) {
        hookSystemProperties(loadPkgParam);
    }
    
    private void hookSystemProperties(LoadPackageParam loadPkgParam) {
        try {
            Class<?> classSysProp = XposedHelpers.findClass("android.os.SystemProperties", loadPkgParam.classLoader);
            
            // Hook SystemProperties.get(String key)
            XposedHelpers.findAndHookMethod(classSysProp, "get", String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if (key == null) return;
                    
                    String result = fakeProperty(key, (String) param.getResult());
                    if (result != null) {
                        param.setResult(result);
                    }
                }
            });
            
            // Hook SystemProperties.get(String key, String def)
            XposedHelpers.findAndHookMethod(classSysProp, "get", String.class, String.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if (key == null) return;
                    
                    String result = fakeProperty(key, (String) param.getResult());
                    if (result != null) {
                        param.setResult(result);
                    }
                }
            });
            
            // Hook SystemProperties.getInt(String key, int def)
            XposedHelpers.findAndHookMethod(classSysProp, "getInt", String.class, int.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if (key == null) return;
                    
                    String result = fakeProperty(key, null);
                    if (result != null) {
                        try {
                            param.setResult(Integer.parseInt(result));
                        } catch (NumberFormatException e) {
                            // Keep original result if parsing fails
                        }
                    }
                }
            });
            
            // Hook SystemProperties.getBoolean(String key, boolean def)
            XposedHelpers.findAndHookMethod(classSysProp, "getBoolean", String.class, boolean.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[0];
                    if (key == null) return;
                    
                    String result = fakeProperty(key, null);
                    if (result != null) {
                        param.setResult("1".equals(result) || "true".equalsIgnoreCase(result));
                    }
                }
            });
            
        } catch (Throwable t) {
            XposedBridge.log("FakeSystemProperties ERROR: " + t);
        }
    }
    
    /**
     * Returns fake value for known emulator detection properties.
     * 
     * @param key The property key
     * @param originalValue The original value (may be null)
     * @return The fake value, or null if property should not be faked
     */
    private String fakeProperty(String key, String originalValue) {
        // ro.kernel.qemu - Primary AVD check
        if (key.equals("ro.kernel.qemu")) {
            return SharedPref.getXValue("QemuMode", DEFAULT_QEMU);
        }
        
        // ro.hardware - Hardware name (goldfish/ranchu/cutf_cvm detection)
        if (key.equals("ro.hardware")) {
            return SharedPref.getXValue("HardwareName", DEFAULT_HARDWARE);
        }
        
        // ro.build.characteristics - Contains "emulator" on AVD
        if (key.equals("ro.build.characteristics")) {
            return SharedPref.getXValue("BuildCharacteristics", DEFAULT_CHARACTERISTICS);
        }
        
        // ro.bootimage.build.fingerprint - Boot image fingerprint
        if (key.equals("ro.bootimage.build.fingerprint") || key.startsWith("ro.bootimage.")) {
            return SharedPref.getXValue("Fingerprint", DEFAULT_BOOTIMAGE_FINGERPRINT);
        }
        
        // ro.secure - Secure mode (0 on userdebug emulators)
        if (key.equals("ro.secure")) {
            return SharedPref.getXValue("SecureMode", DEFAULT_SECURE);
        }
        
        // ro.debuggable - Debuggable flag (1 on userdebug emulators)
        if (key.equals("ro.debuggable")) {
            return SharedPref.getXValue("Debuggable", DEFAULT_DEBUGGABLE);
        }
        
        // ro.serialno - Serial number (contains "EMULATOR" on AVD)
        if (key.equals("ro.serialno") || key.equals("ro.boot.serialno") || 
            key.equals("ril.serialnumber") || key.equals("sys.serialnumber")) {
            return SharedPref.getXValue("AndroidSerial", DEFAULT_SERIALNO);
        }
        
        // ro.dalvik.vm.native.bridge - ARM translation layer
        if (key.equals("ro.dalvik.vm.native.bridge") || key.equals("ro.dalvik.vm.isa.arm")) {
            return SharedPref.getXValue("NativeBridge", DEFAULT_NATIVE_BRIDGE);
        }
        
        // init.svc.qemu* - QEMU service detection
        if (key.startsWith("init.svc.qemu") || key.contains("qemu")) {
            return SharedPref.getXValue("InitSvcQemu", DEFAULT_INIT_SVC_QEMU);
        }
        
        // sys.usb.config - USB configuration state (linked to charging state)
        if (key.equals("sys.usb.config")) {
            return getUsbConfigState();
        }
        
        // sys.usb.state - USB state (linked to charging state)
        if (key.equals("sys.usb.state")) {
            return getUsbConfigState();
        }
        
        // waydroid.* properties - Waydroid-specific detection
        if (key.startsWith("waydroid.")) {
            return ""; // Return empty for all waydroid properties
        }
        
        // anbox.* properties - Anbox-specific detection (Waydroid inherits from Anbox)
        if (key.startsWith("anbox.")) {
            return "";
        }
        
        return null; // Property not faked, use original value
    }
    
    /**
     * Returns USB configuration state based on battery plugged state.
     * Mirrors BAT_PLUGGED state as per requirements.
     * 
     * @return USB config string
     */
    private String getUsbConfigState() {
        // Get battery plugged state (0=unplugged, 2=USB)
        String pluggedStr = SharedPref.getXValue("BatteryPlugged", "2");
        try {
            int plugged = Integer.parseInt(pluggedStr);
            if (plugged == 0) {
                return DEFAULT_USB_UNPLUGGED;
            } else {
                // For USB charging (2) or any other state, return USB config
                return DEFAULT_USB_PLUGGED;
            }
        } catch (NumberFormatException e) {
            return DEFAULT_USB_PLUGGED; // Default to plugged
        }
    }
}
