package com.clean.space.protocol;

import com.clean.space.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class Packet {

	private String cmd;
	public String v = Constants.PROTOCOL_VERSION;

	// //////////////////////////////////////////////////////////////////////////////////////
	public static Packet parse(String jsonStr) {
		Packet packet = null;
		try {
			Gson gson = new Gson();
			packet = gson.fromJson(jsonStr, Packet.class);
		} catch (JsonSyntaxException e) {
		}
		return packet;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	public String getVersion() {
		return v;
	}

	public void setVersion(String v) {
		if ("".equals(v))
			this.v = null;
		else
			this.v = v;
	}

	public String getCmd() {
		return cmd;
	}

	public void setCmd(String cmd) {
		if ("".equals(cmd))
			this.cmd = null;
		else
			this.cmd = cmd;
	}

	public Packet(String v, String cmd) {
		super();
		this.v = v;
		this.cmd = cmd;
	}

	public Packet() {

	}

	public Packet(Packet packet) {
		super();
		this.v = packet.v;
		this.cmd = packet.cmd;
	}

}
