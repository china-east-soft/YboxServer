package com.yyxu.download.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.yyxu.download.utils.MyIntents;

public class DownloadService extends Service {
	private final String TAG = DownloadService.class.getSimpleName();

	private DownloadManager mDownloadManager;
//	private MyHandler handler = new MyHandler(this);
//	private MulticastSocket multicastSocket;
//	private MyReceiver mReceiver;
//
//	private InetAddress inetAddress;
//	private MulticastLock multiLock;
//	private final static String GROUP_HOST = "230.0.0.1";
//	private final static int GROUP_PORT = 7777;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mDownloadManager = new DownloadManager(this);

//		mReceiver = new MyReceiver();
//		IntentFilter filter = new IntentFilter();
//		filter.addAction("com.yyxu.download.activities.DownloadListActivity");
//		registerReceiver(mReceiver, filter);
//
//		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//		multiLock = wifi.createMulticastLock(TAG);
//		multiLock.acquire();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null)
			return START_STICKY;
		if (intent.getAction().equals(
				"com.yyxu.download.services.IDownloadService")) {
			int type = intent.getIntExtra(MyIntents.TYPE, -1);
			Log.i(TAG, "type = " + type);
			String url;
			switch (type) {
			case MyIntents.Types.START:
				if (!mDownloadManager.isRunning()) {
					mDownloadManager.startManage();
				} else {
					mDownloadManager.reBroadcastAddAllTask();
				}
				break;
			case MyIntents.Types.ADD:
				url = intent.getStringExtra(MyIntents.URL);
				if (!TextUtils.isEmpty(url) && !mDownloadManager.hasTask(url)) {
					Log.i(TAG, "add task");
					mDownloadManager.addTask(url);
				} else if (!TextUtils.isEmpty(url)) {
					Log.i(TAG, "continue task");
					mDownloadManager.continueTask(url);
				}
				break;
			case MyIntents.Types.PAUSE:
				url = intent.getStringExtra(MyIntents.URL);
				if (!TextUtils.isEmpty(url)) {
					mDownloadManager.pauseTask(url);
				}
				break;
			default:
				break;
			}
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
//		if (multiLock != null) {
//			multiLock.release();
//		}

		if (mDownloadManager != null) {
			mDownloadManager.close();
			mDownloadManager = null;
		}
//		if (mReceiver != null) {
//			unregisterReceiver(mReceiver);
//			mReceiver = null;
//		}
		super.onDestroy();
	}

//	private static class MyHandler extends WeakHandler<DownloadService> {
//		public MyHandler(DownloadService owner) {
//			super(owner);
//		}
//
//		@Override
//		public void handleMessage(Message msg) {
//			super.handleMessage(msg);
//			if (getOwner() == null) {
//				return;
//			}
//			Bundle bundle = msg.getData();
//			if (bundle == null)
//				return;
//
//			String url = bundle.getString(MyIntents.URL);
//			if (TextUtils.isEmpty(url)) {
//				return;
//			}
//			int type = bundle.getInt(MyIntents.TYPE);
//			switch (type) {
//			case MyIntents.Types.PROCESS:
//				int progress = (int) bundle.getLong(MyIntents.PROCESS_PROGRESS);
//				getOwner().sendDataPack(
//						String.format("{\"url\":\"%s\", \"progress\":%d}", url,
//								progress));
//				break;
//			case MyIntents.Types.COMPLETE:
//				getOwner().sendDataPack(
//						String.format("{\"url\":\"%s\", \"complete\":true}",
//								url));
//				break;
//			case MyIntents.Types.ERROR:
//				int errorCode = bundle.getInt(MyIntents.ERROR_CODE);
//				getOwner().sendDataPack(
//						String.format("{\"url\":\"%s\", \"error\":%d}", url,
//								errorCode));
//				break;
//			}
//		}
//	}
//
//	private void sendDataPack(final String message) {
//		if (TextUtils.isEmpty(message))
//			return;
//
//		new Thread(new Runnable() {
//
//			@Override
//			public void run() {
//				if (multicastSocket == null) {
//					generateMuticastSocket();
//				}
//				if (multicastSocket != null) {
//					Log.i(TAG, "multicast send = " + message);
//					byte[] data = message.getBytes();
//					DatagramPacket pack = new DatagramPacket(data, data.length,
//							inetAddress, GROUP_PORT);
//					try {
//						multicastSocket.send(pack);
//					} catch (IOException e) {
//						e.printStackTrace();
//						try {
//							NetworkInterface eth0 = getInterface();
//							if (eth0 != null) {
//								multicastSocket.setNetworkInterface(eth0);
//							}
//							multicastSocket.send(pack);
//						} catch (SocketException e1) {
//							e1.printStackTrace();
//						} catch (IOException e1) {
//							e1.printStackTrace();
//						}
//					}
//				}
//			}
//		}).start();
//	}
//
//	private void generateMuticastSocket() {
//		try {
//			NetworkInterface eth0 = getInterface();
//			multicastSocket = new MulticastSocket(GROUP_PORT);
//			if (eth0 != null) {
//				Log.i(TAG, "network interface eth0 is not null");
//				multicastSocket.setNetworkInterface(eth0);
//			}
//			inetAddress = InetAddress.getByName(GROUP_HOST);
//			multicastSocket.joinGroup(inetAddress);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}
//
//	private NetworkInterface getInterface() {
//		NetworkInterface eth0 = null;
//		try {
//			Enumeration<NetworkInterface> enumeration = NetworkInterface
//					.getNetworkInterfaces();
//			while (enumeration.hasMoreElements()) {
//				eth0 = enumeration.nextElement();
//				if (eth0.getName().equals("ap0")) {
//					break;
//				}
//			}
//		} catch (SocketException e) {
//			e.printStackTrace();
//		}
//		return eth0;
//	}
//
//	private class MyReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			if (intent == null)
//				return;
//			handleIntent(intent);
//		}
//
//		private void handleIntent(Intent intent) {
//			if (intent.getAction().equals(
//					"com.yyxu.download.activities.DownloadListActivity")) {
//				Bundle data = intent.getExtras();
//				Message msg = handler.obtainMessage();
//				msg.setData(data);
//				handler.sendMessage(msg);
//			}
//		}
//	}
}
