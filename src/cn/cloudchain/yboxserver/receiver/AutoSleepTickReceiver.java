package cn.cloudchain.yboxserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import cn.cloudchain.yboxserver.server.TimetickServer;

public class AutoSleepTickReceiver extends BroadcastReceiver {
	public final static String ACTION_START_TICKER = "cn.cloudchain.yboxserver.START_TICKER";

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ACTION_START_TICKER.equals(action)) {
			Intent service = new Intent(context, TimetickServer.class);
			context.stopService(service);
			context.startService(service);
		}
	}
}
