package com.clean.space;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.xmlpull.v1.XmlPullParser;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Xml;
import android.widget.Toast;

import com.clean.space.log.FLog;
import com.clean.space.util.DownloadManagerHelp;

public class UpdateModule {
	public static final String CLEARSPACE_APKPATH = "/sdcard/clearspace/download/";
	public static final String CLEANSPACE_APKNAME = "cleanspace.apk";
	private String TAG = UpdateModule.class.getName();

	public static interface UpdateListener {
		public void onUpdate(String newVersion, String oldVersion, String desc,
				boolean force);
	}

	private ExecutorService singleThreadExecutor = null;

	private UpdateModule() {
		singleThreadExecutor = Executors.newSingleThreadExecutor();
	}

	private static UpdateModule instance;
	private UpdateListener updateListner;

	public void registerUpdateListener(UpdateListener updateListner) {
		this.updateListner = updateListner;
	}

	public void unRegisterUpdateListener() {
		this.updateListner = null;
		if(mContext != null){
			DownloadManagerHelp.getInstance(mContext).stopDownload();
		}
	}

	public static UpdateModule getInstance() {
		if (instance == null) {
			synchronized (UpdateModule.class) {
				if (instance == null) {
					instance = new UpdateModule();
				}
			}
		}
		return instance;
	}

	private boolean autoUpdate = false;
	private Context mContext = null;
	private UpdateInfo info;

	public void autoUpate(final String appStore, Context context) {
		this.mContext = context;
		if (!autoUpdate) {
			autoUpdate = true;
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					checkUpdateConfig(false, appStore);
				}
			});
		}
	}
	
	public void autoUpate( Context context,final String appStore,final Locale language) {
		this.mContext = context;
		if (!autoUpdate) {
			autoUpdate = true;
			singleThreadExecutor.execute(new Runnable() {
				@Override
				public void run() {
					checkUpdateConfig(false, appStore,language);
				}
			});
		}
	}

	public void userUpdate(final String appStore, Context context) {
		this.mContext = context;
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				checkUpdateConfig(false, appStore);
			}
		});
	}
	
	public void userUpdate(Context context,final String appStore,final Locale language) {
		this.mContext = context;
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				checkUpdateConfig(false, appStore,language);
			}
		});
	}

	public void developerUpdate(final String appStore, Context context) {
		this.mContext = context;
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				checkUpdateConfig(true, appStore);
			}
		});
	}
	
	public void developerUpdate( Context context, final String appStore,final Locale language) {
		this.mContext = context;
		singleThreadExecutor.execute(new Runnable() {
			@Override
			public void run() {
				checkUpdateConfig(true, appStore,language);
			}
		});
	}

	private void checkUpdateConfig(boolean test, String appStore) {
		checkUpdateConfig(test, appStore, null);
	}
	
	private void checkUpdateConfig(boolean test, String appStore,Locale language) {
		try {
			String httpServer = UserSetting.getDownloadServerAddress();
			String path = "";
			// String appStore = ReadMataDataUtil.getChannel(mContent);
			if (test) {
				path = String.format(
						"http://%s/update/android/%s/test/update.xml",
						httpServer, appStore);
			} else {
				path = String.format("http://%s/update/android/%s/update.xml",
						httpServer, appStore);
			}

			FLog.i(TAG, "Upgrade path: " + path);

			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			if (conn == null) {
				FLog.e(TAG, "Can not connect upgrade server: " + httpServer);
				return;
			}
			conn.setConnectTimeout(5000);
			InputStream is = conn.getInputStream();
			info = getUpdateInfo(is);

			String sLocalVersion = getVersionName();
			String filename = info.url.substring(info.url.lastIndexOf('/'));
			if (filename == null) {
				filename = CLEANSPACE_APKNAME;
			}
			if (compareVersion(sLocalVersion, info.getVersion()) < 0) {
				if (updateListner != null) {
					boolean force = false;
					try {
						force = Boolean.parseBoolean(info.getForce());
					} catch (Exception e) {
						FLog.e(TAG, e);
						force = false;
					}
					updateListner.onUpdate(info.getVersion(), sLocalVersion,
							info.getLanguageDescription(language), force);
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
	}

	private String getVersionName() throws Exception {
		PackageManager packageManager = this.mContext.getPackageManager();
		PackageInfo packInfo = packageManager.getPackageInfo(
				mContext.getPackageName(), 0);

		return packInfo.versionName;
	}

	private String getVersionCode(String apkPath) {
		PackageInfo pi = mContext.getPackageManager().getPackageArchiveInfo(
				apkPath, PackageManager.GET_ACTIVITIES);
		String versionName = null;
		if (pi != null) {
			versionName = pi.versionName;
		}
		return versionName;
	}

	private int compareVersion(String version1, String version2) {
		String[] v1s = version1.split("\\.");
		String[] v2s = version2.split("\\.");
		for (int i = 0; i < v1s.length; i++) {
			if (Integer.parseInt(v1s[i]) > Integer.parseInt(v2s[i])) {
				return 1;
			} else if (Integer.parseInt(v1s[i]) < Integer.parseInt(v2s[i])) {
				return -1;
			}
		}
		if (v2s.length > v1s.length) {
			return -1;
		}
		return 0;
	}

	private UpdateInfo getUpdateInfo(InputStream is) throws Exception {
		XmlPullParser parser = Xml.newPullParser();
		parser.setInput(is, "utf-8");
		int type = parser.getEventType();
		UpdateInfo info = new UpdateInfo();

		while (type != XmlPullParser.END_DOCUMENT) {
			switch (type) {
			case XmlPullParser.START_TAG:
				if ("version".equals(parser.getName())) {
					// get version number
					info.setVersion(parser.nextText());
				} else if ( "url".equals(parser.getName())) {
					// get the updated apk file
					info.setUrl(parser.nextText()); 
				} else if ("description".equals(parser.getName())) {
					info.setDescription(parser.nextText()); // get the
															// description file
				}else if("force".equals(parser.getName())){
					info.setForce(parser.nextText());
				}
				break;
			}
			type = parser.next();
		}
		return info;
	}

	public void startUpdate() {
		String filename = info.url.substring(info.url.lastIndexOf('/'));
		if (filename == null) {
			filename = CLEANSPACE_APKNAME;
		}
		File file = new File(CLEARSPACE_APKPATH + filename);
		if (info.getVersion().equals(getVersionCode(file.getAbsolutePath()))) {
			installApk(file);
		} else {
			downLoadApk();
		}
	}

	private void downLoadApk() {
		String filename = info.url.substring(info.url.lastIndexOf('/'));
		if (filename == null) {
			filename = CLEANSPACE_APKNAME;
		}
		DownloadManagerHelp.getInstance(mContext).setFilename(filename);
		Toast.makeText(mContext, R.string.xphone_download_toast,
				Toast.LENGTH_LONG).show();
		DownloadManagerHelp.getInstance(mContext).setUri(info.getUrl());

		DownloadManagerHelp.getInstance(mContext).startDownload();
	}

	protected void installApk(File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		mContext.startActivity(intent);
	}
}
