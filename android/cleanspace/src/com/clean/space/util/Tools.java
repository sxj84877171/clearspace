package com.clean.space.util;

import android.content.Context;
import android.telephony.TelephonyManager;

public class Tools {

	public static String getAndroidDeviceID(Context context) {
		final TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
}
