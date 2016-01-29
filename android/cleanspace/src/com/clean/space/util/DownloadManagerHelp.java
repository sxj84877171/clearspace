package com.clean.space.util;

import java.io.File;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

import com.clean.space.Constants;
import com.clean.space.R;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.network.http.FileDownloadThread;

public class DownloadManagerHelp {

	public static final String BASE_PATH = "/cleanspace/download/";

	private static final String TAG = DownloadManagerHelp.class.getSimpleName();

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	private String filename = "cleanspace.apk";//UpdateVersionTask.APK_NAME;

	private Context context;
	private DownloadManager downloadManager;
	private String uri;
	private long downloadId = 0;

	private DownloadManagerHelp(Context c) {
		this.context = c;
		downloadManager = (DownloadManager) c
				.getSystemService(Context.DOWNLOAD_SERVICE);

	}

	private static DownloadManagerHelp instance;

	public static DownloadManagerHelp getInstance(Context c) {
		if (instance == null) {
			instance = new DownloadManagerHelp(c);
		}
		return instance;
	}

	private long getDownloadId(String dirType){
		downloadId = 0;
		try {
			downloadId = downloadManager.enqueue(new DownloadManager.Request(
					Uri.parse(uri))
					.setAllowedNetworkTypes(
							DownloadManager.Request.NETWORK_MOBILE
									| DownloadManager.Request.NETWORK_WIFI)
					.setAllowedOverRoaming(false)
					.setTitle(context.getString(R.string.app_name))
					.setVisibleInDownloadsUi(true)
					.setDestinationInExternalPublicDir(dirType, filename));
		} catch (Exception e) {	
			// download failed when download manager was disable, so we need create download thread by us.  
			FileDownloadThread.getInstance(context).setUrl(uri);
			FileDownloadThread.getInstance(context).startDownload();
			FLog.i(TAG, "getDownloadId throw error,create download thread by us to try again");
		}
		return downloadId;
	}

	private void enableDownloadManager() {
		try {
		     //Open the specific App Info page:
		     Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
		     intent.setData(Uri.parse("package:" + "com.android.providers.downloads"));
		     context.startActivity(intent);

		} catch ( ActivityNotFoundException ex ) {
		     ex.printStackTrace();

		     //Open the generic Apps page:
		     Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS);
		     context.startActivity(intent);
		}
	}
	public void startDownload() {
		if (downloadManager != null) {			
			FLog.i(TAG, "downlaod strat.");
			UserSetting.setBoolean(context, Constants.DOWNLOAD_START, true);
			// 解决K900无法自动升级问题
			downloadId = getDownloadId(BASE_PATH);
			context.registerReceiver(downloadReceiver, new IntentFilter(
					DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		}
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			DownloadManager.Query query = new DownloadManager.Query();
			query.setFilterById(downloadId);
			Cursor c = downloadManager.query(query);
			if (c != null && c.moveToFirst()) {
				int status = c.getInt(c
						.getColumnIndex(DownloadManager.COLUMN_STATUS));
				String filename = c.getString(c
						.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
				switch (status) {
				case DownloadManager.STATUS_PAUSED:
				case DownloadManager.STATUS_PENDING:
				case DownloadManager.STATUS_RUNNING:
				case DownloadManager.STATUS_SUCCESSFUL:
					File file = new File(filename);
					FLog.i("download", "filename:" + filename);
					installApk(file);
					break;
				case DownloadManager.STATUS_FAILED:
					stopDownload();
					break;
				}
			}
			if (c != null) {
				c.close();
			}
		}
	};

	protected void installApk(File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		FLog.i(TAG, "downlaod ok ,and install the application.");
	}

	public void stopDownload() {
		if (downloadId != 0) {
			downloadManager.remove(downloadId);
			context.unregisterReceiver(downloadReceiver);
			downloadId = 0;
		}
	}

}
