package com.clean.space.network.http;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.RemoteViews;

import com.clean.space.Constants;
import com.clean.space.R;

// later should be move the service.
@SuppressLint("NewApi")
public class FileDownloadThread extends Thread {

	private String url;

	private String path = Constants.APP_ROOT_PATH;
	private String savePath = path + "cleanspace.apk";

	private boolean run = false;

	private final static int DOWNLOAD_ID = 201;

	private Context mContext;
	private Notification.Builder builder;
	private NotificationManager manager;

	private FileDownloadThread(Context context) {
		this.mContext = context;
		manager = (NotificationManager) mContext
				.getSystemService(Context.NOTIFICATION_SERVICE);
		builder = new Notification.Builder(mContext);
	}

	private static FileDownloadThread instance = null;

	public static FileDownloadThread getInstance(Context context) {
		if (instance == null) {
			instance = new FileDownloadThread(context);
		}
		return instance;
	}

	public void sendNotification(int progress, boolean isSuccess) {
		RemoteViews rv = new RemoteViews(mContext.getPackageName(),
				R.layout.download_notification);

		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Date d1 = new Date();
		String t1 = format.format(d1);

		Intent appIntent = new Intent(Intent.ACTION_MAIN);
		appIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		appIntent.setComponent(new ComponentName(mContext.getPackageName(),
				"com.lenovo.linkit.MainActivity"));
		appIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent mainPiIntent = PendingIntent.getActivity(mContext, 0,
				appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		builder.setContentIntent(mainPiIntent);
		builder.setContentTitle(mContext
				.getString(R.string.xphone_download_title));
		builder.setSmallIcon(R.drawable.ic_launcher);
		String text = String.format("%2d", progress) + "%";
		rv.setTextViewText(R.id.notification_desc, text);
		builder.setContentText(text);
		builder.setWhen(System.currentTimeMillis());
		builder.setOngoing(false);
		rv.setTextViewText(R.id.notification_title,
				mContext.getString(R.string.xphone_download_title));
		if (progress != 100 && isSuccess) {
			rv.setViewVisibility(R.id.content_view_progress, View.VISIBLE);
			rv.setViewVisibility(R.id.notification_desc, View.GONE);
			builder.setOngoing(true);
			rv.setProgressBar(R.id.content_view_progress, 100, progress, false);
		} else if (isSuccess) {
			rv.setViewVisibility(R.id.notification_desc, View.VISIBLE);
			rv.setViewVisibility(R.id.content_view_progress, View.GONE);
			rv.setTextViewText(R.id.notification_desc,
					mContext.getString(R.string.xphone_download_desc1));
		} else {
			rv.setViewVisibility(R.id.notification_desc, View.VISIBLE);
			rv.setViewVisibility(R.id.content_view_progress, View.GONE);
			rv.setTextViewText(R.id.notification_desc,
					mContext.getString(R.string.xphone_download_desc2));
		}
		rv.setTextViewText(R.id.notification_time, t1);
		builder.setContent(rv);
		builder.setTicker(mContext.getString(R.string.xphone_download_title));
		Notification notification = builder.build();
		notification.contentView = rv;
		manager.notify(DOWNLOAD_ID, notification);
	}

	public void startDownload() {
		if (run == false) {
			new Thread(this).start();
		}
	}

	@Override
	public void run() {
		super.run();
		URL u;
		try {
			run = true;
			sendNotification(0, true);
			u = new URL(url);
			HttpURLConnection conn = (HttpURLConnection) u.openConnection();
			conn.setConnectTimeout(5000);
			double fileSize = conn.getContentLength();
			InputStream is = conn.getInputStream();
			File pathFile = new File(path);
			if (!pathFile.exists()) {
				pathFile.mkdir();
			}
			File file = new File(savePath);
			if (!file.exists()) {
				file.createNewFile();
			}
			FileOutputStream fos = new FileOutputStream(file);
			BufferedInputStream bis = new BufferedInputStream(is);
			byte[] buffer = new byte[1024];
			int len;
			double total = 0;
			double pcent = 0 ;
			while ((len = bis.read(buffer)) != -1) {
				fos.write(buffer, 0, len);
				total += len;
				double percent = (int) ((total * 100 / fileSize));
				if(percent - pcent >= 1.0){
					pcent = percent ;
					sendNotification((int) percent, true);
				}
			}
			sendNotification(100, true);
			fos.close();
			bis.close();
			is.close();
			installApk(file);
			manager.cancel(DOWNLOAD_ID);
			run = false;
		} catch (Exception e) {
			run = false;
			sendNotification(0, false);
		}
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	protected void installApk(File file) {
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.fromFile(file),
				"application/vnd.android.package-archive");
		mContext.startActivity(intent);
	}

}
