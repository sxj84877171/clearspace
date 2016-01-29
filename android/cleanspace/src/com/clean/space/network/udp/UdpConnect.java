package com.clean.space.network.udp;

import java.net.DatagramSocket;
import java.net.SocketException;

import com.clean.space.Constants;

public class UdpConnect {

	public static DatagramSocket mSocket = null;

	public static DatagramSocket getUdpConnectInstance() {
		if (null == mSocket) {
			try {
				mSocket = new DatagramSocket(Constants.UDP_CMD_PORT);
			} catch (SocketException e) {
			}
		}
		return mSocket;
	}
	
	public static void closeSocket(){
		try {
			if(mSocket != null){
				mSocket.close();
				mSocket = null ;
			}
		} catch (Exception e) {
		}
	}
}
