package com.clean.space.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class PhoneInfoPkt extends Packet {

	private String path;
	private String devicename;
	private String httport;
	private String cmdport;
	private String deviceid;
	private String   groupTime;

	public String getGroupTime() {
		return groupTime;
	}

	public void setGroupTime(String groupTime) {
		this.groupTime = groupTime;
	}

	public String getDeviceid() {
		return deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	public static PhoneInfoPkt parse(String jsonStr) {
		PhoneInfoPkt packet = null;
		try {
			Gson gson = new Gson();
			packet = gson.fromJson(jsonStr, PhoneInfoPkt.class);
		} catch (JsonSyntaxException e) {
		}
		return packet;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}

}
