package com.clean.space;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Created by sam on 14-3-9.
 */
public class UserSetting {
	public static String TAG = "UserSetting";

	private static String mTestHttpServer = "linkandsync.com";
	public static String mDefaultHttpServer = "dworkstudio.com";// "192.168.9.24";//
																// "lynked.net";//"192.168.9.24";//"lynked.net";//"linkandsync.com";//"thelinkit.com";//"lyncmaster.net";////"http://thelinkit.com/"
																// ;//"115.29.178.5";
	public static String mDownloadApkServer = "dworkstudio.com";// "lyncmaster.net";//"xiangfl.com";

	private static final Object mMutex = new Object();

	public static String getClearStausInfo(Context context) {
		synchronized (mMutex) {
			String ret;
			SharedPreferences sh = context.getSharedPreferences(
					Constants.CLEAR_FILE_STATUS, Context.MODE_MULTI_PROCESS);
			ret = sh.getString(Constants.CLEAR_FILE_STATUS, "");
			return ret;
		}
	}

	public static boolean setClearStatusInfo(Context context, String clearstatus) {
		synchronized (mMutex) {
			SharedPreferences sh = context.getSharedPreferences(
					Constants.CLEAR_FILE_STATUS, Context.MODE_MULTI_PROCESS);
			Editor editor = sh.edit();
			editor.putString(Constants.CLEAR_FILE_STATUS, clearstatus);
			editor.apply();
			return editor.commit();
		}
	}

	public static String getDownloadServerAddress() {
		if (Constants.IS_TEST_BUILD) {
			mDownloadApkServer = mTestHttpServer;
		} else {
			mDownloadApkServer = mDefaultHttpServer;
		}
		return mDownloadApkServer;
	}

	public static long getWantCleanSize(Context context, long value) {
		SharedPreferences sh = context.getSharedPreferences(
				Constants.WANT_CLEAN_SIZE, Context.MODE_PRIVATE);
		long ret = sh.getLong(Constants.WANT_CLEAN_SIZE, 0);
		return ret;
	}

	public static void setWantCleanSize(Context context, long content) {
		SharedPreferences sh = context.getSharedPreferences(
				Constants.WANT_CLEAN_SIZE, Context.MODE_PRIVATE);
		Editor editor = sh.edit();
		editor.putLong(Constants.WANT_CLEAN_SIZE, content);
		editor.commit();
		return;
	}

	public static void setString(Context context, String key, String value) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		sp.edit().putString(key, value).commit();

	}

	public static String getString(Context context, String key, String defValue) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		return sp.getString(key, defValue);
	}

	public static void setLong(Context context, String key, long value) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		sp.edit().putLong(key, value).commit();

	}

	public static long getLong(Context context, String key, long defValue) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		return sp.getLong(key, defValue);
	}
	public static void setBoolean(Context context, String key, boolean value) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		sp.edit().putBoolean(key, value).commit();

	}

	public static boolean getBoolean(Context context, String key, boolean defValue) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		return sp.getBoolean(key, defValue);
	}
	
	public static void setInt(Context context, String key, int value) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		sp.edit().putInt(key, value).commit();

	}

	public static int getInt(Context context, String key, int defValue) {
		SharedPreferences sp = context.getSharedPreferences(
				Constants.CONFIGFILE, Context.MODE_PRIVATE);
		return sp.getInt(key, defValue);
	}
}