package cn.cloudchain.yboxserver.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;
import cn.cloudchain.yboxserver.MyApplication;
import cn.cloudchain.yboxserver.bean.DeviceInfo;
import cn.cloudchain.yboxserver.bean.OperType;

public class SetHelper {
	final String TAG = SetHelper.class.getSimpleName();
	private static SetHelper instance;
	private PhoneManager phoneManager;
	private WifiApManager wifiApManager;

	private final int error_oper_invalid = 1;
	private final int error_json_invalid = 2;

	/*
	 * 一般错误 1 空字符串 2
	 */

	public static SetHelper getInstance() {
		if (instance == null)
			instance = new SetHelper();
		return instance;
	}

	private SetHelper() {
		phoneManager = new PhoneManager(MyApplication.getAppContext());
		wifiApManager = new WifiApManager(MyApplication.getAppContext());
	}

	public String handleJsonRequest(String operation) {
		String result = getErrorJson(error_oper_invalid, null);
		if (TextUtils.isEmpty(operation)) {
			return result;
		}
		try {
			JSONObject obj = new JSONObject(operation);
			int oper = obj.optInt("oper");
			JSONObject params = obj.optJSONObject("params");

			if (OperType.shutdown.getValue() == oper) {
				boolean restart = params == null ? false : params.optBoolean(
						"restart", false);
				result = shutdown(restart);
			} else if (OperType.battery.getValue() == oper) {
				result = getBattery();
			} else if (OperType.wifi_info.getValue() == oper) {
				result = getWifiInfo();
			} else if (OperType.wifi_info_set.getValue() == oper) {
				if (params == null) {
					result = getErrorJson(104, "with no params!");
				} else {
					String ssid = params.optString("ssid");
					String pass = params.optString("pass");
					result = setWifiInfo(ssid, pass);
				}
			} else if (OperType.wifi_devices.getValue() == oper) {
				result = getDevices();
			} else if (OperType.wifi_blacklist_add.getValue() == oper) {
				if (params == null) {
					result = getErrorJson(104, "with no params!");
				} else {
					String mac = params.optString("mac");
					boolean once = params.optBoolean("once");
					result = addToBlackList(mac, once);
				}
			} else if (OperType.wifi_blacklist_clear.getValue() == oper) {
				if (params == null) {
					result = getErrorJson(104, "with no params!");
				} else {
					String mac = params.optString("mac");
					result = clearBlackList(mac);
				}
			} else if (OperType.mobile_data_control.getValue() == oper) {
				if (params == null) {
					result = getErrorJson(104, "with no params!");
				} else {
					boolean enable = params.optBoolean("enable");
					result = setMobileDataEnable(enable);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			result = getErrorJson(error_json_invalid, "json exception");
		}
		return result;
	}

	/**
	 * 关机
	 * 
	 * @param restart
	 *            true时重启
	 * @return 请求结果
	 */
	private String shutdown(boolean restart) {
		phoneManager.shutdown(restart);
		return getDefaultSuccessJson();
	}

	/**
	 * 获取电量值
	 * 
	 * @return {"result":true, "remain":59}
	 */
	public String getBattery() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true).name("remain")
					.value(phoneManager.getBattery()).endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	/**
	 * 设置手机数据
	 * 
	 * @param enable
	 *            true时允许数据流量
	 * @return
	 */
	public String setMobileDataEnable(boolean enable) {
		return phoneManager.setMobileDataEnabled(enable) ? getDefaultSuccessJson()
				: getErrorJson();
	}

	/**
	 * 设置无线参数
	 * 
	 * @param ssid
	 * @param pass
	 * @return
	 */
	public String setWifiInfo(String ssid, String pass) {
		Log.i(TAG, "ssid = " + ssid + "; pass = " + pass);
		WifiConfiguration wifiConfig = new WifiConfiguration();
		// StringBuilder builder = new StringBuilder(20);
		// builder.append('\"');
		// builder.append(ssid);
		// builder.append('\"');
		// wifiConfig.SSID = builder.toString();
		wifiConfig.SSID = ssid;
		if (TextUtils.isEmpty(pass)) {
			wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
		} else {
			wifiConfig.allowedKeyManagement.clear();
			wifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
			wifiConfig.preSharedKey = pass;
		}
		wifiConfig.status = WifiConfiguration.Status.ENABLED;
		wifiConfig.hiddenSSID = false;
		return wifiApManager.setWifiApConfiguration(wifiConfig) ? getDefaultSuccessJson()
				: getErrorJson();
	}

	/**
	 * 返回无线参数
	 * 
	 * @return
	 */
	public String getWifiInfo() {
		WifiConfiguration config = wifiApManager.getWifiApConfiguration();
		if (config == null)
			return getErrorJson();

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true).name("ssid")
					.value(config.SSID).endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	/**
	 * 获取连接终端的所有设备信息
	 * 
	 * @return
	 */
	public String getDevices() {
		List<DeviceInfo> infos = wifiApManager.getDeviceList();
		if (infos == null)
			return getErrorJson();

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true).name("devices");
			jWriter.beginArray();

			for (DeviceInfo info : infos) {
				jWriter.beginObject();
				jWriter.name("name").value(info.name);
				jWriter.name("mac").value(info.mac);
				jWriter.name("ip").value(info.ip);
				jWriter.endObject();
			}

			jWriter.endArray();
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	/**
	 * 将设备加入黑名单
	 * 
	 * @param mac
	 *            设备号
	 * @param once
	 *            true是为暂时拒绝该设备连接，false时永久加入黑名单
	 * @return
	 */
	public String addToBlackList(String mac, boolean once) {
		return wifiApManager.addToBlacklist(mac) ? getDefaultSuccessJson()
				: getErrorJson();
	}

	/**
	 * 清除黑名单
	 * 
	 * @param mac
	 *            设备号，为""时清除所有的黑名单
	 * @return
	 */
	public String clearBlackList(String mac) {
		return wifiApManager.clearBlacklist(mac) ? getDefaultSuccessJson()
				: getErrorJson();
	}

	private String getErrorJson() {
		return getErrorJson(-1, null);
	}

	private String getErrorJson(int code, String msg) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(false);
			if (code > 0) {
				jWriter.name("error_code").value(code);
			}
			if (!TextUtils.isEmpty(msg)) {
				jWriter.name("error_msg").value(msg);
			}
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	private String getDefaultSuccessJson() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true).endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}
}
