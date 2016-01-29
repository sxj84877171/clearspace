package com.clean.space;

import java.io.File;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.StatFs;

import com.clean.space.log.FLog;
import com.clean.space.notification.CleanSpaceNotificationManager;
import com.clean.space.onedriver.OneDriveUploadManage;
import com.clean.space.photomgr.AllPhotoManager;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.PhotoManagerFactory;

/**
 * update1:<br>
 * 开启服务，定期检查磁盘剩余空间和照片大小，提醒用户使用PhoneShifter<br>
 * update2:<br>
 * 添加上传文件功能，第一版，支持添加文件上传到OneDriver云上<Br>
 * 在intent里面参数Constants.CLOUD_ONE_DRIVER
 * 
 * @author Elvis
 * 
 */
public class CoreService extends Service {

	public static final String TAG = CoreService.class.getSimpleName();

	public static final double FREE_SIZE_PERCENT = 0.15d;
	public static final long LIMIT_SIZE = 50 * 1024 * 1024;

	private Handler taskHandler = new Handler();
	public static final long CHECK_TIME = 10 * 60 * 1000;
	private boolean sendNotification = false;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!sendNotification) {
			taskHandler.removeCallbacks(task);
			taskHandler.post(task);
		}
		try {
			if (intent != null) {
				Bundle extras = intent.getExtras();
				if (extras != null) {
					String key = extras.getString(Constants.CLOUD_KEY);
					String cmd = extras.getString(Constants.CLOUD_CMD);
					// oneDriver 云存储
					OneDriveUploadManage.getInstance().setContext(this);
					if (Constants.CLOUD_ONE_DRIVER.equals(key)) {
						if (Constants.CLOUD_CMD_START.equals(cmd)) {
							// 开始上传文件
							OneDriveUploadManage.getInstance().start();
						} else if (Constants.CLOUD_CMD_STOP.equals(cmd)) {
							// 停止上传
							OneDriveUploadManage.getInstance().stop();
						}

					}
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public Runnable task = new Runnable() {

		@Override
		public void run() {
			sendNotification = false;
			checkFreeSpaceTask.run();
		}
	};

	private Runnable checkFreeSpaceTask = new Runnable() {

		@Override
		public void run() {
			File root = Environment.getRootDirectory();
			StatFs sf = new StatFs(root.getPath());

			double percent = (double) sf.getAvailableBlocks()
					/ (double) sf.getBlockCount();
			FLog.i(TAG, "freespace:" + percent);
			AllPhotoManager photoMgr = (AllPhotoManager) PhotoManagerFactory
					.getInstance(getApplication(),
							PhotoManagerFactory.PHOTO_MGR_ALL);
			photoMgr.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			long size = photoMgr.getFileSize();

			FLog.i(TAG, "photo size:" + size);
			if (percent < FREE_SIZE_PERCENT && size >= LIMIT_SIZE) {
				CleanSpaceNotificationManager.getInstance()
						.sendLowFreeSpaceNofication(getBaseContext());
				sendNotification = true;
				taskHandler.postDelayed(task, 24 * 6 * CHECK_TIME);
			} else {
				taskHandler.postDelayed(task, CHECK_TIME);
			}
		}
	};

	private long readSDCardSize() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			File sdcardDir = Environment.getExternalStorageDirectory();
			StatFs sf = new StatFs(sdcardDir.getPath());
			return (long) sf.getBlockSize() * (long) sf.getBlockCount();
		}
		return 0;
	}

	private long readSystemSize() {
		File root = Environment.getRootDirectory();
		StatFs sf = new StatFs(root.getPath());
		sf.getAvailableBlocks();
		return (long) sf.getBlockSize() * (long) sf.getBlockCount();
	}

}
