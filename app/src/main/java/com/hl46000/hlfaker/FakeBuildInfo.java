package com.hl46000.hlfaker;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Random;


import android.content.ContentResolver;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Build.VERSION;
import android.provider.Settings;
import android.view.Window;
import android.webkit.WebView;

import com.hl46000.hlfaker.SharedPref;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.XposedHelpers.ClassNotFoundError;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;

/**
 * FakeBuildInfo - Hooks Build properties, GPS, Android ID, IMEI, and related APIs.
 *
 * Uses default values to prevent crashes when SharedPreferences are not initialized.
 * Default values match the Samsung Galaxy S4 (GT-I9505) fingerprint.
 */
public class FakeBuildInfo {

    // Default values for Build properties (matching R.string resources)
    private static final String DEFAULT_BOARD = "MSM8960";
    private static final String DEFAULT_BRAND = "samsung";
    private static final String DEFAULT_ABI = "armeabi-v7a";
    private static final String DEFAULT_ABI2 = "armeabi";
    private static final String DEFAULT_DEVICE = "jflte";
    private static final String DEFAULT_DISPLAY = "JSS15J.I9505XXUEML1";
    private static final String DEFAULT_FINGERPRINT = "samsung/jfltexx/jflte:4.3/JSS15J/I9505XXUEML1:user/release-keys";
    private static final String DEFAULT_HARDWARE = "jfltexx";
    private static final String DEFAULT_ID = "JSS15J";
    private static final String DEFAULT_MANUFACTURER = "samsung";
    private static final String DEFAULT_MODEL = "GT-I9505";
    private static final String DEFAULT_BOOTLOADER = "I9505XXUEML1";
    private static final String DEFAULT_HOST = "72262b77b5e8";
    private static final String DEFAULT_ANDROID_VERSION = "4.4.2";
    private static final String DEFAULT_API_LEVEL = "19";
    private static final String DEFAULT_CODENAME = "REL";
    private static final String DEFAULT_DESCRIPTION = "jfltexx-user 4.3 JSS15J I9505XXUEML1 release-keys";

    // Default values for Telephony/ID properties
    private static final String DEFAULT_IMEI = "506066104722640";
    private static final String DEFAULT_ANDROID_ID = "6c0bb208c33b8c43";
    private static final String DEFAULT_ANDROID_SERIAL = "6c0bb208c33b";
    private static final String DEFAULT_GOOGLE_ADS_ID = "f741b85f-fbab-4eb3-8e44-358e07c3bc50";
    private static final String DEFAULT_BASEBAND = "g5123b-145971-250328-B-13284995";
    private static final String DEFAULT_TAGS = "release-keys";
    private static final String DEFAULT_SUPPORTED_ABIS = "armeabi-v7a,armeabi";

    // Default values for GPS
    private static final String DEFAULT_LATITUDE = "27.82516672";
    private static final String DEFAULT_LONGITUDE = "125.06788613";
    private static final String DEFAULT_ALTITUDE = "125.06";
    private static final String DEFAULT_SPEED = "3.7";

