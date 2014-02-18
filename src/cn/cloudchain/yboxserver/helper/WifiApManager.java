package cn.cloudchain.yboxserver.helper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.util.Log;
import cn.cloudchain.yboxserver.bean.DeviceInfo;

/**
 * 处理和无线控制相关的功能
 * 
 * @author lazzy
 * 
 */
public class WifiApManager {
	private static final String tag = "WifiApManager";
	private final WifiManager mWifiManager;

	public WifiApManager(Context context) {
		mWifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	public WifiManager getWifiManager() {
		return mWifiManager;
	}

	public int getWifiApState() {
		int state = -1;
		try {
			Method method = WifiManager.class.getMethod("getWifiApState");
			state = (Integer) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return state;
	}

	public boolean isWifiApEnabled() {
		boolean result = false;
		try {
			Method method = WifiManager.class.getMethod("isWifiApEnabled");
			result = (Boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	public WifiConfiguration getWifiApConfiguration() {
		WifiConfiguration configuration = null;
		try {
			Method method = WifiManager.class
					.getMethod("getWifiApConfiguration");
			configuration = (WifiConfiguration) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return configuration;
	}

	public boolean setWifiApEnabled(WifiConfiguration wifiConfig, boolean enable) {
		boolean currentState = isWifiApEnabled();
		if (currentState == enable) {
			return true;
		}

		if (enable && !setWifiEnabled(false)) {
			return false;
		}

		boolean result = false;
		try {
			Method method = WifiManager.class.getMethod("setWifiApEnabled",
					WifiConfiguration.class, Boolean.TYPE);
			result = (Boolean) method.invoke(mWifiManager, wifiConfig, enable);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	public boolean setWifiApConfiguration(WifiConfiguration wifiConfig) {
		boolean result = false;
		try {
			Method method = WifiManager.class.getMethod(
					"setWifiApConfiguration", WifiConfiguration.class);
			result = (Boolean) method.invoke(mWifiManager, wifiConfig);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	public boolean setWifiEnabled(boolean enable) {
		if (mWifiManager.isWifiEnabled() == enable) {
			return true;
		}
		return mWifiManager.setWifiEnabled(enable);
	}

	/**
	 * 没有起到禁止连接的作用
	 * 
	 * @param bssid
	 * @return
	 */
	public boolean addToBlacklist(String bssid) {
		boolean result = false;
		try {
			Method method = WifiManager.class.getMethod("addToBlacklist",
					String.class);
			result = (Boolean) method.invoke(mWifiManager, bssid);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 没有起到清除禁止的作用
	 * 
	 * @return
	 */
	public boolean clearBlacklist(String bssid) {
		boolean result = false;
		try {
			Method method = WifiManager.class.getMethod("clearBlacklist");
			result = (Boolean) method.invoke(mWifiManager);
		} catch (Exception e) {
			Log.e(tag, e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 返回所有连接的设备信息
	 * 
	 * @return 不为null
	 */
	public List<DeviceInfo> getDeviceList() {
		List<DeviceInfo> array = new ArrayList<DeviceInfo>();
		try {
			FileReader fileReader = new FileReader("/proc/net/arp");
			BufferedReader br = new BufferedReader(fileReader);
			String line;
			while ((line = br.readLine()) != null) {
				Log.i(tag, line);
				if (line.contains("address")) {
					continue;
				}
				DeviceInfo info = new DeviceInfo();
				StringTokenizer tokenizer = new StringTokenizer(line);
				for (int i = 0; tokenizer.hasMoreElements(); ++i) {
					if (i == 0) {
						info.ip = (String) tokenizer.nextElement();
					} else if (i == 3) {
						info.mac = (String) tokenizer.nextElement();
					}

				}
				array.add(info);
			}
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return array;
	}
}
