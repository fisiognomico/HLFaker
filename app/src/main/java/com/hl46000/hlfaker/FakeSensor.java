package com.hl46000.hlfaker;

import java.util.Random;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * FakeSensor - Hooks sensor-related APIs to return fake values with jitter.
 * 
 * This class intercepts Sensor and SensorEventListener calls to hide emulator
 * characteristics and add realistic noise to sensor values. Apps detect emulators
 * by checking if sensor values have zero variance (static values).
 * 
 * Default values (from R.string resources):
 * - SensorVendor: "Qualcomm"
 * - AccelerometerName: "Kionix KXTJ2-1009 3-axis Accelerometer"
 * - GyroscopeName: "InvenSense MPU-6050 6-axis Gyroscope"
 * - MagnetometerName: "AK09911C 3-axis Magnetic field sensor"
 * - PressureName: "LPS331AP Pressure sensor"
 * - SensorJitterMagnitude: "0.05" (standard deviation for Gaussian noise)
 * 
 * Build constants for jitter configuration (defined in BuildConfig or defaults.xml):
 * - SENSOR_JITTER_MEAN: Mean of Gaussian noise (default: 0.0)
 * - SENSOR_JITTER_STD_DEV: Standard deviation of noise (default: 0.05)
 * 
 * Note: Jitter is applied to accelerometer, gyroscope, and magnetic field sensors
 * to simulate realistic sensor noise and prevent variance-based detection.
 */
public class FakeSensor {
    
    // Default sensor vendor/name values
    private static final String DEFAULT_SENSOR_VENDOR = "Qualcomm";
    private static final String DEFAULT_ACCELEROMETER_NAME = "Kionix KXTJ2-1009 3-axis Accelerometer";
    private static final String DEFAULT_GYROSCOPE_NAME = "InvenSense MPU-6050 6-axis Gyroscope";
    private static final String DEFAULT_MAGNETOMETER_NAME = "AK09911C 3-axis Magnetic field sensor";
    private static final String DEFAULT_PRESSURE_NAME = "LPS331AP Pressure sensor";
    private static final String DEFAULT_PROXIMITY_NAME = "APDS-9930/QPDS-T930 Proximity";
    private static final String DEFAULT_LIGHT_NAME = "APDS-9930/QPDS-T930 Light";
    private static final String DEFAULT_STEP_DETECTOR_NAME = "QTI Step Detector";
    private static final String DEFAULT_STEP_COUNTER_NAME = "QTI Step Counter";
    private static final String DEFAULT_SIGNIFICANT_MOTION_NAME = "QTI Significant Motion Detector";
    
    // Jitter configuration - can be overridden via build constants
    // These values represent the magnitude of Gaussian noise added to sensor values
    private static final float DEFAULT_JITTER_MEAN = 0.0f;
    private static final float DEFAULT_JITTER_STD_DEV = 0.05f;
    
    // Sensor type constants
    private static final int TYPE_ACCELEROMETER = 1;
    private static final int TYPE_MAGNETIC_FIELD = 2;
    private static final int TYPE_GYROSCOPE = 4;
    private static final int TYPE_PRESSURE = 6;
    private static final int TYPE_PROXIMITY = 8;
    private static final int TYPE_LIGHT = 5;
    private static final int TYPE_STEP_DETECTOR = 18;
    private static final int TYPE_STEP_COUNTER = 19;
    private static final int TYPE_SIGNIFICANT_MOTION = 17;
    private static final int TYPE_GRAVITY = 9;
    private static final int TYPE_LINEAR_ACCELERATION = 10;
    private static final int TYPE_ROTATION_VECTOR = 11;
    private static final int TYPE_ORIENTATION = 3;
    private static final int TYPE_GAME_ROTATION_VECTOR = 15;
    private static final int TYPE_GEOMAGNETIC_ROTATION_VECTOR = 20;
    
    // Random instance for jitter generation
    private final Random random;
    
    // Jitter parameters (loaded from SharedPref or defaults)
    private final float jitterMean;
    private final float jitterStdDev;
    
    public FakeSensor(LoadPackageParam loadPkgParam) {
        // Initialize random with seed based on current time for variability
        this.random = new Random(System.currentTimeMillis());
        
        // Load jitter parameters from SharedPref (allows runtime configuration)
        // Build constants can be used by setting these values in defaults.xml
        this.jitterMean = SharedPref.getXFloatValue("SensorJitterMean", DEFAULT_JITTER_MEAN);
        this.jitterStdDev = SharedPref.getXFloatValue("SensorJitterStdDev", DEFAULT_JITTER_STD_DEV);
        
        hookSensorVendor(loadPkgParam);
        hookSensorName(loadPkgParam);
        hookSensorEventValues(loadPkgParam);
    }
    
