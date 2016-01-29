package com.clean.space.network.http;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;
import android.net.wifi.WifiManager;

import com.clean.space.log.FLog;
import com.clean.space.network.discover.DiscoverManager;
import com.clean.space.network.udp.UdpClient;
import com.clean.space.network.udp.UdpConnect;
import com.clean.space.protocol.PCClientItem;
import com.clean.space.protocol.PCInfo;
import com.clean.space.protocol.PCListInfo;
import com.clean.space.util.WifiUtils;

/**
 * 云发现<br>
 * 通过当前ssid的名称在云端查询，是否存在相同ssid的设备。<br>
 * 查询到的设备，通过ip发送udp包，如果对方回应udp包，则属于同一局域网<br>
 * 该类主要是防止路由器屏蔽局域网广播的情景<br><br>
 * 
 * @author Elvis
 * 
 */
public class CloudDiscover {

	public final static String TAG = CloudDiscover.class.getSimpleName();

	public final static String URL = "http://114.215.236.240:8080/relayserver/register?";

	private boolean isStop = true;
	private Context context;
	private WifiManager wifiManager;
	private PCListInfo pcList;
	private DiscoverManager.Receiver receiver;

	/**
	 * 云发现<br>
	 * 通过当前ssid的名称在云端查询，是否存在相同ssid的设备。<br>
	 * 查询到的设备，通过ip发送udp包，如果对方回应udp包，则属于同一局域网<br>
	 * 该类主要是防止路由器屏蔽局域网广播的情景<br><br>
	 * 
	 * @param context
	 */
	public CloudDiscover(Context context) {
		this.context = context;
		wifiManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	private Thread verifyPCThread = new Thread() {
		public void run() {
			while (isStop) {
				try {
					StringBuilder sb = new StringBuilder();
					sb.append("device_id=")
							.append(WifiUtils.getDeviceId(context)).append("&");
					sb.append("net_id=").append(WifiUtils.getSsid(wifiManager))
							.append("&");
					sb.append("ip=").append(WifiUtils.getLocalIp(wifiManager))
							.append("&");
					sb.append("os_type=").append("android");

					HttpGet httpGet = new HttpGet(URL + sb.toString());
					HttpResponse response = new DefaultHttpClient()
							.execute(httpGet);
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						String result = EntityUtils
								.toString(entity, HTTP.UTF_8);
						pcList = PCListInfo.parse(result);
//						FLog.i(TAG, "result:" + result);
					}
					if (pcList != null) {
						for (PCInfo info : pcList.getPeers()) {
							if ("pc".equalsIgnoreCase(info.getOs_type())
									|| info.getOs_type() == null) {
								String[] ips = info.getIp().split(",");
								for (String ip : ips) {
									UdpClient.send(ip,
											"{  \"cmd\": \"kTestAlive\"}");
								}
							}
						}
					}
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} catch (Exception e) {
				}
			}
			FLog.i(TAG, "verifyPCThread stop.");
		}
	};

	private Thread listenerUDPdateThread = new Thread() {
		public void run() {
			try {
				FLog.i(TAG, "listenerUDPdateThread start.");
				String message;
				byte[] lmessage = new byte[1024];
				DatagramPacket packet = new DatagramPacket(lmessage,
						lmessage.length);

				DatagramSocket socket = UdpConnect.getUdpConnectInstance();
				socket.setSoTimeout(1000);
				while (isStop) {
					try {
						socket.receive(packet);
						message = new String(lmessage, 0, packet.getLength());
						String ip = ((InetSocketAddress) packet
								.getSocketAddress()).getAddress().toString();
						ip = ip.replace("/", "");
//						FLog.i(TAG, "receier message:" + message + ",ip:" +  ip);

						PCClientItem server = PCClientItem.parse(message);
						server.setIp(ip);
						server.setRectime(System.currentTimeMillis());
						server.setType(PCClientItem.DISCOVER_BY_CLOUND);
						if (receiver != null) {
							receiver.addAnnouncedServers(server);
						}
					} catch (Exception e) {
					}
				}
				FLog.i(TAG, "listenerUDPdateThread stop.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public void start() {
		FLog.i(TAG, "CloundDiscover start.");
		isStop = true;
		new Thread(listenerUDPdateThread).start();
		new Thread(verifyPCThread).start();
	}

	public void stop() {
		isStop = false;
		FLog.i(TAG, "CloundDiscover stop.");
	}

	public void setReceiver(DiscoverManager.Receiver receiver) {
		this.receiver = receiver;
	}

}
