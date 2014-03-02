package cn.cloudchain.yboxserver;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import cn.cloudchain.yboxserver.helper.PhoneManager;
import cn.cloudchain.yboxserver.helper.PreferenceHelper;
import cn.cloudchain.yboxserver.helper.WifiApManager;
import cn.cloudchain.yboxserver.receiver.BatteryInfoBroadcastReceiver;
import cn.cloudchain.yboxserver.receiver.PhoneStateMonitor;
import cn.cloudchain.yboxserver.server.TcpServer;
import cn.cloudchain.yboxserver.server.UdpServer;

import com.ybox.hal.BSPSystem;

public class MyApplication extends Application {
	final String TAG = MyApplication.class.getSimpleName();
	private static MyApplication instance;
	public int battery = -1;
	public boolean isBatteryLow = false;
	public int signalStrength = -1;
	public boolean isEthernet = false;
	public boolean isSIMReady = false;
	public long clientUpdateTime = 0L;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;
		Log.i(TAG, "onCreate");
		BSPSystem bspSystem = new BSPSystem(this);

		// 如果需要升级
		if (PreferenceHelper.getInt(PreferenceHelper.ROOT_IMAGE_UPDATE, 0) == PreferenceHelper.ROOT_IMAGE_UPDATE_RESTART) {
			String filePath = PreferenceHelper.getString(
					PreferenceHelper.ROOT_IMAGE_UPDATE_PATH, "");
			if (!TextUtils.isEmpty(filePath)) {
				bspSystem.setUpgradeImg(1, filePath);
			}
			PreferenceHelper.remove(PreferenceHelper.ROOT_IMAGE_UPDATE);
			PreferenceHelper.remove(PreferenceHelper.ROOT_IMAGE_UPDATE_PATH);
		}

		isEthernet = bspSystem.getConnected(9);
		isSIMReady = new PhoneManager(this).isSIMReady();

		new Thread(new Runnable() {

			@Override
			public void run() {
				openHotspot();
			}
		}).start();

		Intent service = new Intent(this, TcpServer.class);
		startService(service);

		Intent udp = new Intent(this, UdpServer.class);
		startService(udp);

		IntentFilter batteryFilter = new IntentFilter();
		batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
		batteryFilter.addAction(Intent.ACTION_BATTERY_LOW);
		batteryFilter.addAction(Intent.ACTION_BATTERY_OKAY);
		registerReceiver(new BatteryInfoBroadcastReceiver(), batteryFilter);

		TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telManager.listen(new PhoneStateMonitor(telManager),
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
	}

	public static Context getAppContext() {
		return instance;
	}

	public static MyApplication getInstance() {
		return instance;
	}

	private void openHotspot() {
		WifiApManager wifiApManager = new WifiApManager(this);
		WifiConfiguration config = wifiApManager.getWifiApConfiguration();
		if (config == null) {
			config = new WifiConfiguration();
			config.SSID = "INIT";
			config.preSharedKey = "11111111";
		}
		config.hiddenSSID = false;
		boolean result = wifiApManager.setWifiApEnabled(config, true);
		Log.i(TAG, "enable result = " + result + ";pass = "
				+ config.preSharedKey + ";ssid = " + config.SSID);
	}

}
