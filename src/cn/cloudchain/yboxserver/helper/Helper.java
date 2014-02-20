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
						if (broadcastAddress != null) {
							break;
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
	private boolean isSDcardAvailable() {
		return Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED);
	}
}
