package cn.cloudchain.yboxserver.receiver;

import java.util.ArrayList;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import cn.cloudchain.yboxserver.server.TcpServer;

public class TimeTickBroadcastReceiver extends BroadcastReceiver {
	final String TAG = TimeTickBroadcastReceiver.class.getSimpleName();

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "time tick");
		if (!isRunning(context, TcpServer.class.getName())) {
			Log.i(TAG, "time tick start service");
			Intent service = new Intent(context, TcpServer.class);
			context.startService(service);
		}
	}

	private boolean isRunning(Context context, String serviceName) {
		ActivityManager myAM = (ActivityManager) context
				.getSystemService(Context.ACTIVITY_SERVICE);
		ArrayList<RunningServiceInfo> runningServices = (ArrayList<RunningServiceInfo>) myAM
				.getRunningServices(Integer.MAX_VALUE);

		if (runningServices != null) {
			for (int i = 0; i < runningServices.size(); i++)// 循环枚举对比
			{
				if (runningServices.get(i).service.getClassName().equals(
						serviceName)) {
					return true;
				}
			}
		}
		return false;
	}

}