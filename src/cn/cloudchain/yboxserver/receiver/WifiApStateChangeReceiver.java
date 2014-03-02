package cn.cloudchain.yboxserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.cloudchain.yboxserver.MyApplication;
import cn.cloudchain.yboxserver.helper.PhoneManager;
import cn.cloudchain.yboxserver.helper.WifiApManager;

public class WifiApStateChangeReceiver extends BroadcastReceiver {
	final String TAG = WifiApStateChangeReceiver.class.getSimpleName();
	private final static String ACTION_SIM_STATE_CHANGED = "android.intent.action.SIM_STATE_CHANGED";
	private final String ACTION_ETHERNET_STATE_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
	private final String EXTRA_ETH_STATUS = "eth_status";
	private final String EXTRA_ETH_LINK = "eth_link";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE,
					-1);
			switch (state) {
			case WifiApManager.WIFI_AP_STATE_ENABLED:
				break;
			}
			Log.d(TAG, "wifi ap state change = " + state);
		} else if (WifiApManager.WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION
				.equals(action)) {
			Log.d(TAG, "wifi hotspot client change");
			MyApplication.getInstance().clientUpdateTime = System
					.currentTimeMillis() / 1000;
			context.sendBroadcast(new Intent(
					AutoSleepTickReceiver.ACTION_START_TICKER));
		} else if (ACTION_ETHERNET_STATE_CHANGED.equals(action)) {
			Log.d(TAG, "ethernet state change");
			String status = intent.getStringExtra(EXTRA_ETH_STATUS);
			if (EXTRA_ETH_LINK.equals(status)) {
				MyApplication.getInstance().isEthernet = true;
			} else {
				MyApplication.getInstance().isEthernet = false;
			}
		} else if (ACTION_SIM_STATE_CHANGED.equals(action)) {
			MyApplication.getInstance().isSIMReady = new PhoneManager(context)
					.isSIMReady();
		}

	}
}
