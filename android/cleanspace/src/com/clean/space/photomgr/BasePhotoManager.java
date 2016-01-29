package com.clean.space.photomgr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.clean.space.db.DBMgr;
import com.clean.space.log.FLog;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;
import com.clean.space.util.FileUtils;

public class BasePhotoManager implements IPhotoManager {

	private final String TAG = "ExportedPhotoManager";
	Context mContext = null;
	IPhotoManagerListener mPhotoListner = null;
	private boolean mStopEngine = false;
	private boolean clearMemoryCache = false;

	public boolean isClearMemoryCache() {
		return clearMemoryCache;
	}

	private boolean cancelDelete = false;

	public boolean isCancelDelete() {
		return cancelDelete;
	}

	public void setCancelDelete(boolean cancelDelete) {
		this.cancelDelete = cancelDelete;
	}

	public String sortType = "";
	public String orderType = "";

	public String getSortType() {
		return sortType;
	}

	public void setSortType(String sortType) {
		this.sortType = sortType;
	}

	public String getOrderType() {
		return orderType;
	}

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public BasePhotoManager(Context context) {
		mContext = context;
	}

	@Override
	public void setPhotoManagerListener(IPhotoManagerListener listener) {
		mPhotoListner = listener;
	}

	@Override
	public long getScannedPhotoSize() {
		return 0;
	}

	@Override
	public boolean startScan(String sortType, String orderType, int photoCount) {
		mStopEngine = false;
		return false;
	}

	@Override
	public boolean stopScan() {
		mStopEngine = true;
		return false;
	}

	public long getFileSize(List<?> photos) {
		long fileSize = 0;
		if(null == photos || photos.isEmpty()){
			return fileSize;
		}
		try {
			List<Object> photosTemp = new ArrayList<Object>(photos);
			for (Object itemObj : photosTemp) {
				try {
					FileItem item = (FileItem) itemObj;
					if (null != item) {
						fileSize += item.getSize();
					}

				} catch (Exception e) {
					FLog.e(TAG, "getFileSize throw error", e);
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "getFileSize throw error", e);
		}
		return fileSize;
	}

	// 查詢數據庫后內存比較
	public List<FileItem> filterList(List<FileItem> listNow,
			List<FileItem> listOld) {
		if(null == listNow || listNow.isEmpty()){
			return new ArrayList<FileItem>();
		}
		if(null == listOld || listOld.isEmpty()){
			return listNow;
		}
		// 获取新添加的增量数据
		List<FileItem> listNowTemp = new ArrayList<FileItem>(listNow);
		List<FileItem> listOldTemp = new ArrayList<FileItem>(listOld);
		listNowTemp.removeAll(listOldTemp);
		return listNowTemp;
	}

	public void onScanFinished() {
		if (null != mPhotoListner) {
			mPhotoListner.onScanFinished();
		}
	}

	@Override
	public void cancelDelete() {
		cancelDelete = true;
	}

	@Override
	public void setClearMemoryCache() {
		clearMemoryCache = true;

	}

	private void deletePhotoList(final List<?> photos) {
		if (null == photos || photos.isEmpty()) {
			return;
		}
		Thread thread = new Thread(new Runnable() {

			public void run() {
				long fileTotalSize = getFileSize(photos);
				long fileDeletedSize = 0;
				double percent = 0.0;
				// 刪除文件
				for (Object itemObj : photos) {
					try {
						if (isCancelDelete()) {
							setCancelDelete(false);
							break;
						}
						FileItem item = (FileItem) itemObj;
						if (null == item) {
							continue;
						}
						fileDeletedSize += item.getSize();
						String path = item.getPath();
						boolean bDel = FileUtils.deletePhoto(mContext, path);
						String str = String.format(
								"deletePhotos path = %s? bDel=" + bDel, path);
						FLog.i(TAG, str);
						percent = (double) fileDeletedSize / fileTotalSize
								* 100;

						if (null != mPhotoListner) {
							// 通知界面
							mPhotoListner.onDeletePhotosProgress(
									fileDeletedSize, percent);
							List<FileItem> lstDel = new ArrayList<FileItem>();
							lstDel.add(item);
							mPhotoListner.onDeletePhoto(lstDel);
						}
					} catch (Exception e) {
						FLog.e(TAG, "deletePhotos throw error", e);
					}
				}
				if (null != mPhotoListner) {
					// 删除相片完成,通知界面
					mPhotoListner.onDeleteFinished();
				}	
			}
		});
		thread.start();
	}

	@Override
	public boolean deletePhotos(List<?> photos) {
		deletePhotoList(photos);

		// 清除緩存
		IPhotoManager photoMgr = PhotoManagerFactory.getInstance(
				mContext, PhotoManagerFactory.PHOTO_MGR_ALL);
		photoMgr.setClearMemoryCache();	

		setClearMemoryCache();
		return true;
	}

	public synchronized boolean onNotifyUI(List<?> lstFile) {
		boolean ret = false;
		try {
			// 为了速度,暂时不判断文件是否存在,因为这种情况概率很小
			// lstFile = filterDeletedFile(lstFile);
			if (null != lstFile && !lstFile.isEmpty()) {
				if (null != mPhotoListner) {
					mPhotoListner.onPhoto(lstFile);
					mPhotoListner.onPhotosSize(getFileSize(lstFile));
					ret = true;
				}
			}
		} catch (Exception e) {
		}
		return ret;
	}

	public synchronized List<?> filterDeletedFile(List<?> list) {
		if(null == list || list.isEmpty()){
			return null;
		}
		try {
			for (Object obj : list) {
				FileItem item = (FileItem) obj;
				if (null != item && null != item.getPath()) {
					if (!FileUtils.isFileExist(item.getPath())) {
						list.remove(item);
					}
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "filterDeletedFile throw error", e);
		}
		return list;
	}

	@Override
	public List<?> getPhotosSync(String sortType, String orderType,
			int photoCount) {
		// TODO Auto-generated method stub
		return null;
	}

}
