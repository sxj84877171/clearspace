/**
 * 
 */
package com.clean.space.network.discover;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.clean.space.log.FLog;

/**
 * 热点状态管理类
 * 
 * @author Elvis
 * 
 */
public class WifiStateManager {

	private final String TAG = "WifiStateManager";
	private Context mContext;
	private long mUpdateWifiInternal = 2000;
	private final int ENABLE_SOFTAP = 100;
	private final int ENABLE_WIFI = 101;
	private int mWifiState = 0;

	private WifiStateManager(Context context) {
		this.mContext = context;
	}

	private static WifiStateManager instance = null;

	public static WifiStateManager getInstance(Context context) {
		if (instance == null) {
			synchronized (WifiManager.class) {
				if (instance == null) {
					instance = new WifiStateManager(context);
				}
			}

		}
		instance.mContext = context;
		return instance;
	}

	public void postEnableSoftApState() {
		mWifiState = ENABLE_SOFTAP;
	}

	public void postEnableWifiState() {
		mWifiState = ENABLE_WIFI;
	}

	public void onDestroy() {
		handlerUpdateWifi.removeCallbacks(runUpdateWifi);
	}

	public void onStart() {
		start();
	}

	private void start() {
		handlerUpdateWifi.postDelayed(runUpdateWifi, mUpdateWifiInternal);
	}

	private Handler handlerUpdateWifi = new Handler(Looper.myLooper());
	private Runnable runUpdateWifi = new Runnable() {
		public void run() {
			try {
				updateWifi();
			} catch (Exception e) {
				FLog.e(TAG, "runUpdateWifi run throw exception ", e);
			}
			//FLog.i(TAG, "runUpdateWifi run again ");
			handlerUpdateWifi.postDelayed(runUpdateWifi, mUpdateWifiInternal);
		}
	};

	private void updateWifi() {
		try {
			final int wifiState = mWifiState;
			switch (wifiState) {
			case ENABLE_SOFTAP:
				break;
			case ENABLE_WIFI:
				WifiApManager.getInstance(mContext).enableWifi();
				break;
			default:
				WifiApManager.getInstance(mContext).enableWifi();
				break;
			}
		} catch (Exception e) {
			FLog.e(TAG, "updateWifi run throw exception ", e);
		}
	}

}
