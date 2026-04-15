package com.hl46000.hlfaker;

import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.provider.Settings;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * FakeDebugFlags - Hooks debug-related APIs to hide emulator/debuggable characteristics.
 * 
 * This class intercepts:
 * - ApplicationInfo.FLAG_DEBUGGABLE checks
 * - Settings.Secure/Global.ADB_ENABLED checks
 * 
 * All flags return non-debug/non-emulator values to avoid detection.
 */
public class FakeDebugFlags {
    
    // Default values - always return non-debuggable values
    private static final int DEFAULT_FLAG_DEBUGGABLE = 0; // NOT debuggable
    private static final int DEFAULT_ADB_ENABLED = 0; // ADB disabled
    
    // Settings key constants (not available in all API levels)
    private static final String SECURE_DEBUG_APP = "debug_app";
    private static final String SECURE_WAIT_FOR_DEBUGGER = "wait_for_debugger";
    private static final String GLOBAL_DEBUG_APP = "debug_app";
    private static final String GLOBAL_WAIT_FOR_DEBUGGER = "wait_for_debugger";
    private static final String GLOBAL_DEVICE_PROVISIONED = "device_provisioned";
    private static final String DEVELOPMENT_SETTINGS_ENABLED = "development_settings_enabled";
    
    public FakeDebugFlags(LoadPackageParam loadPkgParam) {
        hookApplicationInfo(loadPkgParam);
        hookSettingsSecure(loadPkgParam);
        hookSettingsGlobal(loadPkgParam);
    }
    
    /**
     * Hooks ApplicationInfo.flags to hide FLAG_DEBUGGABLE
     */
    private void hookApplicationInfo(LoadPackageParam loadPkgParam) {
        try {
            // Hook ApplicationInfo constructor to modify flags field
            XposedHelpers.findAndHookMethod("android.content.pm.ApplicationInfo", 
                    loadPkgParam.classLoader, "<init>", new XC_MethodHook() {
                
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    ApplicationInfo appInfo = (ApplicationInfo) param.thisObject;
                    
                    // Check if FLAG_DEBUGGABLE is set and remove it
                    if ((appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                        appInfo.flags &= ~ApplicationInfo.FLAG_DEBUGGABLE;
                    }
                }
            });
            
            // Hook flags field access for Build class (some detection code checks Build flags)
            // PackageParser was removed in Android 13 (API 33) — guard accordingly
            if (loadPkgParam.packageName.equals("android") && android.os.Build.VERSION.SDK_INT < 33) {
                XposedHelpers.findAndHookMethod("android.content.pm.PackageParser",
                        loadPkgParam.classLoader, "parseApplication",
                        XposedHelpers.findClass("android.content.pm.PackageParser$Package", loadPkgParam.classLoader),
                        android.content.res.Resources.class,
                        org.xmlpull.v1.XmlPullParser.class,
                        android.util.AttributeSet.class,
                        int.class,
                        new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        ApplicationInfo appInfo = (ApplicationInfo) param.getResult();
                        if (appInfo != null && (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                            appInfo.flags &= ~ApplicationInfo.FLAG_DEBUGGABLE;
                        }
                    }
                });
            }

        } catch (Throwable t) {
            XposedBridge.log("FakeDebugFlags ApplicationInfo ERROR: " + t);
        }
    }
    
    /**
     * Hooks Settings.Secure to fake ADB_ENABLED and other debug settings
     */
    private void hookSettingsSecure(LoadPackageParam loadPkgParam) {
        try {
            // Hook Settings.Secure.getInt()
            XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", 
                    loadPkgParam.classLoader, "getInt", 
                    android.content.ContentResolver.class, String.class, int.class,
                    new XC_MethodHook() {
                
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[1];
                    if (key == null) return;
                    
                    // ADB_ENABLED - Return disabled (0)
                    if (key.equals("adb_enabled")) {
                        param.setResult(DEFAULT_ADB_ENABLED);
                        return;
                    }
                    
                    // DEVELOPMENT_SETTINGS_ENABLED - Return disabled (0)
                    if (key.equals("development_settings_enabled")) {
                        param.setResult(0);
                        return;
                    }
                    
                    // DEBUG_APP - Return null/empty
                    if (key.equals(SECURE_DEBUG_APP)) {
                        param.setResult(DEFAULT_ADB_ENABLED);
                        return;
                    }
                    
                    // WAIT_FOR_DEBUGGER - Return disabled (0)
                    if (key.equals(SECURE_WAIT_FOR_DEBUGGER)) {
                        param.setResult(0);
                        return;
                    }
                }
            });
            
            // Hook Settings.Secure.getString()
            XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", 
                    loadPkgParam.classLoader, "getString", 
                    android.content.ContentResolver.class, String.class,
                    new XC_MethodHook() {
                
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[1];
                    if (key == null) return;
                    
                    // DEBUG_APP - Return empty string
                    if (key.equals(SECURE_DEBUG_APP)) {
                        param.setResult("");
                        return;
                    }
                }
            });
            
        } catch (Throwable t) {
            XposedBridge.log("FakeDebugFlags Settings.Secure ERROR: " + t);
        }
    }
    
    /**
     * Hooks Settings.Global to fake ADB_ENABLED and other global debug settings
     */
    private void hookSettingsGlobal(LoadPackageParam loadPkgParam) {
        try {
            // Only available on API 17+
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                return;
            }
            
            // Hook Settings.Global.getInt()
            XposedHelpers.findAndHookMethod("android.provider.Settings.Global", 
                    loadPkgParam.classLoader, "getInt", 
                    android.content.ContentResolver.class, String.class, int.class,
                    new XC_MethodHook() {
                
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[1];
                    if (key == null) return;
                    
                    // ADB_ENABLED - Return disabled (0)
                    if (key.equals("adb_enabled")) {
                        param.setResult(DEFAULT_ADB_ENABLED);
                        return;
                    }
                    
                    // DEVELOPMENT_SETTINGS_ENABLED - Return disabled (0)
                    if (key.equals("development_settings_enabled")) {
                        param.setResult(0);
                        return;
                    }
                    
                    // DEBUG_APP - Return disabled (0)
                    if (key.equals(GLOBAL_DEBUG_APP)) {
                        param.setResult(0);
                        return;
                    }
                    
                    // WAIT_FOR_DEBUGGER - Return disabled (0)
                    if (key.equals(GLOBAL_WAIT_FOR_DEBUGGER)) {
                        param.setResult(0);
                        return;
                    }
                    
                    // DEVICE_PROVISIONED - Return provisioned (1)
                    if (key.equals(GLOBAL_DEVICE_PROVISIONED)) {
                        param.setResult(1);
                        return;
                    }
                }
            });
            
            // Hook Settings.Global.getString()
            XposedHelpers.findAndHookMethod("android.provider.Settings.Global", 
                    loadPkgParam.classLoader, "getString", 
                    android.content.ContentResolver.class, String.class,
                    new XC_MethodHook() {
                
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    String key = (String) param.args[1];
                    if (key == null) return;
                    
                    // DEBUG_APP - Return empty string
                    if (key.equals(GLOBAL_DEBUG_APP)) {
                        param.setResult("");
                        return;
                    }
                }
            });
            
        } catch (Throwable t) {
            XposedBridge.log("FakeDebugFlags Settings.Global ERROR: " + t);
        }
    }
}
