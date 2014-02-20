package cn.cloudchain.yboxserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import cn.cloudchain.yboxserver.helper.WifiApManager;

public class WifiApStateChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (WifiApManager.WIFI_AP_STATE_CHANGED_ACTION.equals(intent
				.getAction())) {
			int state = intent.getIntExtra(WifiApManager.EXTRA_WIFI_AP_STATE,
					-1);
			Toast.makeText(context, "wifi ap state change = " + state,
					Toast.LENGTH_SHORT).show();
		}
	}
}