    // Default User Agent
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4.2; GT-I9505 Build/16.0.A.0.36) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/51.0.2704 Mobile Safari/537.36";

    // GPS jitter configuration
    private static final float DEFAULT_GPS_JITTER_STD_DEV = 0.00001f; // ~1 meter at equator
    private static final float DEFAULT_GPS_MOVE_PROBABILITY = 0.3f; // 30% chance per call

    private final Random random;
    private final float gpsJitterStdDev;
    private final float gpsMoveProbability;

 	public FakeBuildInfo(LoadPackageParam sharePkgParam){
		this.random = new Random(System.currentTimeMillis());
		this.gpsJitterStdDev = SharedPref.getXFloatValue("GpsJitterStdDev", DEFAULT_GPS_JITTER_STD_DEV);
		this.gpsMoveProbability = SharedPref.getXFloatValue("GpsMoveProbability", DEFAULT_GPS_MOVE_PROBABILITY);
		FakeGPS(sharePkgParam);
		FakeAndroidID(sharePkgParam);
		FakeAndroidSerial(sharePkgParam);
		FakeIMEI(sharePkgParam);
		FakeBaseBand(sharePkgParam);
		FakeBuildProp(sharePkgParam);
		FakeUserAgent(sharePkgParam);
		FakeGoogleAdsID(sharePkgParam);
	}

	/**
	 * Generates Gaussian noise using Box-Muller transform.
	 */
	private float generateGaussianNoise(float mean, float stdDev) {
		double u1 = random.nextDouble();
		double u2 = random.nextDouble();
		if (u1 < 1e-10) u1 = 1e-10;
		double z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
		return (float) (mean + z0 * stdDev);
	}

	/**
	 * Applies jitter to GPS coordinate with probability check and boundary clamping.
	 */
	private float applyGpsJitter(float baseValue, float min, float max) {
		if (random.nextFloat() > gpsMoveProbability) {
			return baseValue;
		}
		float jittered = baseValue + generateGaussianNoise(0.0f, gpsJitterStdDev);
		if (jittered < min) jittered = min;
		if (jittered > max) jittered = max;
		return jittered;
	}

	public void FakeUserAgent(LoadPackageParam loadPkgParam){

		//if(!loadPkgParam.packageName.contains("com.bbm")){

			try {
				XposedHelpers.findAndHookMethod("com.android.webview.chromium.ContentSettingsAdapter", loadPkgParam.classLoader, "setUserAgentString", String.class, new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						// TODO Auto-generated method stub
						super.beforeHookedMethod(param);
						param.args[0] = SharedPref.getXValue("UserAgent", DEFAULT_USER_AGENT);
					}

				});
			} catch (ClassNotFoundError e) {
				XposedBridge.log("Fake UA ERROR: " + e.getMessage());
			}

			try {
				Method loadUrl1 = WebView.class.getDeclaredMethod("loadUrl", new Class[]{String.class});
                Method loadUrl2 = WebView.class.getDeclaredMethod("loadUrl", new Class[]{String.class, Map.class});

                XposedBridge.hookMethod(loadUrl1, new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						// TODO Auto-generated method stub
						super.beforeHookedMethod(param);
						XposedBridge.log("Load Url: " + param.args[0]);
			            if (param.args.length > 0 && (param.thisObject instanceof WebView)) {
			                String ua = SharedPref.getXValue("UserAgent", DEFAULT_USER_AGENT);
			                WebView webView = (WebView) param.thisObject;
			                if (webView.getSettings() != null) {
			                    webView.getSettings().setUserAgentString(ua);
			                }
			            }
					}

				});
                XposedBridge.hookMethod(loadUrl2, new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						// TODO Auto-generated method stub
						super.beforeHookedMethod(param);
						XposedBridge.log("load url: " + param.args[0]);
			            if (param.args.length > 0 && (param.thisObject instanceof WebView)) {
			                String ua = SharedPref.getXValue("UserAgent", DEFAULT_USER_AGENT);
			                WebView webView = (WebView) param.thisObject;
			                if (webView.getSettings() != null) {
			                    webView.getSettings().setUserAgentString(ua);
			                }
			            }
					}

				});

			} catch (Throwable e) {
				XposedBridge.log("Fake User Agent ERROR: " + e.getMessage());
			}
		//}
	}

	public void FakeGPS(LoadPackageParam loadPkgParam){
		try {

			XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getLatitude", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					String lat = SharedPref.getXValue("Lat", DEFAULT_LATITUDE);
					float baseLat = Float.parseFloat(lat);
					param.setResult(applyGpsJitter(baseLat, -90.0f, 90.0f));
				}

			});
			XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getLongitude", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					String lng = SharedPref.getXValue("Long", DEFAULT_LONGITUDE);
					float baseLng = Float.parseFloat(lng);
					param.setResult(applyGpsJitter(baseLng, -180.0f, 180.0f));
				}

			});
			XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getAccuracy", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					String acc = SharedPref.getXValue("Accuracy", DEFAULT_ALTITUDE);
					float baseAcc = Float.parseFloat(acc);
					param.setResult(applyGpsJitter(baseAcc, 0.0f, 1000.0f));
				}

			});
			XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getAltitude", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					String alt = SharedPref.getXValue("Alt", DEFAULT_ALTITUDE);
					float baseAlt = Float.parseFloat(alt);
					param.setResult(applyGpsJitter(baseAlt, -1000.0f, 10000.0f));
				}

			});
			XposedHelpers.findAndHookMethod("android.location.Location", loadPkgParam.classLoader, "getSpeed", new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					String speed = SharedPref.getXValue("Speed", DEFAULT_SPEED);
					float baseSpeed = Float.parseFloat(speed);
					param.setResult(applyGpsJitter(baseSpeed, 0.0f, 200.0f));
				}

			});



		} catch (Throwable e) {
			XposedBridge.log("Fake GPS ERROR: " + e.getMessage());
		}
	}

	public void FakeAndroidID(LoadPackageParam loadPkgParam) {
		try {
			XposedHelpers.findAndHookMethod("android.provider.Settings.Secure", loadPkgParam.classLoader, "getString",ContentResolver.class, String.class, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {

					if (param.args[1].equals(Settings.Secure.ANDROID_ID)) {
						param.setResult(SharedPref.getXValue("AndroidID", DEFAULT_ANDROID_ID));
					}
				}
			});

		} catch (Throwable ex) {
			XposedBridge.log("Fake Android ID ERROR: " + ex.getMessage());
		}
	}

	public void FakeAndroidSerial(LoadPackageParam loadPkgParam){
		try {
			Class<?> classBuild = XposedHelpers.findClass("android.os.Build",
					loadPkgParam.classLoader);
			XposedHelpers.setStaticObjectField(classBuild, "SERIAL",
					SharedPref.getXValue("AndroidSerial", DEFAULT_ANDROID_SERIAL));
			Class<?> classSysProp = Class
					.forName("android.os.SystemProperties");
			XposedHelpers.findAndHookMethod(classSysProp, "get", String.class,
					new XC_MethodHook() {

						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							// TODO Auto-generated method stub
							super.afterHookedMethod(param);

							String serialno = (String) param.args[0];
							if (serialno.equals("ro.serialno")
									|| serialno.equals("ro.boot.serialno")
									|| serialno.equals("ril.serialnumber")
									|| serialno.equals("sys.serialnumber")) {
								param.setResult(SharedPref.getXValue("AndroidSerial", DEFAULT_ANDROID_SERIAL));
							}
						}

					});
			XposedHelpers.findAndHookMethod(classSysProp, "get", String.class,
					String.class, new XC_MethodHook() {

						@Override
						protected void afterHookedMethod(MethodHookParam param)
								throws Throwable {
							// TODO Auto-generated method stub
							super.afterHookedMethod(param);

							String serialno = (String) param.args[0];
							if (serialno.equals("ro.serialno")
									|| serialno.equals("ro.boot.serialno")
									|| serialno.equals("ril.serialnumber")
									|| serialno.equals("sys.serialnumber")) {
								param.setResult(SharedPref.getXValue("AndroidSerial", DEFAULT_ANDROID_SERIAL));
							}
						}

					});
			return;

		} catch (IllegalArgumentException ex) {
			XposedBridge.log("Fake AndroidSerial ERROR: " + ex.getMessage());
			return;
		} catch (ClassNotFoundException ex) {
			XposedBridge.log("Fake AndroidSerial ERROR: " + ex.getMessage());
		}
	}

	public void FakeIMEI(LoadPackageParam loadPkgParam){
		String imei = SharedPref.getXValue("IMEI", DEFAULT_IMEI);
		try {
			XposedHelpers.findAndHookMethod("android.telephony.TelephonyManager", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(imei));
		} catch (Throwable t) {
			XposedBridge.log("Fake IMEI TelephonyManager ERROR: " + t);
		}
		// PhoneSubInfo and legacy classes removed in API 23+ — skip on modern Android
		if (VERSION.SDK_INT < 23) {
			try {
				XposedHelpers.findAndHookMethod("com.android.internal.telephony.PhoneSubInfo", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(imei));
			} catch (Throwable t) {
				XposedBridge.log("Fake IMEI PhoneSubInfo ERROR: " + t);
			}
		}
		if (VERSION.SDK_INT < 22) {
			try {
				XposedHelpers.findAndHookMethod("com.android.internal.telephony.gsm.GSMPhone", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(imei));
				XposedHelpers.findAndHookMethod("com.android.internal.telephony.PhoneProxy", loadPkgParam.classLoader, "getDeviceId", XC_MethodReplacement.returnConstant(imei));
			} catch (Throwable t) {
				XposedBridge.log("Fake IMEI legacy ERROR: " + t);
			}
		}
	}

	public void FakeGoogleAdsID(LoadPackageParam loadPkgParam){
		try {
			XposedHelpers.findAndHookMethod("android.os.Binder", loadPkgParam.classLoader, "execTransact", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {

					super.beforeHookedMethod(param);
					if (((IBinder) param.thisObject)
							.getInterfaceDescriptor()
							.equals("com.google.android.gms.ads.identifier.internal.IAdvertisingIdService")
							&& ((Integer) param.args[0]).intValue() == 1) {
						Parcel reply = null;
						try {

							Method methodObtain = Parcel.class.getDeclaredMethod("obtain", Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? int.class : long.class);
							methodObtain.setAccessible(true);
							reply = (Parcel) methodObtain.invoke(null,
									param.args[2]);
						} catch (NoSuchMethodException ex) {
							XposedBridge.log("Fake Google Ads NoSuchMethodException ERROR: " + ex.getMessage());
						}catch (NullPointerException e) {
							XposedBridge.log("Fake Google Ads NullPointerException ERROR: " + e.getMessage());
						}
						if (reply == null) {

						} else {
							reply.setDataPosition(0);
							reply.writeNoException();
							reply.writeString(SharedPref.getXValue("GoogleAdsID", DEFAULT_GOOGLE_ADS_ID));
						}

						param.setResult(Boolean.valueOf(true));
					}

				}

			});

		} catch (Throwable ex) {
			XposedBridge.log("Fake Google Ads ID ERROR: " + ex.getMessage());
		}
	}

	public void FakeBaseBand(LoadPackageParam loadPkgParam) {
		try {
			if (Build.VERSION.SDK_INT <= 14) {
				Class<?> classBuild = XposedHelpers.findClass(
						"android.os.Build", loadPkgParam.classLoader);
				XposedHelpers.setStaticObjectField(classBuild, "RADIO", SharedPref.getXValue("BaseBand", DEFAULT_BASEBAND));
			}else{
				XposedHelpers.findAndHookMethod("android.os.Build",
						loadPkgParam.classLoader, "getRadioVersion", new XC_MethodHook() {

							@Override
							protected void afterHookedMethod(MethodHookParam param)
									throws Throwable {
								param.setResult(SharedPref.getXValue("BaseBand", DEFAULT_BASEBAND));
							}

						});
			}
		} catch (Throwable e) {
			XposedBridge.log("Fake BaseBand ERROR: " + e.getMessage());
		}


	}

	public void FakeBuildProp(LoadPackageParam loadPkgParam){
		try {
			XposedHelpers.findField(Build.class, "BOARD").set(null, SharedPref.getXValue("BOARD", DEFAULT_BOARD));
			XposedHelpers.findField(Build.class, "BRAND").set(null, SharedPref.getXValue("BRAND", DEFAULT_BRAND));
			XposedHelpers.findField(Build.class, "CPU_ABI").set(null, SharedPref.getXValue("ABI", DEFAULT_ABI));
			XposedHelpers.findField(Build.class, "CPU_ABI2").set(null, SharedPref.getXValue("ABI2", DEFAULT_ABI2));
			XposedHelpers.findField(Build.class, "DEVICE").set(null, SharedPref.getXValue("DEVICE", DEFAULT_DEVICE));
			XposedHelpers.findField(Build.class, "DISPLAY").set(null, SharedPref.getXValue("DISPLAY", DEFAULT_DISPLAY));
			XposedHelpers.findField(Build.class, "FINGERPRINT").set(null, SharedPref.getXValue("FINGERPRINT", DEFAULT_FINGERPRINT));
			XposedHelpers.findField(Build.class, "HARDWARE").set(null, SharedPref.getXValue("NAME", DEFAULT_HARDWARE));
			XposedHelpers.findField(Build.class, "ID").set(null, SharedPref.getXValue("ID", DEFAULT_ID));
			XposedHelpers.findField(Build.class, "MANUFACTURER").set(null, SharedPref.getXValue("Manufacture", DEFAULT_MANUFACTURER));
			XposedHelpers.findField(Build.class, "MODEL").set(null, SharedPref.getXValue("MODEL", DEFAULT_MODEL));
			XposedHelpers.findField(Build.class, "PRODUCT").set(null, SharedPref.getXValue("DEVICE", DEFAULT_DEVICE));
            XposedHelpers.findField(Build.class, "BOOTLOADER").set(null, SharedPref.getXValue("BOOTLOADER", DEFAULT_BOOTLOADER));
            XposedHelpers.findField(Build.class, "HOST").set(null, SharedPref.getXValue("BuildHost", DEFAULT_HOST));

            // Build.TAGS - returns "release-keys" not "test-keys"
            try {
                XposedHelpers.findField(Build.class, "TAGS").set(null, SharedPref.getXValue("BuildTags", DEFAULT_TAGS));
            } catch (Throwable e) {
                XposedBridge.log("Fake TAGS ERROR: " + e.getMessage());
            }

            // Build.SUPPORTED_ABIS - returns ARM ABIs (not x86) - API 21+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                try {
                    String supportedAbisStr = SharedPref.getXValue("SupportedABIs", DEFAULT_SUPPORTED_ABIS);
                    String[] supportedAbis = supportedAbisStr.split(",");
                    XposedHelpers.findField(Build.class, "SUPPORTED_ABIS").set(null, supportedAbis);
                    XposedHelpers.findField(Build.class, "SUPPORTED_32_BIT_ABIS").set(null, supportedAbis);
                } catch (Throwable e) {
                    XposedBridge.log("Fake SUPPORTED_ABIS ERROR: " + e.getMessage());
                }
            }

			XposedHelpers.findField(VERSION.class, "INCREMENTAL").set(null, SharedPref.getXValue("BOOTLOADER", DEFAULT_BOOTLOADER));
			XposedHelpers.findField(VERSION.class, "RELEASE").set(null, SharedPref.getXValue("AndroidVer", DEFAULT_ANDROID_VERSION));
			XposedHelpers.findField(VERSION.class, "SDK").set(null, SharedPref.getXValue("API", DEFAULT_API_LEVEL));
			XposedHelpers.findField(VERSION.class, "CODENAME").set(null, DEFAULT_CODENAME);

		} catch (IllegalAccessException e) {
			XposedBridge.log("Fake BuilProp ERROR: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			XposedBridge.log("Fake BuilProp ERROR: " + e.getMessage());
		}

		try {
			Class<?> cls = Class.forName("android.os.SystemProperties");
			if(cls != null){
				for (Member mem : cls.getDeclaredMethods()) {
					XposedBridge.hookMethod(mem, new XC_MethodHook() {

						@Override
						protected void beforeHookedMethod(MethodHookParam param)
								throws Throwable {
							// TODO Auto-generated method stub
							super.beforeHookedMethod(param);

							if (param.args.length > 0 && param.args[0] != null && param.args[0].equals("ro.build.description")) {
								param.setResult(SharedPref.getXValue("DESCRIPTION", DEFAULT_DESCRIPTION));
					        }
						}
					});
				}
			}

		} catch (ClassNotFoundException e) {
			XposedBridge.log("Fake DESCRIPTION ERROR: " + e.getMessage());
		}
	}
}
