package com.clean.space.network.http;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.statistics.StatisticsUtil;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class ServerManager extends Service {
	private Server server = null;

	@Override
	public void onCreate() {
		super.onCreate();
		server = new Server(this, "server");
		server.start();

		StatisticsUtil.getDefaultInstance(getApplicationContext()).onEventCount(Constants.UMENG.NETWORK_TRANSFER.HTTP_SERVER_ONCREATE);
	}

	@Override
	public void onDestroy() {
		server.stopServer();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		FLog.i("ServerManager", "server is start");
		StatisticsUtil.getDefaultInstance(getApplicationContext()).onEventCount(Constants.UMENG.NETWORK_TRANSFER.HTTP_SERVER_ONSTART);

		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

}