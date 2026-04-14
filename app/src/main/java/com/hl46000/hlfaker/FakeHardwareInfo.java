package com.hl46000.hlfaker;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.res.AssetManager;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import com.hl46000.hlfaker.SharedPref;

/**
 * FakeHardwareInfo - Hooks hardware-related APIs (Bluetooth, WiFi, Telephony, CPU files).
 * 
 * Uses default values to prevent crashes when SharedPreferences are not initialized.
 * Default values match the Samsung Galaxy S4 (GT-I9505) fingerprint.
 */
public class FakeHardwareInfo {
    
    // Default values for WiFi/Bluetooth
    private static final String DEFAULT_WIFI_MAC = "6C:C4:08:BB:B1:28";
    private static final String DEFAULT_WIFI_SSID = "MyWifi";
    private static final String DEFAULT_BSSID = "6C:C4:08:BB:B1:28";
    
    // Default values for Telephony
    private static final String DEFAULT_IMEI = "506066104722640";
    private static final String DEFAULT_IMSI = "452011234567890";
    private static final String DEFAULT_PHONE_NUMBER = "84962439943";
    private static final String DEFAULT_SIM_SERIAL = "36066104722647215170";
    private static final String DEFAULT_CARRIER = "Mobifone";
    private static final String DEFAULT_CARRIER_CODE = "45201";
    private static final String DEFAULT_COUNTRY_CODE = "VN";
    private static final String DEFAULT_VOICEMAIL_NUMBER = "84962439944";
    private static final String DEFAULT_SIM_STATE = "5"; // SIM_STATE_READY
    
    // Default values for System/OS
    private static final String DEFAULT_OS_NAME = "Linux";
    private static final String DEFAULT_OS_ARCH = "armv7l";
    private static final String DEFAULT_OS_VERSION = "3.4.0-gd59db4e";

	public FakeHardwareInfo(LoadPackageParam sharePkgParam){
		FakeBluetooth(sharePkgParam);
		FakeWifi(sharePkgParam);
		FakeCPUFile(sharePkgParam);
		FakeTelephony(sharePkgParam);
		
	}
	
