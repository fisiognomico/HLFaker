# Xposed
-keep class de.robv.android.xposed.** { *; }
-keep class com.hl46000.hlfaker.** { *; }

# Keep entry point
-keep class com.hl46000.hlfaker.MainHook {
    public void handleLoadPackage(...);
}

# Keep classes used in Xposed hooks
-keepclassmembers class * {
    public void handleLoadPackage(...);
}
