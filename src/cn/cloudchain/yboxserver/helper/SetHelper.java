package cn.cloudchain.yboxserver.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiConfiguration.KeyMgmt;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;
import android.util.SparseArray;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.DeviceInfo;
import cn.cloudchain.yboxcommon.bean.ErrorBean;
import cn.cloudchain.yboxcommon.bean.OperType;
import cn.cloudchain.yboxcommon.bean.Types;
import cn.cloudchain.yboxserver.MyApplication;

import com.ybox.hal.BSPSystem;
import com.yyxu.download.utils.MyIntents;
import com.yyxu.download.utils.NetworkUtils;
import com.yyxu.download.utils.StorageUtils;

public class SetHelper {
	final String TAG = SetHelper.class.getSimpleName();
	private static SetHelper instance;
	private PhoneManager phoneManager;
	private WifiApManager wifiApManager;
	private DataUsageHelper dataUsageHelper;
	private BSPSystem bspSystem;

	private String middleName = "YboxServer";
	private String middleVersion = "";
	private String imageName = "";
	private String imageVersion = "";

	/*
	 * 一般错误 1 空字符串 2
	 */

	public static SetHelper getInstance() {
		if (instance == null)
			instance = new SetHelper();
		return instance;
	}

	private SetHelper() {
		final Context context = MyApplication.getAppContext();
		phoneManager = new PhoneManager(context);
		wifiApManager = new WifiApManager(context);
		dataUsageHelper = new DataUsageHelper(context);
		bspSystem = new BSPSystem(context);

		middleVersion = Helper.getInstance().getVersionName(context);
		imageName = Helper.getInstance().getImageName();
		imageVersion = Helper.getInstance().getImageVersionName();
	}

