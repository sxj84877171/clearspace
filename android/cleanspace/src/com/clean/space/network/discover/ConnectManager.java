package com.clean.space.network.discover;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;

import com.clean.space.Constants;
import com.clean.space.Constants.UMENG.NETWORK_CONNECT;
import com.clean.space.CoreService;
import com.clean.space.db.DBMgr;
import com.clean.space.log.FLog;
import com.clean.space.network.discover.DiscoverManager.Receiver;
import com.clean.space.network.http.ServerManager;
import com.clean.space.network.udp.UDPDiscover;
import com.clean.space.network.udp.UdpClient;
import com.clean.space.network.udp.UdpConnect;
import com.clean.space.onedriver.DriverManagerFactroy;
import com.clean.space.onedriver.OneDriveUploadManage;
import com.clean.space.phoneinfo.PhoneInfo;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.PCClientItem;
import com.clean.space.protocol.PhoneInfoPkt;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.KeepUserNetwork;
import com.clean.space.util.WifiUtils;

/**
 * 
 发现</br> 1. 收广播包。--</br> 2. 云端获取， 检测是否联通。</br> 3. softap 扫描，扫描符合规则 显示。</br>
 * </br> 连接</br> 1. 直连</br> 2. 直连</br> 3. 用softap的ssid信息 从服务器查询对方连接的ap的ssid及ip，
 * </br> 3.1 尝试连接ssid。 </br> 3.2 连不上 连softap</br> </br>
 * 
 * 对连接第三步具体过程：</br> </br> 1. 根据softap ssid查询pc ssid 有跳第二步，无跳5步</br> 2. 查询pc
 * ssid是否在已连接过的列表中及目前能扫描到的ap列表中 有跳第3步，无跳5步 </br> 3. 连接pc ssid
 * 连上跳第4步，不能连上跳5步</br> 4. 通过ip进行检测是否联通，能联通则跳6步，不能联通跳5步</br> 5. 连接softap
 * ssid，能连上跳6步 不能连跳7步</br> 6. 发送连接请求，进行传输</br> 7. 失败返回</br> </br>
 * 
 * 通过注册registerStateChange 方法，回掉连接状态的变化<br>
 * <br>
 * 
 * 该类不能成功的返回传输结果，需要界面调用之后，监听传输结果，再由界面停止后台连接<br>
 * 调用destroy方法，停止线程，回收资源。无论哪种结果，都需要界面来销毁该连接的资源。 <br>
 * update 1:<br>
 * 首先判断局域网是否连接成功，如果能够连接成功，直接连接。<br>
 * 如果不能连接成功，直接连接softap，如果softap连接不成功<br>
 * 再根据softap查询pc端所在的局域网，如果手机能连上pc所在网络，则连接<br>
 * 并判断是否能连接成功。如果不可以，则失败退出。
 * 
 * @author Elvis <br>
 *         {@code } <br>
 * <br>
 *         update1:<br>
 * <br>
 *         当前存在的问题，无法判断wifi连接上后，要求继续输入用户名和密码才能成功上网。<br>
 *         而切换pc网络成功后，无法迅速判断该wifi是要求输入用户名和密码。<br>
 *         只能等待到一定的时间后，发现pc没有反馈，再判断无法进行配对连接。 <br>
 * <br>
 * <br>
 * <br>
 *         update2:<br>
 *         1.判断当前要使用的连接类型，如果是softap连接，则开启针对该电脑的局域网广播发现<br>
 *         2.如果局域网广播发现了该机器，则更新当前机器的连接类型。<br>
 * <br>
 * 
 * <br>
 * 
 */
public class ConnectManager {

	public static final String TAG = ConnectManager.class.getSimpleName();

	public static final int ERROR = -1;

	public static final int CONNECT_LAN = 1;
	public static final int CONNECT_PC_NET_ID = 2;
	public static final int CONNECT_SOFT_AP = 3;
	public static final int CONNECT_WIFIAP = 4;
	public static final int CONNECT_ONEDRIVE = 5 ;

