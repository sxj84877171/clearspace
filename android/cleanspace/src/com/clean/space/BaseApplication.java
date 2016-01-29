package com.clean.space;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;

import com.clean.space.log.FLog;
import com.clean.space.onedriver.DriverManagerFactroy;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

/**
 * @Des 获取上下文等...
 */
public class BaseApplication extends Application {

	private boolean isNotificationServiceAlive = false;

	private boolean hasLogin = false;

	public void setHasLogin(boolean hasLogin) {
		this.hasLogin = hasLogin;
	}

	public boolean isHasLogin() {
		return hasLogin;
	}

	public boolean isNotificationServiceAlive() {
		return isNotificationServiceAlive;
	}

	public void setNotificationServiceAlive(boolean isNotificationServiceAlive) {
		this.isNotificationServiceAlive = isNotificationServiceAlive;
	}

	private boolean isShowSetNotification = true;

	public boolean isShowSetNotification() {
		return isShowSetNotification;
	}

	public void setShowSetNotification(boolean isShowSetNotification) {
		this.isShowSetNotification = isShowSetNotification;
	}

	@SuppressLint("HandlerLeak")
	private Handler myHandler = new Handler() {
		@Override
		public void dispatchMessage(Message msg) {
			super.dispatchMessage(msg);
			if (msg != null) {
				String message = (String) msg.obj;
				// Toast.makeText(LINKitApplication.this, message,
				// Toast.LENGTH_LONG).show();
				FLog.e("CleanSpace", message);
				int pid = android.os.Process.myPid();
				FLog.e("CleanSpace", "kill pid:" + pid);
				android.os.Process.killProcess(pid);

			}
		}

	};

	@Override
	public void onCreate() {
		Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler());
		DriverManagerFactroy.getInstance().getOneDriveAuthManager(this);
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}

	class MyUncaughtExceptionHandler implements UncaughtExceptionHandler {

		@Override
		public void uncaughtException(Thread thread, Throwable ex) {
			// FLog.i("LINKitApplication", "Thread Name:" + thread.getName());
			// FLog.i("LINKitApplication", "Thread ID :" + thread.getId());
			// FLog.i("LINKitApplication", "Error Message:" + ex.getMessage());
			// FLog.i("LINKitApplication", "Error message statck trace:"+
			// ex.getStackTrace());
			Message msg = new Message();
			msg.obj = ex.getCause() + "\r\n" + ex.getMessage() + "\r\n"
					+ Log.getStackTraceString(ex);
			FLog.e("CleanSpace", msg.obj.toString());
			myHandler.sendMessage(msg);

//			sendHearbeatBroadcast();
			FLog.e("CleanSpace",
					"Application running error , it must be restart it.", ex);
		}

	}

	private void sendHearbeatBroadcast() {
		Intent intent2 = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("class", Constants.MAIN_SERVICE_NAME);
		intent2.putExtras(bundle);
		intent2.setAction(Constants.SERVICE_RESTART_SERVICE_ACTION);
		getApplicationContext().sendBroadcast(intent2);
	}

}
