package com.clean.space.network.http;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Handler;
import android.os.Looper;

import com.clean.space.Constants;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.statistics.StatisticsUtil;

public class Server extends Thread {

	public static int port = Constants.HTTP_SERVER_PORT;
	public Context ctx;
	public boolean RUN = false;
	private long mRefreshGetRequestInternal = 5000;
	private static String TAG = "Server";
	public static long mLastRecvTime = 0;
	private long fristtime = 0;
	public ServerSocket server = null;
	public Socket soket = null;
	
	// 保存实际传输的文件路径
	public static List<String> mRealTransferfileList = new ArrayList<String>();

	public Server(Context context, String threadName) {
		this.ctx = context;
		this.setName(threadName);
		mLastRecvTime = System.currentTimeMillis();
		mRealTransferfileList.clear();
		fristtime = mLastRecvTime ;
		handlerRefreshRequest.postDelayed(runRefreshRequest,
				mRefreshGetRequestInternal);
	}

	@Override
	public void run() {
		super.run();
		RUN = true;
		while (RUN) {
			try {
				FLog.i(TAG, "start blind the server,port:" + port);
				server = new ServerSocket(port);
				server.setReuseAddress(true);
				server.setSoTimeout(60000);
				while (RUN) {
					try {
						Socket soketTemp = server.accept();
						soketTemp.setKeepAlive(true);
						soketTemp.setReuseAddress(true);
						mLastRecvTime = System.currentTimeMillis();
						Thread httpthread = new HttpThread(ctx, soketTemp,
								"httpthread");
						httpthread.start();
					} catch (Exception exc) {
						String eventid = Constants.UMENG.NETWORK_TRANSFER.HTTP_SERVER_CREATE_DOWNLOAD_THREAD_FAILED;
						StatisticsUtil.getInstance(ctx, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
					}
				}

			} catch (Exception e) {
				String eventid = Constants.UMENG.NETWORK_TRANSFER.HTTP_SERVER_START_FAILED;
				StatisticsUtil.getInstance(ctx, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
			} finally {
				clearEnv(server, soket);
			}
		}

	}

	private void clearEnv(ServerSocket server, Socket soket) {
		try {
			if (soket != null) {
				soket.close();
			}
		} catch (Exception ex) {
			FLog.e(TAG, "clearEnv throw error", ex);
		}
		try {
			if (server != null) {
				server.close();
			}
		} catch (IOException e) {
			FLog.e(TAG, "clearEnv  throw error", e);
		}
	}

	private String getVersionCode() {
		String version = "";
		try {
			PackageManager manager = ctx.getPackageManager();
			PackageInfo info = manager.getPackageInfo(ctx.getPackageName(), 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
			FLog.e(TAG, "getVersionCode", e);
		}
		return version;
	}

	public void stopServer() {
		RUN = false;
		clearEnv(server, soket);
		handlerRefreshRequest.removeCallbacks(runRefreshRequest);
		String eventid = Constants.UMENG.NETWORK_TRANSFER.HTTP_SERVER_STOP;
		StatisticsUtil.getInstance(ctx, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
	}

	private Handler handlerRefreshRequest = new Handler(Looper.myLooper());

	private Runnable runRefreshRequest = new Runnable() {
		public void run() {
			try {
				if (mLastRecvTime != fristtime) {
					if (System.currentTimeMillis() - mLastRecvTime > Constants.EXPIRED_RECV_TIME) {
						writeToSetting();
						sendToUi();

						String eventid = Constants.UMENG.NETWORK_TRANSFER.EXPORT_HTTPSERVER_TIMEOUT_TIMES;
						StatisticsUtil.getInstance(ctx, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
					}
				}else{
					if (System.currentTimeMillis() - mLastRecvTime > 5 * Constants.EXPIRED_RECV_TIME) {
						writeToSetting();
						sendToUi();
					}
				}
			} catch (Exception e) {
				FLog.e(TAG, "runRefreshRequest run throw exception ", e);
			}
			handlerRefreshRequest.postDelayed(runRefreshRequest,
					mRefreshGetRequestInternal);
		}
	};

	private void writeToSetting() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(ctx);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			if (pkg.getCleanStatus() == CleanFileStatusPkg.CLEAN_ING) {
				pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_INTERRUPT);
			}
			boolean writeRet = UserSetting
					.setClearStatusInfo(ctx, pkg.toJson());

		} catch (NumberFormatException e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		}
	}

	private void sendToUi() {
		try {
			Intent intent = new Intent();
			intent.setAction(Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
			ctx.sendBroadcast(intent);
		} catch (Exception e) {
			FLog.e(TAG, "sendToUi throw error", e);
		}
	}
}