	private PCClientItem pcInfo;
	private StateChanager stateChange;
	private Context context;
	private HandlerThread logicThread;
	private Handler handler;
	public static final int TRYTIMES = 40;
	public static final int SLEEPTIMES = 500;

	public void registerStateChange(StateChanager stateChange) {
		this.stateChange = stateChange;
	}

	public static interface StateChanager {
		public void onChange(int state);
	}

	/**
	 * 
	 发现</br> 1. 收广播包。--</br> 2. 云端获取， 检测是否联通。</br> 3. softap 扫描，扫描符合规则
	 * 显示。</br> </br> 连接</br> 1. 直连</br> 2. 直连</br> 3. 用softap的ssid信息
	 * 从服务器查询对方连接的ap的ssid及ip， </br> 3.1 尝试连接ssid。 </br> 3.2 连不上 连softap</br>
	 * </br>
	 * 
	 * 对连接第三步具体过程：</br> </br> 1. 根据softap ssid查询pc ssid 有跳第二步，无跳5步</br> 2. 查询pc
	 * ssid是否在已连接过的列表中及目前能扫描到的ap列表中 有跳第3步，无跳5步 </br> 3. 连接pc ssid
	 * 连上跳第4步，不能连上跳5步</br> 4. 通过ip进行检测是否联通，能联通则跳6步，不能联通跳5步</br> 5. 连接softap
	 * ssid，能连上跳6步 不能连跳7步</br> 6. 发送连接请求，进行传输</br> 7. 失败返回</br> </br>
	 * 
	 * 通过注册{@link #registerStateChange()} 方法，回掉连接状态的变化<br>
	 * <br>
	 * update1:<br>
	 * 发现softap设备，如果没有发现softap对应的电脑所在的pc网络，则android开启softap热点 <br>
	 * <br>
	 * <br>
	 * <br>
	 * 该类不能成功的返回传输结果，需要界面调用之后，监听传输结果，再由界面停止后台连接<br>
	 * 调用{@link #destroy()}方法，停止线程，回收资源。无论哪种结果，都需要界面来销毁该连接的资源。<br>
	 * <br>
	 * 
	 */
	public ConnectManager(Context context) {
		this.context = context;
		logicThread = new HandlerThread("softAp");
		logicThread.start();
		handler = new Handler(logicThread.getLooper());
	}

	private long start = System.currentTimeMillis();

	/**
	 * 发起连接，该连接为异步，连接状态的变化，通过注册回掉函数取得<br>
	 * 该类不能成功的返回传输结果，需要界面调用之后，监听传输结果，再由界面停止后台连接<br>
	 * 调用{@link #destroy()}方法，停止线程，回收资源。无论哪种结果，都需要界面来销毁该连接的资源。
	 * 
	 * @param pcInfo
	 */
	public void connect(final PCClientItem pcInfo) {
		FLog.i(TAG, "connect" + pcInfo.toJson());
		if (!logicThread.isAlive()) {
			throw new RuntimeException(
					"the object has called the destroy method,please create new object to call.");
		}
		start = System.currentTimeMillis();
		this.pcInfo = PCClientItem.parse(pcInfo.toJson());
		isNeedRunning = true;

		if (pcInfo.getType() == PCClientItem.DISCOVER_BY_SOFTAP) {
			handler.post(softapConnect);
		} else if(pcInfo.getType() == PCClientItem.DISCOVER_BY_ONEDRIVE){
//			handler.post(oneDriveRun);
			startCoreServer();
		} else {
			handler.post(lanConnect);
		}
	}
	

