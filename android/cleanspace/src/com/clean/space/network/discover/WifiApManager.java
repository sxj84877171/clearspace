/**
 * 
 */
package com.clean.space.network.discover;

import java.lang.reflect.Method;
import java.util.Random;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.clean.space.Constants;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.protocol.PCClientItem;

/**
 * 热点管理类
 * 
 * @author Elvis
 * 
 */
public class WifiApManager {

	private final String TAG = "WifiApManager";
	private boolean enable = false;

	private PCClientItem pcInfo;

	private Context mContext;

	private int networkid = -1;

	private WifiApManager(Context context) {
		this.mContext = context;
	}

	private static WifiApManager instance = null;

	public static WifiApManager getInstance(Context context) {
		if (instance == null) {
			synchronized (WifiManager.class) {
				if (instance == null) {
					instance = new WifiApManager(context);
					WifiStateManager.getInstance(context).onStart();
				}
			}

		}
		instance.mContext = context;
		return instance;
	}

	/**
	 * 
	 * @param pcInfo
	 * @return
	 */
	public boolean enable(PCClientItem pcInfo) {
		this.pcInfo = pcInfo;
		enable = setWifiApEnabled(true);

		// 打开softap失败,则开启wifi,暂时只打开一次softap
		if (enable) {
			WifiStateManager.getInstance(mContext).postEnableSoftApState();
		} else {
			WifiStateManager.getInstance(mContext).postEnableWifiState();
		}
		disableDataConnection();
		return enable;
	}

	/**
	 * 
	 * @return
	 */
	public boolean disableSoftAp() {
		// FLog.i(TAG, "disableSoftAp start" + enable);
		if (enable) {
			setWifiApEnabled(false);
			enable = false;
		}
		openDataConnection();
		restoreOldNetWork();
		WifiStateManager.getInstance(mContext).postEnableWifiState();
		return true;
	}

	public boolean enableWifi() {
		return disableSoftAp();
	}