	public String handleJsonRequest(String operation) {
		String result = getErrorJson(ErrorBean.OPER_NOT_EXIST);
		if (TextUtils.isEmpty(operation)) {
			return result;
		}
		try {
			JSONObject obj = new JSONObject(operation);
			int oper = obj.optInt(Constants.OPER);
			JSONObject params = obj.optJSONObject(Constants.PARAMS);

			switch (OperType.getOperType(oper)) {
			case battery:
				result = getBattery();
				break;
			case ethernet_dhcp_set:
				result = setEthDhcp();
				break;
			case ethernet_info:
				result = getEthInfo();
				break;
			case ethernet_static_set:
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					String ip = params.optString(Constants.Wlan.IP);
					String gateway = params.optString(Constants.Wlan.GATEWAY);
					String mask = params.optString(Constants.Wlan.SUBMASK);
					String dns1 = params.optString(Constants.Wlan.DNS1);
					String dns2 = params.optString(Constants.Wlan.DNS2);
					result = setEthStatic(ip, gateway, mask, dns1, dns2);
				}
				break;
			case mobile_data_control:
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					boolean enable = params.optBoolean(Constants.ENABLE);
					result = setMobileDataEnable(enable);
				}
				break;
			case mobile_net_info:
				result = getMobileNetInfo();
				break;
			case mobile_traffic_info:
				result = getMobileTrafficInfo();
				break;
			case shutdown:
				result = shutdown(false);
				break;
			case signal_quality:
				result = getSignalQuality();
				break;
			case sleep:
				result = goToSleep();
				break;
			case storage:
				result = FileManager.getInstance().getStorageInfo();
				break;
			case traffic:
				break;
			case wifi_auto_disable_info:
				result = getWifiAutoDisable();
				break;
			case wifi_auto_disable_set:
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					int time = params.optInt(Constants.TYPE);
					result = setWifiAutoDisable(time);
				}
				break;
			case wifi_blacklist_add:
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					String mac = params.optString(Constants.DeviceInfo.MAC);
					result = addToBlackList(mac);
				}
				break;
			case wifi_blacklist_clear:
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					String mac = params.optString(Constants.DeviceInfo.MAC);
					result = clearBlackList(mac);
				}
				break;
			case wifi_devices: {
				int type = Types.DEVICES_ALL;
				if (params != null) {
					type = params.optInt(Constants.TYPE, Types.DEVICES_ALL);
				}
				result = getDevices(type);
				break;
			}
			case wifi_info:
				result = getWifiInfo();
				break;
			case wifi_info_set: {
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					String ssid = params.optString(Constants.Wifi.SSID);
					int channel = params.optInt(Constants.Wifi.CHANNEL);
					result = setWifiInfo(ssid, channel);
				}
			}
				break;
			case wifi_restart:
				result = restartWifiAp();
				break;
			case auto_sleep_info:
				result = getAutoSleepType();
				break;
			case auto_sleep_set:
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					int type = params.optInt(Constants.TYPE);
					result = setAutoSleepType(type);
				}
				break;
			case ybox_update: {
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					String imageUrl = params
							.optString(Constants.Update.IMAGE_URL);
					String middleUrl = params
							.optString(Constants.Update.MIDDLE_URL);
					if (TextUtils.isEmpty(imageUrl)
							&& TextUtils.isEmpty(middleUrl)) {
						result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
					} else {
						result = yboxUpdate(imageUrl, middleUrl);
					}
				}
				break;
			}
			case ybox_update_download: {
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					String imageUrl = params
							.optString(Constants.Update.IMAGE_URL);
					String middleUrl = params
							.optString(Constants.Update.IMAGE_URL);
					if (TextUtils.isEmpty(imageUrl)
							&& TextUtils.isEmpty(middleUrl)) {
						result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
					} else {
						result = yboxUpdateDownload(imageUrl, middleUrl);
					}
				}
				break;
			}
			case device_info: {
				result = getDeviceInfo();
				break;
			}
			case files_detail: {
				int type = params == null ? Types.FILE_ALL : params.optInt(
						Constants.TYPE, Types.FILE_ALL);
				switch (type) {
				case Types.FILE_ALL:
					result = FileManager.getInstance().getFilesInDirectory(
							params == null ? "" : params
									.optString(Constants.File.PATH_ABSOLUTE));
					break;
				case Types.FILE_ONLY_AUDIO:
					result = FileManager.getInstance().audioStream();
					break;
				case Types.FILE_ONLY_VIDEO:
					result = FileManager.getInstance().videoStream();
					break;
				}
				break;
			}
			case files_delete: {
				if (params == null) {
					result = getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
				} else {
					JSONArray files = params.optJSONArray(Constants.File.FILES);
					result = deleteFiles(files);
				}
				break;
			}
			case files_download:
				break;
			default:
				break;

			}
		} catch (JSONException e) {
			e.printStackTrace();
			result = getErrorJson(ErrorBean.REQUEST_FORMAT_WRONG);
		}
		return result;
	}

	/**
	 * 删除文件/文件夹
	 * 
	 * @param fileArray
	 * @return
	 */
	private String deleteFiles(JSONArray fileArray) {
		if (fileArray == null) {
			return getErrorJson(ErrorBean.REQUEST_PARAMS_INVALID);
		}
		for (int index = 0; index < fileArray.length(); ++index) {
			FileManager.getInstance().removeFileOrDirectory(
					fileArray.optString(index));
		}
		return getDefaultJson(true);
	}

	/**
	 * 获取设备信息，如设备名，MAC地址，版本信息
	 * 
	 * @return
	 */
	private String getDeviceInfo() {
		String mac = Helper.getInstance().getDevicesMac(
				MyApplication.getAppContext());
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true)
					.name(Constants.DeviceInfo.MAC).value(mac);
			jWriter.name(Constants.Update.IMAGE_NAME).value(imageName);
			jWriter.name(Constants.Update.IMAGE_VERSION).value(imageVersion);
			jWriter.name(Constants.Update.MIDDLE_NAME).value(middleName);
			jWriter.name(Constants.Update.MIDDLE_VERSION).value(middleVersion);
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
	 * 安装升级包
	 * 
	 * @param imageUrl
	 * @param middleUrl
	 * @return
	 */
	private String yboxUpdate(String imageUrl, String middleUrl) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true);
			boolean updating = false;
			if (!TextUtils.isEmpty(imageUrl)) {
				String filePath = StorageUtils.FILE_ROOT
						+ NetworkUtils.getFileNameFromUrl(imageUrl);
				int result = bspSystem.setUpgradeImg(1, filePath);
				if (result == 0) {
					updating = true;
				}
				jWriter.name(Constants.Update.IMAGE_RESULT).value(result);
			}

			if (!TextUtils.isEmpty(middleUrl)) {
				if (updating) {
					PreferenceHelper.putInt(PreferenceHelper.MIDDLE_UPDATE,
							PreferenceHelper.MIDDLE_UPDATE_RESTART);
					PreferenceHelper.putString(
							PreferenceHelper.IMAGE_UPDATE_PATH, middleUrl);
				} else {
					String filePath = StorageUtils.FILE_ROOT
							+ NetworkUtils.getFileNameFromUrl(middleUrl);
					int result = bspSystem.install_apk_slient(filePath);
					jWriter.name(Constants.Update.MIDDLE_URL).value(result);
				}
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
	 * 下载升级包
	 * 
	 * @param imageUrl
	 *            升级链接
	 * @param middleUrl
	 * @return
	 */
	private String yboxUpdateDownload(String imageUrl, String middleUrl) {
		if (!TextUtils.isEmpty(imageUrl)) {
			Intent downloadIntent = new Intent(
					"com.yyxu.download.services.IDownloadService");
			downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.ADD);
			downloadIntent.putExtra(MyIntents.URL, imageUrl);
			MyApplication.getAppContext().startService(downloadIntent);
		}
		if (!TextUtils.isEmpty(middleUrl)) {
			Intent downloadIntent = new Intent(
					"com.yyxu.download.services.IDownloadService");
			downloadIntent.putExtra(MyIntents.TYPE, MyIntents.Types.ADD);
			downloadIntent.putExtra(MyIntents.URL, middleUrl);
			MyApplication.getAppContext().startService(downloadIntent);
		}
		return getDefaultJson(true);
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
	 * @return {Constants.RESULT:true, "remain":59}
	 */
	private String getBattery() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true)
					.name(Constants.REMAIN).value(phoneManager.getBattery())
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
	 * 获取3G信号质量
	 * 
	 * @return {Constants.RESULT:true, "strength":-102}
	 */
	private String getSignalQuality() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true)
					.name(Constants.DeviceInfo.STRENGTH)
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
	 * 获取手机流量信息
	 * 
	 * @return {Constants.RESULT:true, "limit": 2122, "warn":123,
	 *         "today":{"rx":1, "tx":1},
	 *         "month":{"rx":1,"tx":1},"total":{"rx":1;"tx":1}}
	 */
	private String getMobileTrafficInfo() {
		List<SparseArray<Long>> list = dataUsageHelper.getMobileData();
		if (list == null || list.size() == 0)
			return getErrorJson(ErrorBean.SIM_NOT_READY);

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true);
			SparseArray<Long> item = list.get(0);
			jWriter.name(Constants.Traffic.LIMIT).value(
					item.get(DataUsageHelper.KEY_LIMIT));
			jWriter.name(Constants.Traffic.WARN).value(
					item.get(DataUsageHelper.KEY_WARNING));

			jWriter.name(Constants.Traffic.TODAY).beginObject();
			jWriter.name(Constants.Traffic.RX).value(
					item.get(DataUsageHelper.KEY_RX_TODAY));
			jWriter.name(Constants.Traffic.TX).value(
					item.get(DataUsageHelper.KEY_TX_TODAY));
			jWriter.endObject();

			jWriter.name(Constants.Traffic.MONTH).beginObject();
			jWriter.name(Constants.Traffic.RX).value(
					item.get(DataUsageHelper.KEY_RX_MONTH));
			jWriter.name(Constants.Traffic.TX).value(
					item.get(DataUsageHelper.KEY_TX_MONTH));
			jWriter.endObject();

			jWriter.name(Constants.Traffic.TOTAL).beginObject();
			jWriter.name(Constants.Traffic.RX).value(
					item.get(DataUsageHelper.KEY_RX_TOTAL));
			jWriter.name(Constants.Traffic.TX).value(
					item.get(DataUsageHelper.KEY_TX_TOTAL));
			jWriter.endObject();

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
	 * 设置无线参数
	 * 
	 * @param ssid
	 * @param pass
	 * @param keymgmt
	 * @param maxclient
	 * @return
	 */
	private String setWifiInfo(String ssid, int channel) {
		WifiConfiguration wifiConfig = wifiApManager.getWifiApConfiguration();
		wifiConfig.SSID = "YBOX-" + ssid;
		wifiConfig.allowedKeyManagement.set(KeyMgmt.NONE);
		wifiConfig.status = WifiConfiguration.Status.ENABLED;
		wifiConfig.hiddenSSID = false;
		wifiConfig.channelWidth = channel;

		boolean result = wifiApManager.setWifiApConfiguration(wifiConfig);
		return getDefaultJson(result);
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
			jWriter.beginObject().name(Constants.RESULT).value(true)
					.name(Constants.Wifi.SSID).value(config.SSID);
			jWriter.name(Constants.Wifi.CHANNEL).value(config.channelWidth);
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
	private String getDevices(int type) {
		List<DeviceInfo> infos = wifiApManager.getDeviceList(type);
		if (infos == null)
			return getDefaultJson(false);

		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true)
					.name(Constants.Hotspot.DEVICES);
			jWriter.beginArray();

			for (DeviceInfo info : infos) {
				jWriter.beginObject();
				jWriter.name(Constants.Hotspot.NAME).value(info.name);
				jWriter.name(Constants.Hotspot.MAC).value(info.mac);
				jWriter.name(Constants.Hotspot.IP).value(info.ip);
				jWriter.name(Constants.Hotspot.BLOCK).value(info.blocked);
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
	 * @return
	 */
	private String addToBlackList(String mac) {
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
	 * 获取以太网信息
	 * 
	 * @return "mode":-1(none)|1(static)|2(dtcp)
	 */
	private String getEthInfo() {
		StringBuffer ip = new StringBuffer();
		StringBuffer gw = new StringBuffer();
		StringBuffer mask = new StringBuffer();
		StringBuffer dns1 = new StringBuffer();
		StringBuffer dns2 = new StringBuffer();
		boolean result = bspSystem.getEthernetInfo(ip, gw, mask, dns1, dns2);

		StringWriter sw = new StringWriter(80);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(result);
			if (result) {
				jWriter.name(Constants.Wlan.MODE).value(
						bspSystem.getEthernetMode());
				jWriter.name(Constants.Wlan.IP).value(ip.toString());
				jWriter.name(Constants.Wlan.GATEWAY).value(gw.toString());
				jWriter.name(Constants.Wlan.SUBMASK).value(mask.toString());
				jWriter.name(Constants.Wlan.DNS1).value(dns1.toString());
				jWriter.name(Constants.Wlan.DNS2).value(dns2.toString());
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
	 * 设置以太网静态模式，以下参数均不能为空，不然设置失败
	 * 
	 * @param ip
	 * @param gateway
	 * @param mask
	 * @param dns1
	 * @param dns2
	 * @return
	 */
	private String setEthStatic(String ip, String gateway, String mask,
			String dns1, String dns2) {
		return getDefaultJson(bspSystem.setEthernetStatic(ip, gateway, mask,
				dns1, dns2));
	}

	/**
	 * 设置以太网DHCP模式
	 * 
	 * @return
	 */
	private String setEthDhcp() {
		return getDefaultJson(bspSystem.setEthernetDHCP());
	}

	/**
	 * 获取SIM卡网络信息
	 * 
	 * @return
	 */
	private String getMobileNetInfo() {
		if (!MyApplication.getInstance().isSIMReady) {
			return getErrorJson(ErrorBean.SIM_NOT_READY);
		}
		StringBuffer ip = new StringBuffer();
		StringBuffer gw = new StringBuffer();
		StringBuffer mask = new StringBuffer();
		StringBuffer dns1 = new StringBuffer();
		StringBuffer dns2 = new StringBuffer();
		boolean result = bspSystem.getSIMInfo(ip, gw, mask, dns1, dns2);
		Log.i(TAG, "get mobile net info result = " + result);
		StringWriter sw = new StringWriter(80);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(result);
			if (result) {
				jWriter.name(Constants.Wlan.IP).value(ip.toString());
				jWriter.name(Constants.Wlan.GATEWAY).value(gw.toString());
				jWriter.name(Constants.Wlan.SUBMASK).value(mask.toString());
				jWriter.name(Constants.Wlan.DNS1).value(dns1.toString());
				jWriter.name(Constants.Wlan.DNS2).value(dns2.toString());
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
	 * 让系统休眠
	 * 
	 * @return
	 */
	private String goToSleep() {
		bspSystem.goToSleep();
		return getDefaultJson(true);
	}

	/**
	 * 设置当前自动休眠类型，设置成功发送状态改变
	 * 
	 * @param type
	 * @return
	 */
	private String setAutoSleepType(int type) {
		boolean result = PreferenceHelper.putInt(PreferenceHelper.AUTO_SLEEP,
				type);
		return getDefaultJson(result);
	}

	/**
	 * 获取当前自动休眠类型
	 * 
	 * @return
	 */
	private String getAutoSleepType() {
		int type = PreferenceHelper.getInt(PreferenceHelper.AUTO_SLEEP,
				Types.AUTO_SLEEP_OFF);
		StringWriter sw = new StringWriter(20);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true);
			jWriter.name(Constants.TYPE).value(type);
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
	 * 重启热点
	 * 
	 * @return
	 */
	private String restartWifiAp() {
		WifiConfiguration wifiConfig = wifiApManager.getWifiApConfiguration();
		boolean result = false;
		if (wifiApManager.setWifiApEnabled(wifiConfig, false)) {
			result = wifiApManager.setWifiApEnabled(wifiConfig, true);
		}
		return getDefaultJson(result);
	}

	/**
	 * 设置热点自动关闭时间
	 * 
	 * @return
	 */
	private String setWifiAutoDisable(int value) {
		return getDefaultJson(wifiApManager.setWifiAutoDisable(value));
	}

	/**
	 * 获取热点自动关闭时间
	 * 
	 * @return {Constants.RESULT:true, "time":5}
	 */
	private String getWifiAutoDisable() {
		StringWriter sw = new StringWriter(30);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(true);
			jWriter.name(Constants.TYPE).value(
					wifiApManager.getWifiAutoDisable());
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

	private String getErrorJson(int code) {
		return Helper.getInstance().getErrorJson(code);
	}

	/**
	 * 获取默认的返回
	 * 
	 * @param success
	 * @return {Constants.RESULT:success}
	 */
	private String getDefaultJson(boolean success) {
		StringWriter sw = new StringWriter(30);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(success)
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
}
