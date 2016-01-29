package com.clean.space.network.udp;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;

import com.clean.space.Constants;
import com.clean.space.log.FLog;

public class UdpClient {
	
	public static void send(String serverip, String message) {
		message = (message == null ? "Hello IdeasAndroid!" : message);
		int server_port = Constants.UDP_CMD_PORT;
//		FLog.d("UdpClient", "UDP 向 "+ serverip +" 发送数据:" + message);
		InetAddress server = null;
		try {
			server = InetAddress.getByName(serverip);
		} catch (Exception e) {
			FLog.e("UdpClient", "getByName" + e);
		}
		int msg_length = message.length();
		byte[] messageByte = message.getBytes();
		DatagramPacket p = new DatagramPacket(messageByte, msg_length, server,
				server_port);
		try {
			UdpConnect.getUdpConnectInstance().send(p);
		} catch (Exception e) {
			FLog.e("UdpClient", "send" + e);
		}
	}

};