package cn.cloudchain.yboxserver.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.AuthAlgorithm;
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
					boolean once = params.optBoolean("once", false);
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
			} else if (OperType.signal_quality.getValue() == oper) {
				result = getSignalQuality();
			} else if (OperType.storage.getValue() == oper) {
				result = getStorageDetail();
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
		return getDefaultJson(phoneManager.shutdown(restart));
	}

	/**
	 * 获取电量值
	 * 
	 * @return {"result":true, "remain":59}
	 */
	private String getBattery() {
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
	 * 获取3G信号质量
	 * 
	 * @return {"result":true, "strength":-102}
	 */
	private String getSignalQuality() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true).name("strength")
					.value(MyApplication.getInstance().signalStrength)
					.endObject();
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
	private String setMobileDataEnable(boolean enable) {
		return getDefaultJson(phoneManager.setMobileDataEnabled(enable));
	}

	/**
	 * 设置无线参数
	 * 
	 * @param ssid
	 * @param pass
	 * @return
	 */
	private String setWifiInfo(String ssid, String pass) {
		Log.i(TAG, "ssid = " + ssid + "; pass = " + pass);
		WifiConfiguration wifiConfig = new WifiConfiguration();
		wifiConfig.SSID = ssid;
		if (TextUtils.isEmpty(pass)) {
			wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
		} else {
			wifiConfig.allowedKeyManagement.set(KeyMgmt.WPA_PSK);
			wifiConfig.allowedAuthAlgorithms.set(AuthAlgorithm.OPEN);
			wifiConfig.preSharedKey = pass;
		}
		wifiConfig.status = WifiConfiguration.Status.ENABLED;
		wifiConfig.hiddenSSID = false;
		return getDefaultJson(wifiApManager.setWifiApConfiguration(wifiConfig));
	}

	/**
	 * 返回无线参数
	 * 
	 * @return
	 */
	private String getWifiInfo() {
		WifiConfiguration config = wifiApManager.getWifiApConfiguration();
		if (config == null)
			return getDefaultJson(false);

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true).name("ssid")
					.value(config.SSID);
			if (!config.allowedKeyManagement.get(KeyMgmt.NONE)) {
				jWriter.name("pass").value(config.preSharedKey);
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

	/**
	 * 获取连接终端的所有设备信息
	 * 
	 * @return
	 */
	private String getDevices() {
		List<DeviceInfo> infos = wifiApManager.getDeviceList();
		if (infos == null)
			return getDefaultJson(false);

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
				jWriter.name("block").value(info.blocked);
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
	private String addToBlackList(String mac, boolean once) {
		return getDefaultJson(wifiApManager.addToBlacklist(mac));
	}

	/**
	 * 清除黑名单
	 * 
	 * @param mac
	 *            设备号，为""时清除所有的黑名单
	 * @return
	 */
	private String clearBlackList(String mac) {
		return getDefaultJson(wifiApManager.clearBlacklist(mac));
	}

	/**
	 * 获取SD卡存储信息
	 * 
	 * @return {"result":true, "total":"20G","remain":"512MB"}
	 */
	private String getStorageDetail() {
		double totalSize = Helper.getInstance().getSDcardTotalMemory();
		if (totalSize < 0) {
			return getErrorJson(1, "sdcard not available");
		}
		double availableSize = Helper.getInstance().getSDcardAvailableMemory();

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(true);
			jWriter.name("total").value(getStorageBySize(totalSize));
			jWriter.name("remain").value(getStorageBySize(availableSize));
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
	 * 获取便于阅读的存储大小
	 * 
	 * @param size
	 * @return
	 */
	private String getStorageBySize(double size) {
		StringBuilder builder = new StringBuilder();
		int i = 0;
		while (size >= 500 && i < 4) {
			size = size / 1000;
			++i;
		}

		builder.append(String.format("%.2f", size));
		switch (i) {
		case 0:
			builder.append('B');
			break;
		case 1:
			builder.append("KB");
			break;
		case 2:
			builder.append("MB");
			break;
		case 3:
			builder.append("GB");
			break;
		case 4:
			builder.append("TB");
			break;
		}
		return builder.toString();
	}

	/**
	 * 获取错误返回 {"result":false, "error_code":1, "error_msg":"json invalid"}
	 * 
	 * @param code
	 *            可选，值<0时不显示
	 * @param msg
	 *            可选，为空时不显示
	 * @return
	 */
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

	/**
	 * 获取默认的返回
	 * 
	 * @param success
	 * @return {"result":success}
	 */
	private String getDefaultJson(boolean success) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name("result").value(success).endObject();
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
