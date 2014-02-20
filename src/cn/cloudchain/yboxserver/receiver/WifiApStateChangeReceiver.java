package cn.cloudchain.yboxserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;
import cn.cloudchain.yboxserver.helper.WifiApManager;

public class WifiApStateChangeReceiver extends BroadcastReceiver {
	final String TAG = WifiApStateChangeReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		if (WifiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(intent
				.getAction())) {
			int state = intent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE,
					-1);
			Toast.makeText(context, "wifi ap state change = " + state,
					Toast.LENGTH_SHORT).show();
		} else if (WifiApManager.WIFI_HOTSPOT_CLIENTS_CHANGED_ACTION
				.equals(intent.getAction())) {
			Log.i(TAG, "hotspot client changed");
		}
	}
}