	// wifi热点开关
	private boolean setWifiApEnabled(Boolean enable) {
		WifiManager mWifi = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		saveIfNotSaveOldNetWorkId();
		if (mWifi.isWifiEnabled()) { // disable WiFi in any case
			// wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
			mWifi.setWifiEnabled(false);
		}
		try {
			// 热点的配置类
			WifiConfiguration config = new WifiConfiguration();
			// 配置热点的名称(可以在名字后面加点随机数什么的)

			config.SSID = getSSIDString(pcInfo.getSoftAp());
			config.hiddenSSID = false;
			// 配置热点的密码
			config.preSharedKey = Constants.SOFTAP_PASSWORD;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);//
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);//
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.CCMP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
			config.status = WifiConfiguration.Status.ENABLED;
			// 通过反射调用设置热点
			Method method = mWifi.getClass().getMethod("setWifiApEnabled",
					WifiConfiguration.class, Boolean.TYPE);
			// 返回热点打开状态
			return (Boolean) method.invoke(mWifi, config, enable);
		} catch (Exception e) {
			return false;
		}
	}

	private void saveOldNetWorkId() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo connectionInfo = wifiManager.getConnectionInfo();
		if (connectionInfo != null) {
			networkid = connectionInfo.getNetworkId();
		}
	}

	private void restoreOldNetWork() {
		WifiManager wifiManager = (WifiManager) mContext
				.getSystemService(Context.WIFI_SERVICE);
		if (!wifiManager.isWifiEnabled()) {
			wifiManager.setWifiEnabled(true);
		}
		if (networkid != -1) {
			wifiManager.enableNetwork(networkid, true);
			networkid = -1;
		}
	}

	private void saveIfNotSaveOldNetWorkId() {
		if (networkid == -1) {
			saveOldNetWorkId();
		}
	}

	private String getSSIDString(String softap) {
		StringBuilder sb = new StringBuilder();
		sb.append("AND");

		// 每一台设备softap的ssid是固定的
		if (Constants.SOFTAP_SSID_IS_FIXED) {
			String key = "ssid_key";
			String value = UserSetting.getString(mContext, key, "");
			if (value == null || "".equals(value)) {
				value = "";
				Random r = new Random();
				value += (char) ('A' + r.nextInt(26));
				value += (char) ('A' + r.nextInt(26));
				UserSetting.setString(mContext, key, value);
			}
			sb.append(value);
		} else {
			Random r = new Random();
			sb.append((char) ('A' + r.nextInt(26)));
			sb.append((char) ('A' + r.nextInt(26)));
		}
		sb.append(softap.substring(5));
		FLog.i("ConnectManager", "ssid:" + sb.toString());
		return sb.toString();
	}

	private boolean dataEnable = false;

	private void disableDataConnection() {
		FLog.i(TAG, "disableDataConnection");
		TelephonyManager telephony = (TelephonyManager) mContext
				.getSystemService(Context.TELEPHONY_SERVICE);
		dataEnable = telephony.getDataState() > TelephonyManager.DATA_DISCONNECTED;
		FLog.i(TAG, "dataEnable:" + dataEnable);
		// update 数据流量有时候返回结果错误。
		// 直接关闭
		if (dataEnable) {
			setDataConnectionState(false);
		}
//		MobileDataManager.getInstance().disableMobileData(mContext);
	}

	private void disableDataConnectivity(TelephonyManager telephony) {
		try {
			Class telephonyManagerClass = Class.forName(telephony.getClass()
					.getName());
			Method getITelephonyMethod = telephonyManagerClass
					.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			Object ITelephonyStub = getITelephonyMethod.invoke(telephony);
			Class ITelephonyClass = Class.forName(ITelephonyStub.getClass()
					.getName());
			Method dataConnSwitchmethod = ITelephonyClass
					.getDeclaredMethod("disableDataConnectivity");// enableDataConnectivity
			dataConnSwitchmethod.setAccessible(true);
			dataConnSwitchmethod.invoke(ITelephonyStub);
			FLog.i(TAG, "disableDataConnection success.");
		} catch (Exception e) {
			FLog.e(TAG, e);
			FLog.i(TAG, "disableDataConnection fail.");
		}
	}

	private void openDataConnection() {
		if (dataEnable) {
			FLog.i(TAG, "openDataConnection");
			setDataConnectionState(true);
//			MobileDataManager.getInstance().enableMobileData(mContext);
			dataEnable = false;
		}
	}

	private void enableDataConnectivity(TelephonyManager telephony) {
		try {
			Class telephonyManagerClass = Class.forName(telephony.getClass()
					.getName());
			Method getITelephonyMethod = telephonyManagerClass
					.getDeclaredMethod("getITelephony");
			getITelephonyMethod.setAccessible(true);
			Object ITelephonyStub = getITelephonyMethod.invoke(telephony);
			Class ITelephonyClass = Class.forName(ITelephonyStub.getClass()
					.getName());
			Method dataConnSwitchmethod = ITelephonyClass
					.getDeclaredMethod("enableDataConnectivity");//
			dataConnSwitchmethod.setAccessible(true);
			dataConnSwitchmethod.invoke(ITelephonyStub);
			FLog.i(TAG, "openDataConnection success.");
		} catch (Exception e) {
			FLog.e(TAG, e);
			FLog.i(TAG, "openDataConnection fail.");
		}
	}

	public void setDataConnectionState(boolean state) {
		FLog.i(TAG, "setDataConnectionState:" + state);
		ConnectivityManager connectivityManager = null;
		Class connectivityManagerClz = null;
		try {
			connectivityManager = (ConnectivityManager) mContext
					.getSystemService("connectivity");
			connectivityManagerClz = connectivityManager.getClass();
			Method method = connectivityManagerClz.getMethod(
					"setMobileDataEnabled", new Class[] { boolean.class });
			method.invoke(connectivityManager, state);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
