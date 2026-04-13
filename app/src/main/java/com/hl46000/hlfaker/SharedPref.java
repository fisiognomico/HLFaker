package com.hl46000.hlfaker;

import android.content.Context;
import android.content.SharedPreferences;
import com.hl46000.hlfaker.Common;
import de.robv.android.xposed.XSharedPreferences;

/**
 * SharedPref - Manages SharedPreferences for HLFaker.
 * 
 * This class provides both regular SharedPreferences (for MainActivity)
 * and XSharedPreferences (for Xposed hooks). It also supports default values
 * to prevent crashes when preferences haven't been initialized yet.
 */
public class SharedPref {
	private Context shareContext;
    private SharedPreferences mySharedPref;
    private static XSharedPreferences myXsharedPref;
    
    public SharedPref(Context appContext) {
    	shareContext = appContext;
    	mySharedPref = shareContext.getSharedPreferences(Common.PREFS_FILE, 1);
    }
    
    public void setSharedPref(String key, String value) {
    	try {
    		mySharedPref.edit().putString(key, value).commit();
        } catch (Exception e) {
        	System.out.println("setSharedPref ERROR: " + e.getMessage());
        }
    }
    
    /**
     * Get a value from SharedPreferences.
     * @param key The preference key
     * @return The value, or empty string if not found
     */
    public String getValue(String key) {
    	String value = "";
    	try {
    		value = mySharedPref.getString(key, null);
        } catch (Exception e) {
        	System.out.println("getSharedPref ERROR: " + e.getMessage());
        }
    	return value;
    }
    
    /**
     * Get a value from SharedPreferences with a default fallback.
     * @param key The preference key
     * @param defaultValue Default value if key not found
     * @return The value, or defaultValue if not found
     */
    public String getValue(String key, String defaultValue) {
    	try {
    		return mySharedPref.getString(key, defaultValue);
        } catch (Exception e) {
        	System.out.println("getSharedPref ERROR: " + e.getMessage());
        	return defaultValue;
        }
    }
    
    public static XSharedPreferences getMyXSharedPref() {
        if (myXsharedPref != null) {
            myXsharedPref.reload();
            return myXsharedPref;
        }
        myXsharedPref = new XSharedPreferences(Common.PACKAGE_NAME, Common.PREFS_FILE);
        myXsharedPref.makeWorldReadable();
        return myXsharedPref;
    }
    
    /**
     * Get a value from XSharedPreferences (for Xposed hooks).
     * @param key The preference key
     * @return The value, or empty string if not found
     */
    public static String getXValue(String key) {
    	String value = "";
    	try {
    		value = getMyXSharedPref().getString(key, null);
    	} catch (Exception e) {
        	System.out.println("getSharedPref ERROR: " + e.getMessage());
        }
    	return value;
    }
    
    /**
     * Get a value from XSharedPreferences with a default fallback.
     * This is the recommended method for Xposed hooks to prevent crashes
     * when MainActivity hasn't run yet or preferences are empty.
     * 
     * @param key The preference key
     * @param defaultValue Default value if key not found or null
     * @return The value, or defaultValue if not found
     */
    public static String getXValue(String key, String defaultValue) {
    	try {
    		String value = getMyXSharedPref().getString(key, defaultValue);
    		// Also return default if the stored value is null or empty
    		if (value == null || value.isEmpty()) {
    			return defaultValue;
    		}
    		return value;
    	} catch (Exception e) {
        	System.out.println("getSharedPref ERROR: " + e.getMessage());
        	return defaultValue;
        }
    }
    
    /**
     * Get an integer value from XSharedPreferences with a default fallback.
     * @param key The preference key
     * @param defaultValue Default value if key not found or parsing fails
     * @return The integer value
     */
    public static int getXIntValue(String key, int defaultValue) {
    	try {
    		String value = getXValue(key, String.valueOf(defaultValue));
    		return Integer.parseInt(value);
    	} catch (NumberFormatException e) {
    		return defaultValue;
    	}
    }
    
    /**
     * Get a float value from XSharedPreferences with a default fallback.
     * @param key The preference key
     * @param defaultValue Default value if key not found or parsing fails
     * @return The float value
     */
    public static float getXFloatValue(String key, float defaultValue) {
    	try {
    		String value = getXValue(key, String.valueOf(defaultValue));
    		return Float.parseFloat(value);
    	} catch (NumberFormatException e) {
    		return defaultValue;
    	}
    }
}
