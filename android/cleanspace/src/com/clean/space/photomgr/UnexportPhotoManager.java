package com.clean.space.photomgr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.clean.space.db.DBMgr;
import com.clean.space.log.FLog;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;

public class UnexportPhotoManager extends BasePhotoManager {

	private final String TAG = "UnexportPhotoManager";
	Context mContext = null;
	private boolean mStopEngine = false;
	private List<FileItem> mListImage = new ArrayList<FileItem>();

	public UnexportPhotoManager(Context context) {
		super(context);
		mContext = context;
	}
	// 获取已扫描到的文件尺寸
	@Override
	public long getScannedPhotoSize() {
		if (mListImage.isEmpty()) {
			startScan(IPhotoManager.SORT_TYPE_TIME, IPhotoManager.ORDER_BY_ASC,
					0);
		}
		long fileTotalSize = super.getFileSize(mListImage);
		return fileTotalSize;
	}

	@Override
	public boolean startScan(final String sortType, final String orderType,
			final int photoCount) {
		FLog.i(TAG, "startScan start");
		if(null == mListImage){
			mListImage = new ArrayList<FileItem>();
		}
		Thread thread = new Thread(new Runnable() {
			public void run() {
				try {
					AllPhotoManager photoMgr = (AllPhotoManager) PhotoManagerFactory
							.getInstance(mContext,
									PhotoManagerFactory.PHOTO_MGR_ALL);
					

					UnexportPhotoManager unexportedMgr = (UnexportPhotoManager)PhotoManagerFactory
							.getInstance(mContext,
									PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
					if (isClearMemoryCache()) {
						mListImage.clear();
					}
					if (sortType.equalsIgnoreCase(photoMgr.getSortType())
							&& orderType.equalsIgnoreCase(photoMgr
									.getOrderType())) {
						unexportedMgr.onNotifyUI(mListImage);
					} else {
						mListImage.clear();
					}
					// 获取当前手机的所有文件
					List<FileItem> listNow = photoMgr.getPhotosSync(sortType,
							orderType, photoCount);

					// 获取未导出文件
					List<FileItem> listNewlyUxport = getUnexportList(listNow);
					
					// 过滤掉上次已经导出的文件,只把最新的数据通知界面
					listNewlyUxport = photoMgr.filterList(listNewlyUxport,
							mListImage);
					if(null != listNewlyUxport){
						if (null != mPhotoListner) {
							if (listNewlyUxport.size() > photoCount
									&& photoCount != 0) {
								unexportedMgr.onNotifyUI(listNewlyUxport.subList(0,
										photoCount - 1));
							} else {
								unexportedMgr.onNotifyUI(listNewlyUxport);
							}
						}
						if (!listNewlyUxport.isEmpty()) {
							mListImage.addAll(listNewlyUxport);
						}
					}
					onScanFinished();
					unexportedMgr.setSortType(sortType);
					unexportedMgr.setOrderType(orderType);
				} catch (Exception e) {
					FLog.e(TAG, "startScan throw error", e);
				}
			}
		});
		thread.start();
		return false;
	}

	// 只查找數據庫
	private List<FileItem> getUnexportList(List<FileItem> listNow) {

		List<FileItem> listNowTemp = new ArrayList<FileItem>(listNow);
		List<FileItem> listDest = new ArrayList<FileItem>();
		for (FileItem item : listNowTemp) {
			try {
				if(null == item){
					continue;
				}
				// 查询该对象是否存在,如果存在则无需加入数据库表
				if (!DBMgr.getInstance(mContext).queryRecord(ExportedImageItem.class, item)) {
					listDest.add(item);
				}
			} catch (Exception e) {
				FLog.e(TAG, "getUnexportList throw error", e);
			}
		}

		return listDest;
	}

	@Override
	public boolean stopScan() {
		mStopEngine = true;
		return false;
	}

}