	public static boolean CreatDataCpu(Context context) {
        String str = "/data/data/" + context.getPackageName() + "/cpuinfo";
        String str2 = "/data/data/" + context.getPackageName() + "/version";
        try {
            AssetManager assets = context.getAssets();
            InputStream open = assets.open("cpuinfo");
            OutputStream fileOutputStream = new FileOutputStream(str);
            writeValue(open, fileOutputStream);
            open.close();
            fileOutputStream.flush();
            fileOutputStream.close();
            InputStream open2 = assets.open("version");
            OutputStream fileOutputStream2 = new FileOutputStream(str2);
            writeValue(open2, fileOutputStream2);
            open2.close();
            fileOutputStream2.flush();
            fileOutputStream2.close();
            Sendfile(str, str2);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
	
	private static void writeValue(InputStream inputStream, OutputStream outputStream) {
        try {
            byte[] bArr = new byte[1024];
            while (true) {
                int read = inputStream.read(bArr);
                if (read != -1) {
                    outputStream.write(bArr, 0, read);
                } else {
                    return;
                }
            }
        } catch (Exception e) {
        }
    }
	
	private static void Sendfile(String str, String str2) {
        IOException e;
        try {
            DataOutputStream dataOutputStream = new DataOutputStream(Runtime.getRuntime().exec("su").getOutputStream());
            try {
                dataOutputStream.writeBytes("mkdir /data/misc/sys/\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("chmod 777 /data/misc/sys/\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("cp " + str + " /data/misc/sys\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("chmod 444 /data/misc/sys/cpuinfo\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("rm " + str + "\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("cp " + str2 + " /data/misc/sys\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("chmod 444 /data/misc/sys/version\n");
                dataOutputStream.flush();
                dataOutputStream.writeBytes("rm " + str2 + "\n");
                dataOutputStream.flush();
                dataOutputStream.close();
                if (new File("/data/misc/sys/cpuinfo").exists()) {
                    return;
                }
                throw new IOException();
            } catch (IOException e2) {
                e = e2;
                DataOutputStream dataOutputStream2 = dataOutputStream;
                e.printStackTrace();
            }
        } catch (IOException e3) {
            e = e3;
            e.printStackTrace();
        }
    }
	
	
	public void FakeCPUFile(LoadPackageParam loadPkgParam){
		try {

			XposedBridge.hookAllConstructors(File.class, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					if (param.args.length == 1) {
			            if (param.args[0].equals("/proc/cpuinfo")) {
			            	param.args[0] = "/data/misc/sys/cpuinfo";
			            }
			            if (param.args[0].equals("/proc/version")) {
			            	param.args[0] = "/data/misc/sys/version";
			            }
			        } else if (param.args.length == 2 && !File.class.isInstance(param.args[0])) {
			            int i = 0;
			            String str = "";
			            while (i < 2) {
			                String stringBuilder;
			                if (param.args[i] != null) {
			                    if (param.args[i].equals("/proc/cpuinfo")) {
			                    	param.args[i] = "/data/misc/sys/cpuinfo";
			                    }
			                    if (param.args[i].equals("/proc/version")) {
			                    	param.args[i] = "/data/misc/sys/version";
			                    }
			                    stringBuilder = new StringBuilder(String.valueOf(str)).append(param.args[i]).append(":").toString();
			                } else {
			                    stringBuilder = str;
			                }
			                i++;
			                str = stringBuilder;
			            }
			        }
				}
				
			});
			
			XposedHelpers.findAndHookMethod("java.lang.Runtime", loadPkgParam.classLoader, "exec", String[].class, String[].class, File.class, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					if (param.args.length == 1) {
			            if (param.args[0].equals("/proc/cpuinfo")) {
			            	param.args[0] = "/data/misc/sys/cpuinfo";
			            }
			            if (param.args[0].equals("/proc/version")) {
			            	param.args[0] = "/data/misc/sys/version";
			            }
			        } else if (param.args.length == 2 && !File.class.isInstance(param.args[0])) {
			            int i = 0;
			            String str = "";
			            while (i < 2) {
			                String stringBuilder;
			                if (param.args[i] != null) {
			                    if (param.args[i].equals("/proc/cpuinfo")) {
			                    	param.args[i] = "/data/misc/sys/cpuinfo";
			                    }
			                    if (param.args[i].equals("/proc/version")) {
			                    	param.args[i] = "/data/misc/sys/version";
			                    }
			                    stringBuilder = new StringBuilder(String.valueOf(str)).append(param.args[i]).append(":").toString();
			                } else {
			                    stringBuilder = str;
			                }
			                i++;
			                str = stringBuilder;
			            }
			        }
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake CPUFile - 1 ERROR: " + e.getMessage());
		}
		
		
		try {
            XposedBridge.hookMethod(XposedHelpers.findConstructorExact(ProcessBuilder.class, new Class[]{String[].class}), new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					if (param.args[0] != null) {
                    String[] strArr = (String[]) param.args[0];
                    String str = "";
                    for (String str2 : strArr) {
                        str = new StringBuilder(String.valueOf(str)).append(str2).append(":").toString();
                        if (str2 == "/proc/cpuinfo") {
                            strArr[1] = "/data/misc/sys/cpuinfo";
                        }
                        if (str2 == "/proc/version") {
                            strArr[1] = "/data/misc/sys/version";
                        }
                    }
                    param.args[0] = strArr;
                }
				}
            	
			});
        } catch (Exception e) {
        	XposedBridge.log("Fake CPUFile - 2 ERROR: " + e.getMessage());
        }
		
		try {
			//Pattern.compile("").matcher("");
			
			XposedHelpers.findAndHookMethod("java.util.regex.Pattern", loadPkgParam.classLoader, "matcher", CharSequence.class, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					if (param.args.length == 1) {
			            if (param.args[0].equals("/proc/cpuinfo")) {
			            	param.args[0] = "/data/misc/sys/cpuinfo";
			            }
			            if (param.args[0].equals("/proc/version")) {
			            	param.args[0] = "/data/misc/sys/version";
			            }
			        }
				}
				
			});
			
		} catch (Exception e) {
			XposedBridge.log("Fake CPU(Pattern) ERROR: " + e.getMessage());
		}
	}
	
	
	public void FakeBluetooth(LoadPackageParam loadPkgParam){
		try {
			XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothAdapter", loadPkgParam.classLoader, "getAddress", new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					param.setResult(SharedPref.getXValue("WifiMAC", DEFAULT_WIFI_MAC));
				}
				
			});
			XposedHelpers.findAndHookMethod("android.bluetooth.BluetoothDevice", loadPkgParam.classLoader, "getAddress", new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					//super.afterHookedMethod(param);
					param.setResult(SharedPref.getXValue("WifiMAC", DEFAULT_WIFI_MAC));
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake Bluetooth ERROR: " + e.getMessage());
		}
	}
	
	public void FakeWifi(LoadPackageParam loadPkgParam){
		try {
			XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPkgParam.classLoader, "getMacAddress", new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					param.setResult(SharedPref.getXValue("WifiMAC", DEFAULT_WIFI_MAC));
				}
				
			});
			XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPkgParam.classLoader, "getSSID", new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					param.setResult(SharedPref.getXValue("WifiName", DEFAULT_WIFI_SSID));
				}
				
			});
			XposedHelpers.findAndHookMethod("android.net.wifi.WifiInfo", loadPkgParam.classLoader, "getBSSID", new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					param.setResult(SharedPref.getXValue("BSSID", DEFAULT_BSSID));
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake Wifi ERROR: " + e.getMessage());
		}
	}
	
	public void FakeTelephony(LoadPackageParam loadPkgParam){
		String TelePhone = "android.telephony.TelephonyManager";
		HookTelephony(TelePhone, loadPkgParam, "getDeviceId", SharedPref.getXValue("IMEI", DEFAULT_IMEI));
		HookTelephony(TelePhone, loadPkgParam, "getSubscriberId", SharedPref.getXValue("IMSI", DEFAULT_IMSI));
		HookTelephony(TelePhone, loadPkgParam, "getLine1Number", SharedPref.getXValue("PhoneNumber", DEFAULT_PHONE_NUMBER));
		HookTelephony(TelePhone, loadPkgParam, "getSimSerialNumber", SharedPref.getXValue("SimSerial", DEFAULT_SIM_SERIAL));
		
		// Carrier Code: use separate MCC/MNC or fall back to combined CarrierCode
		String carrierCode = SharedPref.getXValue("CarrierCode", DEFAULT_CARRIER_CODE);
		String mcc = SharedPref.getXValue("CarrierCodeMCC", "");
		String mnc = SharedPref.getXValue("CarrierCodeMNC", "");
		if (!mcc.isEmpty() && !mnc.isEmpty()) {
			carrierCode = mcc + mnc;
		}
		
		HookTelephony(TelePhone, loadPkgParam, "getNetworkOperator", carrierCode);
		HookTelephony(TelePhone, loadPkgParam, "getNetworkOperatorName", SharedPref.getXValue("Carrier", DEFAULT_CARRIER));
		HookTelephony(TelePhone, loadPkgParam, "getSimOperator", carrierCode);
		HookTelephony(TelePhone, loadPkgParam, "getSimOperatorName", SharedPref.getXValue("Carrier", DEFAULT_CARRIER));
		HookTelephony(TelePhone, loadPkgParam, "getNetworkCountryIso", SharedPref.getXValue("CountryCode", DEFAULT_COUNTRY_CODE));
		HookTelephony(TelePhone, loadPkgParam, "getSimCountryIso", SharedPref.getXValue("CountryCode", DEFAULT_COUNTRY_CODE));
		
		// New telephony hooks for emulator detection bypass
		HookTelephony(TelePhone, loadPkgParam, "getVoiceMailNumber", SharedPref.getXValue("VoiceMailNumber", DEFAULT_VOICEMAIL_NUMBER));
		
		// Hook getSimState to return SIM_STATE_READY (5) instead of SIM_STATE_ABSENT (1)
		try {
			XposedHelpers.findAndHookMethod(TelePhone, loadPkgParam.classLoader, "getSimState", new XC_MethodHook() {
				@Override
				protected void afterHookedMethod(MethodHookParam param) throws Throwable {
					super.afterHookedMethod(param);
					String simStateStr = SharedPref.getXValue("SimState", DEFAULT_SIM_STATE);
					try {
						int simState = Integer.parseInt(simStateStr);
						param.setResult(simState);
					} catch (NumberFormatException e) {
						param.setResult(5); // Default to SIM_STATE_READY
					}
				}
			});
		} catch (Exception e) {
			XposedBridge.log("Fake getSimState ERROR: " + e.getMessage());
		}
		//HookTelephony(TelePhone, loadPkgParam, "getDeviceId", SharedPref.getXValue("IMEI"));
		try {
			XposedHelpers.findAndHookMethod(System.class, "getProperty", String.class, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					String arg = (String) param.args[0];
					if ("os.version".equals(arg)) {
						param.setResult(SharedPref.getXValue("OSVersion", DEFAULT_OS_VERSION));
			        }
			        if ("os.arch".equals(arg)) {
			        	param.setResult(SharedPref.getXValue("OSArch", DEFAULT_OS_ARCH));
			        }
			        if ("os.name".equals(arg)) {
			        	param.setResult(SharedPref.getXValue("OSName", DEFAULT_OS_NAME));
			        }
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake OS ERROR: " + e.getMessage());
		}
	}
	private void HookTelephony(String hookClass, LoadPackageParam loadPkgParam, String funcName, final String value){
		try {
			XposedHelpers.findAndHookMethod(hookClass, loadPkgParam.classLoader, funcName, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);		
					param.setResult(value);				
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake " + funcName + " ERROR: " + e.getMessage());
		}
	}
}
