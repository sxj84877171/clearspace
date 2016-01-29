package com.clean.space.photomgr;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.clean.space.log.FLog;
import com.clean.space.protocol.FileItem;
import com.clean.space.scanfile.ScanImage;
import com.clean.space.scanfile.ScanImage.ScanImageListener;

public class AllPhotoManager extends BasePhotoManager implements ScanImageListener {

	private final String TAG = "AllPhotoManager";
	Context mContext;
	private boolean mStopEngine = false;

	ScanImage mScanEngine = new ScanImage();
	private List<FileItem> mListImage = new ArrayList<FileItem>();

	public AllPhotoManager(Context context) {
		super(context);
		mContext = context;
	}

	@Override
    public long getScannedPhotoSize() {
		if (null != mListImage && mListImage.isEmpty()) {
			getPhotosSync(IPhotoManager.SORT_TYPE_SIZE,
					IPhotoManager.ORDER_BY_ASC, 0);
		}
		return getFileSize(mListImage);
	}

	// 同步掃描文件
	public List<FileItem> getPhotosSync(final String sortType,
			final String orderType, int photoCount) {
		initEnv();
		try {
			if (null != mScanEngine) {
				mScanEngine.setPhotoCount(photoCount);
				mListImage = mScanEngine.scanFilesSync(mContext, sortType, orderType);
			}
			super.setSortType(sortType);
			super.setOrderType(orderType);
		} catch (Exception e) {
			FLog.e(TAG, "getPhotosSync throw error", e);
		}
		return mListImage;
	}

	// TODO
	public List<FileItem> get1mounth(final String sortType,
			final String orderType, int photoCount) {
		return getSpecialTimeList(sortType, orderType, photoCount, 30);
	}

	public long get1MounthSize() {
		List<FileItem> lstFile = getSpecialTimeList(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_DESC, 0, 30);
		return getListFileSize(lstFile);
	}

	public List<FileItem> get3mounth(final String sortType,
			final String orderType, int photoCount) {
		return getSpecialTimeList(sortType, orderType, photoCount, 90);
	}

	public long get3MounthSize() {
		List<FileItem> lstFile = getSpecialTimeList(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_DESC, 0, 90);
		return getListFileSize(lstFile);
	}

	public List<FileItem> get6mounth(final String sortType,
			final String orderType, int photoCount) {
		return getSpecialTimeList(sortType, orderType, photoCount, 180);
	}

	public long get6MounthSize() {
		List<FileItem> lstFile = getSpecialTimeList(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_DESC, 0, 180);
		return getListFileSize(lstFile);
	}

	public List<FileItem> get12mounth(final String sortType,
			final String orderType, int photoCount) {
		return getSpecialTimeList(sortType, orderType, photoCount, 365);
	}

	public long get12MounthSize() {
		List<FileItem> lstFile = getSpecialTimeList(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_DESC, 0, 365);
		return getListFileSize(lstFile);
	}

	public List<FileItem> getOthermounth(final String sortType,
			final String orderType, int photoCount) {
		return mListImage;
	}

	public long getOtherMounthSize() {
		return getListFileSize(mListImage);
	}

	public List<FileItem> getSpecialTimeList(final String sortType,
			final String orderType, int photoCount, int day) {
		List<FileItem> listDest = new ArrayList<FileItem>();
		try {
			List<FileItem> lstFile = new ArrayList<FileItem>(mListImage);
			if (lstFile.isEmpty()) {
				getPhotosSync(sortType, orderType, 0);
			}
			for (FileItem item : lstFile) {
				if (null != item) {
					long oldTime = timeBeforeDay(day);
					if (item.getDate() < oldTime) {
						listDest.add(item);
					}
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "getOldItem throw error", e);
		}
		return listDest;
	}

	private long timeBeforeDay(int day) {
		long time = System.currentTimeMillis();
		long dateSeconds = 24 * 60 * 60 * 1000;
		long subTime = time - day * dateSeconds;
		return subTime;
	}

	private long getListFileSize(List<FileItem> lstFile) {
		long fileSize = 0;
		for (FileItem item : lstFile) {
			if (null != item) {
				fileSize += item.getSize();
			}
		}
		return fileSize;
	}

	// ///////////////////////////////
	public long getFileSize() {
		// 扫描
		return getFileSize(mListImage);
	}

	public long getFileNumber() {
		return mListImage.size();
	}

	// 異步掃描文件
	@Override
	public boolean startScan(final String sortType, final String orderType,
			int photoCount) {
		FLog.i(TAG, "startScan start");
	
		if (null == mListImage || null == mScanEngine || null == mContext) {
			return false;
		}
		if (isClearMemoryCache()) {
			mListImage.clear();
		}

		if (sortType.equalsIgnoreCase(super.getSortType())
				&& orderType.equalsIgnoreCase(super.getOrderType())) {
			onNotifyUI(mListImage);
		} else {
			mListImage.clear();
		}

		// 获取新增的相片
		mScanEngine.setPhotoCount(photoCount);
		initEnv();
		Thread findSimilar = new Thread(new Runnable() {
			public synchronized void run() {
				try {
					AllPhotoManager phooMgr = (AllPhotoManager) PhotoManagerFactory
							.getInstance(mContext,
									PhotoManagerFactory.PHOTO_MGR_ALL);
					mScanEngine.setScanImageReceiver(phooMgr);
					mScanEngine.setAscOrder(false);
					mScanEngine.setScanViedo(false);
					List<FileItem> lstNow = mScanEngine.scanFilesSync(mContext, sortType, orderType);

					// 过滤掉上次已经导出的文件,只把最新的数据通知界面
					List<FileItem> lstNowAdd = phooMgr.filterList(lstNow,
							mListImage);

					// 把最新文件通知界面
					if(null != lstNowAdd && !lstNowAdd.isEmpty()){
						onNotifyUI(lstNowAdd);
						mListImage.addAll(lstNowAdd);
					}
					onScanFinished();
				} catch (Exception e) {
					FLog.e(TAG, "startScan throw error", e);
				}
			}
		});
		findSimilar.start();
		super.setSortType(sortType);
		super.setOrderType(orderType);
		return true;
	}

	@Override
	public boolean stopScan() {
		mStopEngine = true;
		mScanEngine.setStopScan(true);
		return false;
	}

	@Override
	public void generateItems(boolean scanFinish, List<FileItem> items) {
		if (null == mListImage || null == items || items.isEmpty()) {
			return;
		}
		mListImage.addAll(items);
		onNotifyUI(items);
	}

	private void initEnv() {
		mStopEngine = false;
	}
}
