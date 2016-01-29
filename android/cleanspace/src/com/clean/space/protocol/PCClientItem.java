package com.clean.space.protocol;

import com.clean.space.network.wifi.SoftApDiscover.PCClient;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class PCClientItem extends Packet {
	public static int DISCOVER_BY_UDP = 1 ;
	public static int DISCOVER_BY_CLOUND = 2 ;
	public static int DISCOVER_BY_SOFTAP = 3 ;
	public static int DISCOVER_BY_ONEDRIVE = 4 ;
	public static int OTHER  = 100;
	
	private String name ="";
	private String ip;
	private int status;
	private long rectime;
	private String softAp;
	private PCClient client;
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private String version;

	public PCClient getClient() {
		return client;
	}

	public void setClient(PCClient client) {
		this.client = client;
	}

	private int type = DISCOVER_BY_UDP ;
	
	private int itemType;
	private	boolean online = false;
	private String lastTransferTime;
	private boolean hide;
	
	public boolean isHide() {
		return hide;
	}

	public void setHide(boolean hide) {
		this.hide = hide;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isOnline() {
		return online;
	}

	public void setOnline(boolean online) {
		this.online = online;
	}

	public String getLastTransferTime() {
		return lastTransferTime;
	}

	public void setLastTransferTime(String lastTransferTime) {
		this.lastTransferTime = lastTransferTime;
	}

	public int getItemType() {
		return itemType;
	}

	public void setItemType(int itemType) {
		this.itemType = itemType;
	}
	
	public String getSoftAp() {
		return softAp;
	}

	public void setSoftAp(String softAp) {
		this.softAp = softAp;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public long getRectime() {
		return rectime;
	}

	public void setRectime(long rectime) {
		this.rectime = rectime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getPcname() {
		return name;
	}

	public void setPcname(String pcname) {
		this.name = pcname;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	public static PCClientItem parse(String jsonStr) {
		PCClientItem packet = null;
		try {
			Gson gson = new Gson();
			packet = gson.fromJson(jsonStr, PCClientItem.class);
		} catch (JsonSyntaxException e) {
		}
		return packet;
	}

	@Override
	public boolean equals(Object o) {
		try {
			PCClientItem itemRight = (PCClientItem)o;
			if(null != itemRight){
				return  this.getPcname().equals(itemRight.getPcname());//this.getIp().equalsIgnoreCase(itemRight.getIp()) &&
			}
		} catch (Exception e) {
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public static String getTypeName(int type){
		return type == DISCOVER_BY_UDP ? "UDP发现" : type == DISCOVER_BY_CLOUND ? "云发现":"softap发现" ;
	}
}