	/**
	 * 停止连接，销毁资源
	 * 
	 */
	public void destroy() {
		isNeedRunning = false;
		try {
			if (logicThread != null && logicThread.isAlive()) {
				logicThread.quit();
				// logicThread.quitSafely();
				logicThread = null;
			}
			handler.removeCallbacks(overTimeOneDriveRun);
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
	}

	/**
	 * 连接softap
	 * 
	 * @return 是否连接成功
	 */
	public boolean connectSoftAp() {
		FLog.i(TAG, "connect softap function.softap:" + pcInfo.getSoftAp());
		if (stateChange != null) {
			stateChange.onChange(CONNECT_SOFT_AP);
		}
		WifiManager wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
		boolean result = pcInfo.getSoftAp() != null;
		WifiInfo connectionInfo = wifiManager.getConnectionInfo();// ;.getSSID();
		if (result
				&& connectionInfo != null
				&& !connectionInfo.getSSID().equals(
						"\"" + pcInfo.getSoftAp() + "\"")) {
			FLog.i(TAG, "current ssid is not " + pcInfo.getSoftAp()
					+ ", and connect this softap.");
			try {
				// if (isInScanList(pcInfo.getSoftAp())) {
				KeepUserNetwork.getInstance(context)
						.saveIfNotSaveOldNetWorkId();
				result = WifiUtils.connectSSID(wifiManager, pcInfo.getSoftAp(),
						Constants.SOFTAP_PASSWORD);
				/*
				 * } else { FLog.i(TAG, "can't find this soft ap."); result =
				 * false; }
				 */
			} catch (Exception e) {
				FLog.e(TAG, "WifiUtils.connectSSID", e);
				result = false;
			}
		}
		if (result) {
			FLog.i(TAG,
					"save connect information success:" + pcInfo.getSoftAp());
			ConnectivityManager mConnectivityManager = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);

			NetworkInfo mWiFiNetworkInfo = null;
			int times = 0;
			while (isNeedRunning) {

				try {
					if (mWiFiNetworkInfo != null)
						FLog.i(TAG,
								"wait connect...."
										+ mWiFiNetworkInfo.getState());
					Thread.sleep(SLEEPTIMES);
				} catch (Exception e) {
					FLog.e(TAG, e);
				}

				mWiFiNetworkInfo = mConnectivityManager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				WifiInfo info = wifiManager.getConnectionInfo();
				if (isRightConnectSsid(mWiFiNetworkInfo, info,
						pcInfo.getSoftAp())) {
					FLog.i(TAG,
							"start the server,and send cmd to pc to download:"
									+ pcInfo.getSoftAp());
					startServer();
					if (connectPC(new String[] { pcInfo.getIp() })) {
						eventSuccess();
						return true;
					} else {
						eventFail();
						return false;
					}
				}

				if (++times > TRYTIMES) {
					result = false;
					break;
				}
			}
		} else {
			FLog.i(TAG, "soft ap connect fail.");
		}
		return result;
	}

	/**
	 * 启动服务，开启server，能让pc建立连接
	 */
	public void startServer() {
		FLog.i(TAG, "startServer");
		Intent intent = new Intent(context, ServerManager.class);
		context.startService(intent);
		eventNewExportPhotoUser();
	}
	
	
	public void startCoreServer(){
		FLog.i(TAG, "startCoreServer");
		Bundle extras = new Bundle();
		extras.putString(Constants.CLOUD_KEY, Constants.CLOUD_ONE_DRIVER);
		extras.putString(Constants.CLOUD_CMD, Constants.CLOUD_CMD_START);
		Intent intent = new Intent(context, CoreService.class);
		intent.putExtras(extras);
		context.startService(intent);
		handler.postDelayed(overTimeOneDriveRun, SLEEPTIMES * TRYTIMES * 6);
		if(stateChange != null){
			stateChange.onChange(CONNECT_ONEDRIVE);
		}
	}

