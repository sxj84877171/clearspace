package com.clean.space.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.clean.space.log.FLog;

public class ReadMataDataUtil {

	public static final String UMENG_CHANNEL = "UMENG_CHANNEL" ;
	public static final String ONEDRIVE = "ONEDRIVE_APPKEY" ;
	public static String getChannel(Context context) {
		return getMetaDataValue(context, UMENG_CHANNEL);
	}

	public static String getOneDriveApplicationId(Context context) {
		return getMetaDataValue(context, ONEDRIVE);
	}

	public static String getMetaDataValue(Context context, String key) {
		String channel = "";
		try {
			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			String value = appInfo.metaData.getString(key);
			if (null != value) {
				channel = value;
			}
			FLog.i("Tag", " app key : " + channel);
		} catch (PackageManager.NameNotFoundException e) {
			e.printStackTrace();
		}
		return channel;
	}
}
