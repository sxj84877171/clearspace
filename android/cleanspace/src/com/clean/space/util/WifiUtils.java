package com.clean.space.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.clean.space.Constants;

public class WifiUtils {

	public static String getDeviceId(Context context) {
		TelephonyManager telephonyManager;
		telephonyManager = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);

		return telephonyManager.getDeviceId();
	}

	public static String intToIp(int ip) {

		return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "."
				+ ((ip >> 16) & 0xFF) + "." + (ip >> 24 & 0xFF);
	}

	public static String getLocalIp(WifiManager mWifi) {
		String ip = null;
		WifiInfo wifiInfo = mWifi.getConnectionInfo();
		if (wifiInfo != null) {
			int ipAddress = wifiInfo.getIpAddress();
			ip = intToIp(ipAddress);
		}
		return ip;
	}

	public static String getSsid(WifiManager mWifi) {
		WifiInfo wifiInfo = mWifi.getConnectionInfo();
		if (wifiInfo != null) {
			String ssid = wifiInfo.getSSID();
			if (ssid.startsWith("\"")) {
				ssid = ssid.substring(1, ssid.length() - 1);
			}
			return ssid;
		}
		return null;
	}

	public static boolean connectSSID(WifiManager mWifi, String ssid,
			String password) {
		if (!mWifi.isWifiEnabled()) {
			mWifi.setWifiEnabled(true);
		}
		WifiConfiguration config = new WifiConfiguration();
		config.SSID = "\"" + ssid + "\"";
		config.preSharedKey = "\"" + password + "\"";// password; // 指定密码
		config.hiddenSSID = true;
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
		config.allowedPairwiseCiphers
				.set(WifiConfiguration.PairwiseCipher.TKIP);
		config.allowedPairwiseCiphers
		.set(WifiConfiguration.PairwiseCipher.CCMP);
		config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		config.status = WifiConfiguration.Status.ENABLED;
		WifiConfiguration oldConfig = isExit(mWifi,ssid);
		if(oldConfig != null){
			mWifi.removeNetwork(oldConfig.networkId);
		}
		int netID = mWifi.addNetwork(config);

		return mWifi.enableNetwork(netID, true);
	}
	
	public static boolean disconnectSSID(WifiManager mWifi){
		WifiInfo info = mWifi.getConnectionInfo();
		if(info.getSSID().startsWith("\"" + Constants.SOFTAP_HEADER)){
			String ssid = info.getSSID();
			List<WifiConfiguration> list = mWifi.getConfiguredNetworks();
			for(WifiConfiguration config:list){
				if(config.SSID.equals(ssid)){
					mWifi.disableNetwork(config.networkId);
					return true;
				}
			}
		}
		return false;
	}

	public static WifiConfiguration isExit(WifiManager mWifi, String ssid) {
		List<WifiConfiguration> list = mWifi.getConfiguredNetworks();
		for (WifiConfiguration wifi : list) {
			if (wifi.SSID.equals("\"" + ssid + "\"")) {
				return wifi;
			}
		}
		return null;
	}

	public static boolean connectSSID(WifiManager mWifi,
			WifiConfiguration config, String password) {
		if (!mWifi.isWifiEnabled()) {
			mWifi.setWifiEnabled(true);
		}
		config.preSharedKey = password;// "\"" + password + "\""; // 指定密码
		int netID = mWifi.addNetwork(config);

		return mWifi.enableNetwork(netID, true);
	}

	public static boolean createWifiAp(WifiManager mWifiManager, String ssid,
			String password) {
		Method setWifiApEnabled = null;
		boolean ret = false;
		try {
			setWifiApEnabled = mWifiManager.getClass().getMethod(
					"setWifiApEnabled", WifiConfiguration.class, boolean.class);
			WifiConfiguration apConfig = createPassHotWifiConfig(ssid, password);
			ret = (Boolean) setWifiApEnabled.invoke(mWifiManager, apConfig,
					true);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static WifiConfiguration createPassHotWifiConfig(String ssid,
			String password) {
		return null;
	}
}
