package cn.cloudchain.yboxserver.helper;

import java.io.File;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

public class Helper {
	final String TAG = Helper.class.getSimpleName();
	private static Helper instance;

	public static Helper getInstance() {
		if (instance == null)
			instance = new Helper();
		return instance;
	}

	private Helper() {

	}

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
					for (InterfaceAddress infaceAddress : singleInterface
							.getInterfaceAddresses()) {
						broadcastAddress = infaceAddress.getBroadcast();
//						if (broadcastAddress != null) {
//							break;
//						}
					}
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		}

		return broadcastAddress;
	}

	/**
	 * 返回SD卡的总存储空间，单位b
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public double getSDcardTotalMemory() {
		double memory = -1;
		if (isSDcardAvailable()) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(sdcardDir.getPath());
			int blockCount = statFs.getBlockCount();
			int blockSize = statFs.getBlockSize();
			memory = blockCount * blockSize;
		}
		return memory;
	}

	/**
	 * 返回SD卡的剩余存储空间，单位b
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public double getSDcardAvailableMemory() {
		double memory = -1;
		if (isSDcardAvailable()) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs statFs = new StatFs(sdcardDir.getPath());
			int blockSize = statFs.getBlockSize();
			int availableBlock = statFs.getAvailableBlocks();
			memory = blockSize * availableBlock;
		}
		return memory;
	}

	/**
	 * 返回SD卡是否可用
	 * 
	 * @return
	 */
	public boolean isSDcardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
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
}
