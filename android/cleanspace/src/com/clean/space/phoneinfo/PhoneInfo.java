package com.clean.space.phoneinfo;

import android.content.Context;
import android.telephony.TelephonyManager;

public class PhoneInfo {
	private String phoneNumber;
	private long currentTime;
	private String sdkVersion;
	private String deviceid;
	private String devicename;

	public String getSdkVersion() {
		return this.sdkVersion;
	}

	public void setSdkVersion(String sdkVersion) {
		this.sdkVersion = sdkVersion;
	}

	public String getPhoneNumber() {
		return this.phoneNumber;
	}

	public void setPhoneNumber(String sPhoneNumber) {
		this.phoneNumber = sPhoneNumber;
	}

	public long getCurrentTime() {
		return this.currentTime;
	}

	public void setCurrentTime(long lCurrentTime) {
		this.currentTime = lCurrentTime;
	}

	public String getDeviceid() {
		return this.deviceid;
	}

	public void setDeviceid(String deviceid) {
		this.deviceid = deviceid;
	}

	public void initMoreInfo(Context context) {
		try {
			TelephonyManager telephonyManager = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			this.phoneNumber = telephonyManager.getLine1Number();
			this.devicename = android.os.Build.MODEL;
			this.deviceid = telephonyManager.getDeviceId();
			this.sdkVersion = "" + android.os.Build.VERSION.SDK_INT;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getDevicename() {
		return devicename;
	}

	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}
}
