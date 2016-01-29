package com.clean.space.photomgr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.clean.space.Constants;
import com.clean.space.db.DBMgr;
import com.clean.space.log.FLog;
import com.clean.space.protocol.CurrentExportedImageItem;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.FileUtils;

public class ExportedPhotoManager extends BasePhotoManager {

	private final String TAG = "ExportedPhotoManager";
	Context mContext = null;
	private boolean mStopEngine = false;
	private List<ExportedImageItem> mListImage = new ArrayList<ExportedImageItem>();

	public ExportedPhotoManager(Context context) {
		super(context);
		mContext = context;
	}

	@Override
	public long getScannedPhotoSize() {
		if (mListImage.isEmpty()) {
			startScan(IPhotoManager.SORT_TYPE_TIME, IPhotoManager.ORDER_BY_ASC,
					0);
		}
		return super.getFileSize(mListImage);
	}
	public List<?> getCurrenExportedPhotosSync(final String sortType, final String orderType,
			int photoCount) {
		List<CurrentExportedImageItem> listNow = (List<CurrentExportedImageItem>) DBMgr
				.getInstance(mContext).getData(CurrentExportedImageItem.class,
						sortType, orderType, photoCount);

		return listNow;
	}
	
	@Override
	public List<?> getPhotosSync(final String sortType, final String orderType,
			int photoCount) {
		List<ExportedImageItem> listNow = (List<ExportedImageItem>) DBMgr
				.getInstance(mContext).getData(ExportedImageItem.class,
						sortType, orderType, photoCount);

		return listNow;
	}

	private boolean addToDbTest(String path) {
		List<ExportedImageItem> list2  = null;
		try {
			List<ExportedImageItem> list = new ArrayList<ExportedImageItem>();
			for (int i = 0; i < 100; ++i) {

				ExportedImageItem item = new ExportedImageItem();
				// File file = new File(path);
				item.setDate(i);
				item.setPath("/sdcard/lshl.txt");
				item.setSize(i);
				list.add(item);
				 DBMgr.getInstance(mContext).addTable(item);
			}
		 list2 =	(List<ExportedImageItem>) DBMgr.getInstance(mContext).getData(ExportedImageItem.class, IPhotoManager.SORT_TYPE_SIZE, IPhotoManager.ORDER_BY_ASC, 0);
		int b =0;
			// deletePhotos(list);
		} catch (Exception e) {
			FLog.e(TAG, "addToDb throw error", e);
		}
		return true;
	}

	@Override
	public boolean startScan(final String sortType, final String orderType,
			final int photoCount) {
		FLog.i(TAG, "startScan start");
		mStopEngine = false;
		// addToDbTest("");
		Thread findSimilar = new Thread(new Runnable() {
			public void run() {
				try {
					ExportedPhotoManager photoMgr = (ExportedPhotoManager) PhotoManagerFactory
							.getInstance(mContext,
									PhotoManagerFactory.PHOTO_MGR_EXPORTED);
					if (isClearMemoryCache()) {
						mListImage.clear();
					}

					if (sortType.equalsIgnoreCase(photoMgr.getSortType())
							&& orderType.equalsIgnoreCase(photoMgr
									.getOrderType())) {
						onNotifyUI(mListImage);
					} else {
						mListImage.clear();
					}

					List<ExportedImageItem> listNow = (List<ExportedImageItem>) DBMgr
							.getInstance(mContext).getData(
									ExportedImageItem.class, sortType,
									orderType, photoCount);
					listNow.removeAll(mListImage);
					onNotifyUI(listNow);
					mListImage.addAll(listNow);
					onScanFinished();

					photoMgr.setSortType(sortType);
					photoMgr.setOrderType(orderType);
				} catch (Exception e) {
					FLog.e(TAG, "startScan throw error", e);
				}
			}
		});
		findSimilar.start();
		return false;
	}

	@Override
	public boolean stopScan() {
		mStopEngine = true;
		return false;
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
						ExportedImageItem item = (ExportedImageItem) itemObj;
						if (null == item) {
							continue;
						}
						if (isCancelDelete()) {
							setCancelDelete(false);
							break;
						}
						fileDeletedSize += item.getSize();
						String path = item.getPath();
						boolean bDel = FileUtils.deletePhoto(mContext, path);
						String str = String.format(
								"deletePhotos path = %s? bDel=" + bDel, path);
						FLog.i(TAG, str);
						if(!bDel){
							continue;
						}

						// 删除整个导出数据库表
						deleteWholeExportTable(item);
						
						// 删除单次导出数据表
						deleteCurExportTable(item);

						percent = (double) fileDeletedSize / fileTotalSize
								* 100;

						String eventid = Constants.UMENG.ARRANGE_PHOTO.DELETE_EXPORTED_FILE;
						StatisticsUtil.getInstance(mContext, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
						
						if (null != mPhotoListner) {
							// 通知界面
							mPhotoListner.onDeletePhotosProgress(
									fileDeletedSize, percent);
							List<ExportedImageItem> lstDel = new ArrayList<ExportedImageItem>();
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

			private void deleteCurExportTable(ExportedImageItem item) {
				try {
					CurrentExportedImageItem itemCur = new CurrentExportedImageItem();
					itemCur.setId(item.getId());
					itemCur.setSize(item.getSize());
					itemCur.setPath(item.getPath());
					DBMgr.getInstance(mContext).deleteRecord(itemCur);
				} catch (Exception e) {
					FLog.e(TAG, "deleteCurExportTable deleate current exported db throw error",
							e);
				}
			}
			private void deleteWholeExportTable(ExportedImageItem item) {
				try {
					ExportedImageItem itemCur = new ExportedImageItem();
					itemCur.setId(item.getId());
					itemCur.setSize(item.getSize());
					itemCur.setPath(item.getPath());
					DBMgr.getInstance(mContext).deleteRecord(ExportedImageItem.class, itemCur);
				} catch (Exception e) {
					FLog.e(TAG, "deleteWholeExportTable delete whole exported db throw error",
							e);
				}
			}
		});
		thread.start();
	}
}