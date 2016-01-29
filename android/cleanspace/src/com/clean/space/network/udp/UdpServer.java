package com.clean.space.network.udp;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

import android.os.Handler;
import android.os.Message;

import com.clean.space.Constants;

public class UdpServer extends Thread {
	private boolean bKeepRunning = true;
	private String lastMessage = "";
	private final int MAX_UDP_DATAGRAM_LEN = 1024;

	Handler mHandler = null;

	public UdpServer(Handler handler) {
		mHandler = handler;
	}

	public void run() {
		String message;
		byte[] lmessage = new byte[MAX_UDP_DATAGRAM_LEN];
		DatagramPacket packet = new DatagramPacket(lmessage, lmessage.length);

		DatagramSocket socket = UdpConnect.getUdpConnectInstance();
		try {
			while (bKeepRunning) {
				try {
					socket.receive(packet);
					message = new String(lmessage, 0, packet.getLength());
					lastMessage = message;

					
					if (null != mHandler) {
						Message msg = new Message();
						msg.what = 0;
						mHandler.sendMessage(msg);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
		}

		if (socket != null) {
			socket.close();
		}
	}

	public void kill() {
		bKeepRunning = false;
	}

	public String getLastMessage() {
		return lastMessage;
	}
	public void parsePackage(String message){
		
	}
};