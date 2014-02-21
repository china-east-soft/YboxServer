package cn.cloudchain.yboxserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import cn.cloudchain.yboxserver.MyApplication;
import cn.cloudchain.yboxserver.helper.WifiApManager;

public class WifiApStateChangeReceiver extends BroadcastReceiver {
	final String TAG = WifiApStateChangeReceiver.class.getSimpleName();
	private final String ACTION_ETHERNET_STATE_CHANGED = "android.net.conn.ETHERNET_STATE_CHANGED";
	private final String EXTRA_ETH_STATUS = "eth_status";
	private final String EXTRA_ETH_LINK = "eth_link";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (WifiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(action)) {
			int state = intent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE,
					-1);
			Toast.makeText(context, "wifi ap state change = " + state,
					Toast.LENGTH_SHORT).show();
		} else if (WifiApManager.WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION
				.equals(action)) {
			Log.i(TAG, "hotspot client changed");
		} else if (ACTION_ETHERNET_STATE_CHANGED.equals(action)) {
			Log.i(TAG, "ethernet state change");
			String status = intent.getStringExtra(EXTRA_ETH_STATUS);
			if (EXTRA_ETH_LINK.equals(status)) {
				MyApplication.getInstance().isEthernet = true;
			} else {
				MyApplication.getInstance().isEthernet = false;
			}
		}
	}
}