	/**
	 * 先切换成pc网络，检测是否能连接，不能连接，则连softap<br>
	 * 方法弃用
	 */
	private Runnable connectRun1 = new Runnable() {
		@Override
		public void run() {
			// initPCInformation(pcInfo.getSoftAp());
			if (!connectPCNet(timeoutTask)) {
				if (!connectSoftAp()) {
					if (stateChange != null) {
						stateChange.onChange(ERROR);
					}
					eventFail();
				}
			}
		}
	};

	/**
	 * 先连接softap，检测是否能连接，不能连接，则切换pc同网络<br>
	 * 目前使用此方法<br>
	 * <b>update1</b> 通过确定，在切网失败时，则转到启动热点，进行热点进行连接
	 */
	private Runnable softapConnect = new Runnable() {
		@Override
		public void run() {
			if (!(pcInfo.getClient() != null
					&& pcInfo.getClient().getNet_id() != null
					&& isInOwnerList(pcInfo.getClient().getNet_id()) && connectPCNet(timeoutTask2))) {
				total = System.currentTimeMillis();
				if (enableApEnabled()) {
					FLog.i(TAG, "热点启动成功。");
					// alex 流程图上面是40s。一个try和sleep是20s 所以乘以2
					handler.postDelayed(overTimeRun, SLEEPTIMES * TRYTIMES * 2);
					UDPDiscover.getInstance().registerReceiver(apReceiver);
				} else {
					FLog.i(TAG, "热点启动失败。");
					if (stateChange != null) {
						stateChange.onChange(ERROR);
					}
					eventFail();
				}
			}

		}
	};

	private long total = 0;

	private Receiver apReceiver = new Receiver() {
		/**
		 * 针对不同的pc版本进行处理，如果pc版本协议大于1.0的，则以softap比较。如果不是，则以pcname名字比较。
		 */
		@Override
		public void addAnnouncedServers(PCClientItem servers) {
			if (servers.getVersion() != null
					&& Double.parseDouble(servers.getVersion()) > 1.0) {
				if (servers.getSoftAp() != null
						&& servers.getSoftAp().equals(pcInfo.getSoftAp())) {
					connectPCBySoftAp(servers);
				}
			} else if (servers.getVersion() == null
					&& servers.getPcname() != null
					&& servers.getPcname().equals(pcInfo.getPcname())) {
				connectPCBySoftAp(servers);
			}
		}
	};

	private Runnable overTimeRun = new Runnable() {

		@Override
		public void run() {
			FLog.i(TAG, "给定的时间内，没有监听到任何广播，退出监听，并失败.");
			UDPDiscover.getInstance().unRegisterReceiver(apReceiver);
			eventFail();
			if (stateChange != null) {
				stateChange.onChange(ERROR);
			}
		}
	};
	
	private Runnable overTimeOneDriveRun = new Runnable() {

		@Override
		public void run() {
			FLog.i(TAG, "给定的时间内，没有监听到任何广播，退出监听，并失败.");
			Bundle extras = new Bundle();
			extras.putString(Constants.CLOUD_KEY, Constants.CLOUD_ONE_DRIVER);
			extras.putString(Constants.CLOUD_CMD, Constants.CLOUD_CMD_STOP);
			Intent intent = new Intent(context, CoreService.class);
			intent.putExtras(extras);
			context.startService(intent);
			if (stateChange != null) {
				stateChange.onChange(ERROR);
			}
		}
	};

	private Runnable lanConnect = new Runnable() {
		@Override
		public void run() {
			FLog.i(TAG, "lan transfer..." + pcInfo.getType());
			stateChange.onChange(CONNECT_LAN);
			startServer();
			if (connectPC(new String[] { pcInfo.getIp() })) {
				eventSuccess();
			} else {
				eventFail();
			}
		}
	};

	private void eventTime(String event, long du) {
		StatisticsUtil.getDefaultInstance(context).onEventValueCalculate(event,
				null, (int) du);
	}

	private void event(String event) {
		StatisticsUtil.getDefaultInstance(context).onEventCount(event);
	}