    /**
     * Hooks Sensor.getVendor() to return configurable vendor name.
     */
    private void hookSensorVendor(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.hardware.Sensor", 
                    loadPkgParam.classLoader, "getVendor", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String vendor = SharedPref.getXValue("SensorVendor", DEFAULT_SENSOR_VENDOR);
                    param.setResult(vendor);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("FakeSensor getVendor ERROR: " + t);
        }
    }
    
    /**
     * Hooks Sensor.getName() to return configurable sensor names based on type.
     */
    private void hookSensorName(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.hardware.Sensor", 
                    loadPkgParam.classLoader, "getName", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    // Get the sensor type from the object
                    int type = XposedHelpers.getIntField(param.thisObject, "mType");
                    String name = getSensorNameForType(type);
                    if (name != null) {
                        param.setResult(name);
                    }
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("FakeSensor getName ERROR: " + t);
        }
    }
    
    /**
     * Returns the configured sensor name for a given sensor type.
     */
    private String getSensorNameForType(int type) {
        switch (type) {
            case TYPE_ACCELEROMETER:
                return SharedPref.getXValue("AccelerometerName", DEFAULT_ACCELEROMETER_NAME);
            case TYPE_GYROSCOPE:
                return SharedPref.getXValue("GyroscopeName", DEFAULT_GYROSCOPE_NAME);
            case TYPE_MAGNETIC_FIELD:
                return SharedPref.getXValue("MagnetometerName", DEFAULT_MAGNETOMETER_NAME);
            case TYPE_PRESSURE:
                return SharedPref.getXValue("PressureName", DEFAULT_PRESSURE_NAME);
            case TYPE_PROXIMITY:
                return SharedPref.getXValue("ProximityName", DEFAULT_PROXIMITY_NAME);
            case TYPE_LIGHT:
                return SharedPref.getXValue("LightName", DEFAULT_LIGHT_NAME);
            case TYPE_STEP_DETECTOR:
                return SharedPref.getXValue("StepDetectorName", DEFAULT_STEP_DETECTOR_NAME);
            case TYPE_STEP_COUNTER:
                return SharedPref.getXValue("StepCounterName", DEFAULT_STEP_COUNTER_NAME);
            case TYPE_SIGNIFICANT_MOTION:
                return SharedPref.getXValue("SignificantMotionName", DEFAULT_SIGNIFICANT_MOTION_NAME);
            case TYPE_GRAVITY:
                return SharedPref.getXValue("GravityName", "QTI Gravity");
            case TYPE_LINEAR_ACCELERATION:
                return SharedPref.getXValue("LinearAccelerationName", "QTI Linear Acceleration");
            case TYPE_ROTATION_VECTOR:
                return SharedPref.getXValue("RotationVectorName", "QTI Rotation Vector");
            case TYPE_ORIENTATION:
                return SharedPref.getXValue("OrientationName", "QTI Orientation");
            case TYPE_GAME_ROTATION_VECTOR:
                return SharedPref.getXValue("GameRotationVectorName", "QTI Game Rotation Vector");
            case TYPE_GEOMAGNETIC_ROTATION_VECTOR:
                return SharedPref.getXValue("GeoMagneticRotationVectorName", "QTI GeoMagnetic Rotation Vector");
            default:
                return null; // Let original name pass through
        }
    }
    
    /**
     * Hooks SensorEventListener.onSensorChanged() to add jitter to sensor values.
     * This prevents variance-based emulator detection.
     */
    private void hookSensorEventValues(LoadPackageParam loadPkgParam) {
        try {
            XposedHelpers.findAndHookMethod("android.hardware.SensorEventListener", 
                    loadPkgParam.classLoader, "onSensorChanged", "android.hardware.SensorEvent", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Object sensorEvent = param.args[0];
                    if (sensorEvent == null) return;
                    
                    // Get the sensor type
                    Object sensor = XposedHelpers.getObjectField(sensorEvent, "sensor");
                    if (sensor == null) return;
                    
                    int type = XposedHelpers.getIntField(sensor, "mType");
                    
                    // Get the values array
                    float[] values = (float[]) XposedHelpers.getObjectField(sensorEvent, "values");
                    if (values == null || values.length == 0) return;
                    
                    // Apply jitter based on sensor type
                    applyJitterToValues(type, values);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("FakeSensor onSensorChanged ERROR: " + t);
        }
        
        // Alternative hook: Some apps use the SensorEvent directly from system sensor service
        try {
            XposedHelpers.findAndHookMethod("android.hardware.SystemSensorManager$SensorEventQueue", 
                    loadPkgParam.classLoader, "dispatchSensorEvent", 
                    int.class, float[].class, long.class, long.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    float[] values = (float[]) param.args[1];
                    if (values == null || values.length == 0) return;
                    
                    // Get sensor handle to determine type
                    int handle = (int) param.args[0];
                    int type = getTypeFromHandle(handle);
                    
                    // Apply jitter
                    applyJitterToValues(type, values);
                }
            });
        } catch (Throwable t) {
            XposedBridge.log("FakeSensor dispatchSensorEvent ERROR: " + t);
        }
    }
    
    /**
     * Applies Gaussian jitter to sensor values based on sensor type.
     * Only applies jitter to sensors that should have natural noise.
     */
    private void applyJitterToValues(int type, float[] values) {
        switch (type) {
            case TYPE_ACCELEROMETER:
            case TYPE_GYROSCOPE:
            case TYPE_MAGNETIC_FIELD:
            case TYPE_GRAVITY:
            case TYPE_LINEAR_ACCELERATION:
                // Apply jitter to all axes (x, y, z)
                for (int i = 0; i < Math.min(3, values.length); i++) {
                    values[i] += generateGaussianNoise();
                }
                break;
                
            case TYPE_ROTATION_VECTOR:
            case TYPE_GAME_ROTATION_VECTOR:
            case TYPE_GEOMAGNETIC_ROTATION_VECTOR:
            case TYPE_ORIENTATION:
                // Apply smaller jitter to rotation sensors
                float rotationJitter = jitterStdDev * 0.5f;
                for (int i = 0; i < values.length; i++) {
                    values[i] += generateGaussianNoise(0.0f, rotationJitter);
                }
                break;
                
            case TYPE_PRESSURE:
                // Pressure sensors have very small jitter
                if (values.length > 0) {
                    values[0] += generateGaussianNoise(0.0f, jitterStdDev * 0.1f);
                }
                break;
                
            case TYPE_PROXIMITY:
            case TYPE_LIGHT:
                // These sensors typically don't need jitter as they are discrete
                // But we can add minimal jitter if configured
                if (jitterStdDev > 0.01f) {
                    for (int i = 0; i < values.length; i++) {
                        values[i] += generateGaussianNoise(0.0f, jitterStdDev * 0.01f);
                    }
                }
                break;
                
            // Step detectors and counters should not have jitter (discrete events)
            case TYPE_STEP_DETECTOR:
            case TYPE_STEP_COUNTER:
            case TYPE_SIGNIFICANT_MOTION:
                // No jitter - these are event-based sensors
                break;
        }
    }
    
    /**
     * Generates Gaussian noise using Box-Muller transform.
     * @return Random value from Gaussian distribution with configured mean and std dev
     */
    private float generateGaussianNoise() {
        return generateGaussianNoise(jitterMean, jitterStdDev);
    }
    
    /**
     * Generates Gaussian noise with specified mean and standard deviation.
     * Uses Box-Muller transform for better distribution than simple Random.nextGaussian().
     */
    private float generateGaussianNoise(float mean, float stdDev) {
        // Use ThreadLocalRandom for better performance in multi-threaded environment
        // Box-Muller transform
        double u1 = random.nextDouble();
        double u2 = random.nextDouble();
        // Avoid log(0)
        if (u1 < 1e-10) u1 = 1e-10;
        
        double z0 = Math.sqrt(-2.0 * Math.log(u1)) * Math.cos(2.0 * Math.PI * u2);
        return (float) (mean + z0 * stdDev);
    }
    
    /**
     * Maps sensor handle to sensor type.
     * This is a simplified mapping - actual handles may vary by device.
     */
    private int getTypeFromHandle(int handle) {
        // Handle mapping is device-specific, but we can infer from common values
        // This is a fallback for the dispatchSensorEvent hook
        // In practice, the SensorEventListener hook should catch most cases
        return handle; // Return handle as-is, will be matched by caller if needed
    }
}
