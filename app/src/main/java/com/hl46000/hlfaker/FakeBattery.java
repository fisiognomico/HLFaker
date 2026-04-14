package com.hl46000.hlfaker;

import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XC_MethodHook.MethodHookParam;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import com.hl46000.hlfaker.SharedPref;

/**
 * FakeBattery - Hooks battery-related APIs to return fake values.
 * 
 * Uses default values from SharedPref to prevent crashes when
 * MainActivity hasn't been run yet.
 * 
 * Default values (from R.string resources):
 * - Temperature: 350 (35.0°C)
 * - Level: 35 (35%)
 * - Voltage: 4200 (mV, realistic fully charged voltage)
 * - Plugged: 2 (USB charging, configurable)
 * - Status: random(2-4)
 * - Health: 2 (Good)
 */
public class FakeBattery {
    
    // Default battery values - matching R.string resources
    private static final String DEFAULT_TEMP = "350";
    private static final String DEFAULT_LEVEL = "35";
    private static final String DEFAULT_HEALTH = "2";
    private static final String DEFAULT_VOLTAGE = "4200";
    private static final String DEFAULT_PLUGGED = "2"; // USB charging

	// Fake trang thai cua Pin
	public void fakePinStt(LoadPackageParam loadPkgParam) {
		try {
			XposedHelpers.findAndHookMethod("android.content.Intent", loadPkgParam.classLoader, "getIntExtra", String.class, Integer.TYPE, new XC_MethodHook() {

				@Override
				protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.beforeHookedMethod(param);
					if (param.args[0] != null) {
                        String key = (String) param.args[0];
                        if (key.equals("temperature")) {
                            // Use SharedPref.getXValue with default to prevent crashes
                            String tempValue = SharedPref.getXValue("Temp", DEFAULT_TEMP);
                            param.setResult(Integer.valueOf(Integer.parseInt(tempValue)));
                        }
                        if (key.equals("level")) {
                            // Use SharedPref.getXValue with default to prevent crashes
                            String levelValue = SharedPref.getXValue("Level", DEFAULT_LEVEL);
                            param.setResult(Integer.valueOf(Integer.parseInt(levelValue)));
                        }
                        if (key.equals("plugged")) {
                            // Use SharedPref value for BatteryPlugged (0=unplugged, 2=USB)
                            String pluggedValue = SharedPref.getXValue("BatteryPlugged", DEFAULT_PLUGGED);
                            param.setResult(Integer.valueOf(Integer.parseInt(pluggedValue)));
                        }
                        if (key.equals("status")) {
                            param.setResult(Integer.valueOf(random24()));
                        }
                        if (key.equals("health")) {
                            param.setResult(Integer.valueOf(DEFAULT_HEALTH));
                        }
                        if (key.equals("voltage")) {
                            // Use SharedPref value for BatteryVoltage
                            String voltageValue = SharedPref.getXValue("BatteryVoltage", DEFAULT_VOLTAGE);
                            param.setResult(Integer.valueOf(Integer.parseInt(voltageValue)));
                        }
			        }
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake Pin ERROR: " + e.getMessage());
		}
		
	}
	private String random02() {
        String[] arrayValue = new String[]{"0", "1", "2"};
        return arrayValue[new Random().nextInt(arrayValue.length)];
    }

    private String random24() {
        String[] arrayValue = new String[]{"2", "3", "4"};
        return arrayValue[new Random().nextInt(arrayValue.length)];
    }
}

