package com.hl46000.hlfaker;

import java.util.Arrays;
import com.hl46000.hlfaker.SharedPref;
import android.accounts.Account;
import android.text.TextUtils;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * FakeEmail - Hooks AccountManager to return fake Gmail account.
 * 
 * Uses default values to prevent crashes when SharedPreferences are not initialized.
 * Default values:
 * - Email: hl.46000@gmail.com
 * - Target Package: com.alibaba.aliexpresshd
 */
public class FakeEmail {
    
    // Default values
    private static final String DEFAULT_EMAIL = "hl.46000@gmail.com";
    private static final String DEFAULT_FAKE_EMAIL_PACKAGE = "com.alibaba.aliexpresshd";

	public void fakeGmail(final LoadPackageParam loadPkgParam){
		try {
			XposedHelpers.findAndHookMethod("android.accounts.AccountManager", loadPkgParam.classLoader, "getAccounts", new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					String fakeEmailPackage = SharedPref.getXValue("FakeEmailPackge", DEFAULT_FAKE_EMAIL_PACKAGE);
					if (getPackage(fakeEmailPackage, loadPkgParam.packageName)) {
						String email = SharedPref.getXValue("Email", DEFAULT_EMAIL);
						param.setResult(new Account[]{new Account(email, "com.google")});
			        }
				}
				
			});
			XposedHelpers.findAndHookMethod("android.accounts.AccountManager", loadPkgParam.classLoader, "getAccountsByType", String.class, new XC_MethodHook() {

				@Override
				protected void afterHookedMethod(MethodHookParam param)
						throws Throwable {
					// TODO Auto-generated method stub
					super.afterHookedMethod(param);
					String fakeEmailPackage = SharedPref.getXValue("FakeEmailPackge", DEFAULT_FAKE_EMAIL_PACKAGE);
					if (getPackage(fakeEmailPackage, loadPkgParam.packageName)) {
						String email = SharedPref.getXValue("Email", DEFAULT_EMAIL);
						param.setResult(new Account[]{new Account(email, "com.google")});
			        }
				}
				
			});
		} catch (Exception e) {
			XposedBridge.log("Fake Email ERROR: " + e.getMessage());
		}
		
	}
	
	public static boolean getPackage(String lisPkg, String pkg) {
        if (TextUtils.isEmpty(lisPkg)) {
            return false;
        }
        return Arrays.asList(TextUtils.split(lisPkg.replace(" ", ""), ",")).contains(pkg);
    }
	
}
