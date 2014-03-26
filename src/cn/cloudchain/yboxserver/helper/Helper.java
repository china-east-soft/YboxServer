package cn.cloudchain.yboxserver.helper;

import java.io.IOException;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.ErrorBean;

import android.os.SystemProperties;

public class Helper {
	final String TAG = Helper.class.getSimpleName();
	private final String PropertyImageName = "IMAGE_NAME";
	private final String PropertyImageVersion = "IMAGE_VERSION";
	private static Helper instance;

	public static Helper getInstance() {
		if (instance == null)
			instance = new Helper();
		return instance;
	}

	private Helper() {

	}
	
	/**
	 * 获取root image的版本号
	 * @return
	 */
	public String getImageVersionName() {
		return SystemProperties.get(PropertyImageVersion);
	}
	
	/**
	 * 获取root image的名字
	 * @return
	 */
	public String getImageName() {
		return SystemProperties.get(PropertyImageName);
	}

	/**
	 * 获取设备MAC地址
	 * 
	 * @param context
	 * @return
	 */
	public String getDevicesMac(Context context) {
		WifiManager wifi = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		return info.getMacAddress();
	}

	/**
	 * 获取中间件的版本信息
	 * 
	 * @param context
	 * @return
	 */
	public String getVersionName(Context context) {
		String verName = "";
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			if (info != null) {
				verName = info.versionName;
			}
		} catch (NameNotFoundException e) {
			Log.e(TAG, e.getMessage());
		}
		return verName;
	}

	/**
	 * 获取广播地址，获取不到返回null
	 * 
	 * @return
	 */
	public InetAddress getBroadcastAddress() {
		InetAddress broadcastAddress = null;
		try {
			Enumeration<NetworkInterface> networkInterface = NetworkInterface
					.getNetworkInterfaces();

			while (broadcastAddress == null
					&& networkInterface.hasMoreElements()) {
				NetworkInterface singleInterface = networkInterface
						.nextElement();
				String interfaceName = singleInterface.getName();
				if (interfaceName.contains("wlan0")
						|| interfaceName.contains("eth0")) {
					Log.i("UdpServer", "interface name = " + interfaceName);
					for (InterfaceAddress infaceAddress : singleInterface
							.getInterfaceAddresses()) {
						broadcastAddress = infaceAddress.getBroadcast();
						if (broadcastAddress != null) {
							Log.i("UdpServer", "broadcast address = "
									+ broadcastAddress.getHostAddress());
							// break;
						}
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

		return broadcastAddress;
	}

	/**
	 * 获取默认的错误返回json字符串
	 * 
	 * @param code
	 *            错误码
	 * @return
	 */
	public String getErrorJson(int code) {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.RESULT).value(false);
			if (code > 0) {
				jWriter.name(Constants.ERROR_CODE).value(code);
			}
			String msg = ErrorBean.getInstance().getErrorMsg(code);
			if (!TextUtils.isEmpty(msg)) {
				jWriter.name(Constants.ERROR_MSG).value(msg);
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
	 * 获取便于阅读的存储大小
	 * 
	 * @param size
	 * @return
	 */
	public String getStorageBySize(double size) {
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

	@SuppressLint("SimpleDateFormat")
	public static String getTimeByMillis(long millis) {
		Calendar ca = Calendar.getInstance();
		ca.setTimeInMillis(millis);
		Date tasktime = ca.getTime();
		// 设置日期输出的格式

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		return df.format(tasktime);
	}
}
