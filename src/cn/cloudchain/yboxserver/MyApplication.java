package cn.cloudchain.yboxserver;

import java.io.IOException;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiConfiguration;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxserver.helper.PhoneManager;
import cn.cloudchain.yboxserver.helper.PreferenceHelper;
import cn.cloudchain.yboxserver.helper.WifiApManager;
import cn.cloudchain.yboxserver.receiver.BatteryInfoBroadcastReceiver;
import cn.cloudchain.yboxserver.receiver.PhoneStateMonitor;
import cn.cloudchain.yboxserver.server.HttpServer;
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
		if (PreferenceHelper.getInt(PreferenceHelper.MIDDLE_UPDATE, 0) == PreferenceHelper.MIDDLE_UPDATE_RESTART) {
			String filePath = PreferenceHelper.getString(
					PreferenceHelper.MIDDLE_UPDATE_PATH, "");
			if (!TextUtils.isEmpty(filePath)) {
				bspSystem.install_apk_slient(filePath);
			}
			PreferenceHelper.remove(PreferenceHelper.MIDDLE_UPDATE);
			PreferenceHelper.remove(PreferenceHelper.MIDDLE_UPDATE);
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

		try {
			new HttpServer(Constants.MIDDLE_HTTP_PORT);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			config.SSID = "YBOX-123";
		}
		config.allowedKeyManagement.clear();
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
		config.hiddenSSID = false;
		wifiApManager.setWifiApEnabled(config, true);
	}

}
