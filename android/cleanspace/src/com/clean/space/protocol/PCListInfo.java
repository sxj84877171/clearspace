package com.clean.space.protocol;

import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class PCListInfo extends Packet{
	
	private ArrayList<PCInfo> peers = new ArrayList<PCInfo>();

	public ArrayList<PCInfo> getPeers() {
		return peers;
	}

	public void setPeers(ArrayList<PCInfo> peers) {
		this.peers = peers;
	}

	
	public static PCListInfo parse(String jsonStr) {
		PCListInfo packet = null;
		try {
			Gson gson = new Gson();
			packet = gson.fromJson(jsonStr, PCListInfo.class);
		} catch (JsonSyntaxException e) {
		}
		return packet;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