	// 获取从未导出图片的用户
	private void eventNewExportPhotoUser() {
		try {
			if (null == DBMgr.getInstance(context).getMaxObject("size",
					ExportedImageItem.class)) {
				String eventid = Constants.UMENG.USER_BEHAVE.USED_EXPORT_FUNCTION_NEW_USER;
				StatisticsUtil.getDefaultInstance(context)
						.onEventCount(eventid);
			}
		} catch (Exception e) {
			FLog.e(TAG, "eventNewUser throw error", e);
		}
	}

	private void eventSuccess() {
		long time = System.currentTimeMillis() - start;
		if (pcInfo.getType() == PCClientItem.DISCOVER_BY_CLOUND) {
			event(NETWORK_CONNECT.UM_EVENT_CONNECT_CLOUD_SUCCESS);
			eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_CLOUD_SUCCESS_TIME, time);
		} else if (pcInfo.getType() == PCClientItem.DISCOVER_BY_UDP) {
			event(NETWORK_CONNECT.UM_EVENT_CONNECT_LAN_SUCCESS);
			eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_LAN_SUCCESS_TIME, time);
		} else {
			event(NETWORK_CONNECT.UM_EVENT_CONNECT_SOFATAP_SUCCESS);
			eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_SOFATAP_SUCCESS_TIME,
					time);
		}
		event(NETWORK_CONNECT.UM_EVENT_CONNECT_SUCCESS);
		eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_SUCCESS_TIME, time);
	}

	private void eventFail() {
		long time = System.currentTimeMillis() - start;
		if (pcInfo.getType() == PCClientItem.DISCOVER_BY_CLOUND) {
			event(NETWORK_CONNECT.UM_EVENT_CONNECT_CLOUD_FAIL);
			eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_CLOUD_FAIL_TIME, time);
		} else if (pcInfo.getType() == PCClientItem.DISCOVER_BY_UDP) {
			event(NETWORK_CONNECT.UM_EVENT_CONNECT_LAN_FAIL);
			eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_LAN_FAIL_TIME, time);
		} else {
			event(NETWORK_CONNECT.UM_EVENT_CONNECT_SOFATAP_FAIL);
			eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_SOFATAP_FAIL_TIME, time);
		}
		event(NETWORK_CONNECT.UM_EVENT_CONNECT_FAIL);
		eventTime(NETWORK_CONNECT.UM_EVENT_CONNECT_FAIL_TIME, time);
	}

	private boolean isStop = true;

	/**
	 * 切换网络，发送udp包，监听回包
	 */
	private Thread listenerUDPdateThread = new Thread() {
		public void run() {
			try {
				FLog.i(TAG, "listenerUDPdateThread start.");
				String message;
				byte[] lmessage = new byte[1024];
				DatagramPacket packet = new DatagramPacket(lmessage,
						lmessage.length);

				UdpConnect.closeSocket();
				DatagramSocket socket = UdpConnect.getUdpConnectInstance();
				socket.setSoTimeout(1000);
				while (isStop) {
					try {
						FLog.i(TAG, "receier message:");
						socket.receive(packet);
						message = new String(lmessage, 0, packet.getLength());
						String ip = ((InetSocketAddress) packet
								.getSocketAddress()).getAddress().toString();
						ip = ip.replace("/", "");
						FLog.i(TAG, pcInfo.getClient().getIp() + " receier "
								+ ip + " message:" + message);
						if (pcInfo.getClient().getIp().contains(ip)) {
							isStop = false;
							handler.removeCallbacks(timeoutTask);
							startServer();
							if (connectPC(new String[] { ip })) {
								eventSuccess();
							} else {
								eventFail();
							}
						}

					} catch (SocketTimeoutException e) {
					}
				}
				FLog.i(TAG, "listenerUDPdateThread stop.");
			} catch (Exception e) {
				FLog.e(TAG, e);
			}
		}
	};

	private boolean isNeedRunning = true;

	/**
	 * 向指定pc发起请求下载命令
	 * 
	 * @param ip
	 */
	private boolean connectPC(String[] ips) {
		int count = 0;
		PhoneInfo info = new PhoneInfo();
		info.initMoreInfo(context);
		PhoneInfoPkt pkg = new PhoneInfoPkt();
		pkg.setCmd(Constants.CMD_START_DOWNLOAD);
		pkg.setDevicename(info.getDevicename());
		pkg.setDeviceid(info.getDeviceid());
		pkg.setGroupTime(String.valueOf(System.currentTimeMillis()));
		String path = Constants.APP_ROOT_PATH + Constants.HTTP_EXPORT_FILE_NAME;
		pkg.setPath(path);
		for (String ip : ips) {
			FLog.i(TAG, "connectPC start...." + ip);
		}
		while (isNeedRunning) {
			try {
				for (String ip : ips) {
					UdpClient.send(ip, pkg.toJson());
				}
				Thread.sleep(Constants.INTERVAL_SEND_START_DOWNLOAD_UDP);
			} catch (Exception e) {
				FLog.e(TAG, "send udp package to pc, throw error", e);
			}
			if (++count > TRYTIMES) {
				FLog.i(TAG,
						"send >20s to pc to start download.no resoponse and exit.");
				if (stateChange != null) {
					stateChange.onChange(ERROR);
				}
				eventFail();
				return false;
			}
		}
		FLog.i(TAG, "connectPC end....");
		return true;
	}

	/**
	 * 如果在给定时间内不能收到回应，则视为超时。切换连接softap<br>
	 * 已弃用
	 */
	private Runnable timeoutTask = new Runnable() {

		@Override
		public void run() {
			FLog.i(TAG, "receiver message,timeout.");
			isStop = false;
			if (!connectSoftAp()) {
				if (stateChange != null) {
					stateChange.onChange(ERROR);
				}
				eventFail();
			}
		}
	};

	/**
	 * 如果在给定时间内不能收到回应，则视为超时,退出。<br>
	 * 目前使用此方法
	 */
	private Runnable timeoutTask2 = new Runnable() {

		@Override
		public void run() {
			FLog.i(TAG, "receiver message,timeout.");
			isStop = false;
			if (stateChange != null) {
				stateChange.onChange(ERROR);
			}
			eventFail();
		}
	};

	/**
	 * 查询给定ssid，是否存在在android手机列表中
	 * 
	 * @param pcSsid
	 * @return
	 */
	private boolean isInOwnerList(String pcSsid) {
		return isInConnecttedList(pcSsid) && isInScanList(pcSsid);
	}

	/**
	 * 根据ssid是否是已连接过的列表中<br>
	 * 主要是判断是否存有该ssid的密码<br>
	 * 
	 * @param pcSsid
	 * @return
	 */
	private boolean isInConnecttedList(String pcSsid) {
		return getWifiConfiguration(pcSsid) != null;
	}

	/**
	 * 根据ssid查询该ssid的连接信息
	 * 
	 * @param ssid
	 * @return
	 */
	private WifiConfiguration getWifiConfiguration(String ssid) {
		try {
			do {
				if (null == ssid || ssid.equalsIgnoreCase("")) {
					break;
				}
				WifiManager mWifi = (WifiManager) context
						.getSystemService(Context.WIFI_SERVICE);
				List<WifiConfiguration> result = mWifi.getConfiguredNetworks();
				if (null == result) {
					break;
				}
				for (WifiConfiguration config : result) {
					if (null != config
							&& config.SSID.equals("\"" + ssid + "\"")) {
						return config;
					}
				}
			} while (false);
		} catch (Exception e) {
			FLog.e(TAG, "getWifiConfiguration throw error", e);
		}
		return null;
	}

	/**
	 * 根据ssid，查询是否能搜素到并匹配
	 * 
	 * @param pcSsid
	 * @return
	 */
	private boolean isInScanList(String pcSsid) {
		/*
		 * WifiManager mWifi = (WifiManager) context
		 * .getSystemService(Context.WIFI_SERVICE); List<ScanResult> result =
		 * mWifi.getScanResults(); for (ScanResult config : result) { if
		 * (config.SSID.equals("\"" + pcSsid + "\"")) { return true; } } return
		 * false;
		 */
		return true;
	}

	/**
	 * 尝试连接pc所在的wifi
	 * 
	 * @param mWifi
	 * @param mConnectivityManager
	 * @param task
	 * @return
	 */
	private boolean tryConnectPCNet(WifiManager mWifi,
			ConnectivityManager mConnectivityManager, Runnable task) {
		int times = 0;
		while (isStop) {
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			WifiInfo info = mWifi.getConnectionInfo();
			if (isRightConnectSsid(mWiFiNetworkInfo, info, pcInfo.getClient()
					.getNet_id())) {
				FLog.i(TAG, "Android has connectted to this networkid:"
						+ pcInfo.getClient().getNet_id()
						+ ", and to test pc client.");
				handler.postDelayed(task, SLEEPTIMES * TRYTIMES);
				new Thread(listenerUDPdateThread).start();
				for (int i = 1; i < TRYTIMES && isStop; i++) {
					String[] ips = pcInfo.getClient().getIp().split(",");
					for (String ip : ips) {
						UdpClient.send(ip, "{\"cmd\": \"kTestAlive\"}");
					}
					try {
						Thread.sleep(SLEEPTIMES);
					} catch (InterruptedException e) {
						FLog.e(TAG, e);
					}
				}
				break;
			}
			if (++times < TRYTIMES) {
				try {
					Thread.sleep(SLEEPTIMES);
				} catch (InterruptedException e) {
					FLog.e(TAG, e);
				}
			}
		}

		if (times >= TRYTIMES) {
			FLog.i(TAG, "after try 20s, connect already not succes,fail.");
			return false;
		}
		return true;
	}

	/**
	 * 尝试连接pc所在的wifi
	 * 
	 * @param mWifi
	 * @param mConnectivityManager
	 * @param task
	 * @return
	 */
	private boolean tryConnectPCNet(WifiManager mWifi,
			ConnectivityManager mConnectivityManager) {
		int times = 0;
		while (isNeedRunning) {
			NetworkInfo mWiFiNetworkInfo = mConnectivityManager
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			WifiInfo info = mWifi.getConnectionInfo();
			if (isRightConnectSsid(mWiFiNetworkInfo, info, pcInfo.getClient()
					.getNet_id())) {
				FLog.i(TAG, "Android has connectted to this networkid:"
						+ pcInfo.getClient().getNet_id());
				startServer();
				String[] ips = pcInfo.getClient().getIp().split(",");
				if (connectPC(ips)) {
					eventSuccess();
					return true;
				} else {
					eventFail();
					return false;
				}
			}
			if (++times < TRYTIMES) {
				try {
					Thread.sleep(SLEEPTIMES);
				} catch (InterruptedException e) {
					FLog.e(TAG, e);
				}
			}
			if (times >= TRYTIMES) {
				FLog.i(TAG, "after try 20s, connect already not succes,fail.");
				return false;
			}
		}

		return true;
	}

	/**
	 * 是否正确的且已经连接上该ssid
	 * 
	 * @param mWiFiNetworkInfo
	 * @param info
	 * @param ssid
	 * @return
	 */
	private boolean isRightConnectSsid(NetworkInfo mWiFiNetworkInfo,
			WifiInfo info, String ssid) {
		boolean ret = mWiFiNetworkInfo != null;
		ret = ret && mWiFiNetworkInfo.isConnected();
		if (ret) {
			ret = ret && info != null;
			ret = ret && info.getSSID().equals("\"" + ssid + "\"");
			if (ret) {
				FLog.i(TAG, "current ssid is \"" + ssid + "\"");
			} else {
				FLog.i(TAG, "current ssid is " + info.getSSID()
						+ " but we need ssid is \"" + ssid + "\"");
			}
		}
		return ret;
	}

	/***
	 * 连接pc所在的wifi
	 * 
	 * @param task
	 *            未连接上的回调callback
	 * @return
	 */
	private boolean connectPCNet(Runnable task) {
		long start = System.currentTimeMillis();
		boolean ret = false;
		if (pcInfo.getClient() != null
				&& pcInfo.getClient().getNet_id() != null
				&& !"".equals(pcInfo.getClient().getNet_id().trim())) {
			FLog.i(TAG, "pc net id:" + pcInfo.getClient().getNet_id());
			if (isInOwnerList(pcInfo.getClient().getNet_id())) {
				WifiConfiguration config = getWifiConfiguration(pcInfo
						.getClient().getNet_id());
				if (config != null) {
					FLog.i(TAG,
							"Android has information to connect this networkid:"
									+ pcInfo.getClient().getNet_id());

					WifiManager mWifi = (WifiManager) context
							.getSystemService(Context.WIFI_SERVICE);
					boolean result = true;
					if (mWifi.getConnectionInfo() == null
							|| !pcInfo
									.getClient()
									.getNet_id()
									.equals(mWifi.getConnectionInfo().getSSID())) {
						String message = "We need ssid is "
								+ pcInfo.getClient().getNet_id();
						if (mWifi.getConnectionInfo() != null) {
							message = message + ",but current ssid is"
									+ mWifi.getConnectionInfo().getSSID();
						}
						FLog.i(TAG, message);
						KeepUserNetwork.getInstance(context)
								.saveIfNotSaveOldNetWorkId();
						result = mWifi.enableNetwork(config.networkId, true);
					}
					if (result) {
						FLog.i(TAG, "Android connect to this networkid:"
								+ pcInfo.getClient().getNet_id()
								+ ", and to get connected info");
						ConnectivityManager mConnectivityManager = (ConnectivityManager) context
								.getSystemService(Context.CONNECTIVITY_SERVICE);

						ret = tryConnectPCNet(mWifi, mConnectivityManager);
					} else {
						FLog.i(TAG, "some error,can't connect this "
								+ pcInfo.getClient().getNet_id() + "");
						ret = false;
					}
				} else {
					FLog.i(TAG,
							"Android don't has this network password,can't connect.");
					ret = false;
				}
			} else {
				FLog.i(TAG,
						"Android don't has this network this time,can't connect.");
				ret = false;
			}
		} else {
			FLog.i(TAG,
					"can't got the pc network id.may be pc not register information.");
			ret = false;
		}
		return ret;
	}

	// wifi热点开关
	public boolean enableApEnabled() {
		if (stateChange != null) {
			stateChange.onChange(CONNECT_WIFIAP);
		}
		FLog.i(TAG, "enable 热点,ssid:" + pcInfo.getSoftAp()
				+ "_Android and pwd is Aa123456");
		return WifiApManager.getInstance(context).enable(pcInfo);
	}

	private void connectPCBySoftAp(PCClientItem servers) {
		FLog.i(TAG, "cost time:" + (System.currentTimeMillis() - total));
		FLog.i(TAG, "热点启动成功，监听该局域网下的广播，监听到了，就发送下载命令，并退出监听");
		UDPDiscover.getInstance().unRegisterReceiver(apReceiver);
		handler.removeCallbacks(overTimeRun);
		FLog.i(TAG, servers.getPcname() + ",下载命令ip:" + servers.getIp()+",softap:" + servers.getSoftAp());
		startServer();
		if (connectPC(new String[] { servers.getIp() })) {
			eventSuccess();
		} else {
			eventFail();
		}
	}
}
