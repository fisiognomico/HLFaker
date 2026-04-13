package com.hl46000.hlfaker;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;

/**
 * DefaultsManager - Provides default values from XML resources.
 * 
 * This class loads default values from res/values/defaults.xml at runtime.
 * The values can be customized at build time by modifying the XML resources,
 * allowing different default device fingerprints without changing Java code.
 * 
 * Usage:
 *   String board = DefaultsManager.getDefaultString(context, R.string.default_board);
 *   int dpi = DefaultsManager.getDefaultInt(context, R.string.default_dpi);
 * 
 * In Xposed hook context (where Context may not be available):
 *   Use SharedPref.getXValueWithDefault(key, fallbackValue) with hardcoded fallbacks.
 */
public class DefaultsManager {
    
    private static Context appContext;
    
    /**
     * Initialize the DefaultsManager with application context.
     * Should be called once during app startup (e.g., in MainActivity.onCreate).
     * 
     * @param context Application context
     */
    public static void init(Context context) {
        if (appContext == null && context != null) {
            appContext = context.getApplicationContext();
        }
    }
    
    /**
     * Get a string default value from resources.
     * 
     * @param context Context for resource access (can be null if init() was called)
     * @param resId Resource ID of the string (e.g., R.string.default_board)
     * @param fallback Fallback value if resource not found
     * @return The string value
     */
    public static String getDefaultString(Context context, int resId, String fallback) {
        Context ctx = (context != null) ? context : appContext;
        if (ctx == null) {
            return fallback;
        }
        try {
            return ctx.getString(resId);
        } catch (Resources.NotFoundException e) {
            return fallback;
        }
    }
    
    /**
     * Get a string default value from resources (uses cached context).
     * 
     * @param resId Resource ID of the string
     * @param fallback Fallback value if resource not found
     * @return The string value
     */
    public static String getDefaultString(int resId, String fallback) {
        return getDefaultString(null, resId, fallback);
    }
    
