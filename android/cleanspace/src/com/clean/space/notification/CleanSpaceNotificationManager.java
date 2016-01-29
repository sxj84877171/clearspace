/**
 * 
 */
package com.clean.space.notification;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.clean.space.ExportActivity;
import com.clean.space.R;
import com.clean.space.SplishActivity;
import com.clean.space.log.FLog;

/**
 * @author Elvis
 * 
 */
public class CleanSpaceNotificationManager {

	public static final String TAG = CleanSpaceNotificationManager.class
			.getSimpleName();

	public static final int NOTIFICATION_TAKE_PHOTO_ID = 100;
	public static final int NOTIFICATION_TAKE_VIDEO_ID = NOTIFICATION_TAKE_PHOTO_ID;
	public static final int NOTIFICATION_FREESPACE_LOWER_ID = NOTIFICATION_TAKE_VIDEO_ID;
	public static final int NOTIFICATION_EXPORT_PROCESS_ID = NOTIFICATION_FREESPACE_LOWER_ID;

	private boolean isNeedPopNotification = false;

	private String process = null;

	/**
	 * 
	 */
	private CleanSpaceNotificationManager() {
	}

	private static CleanSpaceNotificationManager instance = new CleanSpaceNotificationManager();

	public static CleanSpaceNotificationManager getInstance() {
		return instance;
	}

	private void sendNotification(Context context, int notificationID, int resid) {
		FLog.i(TAG, "sendNotification :" + isNeedPopNotification);
		sendNotification(context, notificationID, context.getString(resid));
	}

	private void sendNotification(Context context, int notificationID,
			String desc) {
		FLog.i(TAG, "sendNotification :" + isNeedPopNotification);
		if (isNeedPopNotification) {
			NotificationManager mNotifyMgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(context, ExportActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, PendingIntent.FLAG_UPDATE_CURRENT);
//			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;
			// notification.tickerText = desc;
			notification.when = System.currentTimeMillis();
			notification.setLatestEventInfo(context,
					context.getText(R.string.app_name), desc, pendingIntent);
			notification.number = 1;
//			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			mNotifyMgr.notify(notificationID, notification);
			process = null;
		} else {
			process = desc;
		}
	}

	/**
	 * 
	 * @param context
	 */
	public void sendTakePhotoNotification(Context context) {
		FLog.i(TAG, "sendTakePhotoNotification :" + isNeedPopNotification);
		if (isNeedPopNotification) {
			NotificationManager mNotifyMgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					new Intent(context, SplishActivity.class), 0);
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;
			notification.when = System.currentTimeMillis();
			notification.setLatestEventInfo(context,
					context.getText(R.string.app_name),
					context.getString(R.string.clean_notification_desc),
					pendingIntent);
			notification.number = 1;
//			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			mNotifyMgr.notify(NOTIFICATION_TAKE_PHOTO_ID, notification);
		}
	}

	/**
	 * 
	 * @param context
	 */
	public void sendTakeVideoNofication(Context context) {
		FLog.i(TAG, "sendTakeVideoNofication :" + isNeedPopNotification);
		sendNotification(context, NOTIFICATION_TAKE_PHOTO_ID,
				R.string.clean_notification_desc);
	}

	/**
	 * 
	 * @param context
	 */
	public void sendLowFreeSpaceNofication(Context context) {
		FLog.i(TAG, "sendLowFreeSpaceNofication :" + isNeedPopNotification);
		sendNotification(context, NOTIFICATION_TAKE_PHOTO_ID,
				R.string.clean_notification_desc4);
	}

	/**
	 * @param context
	 * @param process
	 */
	public void sendExportProcessNotification(Context context, String process) {
		FLog.i(TAG, "sendExportProcessNotification :" + isNeedPopNotification);
		if (!"100%".equals(process)) {
			String desc = context.getString(R.string.clean_notification_desc2);
			desc = String.format(desc, process);
			sendNotification(context, NOTIFICATION_EXPORT_PROCESS_ID, desc);
		} else {
			sendExportProcessFinshNotification(context);
		}
	}

	/**
	 * @param context
	 */
	public void sendExportProcessFinshNotification(Context context) {
		FLog.i(TAG, "sendExportProcessFinshNotification :"
				+ isNeedPopNotification);
		if (isNeedPopNotification) {
			NotificationManager mNotifyMgr = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			Intent intent = new Intent(context, ExportActivity.class);
			intent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
					intent, 0);
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;
			// notification.tickerText = desc;
			notification.when = System.currentTimeMillis();
			notification.setLatestEventInfo(context,
					context.getText(R.string.app_name),
					context.getString(R.string.clean_notification_desc3),
					pendingIntent);
			notification.number = 1;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			mNotifyMgr.notify(NOTIFICATION_EXPORT_PROCESS_ID, notification);
			process = null;
		} else {
			process = "100%";
		}
	}

	public void cleanAll(Context context) {
		FLog.i(TAG, "cleanAll");
		NotificationManager mNotifyMgr = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotifyMgr.cancelAll();
	}

	public void closeNotificationFunction(Context context) {
		FLog.i(TAG, "closeNotificationFunction");
		cleanAll(context);
		isNeedPopNotification = false;
	}

	public void openNotificationFunction(Context context) {
		FLog.i(TAG, "openNotificationFunction");
		isNeedPopNotification = true;
		if (process != null) {
			sendNotification(context, NOTIFICATION_TAKE_PHOTO_ID, process);
		}
	}
}
