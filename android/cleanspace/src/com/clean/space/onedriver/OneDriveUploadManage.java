package com.clean.space.onedriver;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import com.clean.space.Constants;
import com.clean.space.UserSetting;
import com.clean.space.db.DBMgr;
import com.clean.space.log.FLog;
import com.clean.space.notification.CleanSpaceNotificationManager;
import com.clean.space.photomgr.AllPhotoManager;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.protocol.CurrentExportedImageItem;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;
import com.cleanspace.lib.onedriverlib.OneDriveManager;
import com.cleanspace.lib.onedriverlib.OneDriveProgressListener;
import com.cleanspace.lib.onedriverlib.TransferController;
import com.microsoft.onedriveaccess.model.Folder;
import com.microsoft.onedriveaccess.model.Item;

public class OneDriveUploadManage {

	public static final String TAG = OneDriveUploadManage.class.getSimpleName();

	public static final String FOLDER_NAME = "PhotoShifter";
	public static final String DEVICE_NAME = android.os.Build.MODEL;

	// File.separator + + File.separator;

	private OneDriveManager oneDriveManager;
	private String rootFolderItemId = null;
	private List<String> rootFolderItems;
	private Context mContext;
	private boolean run = false;
	private Thread thread;
	private Handler handlerRefreshRequest = new Handler(Looper.myLooper());

	private OneDriveUploadManage() {
		oneDriveManager = DriverManagerFactroy.getInstance()
				.getOneDriveManager();
	}

	private static OneDriveUploadManage instance = null;

	public static OneDriveUploadManage getInstance() {
		if (instance == null) {
			synchronized (OneDriveUploadManage.class) {
				if (instance == null) {
					instance = new OneDriveUploadManage();
				}
			}
		}
		return instance;
	}

	public void setContext(Context mContext) {
		this.mContext = mContext;
	}

	public void start() {
		if (!run) {
			run = true;
			thread = new Thread(updateTask);
			thread.setName("OneDrive");
			thread.setPriority(Thread.NORM_PRIORITY);
			thread.start();
			handlerRefreshRequest.removeCallbacks(overTimeTask);
			handlerRefreshRequest.postDelayed(overTimeTask,
					Constants.INTERVAL_SEND_START_DOWNLOAD_UDP * 2 * 60 * 3);
		}
	}

	public void stop() {
		if (run) {
			run = false;
			transferControllerListener.cancel();
			instance = null;
		}
	}

