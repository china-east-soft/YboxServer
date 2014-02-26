package cn.cloudchain.yboxserver.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import cn.cloudchain.yboxserver.MyApplication;

public class BatteryInfoBroadcastReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (Intent.ACTION_BATTERY_CHANGED.equals(intent.getAction())) {
			// 获取当前电量
			int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			MyApplication.getInstance().battery = level;
		} else if (Intent.ACTION_BATTERY_LOW.equals(intent.getAction())) {
			MyApplication.getInstance().isBatteryLow = true;
		} else if (Intent.ACTION_BATTERY_OKAY.equals(intent.getAction())) {
			MyApplication.getInstance().isBatteryLow = false;
		}

	}

}