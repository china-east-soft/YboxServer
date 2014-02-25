package cn.cloudchain.yboxserver.helper;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.util.Log;
import cn.cloudchain.yboxserver.MyApplication;

public class PhoneManager {
	private final String TAG = PhoneManager.class.getSimpleName();
	private ConnectivityManager connManager;
	private Context context;

	public PhoneManager(Context context) {
		this.context = context;
		this.connManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
	}

	/**
	 * 关机
	 * 
	 * @param restart
	 *            true时重启
	 */
	public boolean shutdown(boolean restart) {
		boolean result = false;
		try {
			Intent intent = new Intent(restart ? Intent.ACTION_REBOOT
					: Intent.ACTION_SHUTDOWN);
			context.sendBroadcast(intent);
			result = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	/**
	 * 获取电量
	 * 
	 * @return 若未获取到则返回-1
	 */
	public int getBattery() {
		return MyApplication.getInstance().battery;
	}

	/**
	 * 手机数据是否启用
	 * 
	 * @return
	 */
	public boolean isMobileDataEnabled() {
		boolean result = false;
		try {
			Method method = ConnectivityManager.class
					.getMethod("getMobileDataEnabled");
			result = (Boolean) method.invoke(connManager);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 设置是否启用手机数据
	 * 
	 * @param enable
	 * @return
	 */
	public boolean setMobileDataEnabled(boolean enable) {
		boolean result = false;
		try {
			Method method = ConnectivityManager.class.getMethod(
					"setMobileDataEnabled", Boolean.TYPE);
			method.invoke(connManager, enable);
			result = true;
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		return result;
	}

}