	private Runnable updateTask = new Runnable() {

		@Override
		public void run() {
			if (rootFolderItemId == null) {
				checkRootFolder();
			}
			int complete = 0;
			if (run) {
				sendToUi(complete, Constants.PC_START_DOWNLOAD);
			}
			List<FileItem> list = getNeedUploadFileList();
			rootFolderItems = getRootFolderItemChildren();
			if (run && rootFolderItemId != null) {
				for (FileItem fileItem : list) {
					if (!run) {
						break;
					}
					writePath(fileItem.getPath());
					sendToUi(++complete,
							Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
					try {
						boolean ret = isExsitInCloud(fileItem.getFilename());
						if (!ret) {
							FLog.i(TAG, fileItem.getFilename()
									+ " is not exsit. to upload");
							updateFile(fileItem);
						} else {
							if (run) {
								FLog.i(TAG, fileItem.getFilename()
										+ ": has been exsit.");
								refreshCleanedSpace(fileItem.getSize(),
										fileItem.getSize(), fileItem.getSize());
								refreshHandlerNumber();
								addToExportedDb(mContext, fileItem.getPath());
								addToCurrentExportedDb(mContext,
										fileItem.getPath());
								sendToUi(
										fileItem.getSize(),
										Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
							}
						}
					} catch (Exception e) {
						try {
							updateFile(fileItem);
						} catch (Exception e1) {
							FLog.e(TAG, e1);
						}
					}

					FLog.i(TAG, "file size：" + fileItem.getSize());

					FLog.i(TAG, "complete:" + complete);
				}
				FLog.i(TAG, "Finish.");
				sendToUi(++complete, Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
				stop();
			}else{
				overTimeTask.run();
			}
		}
	};

	private boolean isExsitInCloud(String filename) {
		if (filename != null) {
			for (String name : rootFolderItems) {
				if (filename.equals(name)) {
					return true;
				}
			}
		}
		return false;
	}

	private Runnable overTimeTask = new Runnable() {

		@Override
		public void run() {
			stop();
			writeToSetting();
			sendToUi();
		}
	};

	private void updateFile(FileItem file) throws Exception {
		transferControllerListener = new TransferController(
				new OneDriveProgressListenerImple(file));
		transferControllerListener.setRepeatCount(3);
		oneDriveManager.uploadById(rootFolderItemId, file.getFilename(),
				new File(file.getPath()), transferControllerListener);

	}

	private TransferController transferControllerListener;

	class OneDriveProgressListenerImple implements OneDriveProgressListener {
		private long transfered = 0l;
		private boolean isCompleted = false;
		private FileItem fileItem;

		public OneDriveProgressListenerImple(FileItem fileItem) {
			this.fileItem = fileItem;
		}

		@Override
		public void onSuccess(Object object) {
			FLog.i(TAG, "success:" + (object == null ? "" : object.toString()));

			if (!isCompleted && run) {
				refreshHandlerNumber();
				addToExportedDb(mContext, fileItem.getPath());
				addToCurrentExportedDb(mContext, fileItem.getPath());
				sendToUi(1, Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
			}

		}

		@Override
		public void onFailure(Exception exception) {
			FLog.e(TAG, "exception:", exception);
		}

		@Override
		public void onProgress(long totalBytes, long bytesTransferred) {
			if (run) {
				refreshCleanedSpace(bytesTransferred - transfered, totalBytes,
						bytesTransferred);
				transfered = bytesTransferred;
				if (bytesTransferred >= totalBytes) {
					if(!isCompleted){
						isCompleted = true;
						refreshHandlerNumber();
						addToExportedDb(mContext, fileItem.getPath());
						addToCurrentExportedDb(mContext, fileItem.getPath());
						sendToUi(bytesTransferred,
								Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
					}
				}
				FLog.i(TAG, fileItem.getFilename() + "-totalBytes:" + totalBytes + ",bytesTransferred:"
						+ bytesTransferred);
				sendToUi(bytesTransferred,
						Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
			}

		}
	};

	private List<FileItem> getNeedUploadFileList() {
		int position = UserSetting.getInt(mContext, "position", 3);
		AllPhotoManager photoMgr = (AllPhotoManager) PhotoManagerFactory
				.getInstance(mContext, PhotoManagerFactory.PHOTO_MGR_ALL);
		List<FileItem> lstFile = new ArrayList<FileItem>();
		List<FileItem> temp = null;
		switch (position) {
		case 0:
			temp = photoMgr.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			break;
		case 1:
			temp = photoMgr.get1mounth(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			break;
		case 2:
			temp = photoMgr.get3mounth(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			break;
		case 3:
			temp = photoMgr.get6mounth(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			break;
		case 4:
			temp = photoMgr.get12mounth(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			break;
		default:
			temp = photoMgr.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_DESC, 0);
			break;

		}

		for (FileItem fileItem : temp) {
			lstFile.add(fileItem.clone());
		}
		return lstFile;
	}

	private void writeToSetting() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(mContext);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			if (pkg.getCleanStatus() == CleanFileStatusPkg.CLEAN_ING) {
				pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_INTERRUPT);
			}
			boolean writeRet = UserSetting.setClearStatusInfo(mContext,
					pkg.toJson());

		} catch (NumberFormatException e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		}
	}

	private void sendToUi() {
		try {
			Intent intent = new Intent();
			intent.setAction(Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
			mContext.sendBroadcast(intent);
		} catch (Exception e) {
			FLog.e(TAG, "sendToUi throw error", e);
		}
	}

	public void sendToUi(long fileSize, String action) {
		try {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putLong("fileSize", fileSize);
			intent.putExtras(bundle);
			intent.setAction(action);
			mContext.sendBroadcast(intent);
			handlerRefreshRequest.removeCallbacks(overTimeTask);
			handlerRefreshRequest.postDelayed(overTimeTask,
					Constants.INTERVAL_SEND_START_DOWNLOAD_UDP * 2 * 60 * 3);
		} catch (Exception e) {
			FLog.e(TAG, "sendToUi throw error", e);
		}
	}

	private void checkRootFolder() {
		int step = 0;
		try {
			rootFolderItemId = getItemId(FOLDER_NAME);
			step = 1;
			if (rootFolderItemId == null) {
				rootFolderItemId = createFolderRecursion("" + File.separator,
						FOLDER_NAME);
			}
			rootFolderItemId = null;
			rootFolderItemId = getItemId(FOLDER_NAME + File.separator
					+ DEVICE_NAME);
			if (rootFolderItemId == null) {
				rootFolderItemId = createFolderRecursion(FOLDER_NAME,
						DEVICE_NAME);
			}
			step = 2;
		} catch (Exception e) {
			try {
				if (step == 0) {
					rootFolderItemId = createFolderRecursion(""
							+ File.separator, FOLDER_NAME);
					step = 1;
				}
				if (step == 1) {
					rootFolderItemId = createFolderRecursion(FOLDER_NAME,
							DEVICE_NAME);
					step = 2;
				}
			} catch (Exception e1) {
				FLog.e(TAG, e1);
			}
		}finally{
			FLog.i(TAG, "folder id:" + rootFolderItemId);
		}

	}

	private void printFolder(Object item) throws Exception {
		if (item != null && item instanceof Item) {
			Item i = (Item) item;
			if (i.File != null) {
				FLog.i(TAG, "filename:" + i.Name);
				FLog.i(TAG, "Description:" + i.Description);
			}
			if (i.Folder != null) {
				FLog.i(TAG, "Folder name:" + i.Name);
				FLog.i(TAG, "Description:" + i.Description);
				Object itemChildren = oneDriveManager.getItemChildren(i.Id);
				printFolder(itemChildren);
			}
		}
	}

	private boolean addToExportedDb(Context context, String path) {
		try {
			ExportedImageItem item = new ExportedImageItem();
			File file = new File(path);
			item.setDate(file.lastModified());
			item.setPath(path);
			item.setSize(file.length());
			item.setSaveOneDrive();
			DBMgr.getInstance(context).addTableUniqueSaveOrUpdate(
					ExportedImageItem.class, item);
			// FLog.i(TAG, "addToDb  time " + item.getDate());
			IPhotoManager photoMgr = PhotoManagerFactory.getInstance(context,
					PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
			photoMgr.setClearMemoryCache();
		} catch (Exception e) {
			FLog.e(TAG, "addToDb throw error", e);
		}
		return true;
	}

	private boolean addToCurrentExportedDb(Context context, String path) {
		try {
			CurrentExportedImageItem item = new CurrentExportedImageItem();
			File file = new File(path);
			item.setDate(file.lastModified());
			item.setPath(path);
			item.setSize(file.length());
			item.setSaveOneDrive();
			DBMgr.getInstance(context).addTableUniqueSaveOrUpdate(
					CurrentExportedImageItem.class, item);
		} catch (Exception e) {
			FLog.e(TAG, "addToDb throw error", e);
		}
		return true;
	}

	private String getItemId(String path) throws Exception {
		String id = null ;
		Object result = oneDriveManager.getItemByPath(path, null);
		if (result != null && result instanceof Item) {
			id =  ((Item) result).Id;
		}
		FLog.i(TAG, path + " id is :" + id);
		return id;
	}

	private String createFolderRecursion(String path, String folderName)
			throws Exception {
		final Item newItem = new Item();
		newItem.Name = folderName;
		newItem.Folder = new Folder();

		Object result = oneDriveManager.createFolderRecursion(path, newItem);
		if (result instanceof Item) {
			return ((Item) result).Id;
		}
		return null;
	}

	public void refreshCleanedSpace(long fileSize, long totalSize,
			long currrentSize) {
		try {
			String clearStatus = UserSetting.getClearStausInfo(mContext);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_ING);

			String strClearSpace = pkg.getCleanedSpace();
			long clearedSpace = 0;
			if (null != strClearSpace) {
				clearedSpace = Long.parseLong(strClearSpace);
			}
			clearedSpace += fileSize;
			pkg.setClearedSpace(String.valueOf(clearedSpace));
			pkg.setCurrentFileSize("" + totalSize);
			pkg.setCurrentTransferSize("" + currrentSize);
			if (pkg.getStart() <= 0) {
				pkg.setStart(System.currentTimeMillis());
			}
			pkg.setEnd(System.currentTimeMillis());

			boolean writeRet = UserSetting.setClearStatusInfo(mContext,
					pkg.toJson());

			// FLog.i(TAG, "writeToSetting download complete filenumber "
			// + number);
		} catch (NumberFormatException e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		}
	}

	public void refreshHandlerNumber() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(mContext);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			String strHandleNumber = pkg.getHandlePicNumber();
			pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_ING);
			long number = 1;
			if (null != strHandleNumber) {
				number = Long.parseLong(strHandleNumber) + 1;
			}
			pkg.setHandlePicNumber(String.valueOf(number));
			sendProcess(pkg);
			boolean writeRet = UserSetting.setClearStatusInfo(mContext,
					pkg.toJson());
			handlerRefreshRequest.removeCallbacks(overTimeTask);
			handlerRefreshRequest.postDelayed(overTimeTask,
					Constants.INTERVAL_SEND_START_DOWNLOAD_UDP * 2 * 60 * 3);
		} catch (NumberFormatException e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		}
	}

	private List<String> getRootFolderItemChildren() {
		List<String> fileNameList = new ArrayList<String>();
		try {
			Object itemChildren = oneDriveManager
					.getItemChildren(rootFolderItemId);
			if (itemChildren != null) {
				Item item = (Item) itemChildren;
				List<Item> list = item.Value;
				
				for (Item i : list) {
					if (i.File != null) {
						FLog.i(TAG, "onedrive has this file:" + i.Name);
						fileNameList.add(i.Name);
					}
				}
			}
			

		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return fileNameList;
	}

	public void sendProcess(CleanFileStatusPkg pkg) {
		String strHandleNumber = pkg.getHandlePicNumber();
		String handlePicTotal = pkg.getHandlePicTotal();
		long number = 1;
		if (null != strHandleNumber) {
			number = Long.parseLong(strHandleNumber) + 1;
		}
		long total = 1;
		if (null != handlePicTotal) {
			total = Long.parseLong(handlePicTotal);
		}
		if (total <= number) {
			CleanSpaceNotificationManager.getInstance()
					.sendExportProcessFinshNotification(mContext);
		} else {
			CleanSpaceNotificationManager.getInstance()
					.sendExportProcessNotification(
							mContext,
							String.format("%1$.2f", (100.0 * number) / total)
									+ "%");
		}
	}

	/**
	 * 实时将路径保存到sp.用于export界面读取
	 * 
	 * @param path
	 *            当前处理照片的路径
	 */
	private void writePath(String path) {
		String path1 = path.substring(path.lastIndexOf("/") + 1);
		UserSetting.setString(mContext, Constants.PATH, path1);
	}
}
