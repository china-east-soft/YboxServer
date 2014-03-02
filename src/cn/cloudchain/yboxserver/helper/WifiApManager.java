package cn.cloudchain.yboxserver.helper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings.System;
import android.util.Log;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;
import cn.cloudchain.yboxcommon.bean.Types;
import android.net.wifi.HotspotClient;

/**
 * 处理和无线控制相关的功能
 * 
 * @author lazzy
 * 
 */
public class WifiApManager {
	private static final String tag = "WifiApManager";
	private Context context;
	private final WifiManager mWifiManager;
	public static final String WIFI_AP_STATE_CHANGED_ACTION = "android.net.wifi.WIFI_AP_STATE_CHANGED";
	public static final String EXTRA_WIFI_AP_STATE = "wifi_state";
	public static final int WIFI_AP_STATE_DISABLING = 10;
	public static final int WIFI_AP_STATE_DISABLED = 11;
	public static final int WIFI_AP_STATE_ENABLING = 12;
	public static final int WIFI_AP_STATE_ENABLED = 13;
	public static final int WIFI_AP_STATE_FAILED = 14;

	public static final String WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION = "android.net.wifi.WIFI_HOTSPOT_CLIENTS_CHANGED";

	public static final String WIFI_HOTSPOT_OVERLAP_ACTION = "android.net.wifi.WIFI_HOTSPOT_OVERLAP";

	public WifiApManager(Context context) {
		this.context = context;
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
	 * @param mac
	 * @return
	 */
	public boolean addToBlacklist(String mac) {
		List<HotspotClient> mClientList = mWifiManager.getHotspotClients();
		if (mClientList == null) {
			return false;
		}
		boolean result = false;
		for (HotspotClient client : mClientList) {
			if (mac.equals(client.deviceAddress)) {
				result = mWifiManager.blockClient(client);
				break;
			}
		}
		return result;
	}

	/**
	 * 没有起到清除禁止的作用
	 * 
	 * @return
	 */
	public boolean clearBlacklist(String mac) {
		List<HotspotClient> mClientList = mWifiManager.getHotspotClients();
		if (mClientList == null) {
			return false;
		}
		boolean result = false;
		for (HotspotClient client : mClientList) {
			if (mac.equals(client.deviceAddress)) {
				result = mWifiManager.unblockClient(client);
				break;
			}
		}
		return result;
	}

	/**
	 * 返回所有连接的设备信息
	 * 
	 * @param type 
	 * @return 不为null
	 */
	public List<DeviceInfo> getDeviceList(int type) {
		List<HotspotClient> mClientList = mWifiManager.getHotspotClients();
		if (mClientList == null) {
			return null;
		}
		List<DeviceInfo> deviceList = new ArrayList<DeviceInfo>(
				mClientList.size());
		for (HotspotClient client : mClientList) {
			if(client.isBlocked && type == Types.DEVICES_UNBLOCK) {
				continue;
			} else if(!client.isBlocked && type == Types.DEVICES_BLOCK) {
				continue;
			}
			DeviceInfo info = new DeviceInfo();
			info.mac = client.deviceAddress;
			info.ip = mWifiManager.getClientIp(info.mac);
			info.blocked = client.isBlocked;
			deviceList.add(info);
		}

		return deviceList;

	}

	/**
	 * 设置最大支持的设备数
	 * 
	 * @return
	 */
	public boolean setMaxClientNum(int num) {
		return System.putInt(context.getContentResolver(),
				System.WIFI_HOTSPOT_MAX_CLIENT_NUM, num);
	}

	/**
	 * 获取最大支持的设备数
	 * 
	 * @return
	 */
	public int getMaxClientNum() {
		return System.getInt(context.getContentResolver(),
				System.WIFI_HOTSPOT_MAX_CLIENT_NUM,
				System.WIFI_HOTSPOT_DEFAULT_CLIENT_NUM);
	}

	/**
	 * 设置热点自动关闭时间
	 * 
	 * @param value
	 * @return
	 */
	public boolean setWifiAutoDisable(int value) {
		return System.putInt(context.getContentResolver(),
				System.WIFI_HOTSPOT_AUTO_DISABLE, value);
	}

	/**
	 * 获取热点自动关闭时间
	 * 
	 * @return
	 */
	public int getWifiAutoDisable() {
		return System.getInt(context.getContentResolver(),
				System.WIFI_HOTSPOT_AUTO_DISABLE,
				System.WIFI_HOTSPOT_AUTO_DISABLE_FOR_FIVE_MINS);
	}
}
