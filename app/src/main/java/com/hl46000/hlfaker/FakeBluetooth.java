package com.hl46000.hlfaker;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * FakeBluetooth - Hooks Bluetooth-related APIs to return fake values.
 * 
 * This class intercepts BluetoothAdapter and BluetoothManager calls to hide
 * emulator characteristics. When Bluetooth is disabled on a real device,
 * most methods return null or indicate OFF state.
 * 
 * Default values (from R.string resources):
 * - BluetoothName: "Pixel 6a"
 * - BluetoothAddress: "" (empty, unavailable when disabled)
 * - BluetoothEnabled: "false"
 * 
 * Note: On a real Pixel 6a device, even when Bluetooth is OFF, the BluetoothManager
 * returns State: BLE_ON. However, we ignore this and return everything OFF as specified.
 */
public class FakeBluetooth {
    
    // BluetoothAdapter state constants
    private static final int STATE_OFF = 10;
    private static final int STATE_ON = 12;
    private static final int STATE_TURNING_ON = 11;
    private static final int STATE_TURNING_OFF = 13;
    
    // Default values - matching R.string resources
    private static final String DEFAULT_BLUETOOTH_NAME = "Pixel 6a";
    private static final String DEFAULT_BLUETOOTH_ADDRESS = "";
    private static final String DEFAULT_BLUETOOTH_ENABLED = "false";
    
    public FakeBluetooth(LoadPackageParam loadPkgParam) {
        hookBluetoothAdapter(loadPkgParam);
        hookBluetoothManager(loadPkgParam);
    }
    
    /**
     * Hooks BluetoothAdapter methods to return fake values.
     */
    private void hookBluetoothAdapter(LoadPackageParam loadPkgParam) {
        try {
            // Hook BluetoothAdapter.isEnabled()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", 
                    loadPkgParam.classLoader, "isEnabled", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Always return false (disabled)
                    param.setResult(false);
                }
            });
            
            // Hook BluetoothAdapter.isDiscovering()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", 
                    loadPkgParam.classLoader, "isDiscovering", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Always return false (not discovering)
                    param.setResult(false);
                }
            });
            
            // Hook BluetoothAdapter.getState()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", 
                    loadPkgParam.classLoader, "getState", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Always return STATE_OFF (10)
                    param.setResult(STATE_OFF);
                }
            });
            
            // Hook BluetoothAdapter.getName()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", 
                    loadPkgParam.classLoader, "getName", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Return configured name or default
                    String name = SharedPref.getXValue("BluetoothName", DEFAULT_BLUETOOTH_NAME);
                    param.setResult(name);
                }
            });
            
            // Hook BluetoothAdapter.getAddress()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", 
                    loadPkgParam.classLoader, "getAddress", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Return null or empty when Bluetooth is disabled
                    // On real devices, address is unavailable when Bluetooth is off
                    String address = SharedPref.getXValue("BluetoothAddress", DEFAULT_BLUETOOTH_ADDRESS);
                    if (address == null || address.isEmpty()) {
                        param.setResult(null);
                    } else {
                        param.setResult(address);
                    }
                }
            });
            
            // Hook BluetoothAdapter.getDefaultAdapter() - return null or fake adapter
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", 
                    loadPkgParam.classLoader, "getDefaultAdapter", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    // Check if we should return null based on BluetoothEnabled setting
                    String enabled = SharedPref.getXValue("BluetoothEnabled", DEFAULT_BLUETOOTH_ENABLED);
                    if ("false".equals(enabled)) {
                        // Return null to simulate no Bluetooth adapter available
                        param.setResult(null);
                    }
                }
            });
            
        } catch (Throwable t) {
            XposedBridge.log("FakeBluetooth Adapter ERROR: " + t);
        }
    }
    
    /**
     * Hooks BluetoothManager methods to return fake values.
     */
    private void hookBluetoothManager(LoadPackageParam loadPkgParam) {
        try {
            // Hook BluetoothManager.getAdapter()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothManager", 
                    loadPkgParam.classLoader, "getAdapter", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Check if we should return null based on BluetoothEnabled setting
                    String enabled = SharedPref.getXValue("BluetoothEnabled", DEFAULT_BLUETOOTH_ENABLED);
                    if ("false".equals(enabled)) {
                        // Return null to simulate no adapter available
                        param.setResult(null);
                    }
                }
            });
            
            // Hook BluetoothManager.getConnectionState()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothManager", 
                    loadPkgParam.classLoader, "getConnectionState", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Return STATE_DISCONNECTED (0) when Bluetooth is off
                    param.setResult(0);
                }
            });
            
            // Hook BluetoothManager.getConnectedDevices()
            XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothManager", 
                    loadPkgParam.classLoader, "getConnectedDevices", int.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Return empty list when Bluetooth is off
                    param.setResult(new java.util.ArrayList<Object>());
                }
            });
            
        } catch (Throwable t) {
            XposedBridge.log("FakeBluetooth Manager ERROR: " + t);
        }
    }
}
