package com.hl46000.hlfaker;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
import com.hl46000.hlfaker.FakeBattery;
import com.hl46000.hlfaker.FakeBluetooth;
import com.hl46000.hlfaker.FakeBuildInfo;
import com.hl46000.hlfaker.FakeEmail;
import com.hl46000.hlfaker.FakeHardwareInfo;
import com.hl46000.hlfaker.FakeNetwork;
import com.hl46000.hlfaker.FakeOpenGL;
import com.hl46000.hlfaker.FakeRAM;
import com.hl46000.hlfaker.FakeSensor;
import com.hl46000.hlfaker.RootCloak;
import com.hl46000.hlfaker.FakeCPU;
import com.hl46000.hlfaker.FakeDebugFlags;
import com.hl46000.hlfaker.FakeSystemProperties;

public class MainHook implements IXposedHookLoadPackage {

	@Override
	public void handleLoadPackage(final LoadPackageParam sharePkgParam) throws Throwable {
		XposedBridge.log("HLFaker: loading for " + sharePkgParam.packageName);
		tryHook("FakeSystemProperties", () -> new FakeSystemProperties(sharePkgParam));
		tryHook("FakeDebugFlags",       () -> new FakeDebugFlags(sharePkgParam));
		tryHook("FakeBattery",          () -> new FakeBattery().fakePinStt(sharePkgParam));
		tryHook("FakeBluetooth",        () -> new FakeBluetooth(sharePkgParam));
		tryHook("FakeNetwork",          () -> new FakeNetwork(sharePkgParam));
		tryHook("FakeHardwareInfo",     () -> new FakeHardwareInfo(sharePkgParam));
		tryHook("FakeBuildInfo",        () -> new FakeBuildInfo(sharePkgParam));
		tryHook("FakeOpenGL",           () -> new FakeOpenGL().FakeDisplay(sharePkgParam));
		tryHook("FakeSensor",           () -> new FakeSensor(sharePkgParam));
		tryHook("FakeEmail",            () -> new FakeEmail().fakeGmail(sharePkgParam));
		tryHook("RootCloak",            () -> new RootCloak().handleLoadPackage(sharePkgParam));
		tryHook("FakeCPU",              () -> new FakeCPU(sharePkgParam));
		tryHook("FakeRAM",              () -> new FakeRAM(sharePkgParam));
	}

	private void tryHook(String name, Runnable r) {
		try {
			r.run();
			XposedBridge.log("HLFaker: " + name + " OK");
		} catch (Throwable t) {
			XposedBridge.log("HLFaker: " + name + " FAILED: " + t);
		}
	}

}