    /**
     * Get an integer default value from resources.
     * 
     * @param context Context for resource access (can be null if init() was called)
     * @param resId Resource ID of the integer string (e.g., R.string.default_dpi)
     * @param fallback Fallback value if parsing fails
     * @return The integer value
     */
    public static int getDefaultInt(Context context, int resId, int fallback) {
        Context ctx = (context != null) ? context : appContext;
        if (ctx == null) {
            return fallback;
        }
        try {
            String strValue = ctx.getString(resId);
            return Integer.parseInt(strValue);
        } catch (Resources.NotFoundException e) {
            return fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
    
    /**
     * Get an integer default value from resources (uses cached context).
     * 
     * @param resId Resource ID of the integer string
     * @param fallback Fallback value if parsing fails
     * @return The integer value
     */
    public static int getDefaultInt(int resId, int fallback) {
        return getDefaultInt(null, resId, fallback);
    }
    
    /**
     * Get a float default value from resources.
     * 
     * @param context Context for resource access
     * @param resId Resource ID of the float string
     * @param fallback Fallback value if parsing fails
     * @return The float value
     */
    public static float getDefaultFloat(Context context, int resId, float fallback) {
        Context ctx = (context != null) ? context : appContext;
        if (ctx == null) {
            return fallback;
        }
        try {
            String strValue = ctx.getString(resId);
            return Float.parseFloat(strValue);
        } catch (Resources.NotFoundException e) {
            return fallback;
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
    
    /**
     * Get a float default value from resources (uses cached context).
     * 
     * @param resId Resource ID of the float string
     * @param fallback Fallback value if parsing fails
     * @return The float value
     */
    public static float getDefaultFloat(int resId, float fallback) {
        return getDefaultFloat(null, resId, fallback);
    }
    
    /**
     * Get an array of integer values from resources.
     * Used for RAM values, etc.
     * 
     * @param context Context for resource access
     * @param resId Resource ID of the array
     * @return Array of integers, or empty array if not found
     */
    public static int[] getDefaultIntArray(Context context, int resId) {
        Context ctx = (context != null) ? context : appContext;
        if (ctx == null) {
            return new int[0];
        }
        try {
            TypedArray array = ctx.getResources().obtainTypedArray(resId);
            int[] result = new int[array.length()];
            for (int i = 0; i < array.length(); i++) {
                result[i] = array.getInt(i, 0);
            }
            array.recycle();
            return result;
        } catch (Resources.NotFoundException e) {
            return new int[0];
        }
    }
    
    /**
     * Get an array of integer values from resources (uses cached context).
     * 
     * @param resId Resource ID of the array
     * @return Array of integers
     */
    public static int[] getDefaultIntArray(int resId) {
        return getDefaultIntArray(null, resId);
    }
    
    // ============================================================
    // Convenience methods for specific property types
    // These provide a clean API for accessing specific defaults
    // ============================================================
    
    /**
     * Get default build properties.
     */
    public static class BuildDefaults {
        public static String getBoard(Context ctx) { return getDefaultString(ctx, R.string.default_board, "MSM8960"); }
        public static String getBrand(Context ctx) { return getDefaultString(ctx, R.string.default_brand, "samsung"); }
        public static String getDevice(Context ctx) { return getDefaultString(ctx, R.string.default_device, "jflte"); }
        public static String getModel(Context ctx) { return getDefaultString(ctx, R.string.default_model, "GT-I9505"); }
        public static String getManufacturer(Context ctx) { return getDefaultString(ctx, R.string.default_manufacturer, "samsung"); }
        public static String getFingerprint(Context ctx) { return getDefaultString(ctx, R.string.default_fingerprint, "samsung/jfltexx/jflte:4.3/JSS15J/I9505XXUEML1:user/release-keys"); }
        public static String getAndroidVersion(Context ctx) { return getDefaultString(ctx, R.string.default_android_version, "4.4.2"); }
        public static String getApiLevel(Context ctx) { return getDefaultString(ctx, R.string.default_api_level, "19"); }
    }
    
    /**
     * Get default telephony properties.
     */
    public static class TelephonyDefaults {
        public static String getIMEI(Context ctx) { return getDefaultString(ctx, R.string.default_imei, "506066104722640"); }
        public static String getIMSI(Context ctx) { return getDefaultString(ctx, R.string.default_imsi, "452011234567890"); }
        public static String getPhoneNumber(Context ctx) { return getDefaultString(ctx, R.string.default_phone_number, "84962439943"); }
        public static String getCarrier(Context ctx) { return getDefaultString(ctx, R.string.default_carrier, "Mobifone"); }
        public static String getCarrierCode(Context ctx) { return getDefaultString(ctx, R.string.default_carrier_code, "45201"); }
        public static String getCountryCode(Context ctx) { return getDefaultString(ctx, R.string.default_country_code, "VN"); }
    }
    
    /**
     * Get default WiFi/Bluetooth properties.
     */
    public static class WifiDefaults {
        public static String getMacAddress(Context ctx) { return getDefaultString(ctx, R.string.default_wifi_mac, "6C:C4:08:BB:B1:28"); }
        public static String getSSID(Context ctx) { return getDefaultString(ctx, R.string.default_wifi_ssid, "MyWifi"); }
        public static String getBSSID(Context ctx) { return getDefaultString(ctx, R.string.default_bssid, "6C:C4:08:BB:B1:28"); }
    }
    
    /**
     * Get default GPS properties.
     */
    public static class GpsDefaults {
        public static String getLatitude(Context ctx) { return getDefaultString(ctx, R.string.default_latitude, "27.82516672"); }
        public static String getLongitude(Context ctx) { return getDefaultString(ctx, R.string.default_longitude, "125.06788613"); }
        public static String getAltitude(Context ctx) { return getDefaultString(ctx, R.string.default_altitude, "125.06"); }
        public static String getSpeed(Context ctx) { return getDefaultString(ctx, R.string.default_speed, "3.7"); }
    }
    
    /**
     * Get default battery properties.
     */
    public static class BatteryDefaults {
        public static int getTemperature(Context ctx) { return getDefaultInt(ctx, R.string.default_battery_temp, 350); }
        public static int getLevel(Context ctx) { return getDefaultInt(ctx, R.string.default_battery_level, 35); }
        public static int getHealth(Context ctx) { return getDefaultInt(ctx, R.string.default_battery_health, 2); }
    }
    
    /**
     * Get default OpenGL properties.
     */
    public static class OpenGlDefaults {
        public static String getVendor(Context ctx) { return getDefaultString(ctx, R.string.default_gl_vendor, "Qualcomm"); }
        public static String getRenderer(Context ctx) { return getDefaultString(ctx, R.string.default_gl_renderer, "Adreno (TM) 330"); }
        public static int getDPI(Context ctx) { return getDefaultInt(ctx, R.string.default_dpi, 320); }
    }
}
