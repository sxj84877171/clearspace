package com.clean.space;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.clean.space.log.FLog;
import com.clean.space.notification.CleanSpaceNotificationManager;

public class CameraReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		FLog.i(CameraReceiver.class.getSimpleName(), "CameraReceiver");
		Intent service = new Intent();
		service.setAction("com.clean.space.CoreService");
		service.setPackage(context.getPackageName());
		context.startService(service);
		CleanSpaceNotificationManager.getInstance().sendTakePhotoNotification(
				context);
	}

}
