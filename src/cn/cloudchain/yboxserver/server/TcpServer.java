package cn.cloudchain.yboxserver.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import cn.cloudchain.yboxserver.helper.SetHelper;

public class TcpServer extends Service {
	private final String TAG = TcpServer.class.getSimpleName();
	private final int port = 8888;
	private ExecutorService executor;
	private boolean stopListen = false;

	@Override
	public void onCreate() {
		super.onCreate();
		stopListen = false;
		executor = Executors.newCachedThreadPool();
	}

	@Override
	public void onDestroy() {
		stopListen = true;
		if (executor != null) {
			executor.shutdownNow();
			executor = null;
		}
		super.onDestroy();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		executor.execute(new Runnable() {

			@Override
			public void run() {
				listenPort(port);
			}
		});
		return START_STICKY;
	}

	private void listenPort(final int port) {
		Log.i(TAG, "listen port");
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (serverSocket == null)
			return;

		while (!stopListen) {
			try {
				Socket socket = serverSocket.accept();
				Log.i(TAG, "accept");
				executor.execute(new SocketTask(socket));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private class SocketTask implements Runnable {

		private Socket socket;

		public SocketTask(Socket socket) {
			this.socket = socket;
		}

		@Override
		public void run() {
			handleSocket();
		}

		private void handleSocket() {
			OutputStream os = null;
			InputStream is = null;
			try {
				os = socket.getOutputStream();
				is = socket.getInputStream();

				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String receiveMsg = br.readLine();
				Log.i(TAG, "from client: " + receiveMsg);

				// 读完后写一句
				String send = SetHelper.getInstance().handleJsonRequest(
						receiveMsg);
				Log.i(TAG, "to client: " + send);
				OutputStreamWriter osw = new OutputStreamWriter(os);
				osw.write(send);
				// 这个换行符必须加，避免服务端在readline由于获取不到'\r'或者'\n'或者'\r\n'时一直阻塞
				osw.write('\n');
				osw.flush();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				if (os != null) {
					try {
						os.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				if (socket != null && !socket.isClosed()) {
					try {
						socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

}
