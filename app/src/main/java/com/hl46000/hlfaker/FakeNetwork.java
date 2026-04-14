package com.hl46000.hlfaker;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * FakeNetwork - Hooks network-related APIs to return fake values.
 * 
 * This class intercepts WiFi, NetworkInterface, and ConnectivityManager calls
 * to hide emulator characteristics and provide consistent fake network info.
 * 
 * Default values (from R.string resources):
 * - NetworkIP: "192.168.1.100" (avoid 10.0.2.15 emulator IP)
 * - NetworkInterface: "wlan0" (avoid eth0 emulator interface)
 * - NetworkMacOUI: "6C:C4:08" (realistic MAC OUI prefix)
 * - ConnectivityType: "WIFI" (TYPE_WIFI=1, not TYPE_ETHERNET=9)
 * - WifiSSID: "MyWifi"
 * - WifiBSSID: "6C:C4:08:BB:B1:28"
 */
public class FakeNetwork {
    
    // ConnectivityManager constants
    private static final int TYPE_WIFI = 1;
    private static final int TYPE_ETHERNET = 9;
    private static final int TYPE_MOBILE = 0;
    
    // Default values
    private static final String DEFAULT_NETWORK_IP = "192.168.1.100";
    private static final String DEFAULT_NETWORK_INTERFACE = "wlan0";
    private static final String DEFAULT_NETWORK_MAC_OUI = "6C:C4:08";
    private static final String DEFAULT_CONNECTIVITY_TYPE = "WIFI";
    private static final String DEFAULT_WIFI_SSID = "MyWifi";
    private static final String DEFAULT_WIFI_BSSID = "6C:C4:08:BB:B1:28";
    
    public FakeNetwork(LoadPackageParam loadPkgParam) {
        hookWifiInfo(loadPkgParam);
        hookNetworkInterface(loadPkgParam);
        hookConnectivityManager(loadPkgParam);
    }
    
    /**
     * Hooks WifiInfo methods to return fake values.
     */
    private void hookWifiInfo(LoadPackageParam loadPkgParam) {
        try {
            // Hook WifiInfo.getIpAddress() - returns IP as integer
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", 
                    loadPkgParam.classLoader, "getIpAddress", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String ipStr = SharedPref.getXValue("NetworkIP", DEFAULT_NETWORK_IP);
                    int ipInt = ipToInt(ipStr);
                    param.setResult(ipInt);
                }
            });
            
            // Hook WifiInfo.getSSID()
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", 
                    loadPkgParam.classLoader, "getSSID", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String ssid = SharedPref.getXValue("WifiSSID", DEFAULT_WIFI_SSID);
                    // SSID is typically returned with quotes
                    if (!ssid.startsWith("\"")) {
                        ssid = "\"" + ssid + "\"";
                    }
                    param.setResult(ssid);
                }
            });
            
            // Hook WifiInfo.getBSSID()
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", 
                    loadPkgParam.classLoader, "getBSSID", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String bssid = SharedPref.getXValue("WifiBSSID", DEFAULT_WIFI_BSSID);
                    param.setResult(bssid);
                }
            });
            
            // Hook WifiInfo.getMacAddress() - already in FakeHardwareInfo but ensure consistency
            XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", 
                    loadPkgParam.classLoader, "getMacAddress", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Generate MAC from OUI prefix
                    String macOui = SharedPref.getXValue("NetworkMacOUI", DEFAULT_NETWORK_MAC_OUI);
                    String fullMac = macOui + ":BB:B1:28";
                    param.setResult(fullMac);
                }
            });
            
        } catch (Exception e) {
            XposedBridge.log("FakeNetwork WifiInfo ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Hooks NetworkInterface methods.
     */
    private void hookNetworkInterface(LoadPackageParam loadPkgParam) {
        try {
            // Hook NetworkInterface.getByName()
            XposedHelpers.findAndHookMethod("java.net.NetworkInterface", 
                    loadPkgParam.classLoader, "getByName", String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String requestedName = (String) param.args[0];
                    String fakeInterface = SharedPref.getXValue("NetworkInterface", DEFAULT_NETWORK_INTERFACE);
                    
                    // If requesting emulator interface (eth0), redirect to fake interface
                    if ("eth0".equals(requestedName)) {
                        param.args[0] = fakeInterface;
                    }
                }
            });
            
            // Hook NetworkInterface.getName()
            XposedHelpers.findAndHookMethod("java.net.NetworkInterface", 
                    loadPkgParam.classLoader, "getName", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String fakeInterface = SharedPref.getXValue("NetworkInterface", DEFAULT_NETWORK_INTERFACE);
                    param.setResult(fakeInterface);
                }
            });
            
            // Hook NetworkInterface.getHardwareAddress()
            XposedHelpers.findAndHookMethod("java.net.NetworkInterface", 
                    loadPkgParam.classLoader, "getHardwareAddress", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Generate MAC bytes from OUI prefix
                    String macOui = SharedPref.getXValue("NetworkMacOUI", DEFAULT_NETWORK_MAC_OUI);
                    String fullMac = macOui + ":BB:B1:28";
                    byte[] macBytes = macToBytes(fullMac);
                    param.setResult(macBytes);
                }
            });
            
        } catch (Exception e) {
            XposedBridge.log("FakeNetwork NetworkInterface ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Hooks ConnectivityManager methods.
     */
    private void hookConnectivityManager(LoadPackageParam loadPkgParam) {
        try {
            // Hook ConnectivityManager.getActiveNetworkInfo()
            XposedHelpers.findAndHookMethod("android.net.ConnectivityManager", 
                    loadPkgParam.classLoader, "getActiveNetworkInfo", new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    Object networkInfo = param.getResult();
                    if (networkInfo != null) {
                        // Override the type to be WIFI instead of ETHERNET
                        String connType = SharedPref.getXValue("ConnectivityType", DEFAULT_CONNECTIVITY_TYPE);
                        int type = TYPE_WIFI; // Default to WIFI
                        if ("ETHERNET".equals(connType)) {
                            type = TYPE_ETHERNET;
                        } else if ("MOBILE".equals(connType)) {
                            type = TYPE_MOBILE;
                        }
                        
                        // Hook NetworkInfo.getType()
                        XposedHelpers.setIntField(networkInfo, "mNetworkType", type);
                    }
                }
            });
            
        } catch (Exception e) {
            XposedBridge.log("FakeNetwork ConnectivityManager ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Convert IP string to integer (little-endian format used by Android).
     */
    private int ipToInt(String ip) {
        try {
            String[] parts = ip.split("\\.");
            if (parts.length == 4) {
                int a = Integer.parseInt(parts[0]);
                int b = Integer.parseInt(parts[1]);
                int c = Integer.parseInt(parts[2]);
                int d = Integer.parseInt(parts[3]);
                // Android uses little-endian: d.c.b.a
                return (d << 24) | (c << 16) | (b << 8) | a;
            }
        } catch (Exception e) {
            XposedBridge.log("FakeNetwork ipToInt ERROR: " + e.getMessage());
        }
        // Default: 192.168.1.100
        return (100 << 24) | (1 << 16) | (168 << 8) | 192;
    }
    
    /**
     * Convert MAC address string to byte array.
     */
    private byte[] macToBytes(String mac) {
        try {
            String[] hex = mac.split(":");
            byte[] bytes = new byte[6];
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
            return bytes;
        } catch (Exception e) {
            // Return default MAC bytes for 6C:C4:08:BB:B1:28
            return new byte[] { (byte) 0x6C, (byte) 0xC4, (byte) 0x08, (byte) 0xBB, (byte) 0xB1, (byte) 0x28 };
        }
    }
}
