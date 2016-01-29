package com.clean.space.util;

import android.content.Context;
import android.net.wifi.WifiManager;

/**
 * update1:
 * 不帮用户恢复网络
 * @author Elvis
 *
 */
public class KeepUserNetwork {

	private int networkid = -1;
	private Context context;

	private KeepUserNetwork(Context context) {
		this.context = context;
	}

	private static KeepUserNetwork instance = null;

	public static KeepUserNetwork getInstance(Context context) {
		if (instance == null) {
			instance = new KeepUserNetwork(context);
		}
		instance.context = context;
		return instance;
	}

	public void saveOldNetWorkId() {
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		networkid = wifiManager.getConnectionInfo().getNetworkId();
	}

	public void restoreOldNetWork() {
		/*if (networkid != -1) {
			WifiManager wifiManager = (WifiManager) context
					.getSystemService(Context.WIFI_SERVICE);
			wifiManager.enableNetwork(networkid, true);
			networkid = -1;
		}*/
	}

	public void saveIfNotSaveOldNetWorkId() {
		if (networkid == -1) {
			saveOldNetWorkId();
		}
	}

}
