package com.clean.space.photomgr;

import java.util.ArrayList;
import java.util.List;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.content.Context;

import com.clean.space.Constants;
import com.clean.space.db.DBMgr;
import com.clean.space.image.ImageCompareFactory;
import com.clean.space.log.FLog;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;
import com.clean.space.protocol.SimilarImageItem;
import com.clean.space.scanfile.ScanImage;
import com.clean.space.scanfile.ScanImage.ScanImageListener;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.FileUtils;

public class SimilarPhotoManager extends BasePhotoManager implements
		ScanImageListener {

	private final String TAG = "SimilarPhotoManager";
	Context mContext = null;
	private boolean mStopEngine = false;
	private long startFindSimilarTime = 0;
	private List<SimilarImageItem> mListImage = new ArrayList<SimilarImageItem>();

	private long staticsStartfindSimilarImageTime = 0;
	private long staticsfindImagsNumber = 0;
	private long staticsTotalSimilarImagsNumber = 0;
	public SimilarPhotoManager(Context context) {
		super(context);
		mContext = context;
	}

	// 获取已扫描到的文件尺寸
	@Override
	public long getScannedPhotoSize() {
		if (null == mListImage || mListImage.isEmpty()) {
			return 0;
		}
		long fileTotalSize = super.getFileSize(mListImage);
		return fileTotalSize;
	}

	// 开始扫描
	@Override
	public boolean startScan(final String sortType, final String orderType,
			final int photoCount) {
		FLog.i(TAG, "startScan start");
		final String sortTypeTemp = IPhotoManager.SORT_TYPE_TIME/*sortType*/;
		final String orderTypeTemp = IPhotoManager.ORDER_BY_ASC/*orderType*/;
		start();

		Thread findSimilar = new Thread(new Runnable() {
			public void run() {
				try {
				//	mListImage.clear();
					// 获取数据库保存的相似查找相片
					mListImage = (List<SimilarImageItem>) DBMgr.getInstance(
							mContext).getLaterData(SimilarImageItem.class,0);
					List<SimilarImageItem> lstSimilar = new ArrayList<SimilarImageItem>();
					if (null != mListImage && !mListImage.isEmpty()) {

						long type = mListImage.get(0).getType();
						for (SimilarImageItem item : mListImage) {
							// 前一张相片和后一张相片类型不一致
							if (type != item.getType()) {
								if (lstSimilar.size() > 1) {
									postSimilarList(lstSimilar);
								}
								lstSimilar.clear();
							}
							type = item.getType();
							// 文件必须存在
							if (FileUtils.isFileExist(item.getPath())) {
								lstSimilar.add(item);
							}
						}
						// 处理最好一组相似照片
						if (lstSimilar.size() > 1) {
							postSimilarList(lstSimilar);
						}
						
						// 因为寻找相似相片是升序,所以获取当前数据库的最大时间
						SimilarImageItem item = (SimilarImageItem) DBMgr
								.getInstance(mContext).getMaxObject(
										IPhotoManager.SORT_TYPE_TIME,
										SimilarImageItem.class);

						// 跳过已经找到的最后一张相似照片
						startFindSimilarTime = item.getDate() + 1;	
						staticsTotalSimilarImagsNumber = mListImage.size();
					}

					// 扫描本地未曾比较的相片
					ScanImage scan = new ScanImage();
					scan.setScanImageReceiver((SimilarPhotoManager) PhotoManagerFactory
							.getInstance(mContext,
									PhotoManagerFactory.PHOTO_MGR_SIMILAR));
					scan.setAscOrder(false);
					scan.setScanViedo(false);
					// 目前查找相似相片机制是,按时间升序查找图片,查找到时间相近一组后进行分析相似相片
					scan.scanFilesAsyc(mContext, sortTypeTemp, orderTypeTemp,
							startFindSimilarTime);

				} catch (Exception e) {
					FLog.e(TAG, "findSimilarImage throw error", e);
				}
			}
		});
		findSimilar.start();
		return true;
	}

	@Override
	public boolean stopScan() {
		stop();
		return false;
	}

	public long getStartFindSimilarTime() {
		return startFindSimilarTime;
	}

	public void setStartFindSimilarTime(long startFindSimilarTime) {
		this.startFindSimilarTime = startFindSimilarTime;
	}

	public void start() {
		init();
		FLog.i(TAG, "start.");
	}

	public void stop() {
		mStopEngine = true;
	}

	private void filterSimilarImage(List<FileItem> lstFile) {
		if (null == lstFile || lstFile.isEmpty()) {
			return;
		}
		try {
			final int size = lstFile.size();
			switch (size) {
			// 单列表为0,无需比较
			case 0:
				break;
			// 当列表为1,无需比较
			case 1:
				break;
			default: {
				// 计算直方图
				List<SimilarImageItem> listImageSub = calcHist(lstFile);

				// 循环查找图片
				listImageSub = findSimilarImage(listImageSub);
				break;
			}
			}
		} catch (Exception e) {
			FLog.e(TAG, "filterSimilarImage throw error", e);
		}
	}

	private List<SimilarImageItem> calcHist(List<FileItem> listFile) {
		List<SimilarImageItem> listImage = new ArrayList<SimilarImageItem>();
		try {
			for (FileItem item : listFile) {
				SimilarImageItem imageItem = new SimilarImageItem();
				imageItem.copy(item);

				// 计算直方图
				ImageCompareFactory.getCompareEngine(mContext,
						ImageCompareFactory.IMAGE_COMPARE_TYPE_HIST).calcHist(
						imageItem);

				listImage.add(imageItem);
			}
		} catch (Exception e) {
			FLog.e(TAG, "calcHist throw error", e);
		}
		return listImage;
	}

	private List<SimilarImageItem> findSimilarImage(
			List<SimilarImageItem> listImage) {
		if (null == listImage || listImage.isEmpty()) {
			return null;
		}
		List<SimilarImageItem> lstDes = new ArrayList<SimilarImageItem>(
				listImage);

		// 　获取当前时间作为相似照片的标记
		long type = System.currentTimeMillis();
		try {
			List<SimilarImageItem> lstSimilar = new ArrayList<SimilarImageItem>();
			for (SimilarImageItem item : listImage) {
				boolean notifyObserver = false;
				// 已经在其他图片的相似图片列表
				if (item.isInOtherImageSimilarList()) {
					continue;
				}
				// 已经存在相似相片据表就无需再查找
				if(mListImage.contains(item)){
					continue;
				}
				for (SimilarImageItem itemRight : listImage) {
					// 同一张图片
					if (item.equals(itemRight)) {
						continue;
					}
					if (mStopEngine) {
						return lstDes;
					}
					// 已经在其他图片的相似图片列表
					if (itemRight.isInOtherImageSimilarList()) {
						continue;
					}
					// 比较直方图
					boolean bSimilar = ImageCompareFactory.getCompareEngine(
							mContext,
							ImageCompareFactory.IMAGE_COMPARE_TYPE_HIST)
							.compareImage(item, itemRight);

					// 如果相似,则加入列表
					if (bSimilar) {
						notifyObserver = bSimilar;
						item.setType(type);
						itemRight.setType(type);
						item.setInOtherImageSimilarList(true);
						itemRight.setInOtherImageSimilarList(true);
						lstSimilar.add(itemRight);
						lstDes.remove(itemRight);
					}
				}
				// 找完这张相片对应的相似照片,如果有相似照片,则通知UI
				if (notifyObserver) {
					// 添加本身,否则UI需要自己添加
					lstSimilar.add(item);
					type += 1;
					if (postSimilarList(lstSimilar) && !DBMgr.getInstance(mContext)
							.queryRecord(SimilarImageItem.class,item)) {
						// 插入数据库
						DBMgr.getInstance(mContext).addTables(lstSimilar);
					}
					staticsTotalSimilarImagsNumber += lstSimilar.size();
					lstSimilar.clear();
					FLog.i(TAG, "findSimilarImage notify observer");
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "findSimilarImage throw error", e);
		}
		return lstDes;
	}

	private void init() {
		staticsStartfindSimilarImageTime = System.currentTimeMillis();
		mStopEngine = false;
		if (!OpenCVLoader.initDebug()) {
			FLog.d(TAG,
					"Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, mContext,
					mLoaderCallback);
		} else {
			FLog.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(
			mContext) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				FLog.i(TAG, "OpenCV loaded successfully");
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	// 扫描图片过程中回调该函数
	@Override
	public void generateItems(boolean scanFinish, List<FileItem> items) {
		try {
			do {
				List<FileItem> lstFile = new ArrayList<FileItem>(items);
				if (null == lstFile || lstFile.isEmpty()) {
					break;
				}
				staticsfindImagsNumber += items.size();
				filterSimilarImage(lstFile);				

			} while (false);
			
			// 扫描完成通知界面
			if(scanFinish){
				super.onScanFinished();
				statics();
			}
		} catch (Exception e) {
			FLog.e(TAG, "generateImages throw error", e);
		}
	}

	private void statics() {
		String eventid = Constants.UMENG.ARRANGE_PHOTO.FIND_SIMILAR_PHOTO_TIMES;
		int du = (int)(System.currentTimeMillis() - staticsStartfindSimilarImageTime);
		StatisticsUtil.getInstance(mContext, StatisticsUtil.TYPE_UMENG).onEventValueCalculate(eventid, null, du);

		// 5.9.查找10张相似照片耗费的时间(因为umeng的计算事件参数必须是int型)
		if (staticsfindImagsNumber > 0) {
			String eventidEveryTen = Constants.UMENG.ARRANGE_PHOTO.FIND_SIMILAR_EVERY_TEN_PHOTO_TIMES;
			double wasteTimeEveryTen = (10.0 * (System.currentTimeMillis() - staticsStartfindSimilarImageTime))
					/ staticsfindImagsNumber;
			StatisticsUtil.getInstance(mContext, StatisticsUtil.TYPE_UMENG)
					.onEventValueCalculate(eventidEveryTen, null,
							(int) wasteTimeEveryTen);
		}
		
		AllPhotoManager photoMgr = (AllPhotoManager) PhotoManagerFactory
				.getInstance(mContext,
						PhotoManagerFactory.PHOTO_MGR_ALL);

		// 5.6.获取相似照片个数占相册照片的比例.
		if(photoMgr.getFileNumber() > 0){
			String eventidPercent = Constants.UMENG.ARRANGE_PHOTO.FIND_SIMILAR_PHOTO_PERCENT;
			double percent = (100.0 * staticsTotalSimilarImagsNumber) / photoMgr.getFileNumber();
			StatisticsUtil.getInstance(mContext, StatisticsUtil.TYPE_UMENG).onEventValueCalculate(eventidPercent, null, (int)percent);
		}
	}

	@Override
	public boolean deletePhotos(final List<?> photos) {

		deletePhotoList(photos);

		// 清除緩存
		IPhotoManager photoMgr = PhotoManagerFactory.getInstance(
				mContext, PhotoManagerFactory.PHOTO_MGR_ALL);
		photoMgr.setClearMemoryCache();
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
						SimilarImageItem item = (SimilarImageItem) itemObj;
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

						// 删除数据库
						try {
							DBMgr.getInstance(mContext).deleteRecord(item);
						} catch (Exception e) {
							FLog.e(TAG, "deletePhotos deleate db throw error",
									e);
						}

						percent = (double) fileDeletedSize / fileTotalSize
								* 100;

						if (null != mPhotoListner) {
							// 通知界面
							mPhotoListner.onDeletePhotosProgress(
									fileDeletedSize, percent);
							List<SimilarImageItem> lstDel = new ArrayList<SimilarImageItem>();
							lstDel.add(item);
							mPhotoListner.onDeletePhoto(lstDel);
						}						
					} catch (Exception e) {
						FLog.e(TAG, "deletePhotos throw error", e);
					}
					if (null != mPhotoListner) {
						// 删除相片完成,通知界面
						mPhotoListner.onDeleteFinished();
					}	
				}
			}
		});
		thread.start();
	}

	// 只查找數據庫
	private List<SimilarImageItem> getUnexportList(List<SimilarImageItem> listNow) {

		List<SimilarImageItem> listNowTemp = new ArrayList<SimilarImageItem>(listNow);
		List<SimilarImageItem> listDest = new ArrayList<SimilarImageItem>();
		for (SimilarImageItem item : listNowTemp) {
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

	public synchronized boolean postSimilarList(List<SimilarImageItem> lstFile) {
		boolean ret = false;
		try {
			// 为了速度,暂时不判断文件是否存在,因为这种情况概率很小
			// lstFile = filterDeletedFile(lstFile);

			lstFile = getUnexportList(lstFile);
			if (null != lstFile && lstFile.size() > 1) {
				ret = super.onNotifyUI(lstFile);
			}
		} catch (Exception e) {
		}
		return ret;
	}
}
