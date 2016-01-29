package com.clean.space.scanfile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.protocol.FileItem;
import com.clean.space.util.FileUtils;

public class ScanImage {
	private final String mPhotoDir = "DCIM";
	private static final Object mMutex = new Object();


	private boolean stopScan = false;
	public boolean isStopScan() {
		return stopScan;
	}

	public void setStopScan(boolean stopScan) {
		this.stopScan = stopScan;
	}

	private ScanImageListener mListener ;
	private static final String TAG = "ScanImage";
	private boolean ascOrder = true;
	private boolean scanViedo = true;
	private long photoCount = 0;

	public void setScanImageReceiver(ScanImageListener listener) {
		this.mListener = listener;
	}

	public static interface ScanImageListener {
		/**
		 * Process the list of discovered servers. This is always called once
		 * after a short timeout.
		 * 
		 * @param servers
		 *            list of discovered servers, null on error
		 */
		void generateItems(boolean scanFinish, List<FileItem> items);

	}
//	public List<FileItem> getmFileItemList() {
//		return mFileItemList;
//	}

//	public void setmFileItemList(List<FileItem> mFileItemList) {
//		this.mFileItemList = mFileItemList;
//	}

	public long getPhotoCount() {
		return photoCount;
	}

	public void setPhotoCount(long photoCount) {
		this.photoCount = photoCount;
	}

	public boolean isScanViedo() {
		return scanViedo;
	}

	public void setScanViedo(boolean scanViedo) {
		this.scanViedo = scanViedo;
	}

	public boolean isAscOrder() {
		return ascOrder;
	}

	public void setAscOrder(boolean ascOrder) {
		this.ascOrder = ascOrder;
	}
	/**
	 * 利用ContentProvider扫描手机中的图片，此方法在运行在子线程中
	 */
	public  synchronized List<FileItem> scanFilesSync(final Context context, String sortType, String orderBy) {
		List<FileItem> listFile = new ArrayList<FileItem>();
		try {
			initEnv();
			
			sortType = convertSortType(sortType);
			Cursor mCursor = getCursor(context, sortType, orderBy, 0);
			if(null == mCursor){
				return listFile;
			}

			while (mCursor.moveToNext()) {
				// 获取图片的路径
				try {
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));
					FileItem item = generateFileItem(path, 0);
					if(null != item){
						listFile.add(item);
					}
					if (0 != photoCount && photoCount <= listFile.size()) {
						stopScan = true;
					}
					if (stopScan) {
						break;
					}
				} catch (Exception e) {
					FLog.e(TAG, e);
				}
			}
			mCursor.close();
			if (isScanViedo()) {
				listFile.addAll(getVideoFiles(context));
			}
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return listFile;
	}

	// 该函数主要针对相似照片的查找,所以暂时没扫描视频文件
	public void scanFilesAsyc(final Context context, String sortType,
			String orderBy, long startTime) {
		try {
			initEnv();
			Cursor mCursor = getCursor(context, sortType, orderBy, startTime);
			if(null == mCursor){
				return;
			}

			List<FileItem> lstFile = new ArrayList<FileItem>();
			long prevImageTime = 0;
			boolean firstImage = true;
			while (mCursor.moveToNext()) {
				try {
					if (stopScan) {
						break;
					}
					String path = mCursor.getString(mCursor
							.getColumnIndex(MediaStore.Images.Media.DATA));

					FileItem item = generateFileItem(path, startTime);
					if (null == item) {
						continue;
					}
					// 获取时间最相近的文件
					long subTime = Math.abs(item.getDate() - prevImageTime);
					if (subTime > Constants.LEAST_NEAR_TIME && !firstImage) {
						if (null != mListener) {
							mListener.generateItems(false, lstFile);
						}
						lstFile.clear();
					}
					prevImageTime = item.getDate();
					firstImage = false;
					if (null != item) {
						lstFile.add(item);
					}
				} catch (Exception e) {
					FLog.e(TAG, e);
				}
			}

			// 通知客户端,扫描文件完成
			if (null != mListener) {
				mListener.generateItems(true, lstFile);
			}
			mCursor.close();
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
	}


	private String convertSortType(String sortType) {
		if (sortType.equals(IPhotoManager.SORT_TYPE_SIZE)) {
			sortType = MediaStore.Images.Media.SIZE;
		}
		if (sortType.equals(IPhotoManager.SORT_TYPE_TIME)) {
			sortType = MediaStore.Images.Media.DATE_TAKEN;
		}
		return sortType;
	}
	private Cursor getCursor(final Context context, String sortType,
			String orderBy, long startTime) {
		Cursor cursor = null;
		try {
			Uri mImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			ContentResolver mContentResolver = context.getContentResolver();

			// 只查询jpeg和png的图片
			String pathQuery = MediaStore.Images.Media.DATA + " like '% "
					+ this.mPhotoDir + "'";

			sortType = convertSortType(sortType);
			String timeQuery = "";
			if(startTime> 0){
				timeQuery += " and datetaken > " + (startTime);
			}
			cursor = mContentResolver.query(mImageUri, null,
					MediaStore.Images.Media.MIME_TYPE + "=? " +  " "
							 + timeQuery, new String[] {
							"image/jpeg" }, sortType + " "
							+ orderBy);
		} catch (Exception e) {
			FLog.e(TAG, "getCursor throw error", e);
		}

		return cursor;
	}

	private FileItem generateFileItem(String path, long startTime) {
		// 获取该图片的父路径名
		try {
			if(!FileUtils.isFileExist(path)){
				return null;
			}
			File file = new File(path);
			if (null != file && file.isFile()) {
				String parentName = file.getParentFile().getPath();

				FileItem item = new FileItem();
				long date = file.lastModified();
				item.setDate(date);
				item.setDir(parentName);
				item.setPath(path);
				item.setFilename(file.getName());
				long length = file.length();
				// 过滤文件小于等于0的文件
				if (length <= 0) {
					return null;
				}
				if(!path.contains(mPhotoDir)){
				//	return null;
				}
				if(date < startTime){
					return null;
				}
				//FLog.i(TAG, "generateFileItem path= " + item.getPath() + "date= " + item.getDate());
				item.setSize(length);

				return item;
			}
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return null;
	}

//  获取视频文件,该功能后期重构可以单独作为一个组件
	private List<FileItem> getVideoFiles(final Context context) {

		List<FileItem> listFile = new ArrayList<FileItem>();
		Cursor cursor = null;
		try {
			Uri mImageUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
			ContentResolver mContentResolver = context.getContentResolver();

			String pathQuery = MediaStore.Video.Media.DATA + " like '%"
					+ mPhotoDir + "'";
			cursor = mContentResolver.query(mImageUri, null, null, null,
					MediaStore.Video.Media.DATE_MODIFIED);

			if (cursor == null) {
				return listFile;
			}

			while (cursor.moveToNext()) {
				// 获取图片的路径
				String path = cursor.getString(cursor
						.getColumnIndex(MediaStore.Video.Media.DATA));
				if (path.contains(mPhotoDir)) {
				FileItem item =	generateFileItem(path, 0);
				if(null != item){
					listFile.add(item);
				}
				}
				if (stopScan) {
					break;
				}

			}
			// 通知Handler扫描图片完成
			// mHandler.sendEmptyMessage(SCAN_OK);
			cursor.close();
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return listFile;
	}
	private void initEnv(){
		photoCount = 0;
	}

}
