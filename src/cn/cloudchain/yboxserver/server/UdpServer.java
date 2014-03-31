package cn.cloudchain.yboxserver.server;

import java.io.IOException;
import java.io.StringWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.MulticastLock;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.util.JsonWriter;
import android.util.Log;
import cn.cloudchain.yboxcommon.bean.Constants;
import cn.cloudchain.yboxcommon.bean.Types;
import cn.cloudchain.yboxserver.MyApplication;
import cn.cloudchain.yboxserver.helper.Helper;
import cn.cloudchain.yboxserver.helper.PhoneManager;
import cn.cloudchain.yboxserver.helper.WeakHandler;

import com.ybox.hal.BSPSystem;
import com.yyxu.download.utils.MyIntents;

public class UdpServer extends Service {
	final String TAG = UdpServer.class.getSimpleName();

	private PhoneManager phoneManager;
	private ScheduledExecutorService executor;
	private BSPSystem bspSystem;

	private InetAddress inetAddress;
	private MulticastLock multiLock;
	private MyHandler handler = new MyHandler(this);
	private MulticastSocket multicastSocket;
	private MyReceiver mReceiver;

	@Override
	public void onCreate() {
		super.onCreate();
		bspSystem = new BSPSystem(this);
		phoneManager = new PhoneManager(this);
		executor = Executors.newScheduledThreadPool(1);

		WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		multiLock = wifi.createMulticastLock(TAG);
		multiLock.acquire();
		Log.i(TAG, "onCreate");
		Helper.getInstance().getBroadcastAddress();

		mReceiver = new MyReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("com.yyxu.download.activities.DownloadListActivity");
		registerReceiver(mReceiver, filter);
	}

	@Override
	public void onDestroy() {
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}

		if (multiLock != null) {
			multiLock.release();
		}

		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}

		if (multicastSocket != null) {
			try {
				multicastSocket.leaveGroup(inetAddress);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				multicastSocket.close();
				multicastSocket = null;
			}
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		executor.scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				sendStatusUdpBroadcast();
			}
		}, 0, 3, TimeUnit.SECONDS);
		return START_STICKY;
	}

	/**
	 * 定时发送终端状态信息
	 */
	private void sendStatusUdpBroadcast() {
		if (multicastSocket == null) {
			generateMuticastSocket();
		}

		try {
			String message = generateContent();
			byte[] data = message.getBytes();
			DatagramPacket pack = new DatagramPacket(data, data.length,
					inetAddress, Constants.GROUP_PORT);
			multicastSocket.send(pack);

			Log.i(TAG, "send udp broadcast");
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 生成UDP广播的内容
	 * 
	 * @return
	 */
	private String generateContent() {
		StringWriter sw = new StringWriter(50);
		JsonWriter jWriter = new JsonWriter(sw);
		try {
			jWriter.beginObject().name(Constants.Udp.CONN_TYPE);
			if (bspSystem.getConnected(9)) {
				jWriter.value(Types.CONN_TYPE_ETHERNET);
			} else if (!MyApplication.getInstance().isSIMReady) {
				jWriter.value(Types.CONN_TYPE_NONE);
			} else if (phoneManager.isMobileDataEnabled()) {
				jWriter.value(Types.CONN_TYPE_MOBILE_DATA_ON);
			} else {
				jWriter.value(Types.CONN_TYPE_MOBILE_DATA_OFF);
			}
			jWriter.name(Constants.Udp.BATTERY).value(
					MyApplication.getInstance().battery);
			jWriter.name(Constants.Udp.CLIENTS_UPDATE_TIME).value(
					MyApplication.getInstance().clientUpdateTime);
			jWriter.endObject();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (jWriter != null) {
				try {
					jWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return sw.toString();
	}

	/**
	 * 用于处理下载相关进度更新
	 * 
	 * @author lazzy
	 * 
	 */
	private static class MyHandler extends WeakHandler<UdpServer> {
		public MyHandler(UdpServer owner) {
			super(owner);
		}

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			if (getOwner() == null) {
				return;
			}
			Bundle bundle = msg.getData();
			if (bundle == null)
				return;

			String url = bundle.getString(MyIntents.URL);
			if (TextUtils.isEmpty(url)) {
				return;
			}
			int type = bundle.getInt(MyIntents.TYPE);
			switch (type) {
			case MyIntents.Types.PROCESS:
				int progress = (int) bundle.getLong(MyIntents.PROCESS_PROGRESS);
				getOwner().sendDowloadDataPack(
						String.format("{\"url\":\"%s\", \"progress\":%d}", url,
								progress));
				break;
			case MyIntents.Types.COMPLETE:
				getOwner().sendDowloadDataPack(
						String.format("{\"url\":\"%s\", \"complete\":true}",
								url));
				break;
			case MyIntents.Types.ERROR:
				int errorCode = bundle.getInt(MyIntents.ERROR_CODE);
				getOwner().sendDowloadDataPack(
						String.format("{\"url\":\"%s\", \"error\":%d}", url,
								errorCode));
				break;
			}
		}
	}

	/**
	 * 发送下载进度信息
	 * 
	 * @param message
	 */
	private void sendDowloadDataPack(final String message) {
		if (TextUtils.isEmpty(message))
			return;

		new Thread(new Runnable() {

			@Override
			public void run() {
				if (multicastSocket == null) {
					generateMuticastSocket();
				}
				if (multicastSocket != null) {
					try {
						Log.i(TAG, "multicast send = " + message);
						byte[] data = message.getBytes();
						DatagramPacket pack = new DatagramPacket(data,
								data.length, inetAddress, Constants.GROUP_PORT);
						multicastSocket.send(pack);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	/**
	 * 生成组播socket
	 */
	private void generateMuticastSocket() {
		try {
			NetworkInterface eth0 = getInterface();
			multicastSocket = new MulticastSocket(Constants.GROUP_PORT);
			if (eth0 != null) {
				multicastSocket.setNetworkInterface(eth0);
			}
			inetAddress = InetAddress.getByName(Constants.GROUP_HOST);
			multicastSocket.joinGroup(inetAddress);
		} catch (IOException e) {
			e.printStackTrace();
			multicastSocket = null;
		}
	}

	/**
	 * 获取ap0的interface
	 * 
	 * @return
	 */
	private NetworkInterface getInterface() {
		NetworkInterface eth0 = null;
		try {
			Enumeration<NetworkInterface> enumeration = NetworkInterface
					.getNetworkInterfaces();
			while (enumeration.hasMoreElements()) {
				eth0 = enumeration.nextElement();
				if (eth0.getName().equals("ap0")) {
					break;
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return eth0;
	}

	/**
	 * 监听下载进度广播
	 * 
	 * @author lazzy
	 * 
	 */
	private class MyReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent == null)
				return;
			handleIntent(intent);
		}

		private void handleIntent(Intent intent) {
			if (intent.getAction().equals(
					"com.yyxu.download.activities.DownloadListActivity")) {
				Bundle data = intent.getExtras();
				Message msg = handler.obtainMessage();
				msg.setData(data);
				handler.sendMessage(msg);
			}
		}
	}

}
