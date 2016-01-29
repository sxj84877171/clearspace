package com.clean.space.network.wifi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.widget.TextView;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.network.discover.DiscoverManager;
import com.clean.space.network.discover.DiscoverManager.Receiver;
import com.clean.space.protocol.PCClientItem;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * softap发现<br>
 * 通过扫描附近的设备，如果发现附近的ssid符合以zpdny_开头，则为我们所发现的pc设备<br>
 * <br>
 * update1<br>
 * 发现softap时，并尝试查询该softap所对应pc的ssid。<br>
 * update2<br>
 * 查询到softap所对应的ssid，查询手机端是否可以连接<br>
 * <br>
 * 如果可以连接上，则显示到可以连接列表中，通知界面 <br>
 * 在界面检查到电脑不存在时，如果重新读取到该pc已重新上线，应该重新读取该pc的ssid值，目前仍然读取的是缓存。<br>
 * 
 * @author Elvis
 * 
 */
public class SoftApDiscover {
	public static final String TAG = SoftApDiscover.class.getSimpleName();
	public static final int WIFICIPHER_NOPASS = 0;
	public static final int WIFICIPHER_WEP = 1;
	public static final int WIFICIPHER_WPA = 2;
	private WifiManager mWifi;
	private List<ScanResult> scanResultList;
	private boolean isRun = true;
	private Map<String, PCClient> clients = new HashMap<String, PCClient>();
	private Thread timer = new Thread() {
		public void run() {
			if (!mWifi.isWifiEnabled()) {
				mWifi.setWifiEnabled(true);
				mWifi.startScan();
			}
			try {
				Thread.sleep(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			while (isRun) {
				if (mWifi.isWifiEnabled()) {
					mWifi.startScan();
					scanResultList = mWifi.getScanResults();
					searchSoftAp(scanResultList);
				}
				//由于有线程是一直更新数据，所以无需再判断删除数据
				/*for (PCClientItem item : items) {
					if (System.currentTimeMillis() - item.getRectime() > Constants.CHECK_PC_LIVE_TIME) {
						items.remove(item);// 每次只删除一个，防止items list 报异常。暂时方案。
						clients.remove(item.getSoftAp());
						FLog.i(TAG, item.getPcname()
								+ " offline, and remove information.");
						break;
					}
				}*/
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
				}
			}
			FLog.i(TAG, "SoftApDiscover is stop.");
		}
	};

	private Thread checkSsidPCnetID = new Thread() {
		public void run() {
			while (isRun) {
				Object[] ssids = clients.keySet().toArray();
				for (Object object : ssids) {
					if (object != null && !"".equals(object.toString())) {
						PCClient client = queryPcNetWorkID(object.toString());
//						getWifiConfiguration(object.toString());
						if (client != null) {
							clients.put(object.toString(), client);
						}
					}
				}
				try {
					Thread.sleep(2000);
				} catch (Exception e) {
				}
			}
		};
	};

	/**
	 * softap发现<br>
	 * 通过扫描附近的设备，如果发现附近的ssid符合以zpdny_开头，则为我们所发现的pc设备
	 * 
	 * @param context
	 */
	public SoftApDiscover(Context context) {
		mWifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		mWifi.setWifiEnabled(true);
	}

	/**
	 * 开启云端发现
	 */
	public void start() {
		isRun = true;

		// mWifi.startScan();
		// scanResultList = mWifi.getScanResults();
		// searchSoftAp(scanResultList);
		/*
		 * IntentFilter intentWifiApoints = new IntentFilter(
		 * WifiManager.SCAN_RESULTS_AVAILABLE_ACTION); broadcastReciver = new
		 * BroadcastReceiver() {
		 * 
		 * @Override public void onReceive(Context context, Intent intent) { if
		 * (intent.getAction().equalsIgnoreCase(
		 * WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) { scanResultList =
		 * mWifi.getScanResults(); searchSoftAp(scanResultList); } }
		 * 
		 * }; context.registerReceiver(broadcastReciver, intentWifiApoints);
		 */
		new Thread(timer).start();
		new Thread(checkSsidPCnetID).start();
		FLog.i(TAG, "SoftApDiscover start.");
	}

	/**
	 * 关闭SoftApDiscover发现
	 */
	public void stop() {
		isRun = false;
		// if (broadcastReciver != null) {
		// context.unregisterReceiver(broadcastReciver);
		// }
		FLog.i(TAG, "SoftApDiscover stop.");
	}

	/**
	 * 通过密码，加密类型，连接到指定的ssid上
	 */
	public boolean connectSSID(String ssid, String password, int type) {
		WifiConfiguration config = new WifiConfiguration();
		config.allowedAuthAlgorithms.clear();
		config.allowedGroupCiphers.clear();
		config.allowedKeyManagement.clear();
		config.allowedPairwiseCiphers.clear();
		config.allowedProtocols.clear();
		config.SSID = "\"" + ssid + "\"";

		if (type == WIFICIPHER_NOPASS) {
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (type == WIFICIPHER_WEP) {
			config.hiddenSSID = true;
			config.wepKeys[0] = "\"" + password + "\"";
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.SHARED);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
			config.allowedGroupCiphers
					.set(WifiConfiguration.GroupCipher.WEP104);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			config.wepTxKeyIndex = 0;
		}
		if (type == WIFICIPHER_WPA) {
			config.preSharedKey = "\"" + password + "\"";
			config.hiddenSSID = true;
			config.allowedAuthAlgorithms
					.set(WifiConfiguration.AuthAlgorithm.OPEN);
			config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
			config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
			config.allowedPairwiseCiphers
					.set(WifiConfiguration.PairwiseCipher.TKIP);
			config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
			config.status = WifiConfiguration.Status.ENABLED;
		}
		int netID = mWifi.addNetwork(config);

		return mWifi.enableNetwork(netID, true);
	}

	private DiscoverManager.Receiver receiver;

	public void setReceiver(DiscoverManager.Receiver receiver) {
		this.receiver = receiver;
	}

	private void searchSoftAp(List<ScanResult> result) {
		if (result != null) {
			for (ScanResult sr : result) {
				if (isOwnenAp(sr.SSID)) {
					// FLog.i(TAG, "ssid:" + sr.SSID);
					PCClientItem item = createPCClientItem(sr.SSID);
					/*if (item.getClient() != null) {
						if (item.getClient().net_id != null) {
							if (getWifiConfiguration(item.getClient().net_id) != null) {
								// receiver.addAnnouncedServers(item);
								owenReiver.addAnnouncedServers(item);
							}
						}
					}*/
					//modify 需求发生变更，要求不管有没有检测到对方所在的网络，都需显示出来，可以被用户点击进行连接
					owenReiver.addAnnouncedServers(item);
				}
			}
		}
	}

	private boolean isOwnenAp(String ssid) {
		if (ssid != null && !"".equals(ssid.trim())) {
			if (ssid.length() > 12 && ssid.startsWith(Constants.SOFTAP_HEADER)) {
				return true;
			}
		}
		return false;
	}

	private PCClientItem createPCClientItem(String ssid) {
		PCClientItem item = new PCClientItem();
		item.setIp(Constants.SOFTAP_IP);
		item.setPcname(ssid.substring(Constants.SOFTAP_HEADER.length() + 6));
		item.setRectime(System.currentTimeMillis());
		item.setType(PCClientItem.DISCOVER_BY_SOFTAP);
		PCClient client = clients.get(ssid);
		if (!clients.containsKey(ssid)) {
//			client = queryPcNetWorkID(ssid);
			clients.put(ssid, client);
			FLog.i(TAG, ssid + " : " + (client != null ? client.toString()
					: "null"));
		}
		client = clients.get(ssid);
		item.setClient(client);
		item.setSoftAp(ssid);
		return item;
	}

	private PCClient queryPcNetWorkID(String ssid) {
//		FLog.i(TAG, "query pc net workid ,where softap =  " + ssid + " ");
		StringBuilder sb = new StringBuilder();
		sb.append("softap_id=").append(ssid);
//		FLog.i(TAG, Constants.SOFTAP_GETPC_SSID_URL + sb.toString());
		HttpGet httpGet = new HttpGet(Constants.SOFTAP_GETPC_SSID_URL
				+ sb.toString());
		try {
			DefaultHttpClient client = new DefaultHttpClient();
			client.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 5000);
			HttpResponse response = client.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				String result = EntityUtils.toString(entity, HTTP.UTF_8);
//				FLog.i(TAG, "result:" + result);
				PCClients clients = PCClients.parse(result);
				if (clients != null && clients.getPeers() != null
						&& clients.getPeers().size() > 0) {
					return clients.getPeers().get(0);
				}
			}
		} catch (Exception e) {
		}
		return null;
	}

	/**
	 * 根据ssid查询该ssid的连接信息
	 * 
	 * @param ssid
	 * @return
	 */
	private WifiConfiguration getWifiConfiguration(String ssid) {
		List<WifiConfiguration> result = mWifi.getConfiguredNetworks();
		if(result != null){
			for (WifiConfiguration config : result) {
				if (config.SSID.equals("\"" + ssid + "\"")) {
					return config;
				}
			}
		}
		return null;
	}

	private SoftApDiscoverReceiver owenReiver = new SoftApDiscoverReceiver();

	private class SoftApDiscoverReceiver implements Receiver {

		@Override
		public void addAnnouncedServers(PCClientItem servers) {
			/*int indexOf = items.indexOf(servers);
			if (indexOf < 0) {
				items.add(servers);
			} else {
				items.get(indexOf).setRectime(servers.getRectime());
			}*/
			if (receiver != null) {
				receiver.addAnnouncedServers(servers);
			}
		}

	}

	private List<PCClientItem> items = new ArrayList<PCClientItem>();

	/**
	 * 与云端匹配的bean信息
	 * 
	 * @author Elvis
	 * 
	 */
	public static class PCClients {
		private List<PCClient> peers;

		public List<PCClient> getPeers() {
			return peers;
		}

		public void setPeers(List<PCClient> peers) {
			this.peers = peers;
		}

		public static PCClients parse(String jsonStr) {
			PCClients packet = null;
			try {
				Gson gson = new Gson();
				packet = gson.fromJson(jsonStr, PCClients.class);
			} catch (JsonSyntaxException e) {
			}
			return packet;
		}
	}

	/**
	 * 与云端匹配的bean信息
	 * 
	 * @author Elvis
	 * 
	 */
	public static class PCClient {
		private String device_id;
		private String net_id;
		private String last_time;
		private String os_type;
		private String ip;
		private long receiveTime;

		public PCClient() {
			super();
			this.receiveTime = System.currentTimeMillis();
		}

		public long getReceiveTime() {
			return receiveTime;
		}

		public void setReceiveTime(long receiveTime) {
			this.receiveTime = receiveTime;
		}

		public String getDevice_id() {
			return device_id;
		}

		public void setDevice_id(String device_id) {
			this.device_id = device_id;
		}

		public String getNet_id() {
			return net_id;
		}

		public void setNet_id(String net_id) {
			this.net_id = net_id;
		}

		public String getLast_time() {
			return last_time;
		}

		public void setLast_time(String last_time) {
			this.last_time = last_time;
		}

		public String getOs_type() {
			return os_type;
		}

		public void setOs_type(String os_type) {
			this.os_type = os_type;
		}

		public String getIp() {
			return ip;
		}

		public void setIp(String ip) {
			this.ip = ip;
		}

		@Override
		public String toString() {
			return "PCClient [device_id=" + device_id + ", net_id=" + net_id
					+ ", last_time=" + last_time + ", os_type=" + os_type
					+ ", ip=" + ip + "]";
		}

	}
}
