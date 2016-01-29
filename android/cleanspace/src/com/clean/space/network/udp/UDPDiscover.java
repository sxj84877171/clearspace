package com.clean.space.network.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.network.discover.DiscoverManager.Receiver;
import com.clean.space.protocol.PCClientItem;

/**
 * 通过局域网发送广播，来监听pc端的回音。
 * 
 * @author Elvis
 * 
 */
public class UDPDiscover {
	private static final String TAG = UDPDiscover.class.getSimpleName();
	private static final int DISCOVERY_PORT = Constants.UDP_SERVER_BROADCAST_PORT;
	private List<Receiver> mReceivers = new ArrayList<Receiver>();
	private static UDPDiscover instance = new UDPDiscover();

	public static UDPDiscover getInstance() {
		return instance;
	}

	public void stop() {
		isStop = false;
	}

	/**
	 * 通过局域网发送广播，来监听pc端的回音。
	 */
	private UDPDiscover() {
	}

	public void registerReceiver(Receiver mReceiver) {
		if (!mReceivers.contains(mReceiver)) {
			mReceivers.add(mReceiver);
		}
	}

	private boolean isStop = false;

	public void unRegisterReceiver(Receiver receiver){
		mReceivers.remove(receiver);
	}
	
	private Thread listenerThread = new Thread() {
		public void run() {
			listenForResponses();
		}
	};

	/**
	 * Listen on socket for responses, timing out after TIMEOUT_MS
	 * 
	 */
	private void listenForResponses() {
		FLog.i(TAG, "UDPDiscover start." + Thread.currentThread().getId());
		byte[] buf = new byte[1024];
		ArrayList<PCClientItem> servers = new ArrayList<PCClientItem>();

		DatagramSocket socket = null;
		while (isStop) {
			try {
				socket = new DatagramSocket(DISCOVERY_PORT);
				while (isStop) {
					DatagramPacket packet = new DatagramPacket(buf, buf.length);
					socket.receive(packet);
					String jsonStr = new String(packet.getData(), 0,
							packet.getLength());
					String ip = ((InetSocketAddress) packet.getSocketAddress())
							.getAddress().toString();
					ip = ip.replace("/", "");
					PCClientItem server = PCClientItem.parse(jsonStr);
//					 FLog.i(TAG, "message:" + server.toJson() + "ip:" + ip);
					server.setIp(ip);
					server.setRectime(System.currentTimeMillis());
					server.setType(PCClientItem.DISCOVER_BY_UDP);
					for (Receiver mReceiver : mReceivers) {
						if (mReceiver != null) {
							mReceiver.addAnnouncedServers(server);
						}
					}
				}
			} catch (Exception e) {
				try {
					Thread.sleep(2000);
				} catch (Exception e1) {
				}
			} finally {
				if (socket != null) {
					socket.close();
				}
			}
		}
		FLog.i(TAG, "listenForResponses stop." + Thread.currentThread().getId());
	}

	public void start() {
		if (!isStop) {
			FLog.i(TAG, "UDPDiscover start.");
			isStop = true;
			new Thread(listenerThread).start();
		}
	}

}
