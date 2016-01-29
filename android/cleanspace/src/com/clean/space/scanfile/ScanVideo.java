package com.clean.space.scanfile;

import java.io.File;

import com.clean.space.log.FLog;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class ScanVideo {

	private static final String TAG = "ScanVideo";

	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	private void getFiles(final Context context) {

		new Thread(new Runnable() {

			@Override
			public void run() {
				Cursor cursor = null;
				try {
					Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
					ContentResolver mContentResolver = context
							.getContentResolver();

					cursor = mContentResolver.query(mImageUri, null,
							MediaStore.Video.Media.DATA + "=%?%",
							new String[] { "DCIM" },
							MediaStore.Video.Media.DATE_MODIFIED);

					if (cursor == null) {
						return;
					}
				} catch (Exception e) {
					FLog.e(TAG, e);
				}

				try {
					while (cursor.moveToNext()) {
						// 获取图片的路径
						String path = cursor.getString(cursor
								.getColumnIndex(MediaStore.Images.Media.DATA));

						// 获取该图片的父路径名
						String parentName = new File(path).getParentFile()
								.getName();

					}
				} catch (Exception e) {
					FLog.e(TAG, e);
				}
				// 通知Handler扫描图片完成
				// mHandler.sendEmptyMessage(SCAN_OK);
				cursor.close();
			}
		}).start();

	}

}
