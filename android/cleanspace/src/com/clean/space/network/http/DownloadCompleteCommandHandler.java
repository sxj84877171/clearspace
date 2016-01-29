package com.clean.space.network.http;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.clean.space.Constants;
import com.clean.space.UserSetting;
import com.clean.space.db.DBMgr;
import com.clean.space.log.FLog;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.protocol.CurrentExportedImageItem;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.FileUtils;

public class DownloadCompleteCommandHandler extends DownloadCommandHandlerBase implements HttpRequestHandler {

	private Context context = null;
	private String host = "localhost";
	private static final String TAG = "DownloadCompleteCommandHandler";
	private final int HTTP_ERROR_CODE_SUCCEED = 200;
	private static int numberTotal = 0;
	private static final Object mMutex = new Object();

	public DownloadCompleteCommandHandler(Context ctx, Boolean bFromWeb) {
		super(ctx, bFromWeb);
		this.context = ctx;
	}

	public void handle(HttpRequest req, HttpResponse resp, HttpContext arg2)
			throws HttpException, IOException {

		String uriString = "";
		try {

//			FLog.i(TAG, "writeToSetting download complete numberTotal "
//					+ ++numberTotal);

			try {
				String uri = req.getRequestLine().getUri();
				if (uri.length() >= Constants.DOWNLOAD_COMPLETE_PATTERN
						.length()) {
					uriString = uri
							.substring(Constants.DOWNLOAD_COMPLETE_PATTERN
									.length() -1 );
				}
			} catch (Exception e) {
				FLog.e(TAG, "analyze url throw error" + uriString, e);
			}
			handleRequest(uriString);
			HttpEntity entity = generateReplyMsg(resp);
			if (null != entity) {
				resp.setEntity(entity);
			}
		} catch (Exception e) {
			FLog.e(TAG, "handle throw error" + uriString, e);
		}

	}

	private boolean handleRequest(String pathSrc) {
		try {
			// send to ui and save to perference
			long fileSize = 1;
			String path = "";
			try {
				path = Uri.decode(pathSrc);
				boolean isChecked = UserSetting.getBoolean(context, Constants.CHECK_EXPORT_AND_CLEAN, true);
				fileSize = FileUtils.getFileSize(path);
				if (FileUtils.isFileExist(path) && isChecked) {
//					FileUtils.deleteFile(path);
					FLog.i(TAG, "handleRequest deleted file succeed" + path);
				} else {
					FLog.i(TAG,
							"handleRequest deleted file failed because can't find this file or user select undelete file"
									+ path + "isChecked =" + isChecked);
				}
				
				writePath(path);
				
				// 加入全部导出图片数据库表
				addToExportedDb(path);
				
				// 加入当前导出数据库表(为了避免在同一个表里面进行读写操作导致的多线程问题).
				addToCurrentExportedDb(path);

				String eventid = Constants.UMENG.NETWORK_TRANSFER.TRANSFER_FILE_FINISHED;
				StatisticsUtil.getInstance(context, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
			} catch (Exception e) {
				FLog.i(TAG, "handleRequest handle file failed" + path);
			}
			// 刷新处理个数
			super.refreshHandlerNumber();
			
			// 用户PC已存在该文件,则没有进入实际下载文件线程,把该文件的大小计入统计
			if(!Server.mRealTransferfileList.contains(pathSrc)){
				refreshCleanedSpace(fileSize,fileSize,fileSize);	
			}
			super.sendToUi(fileSize, Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
		} catch (Exception e) {
			FLog.e(TAG, "handleRequest throw error", e);
		}
		return false;
	}

	/**
	 * 实时将路径保存到sp.用于export界面读取
	 * @param path 当前处理照片的路径
	 */
	private void writePath(String path) {
		String path1 = path.substring(path.lastIndexOf("/")+1);
		UserSetting.setString(context, Constants.PATH, path1);
	}

	private boolean addToExportedDb(String path){
		try {
			ExportedImageItem item = new ExportedImageItem();
			File file = new File(path);
			item.setDate(file.lastModified());
			item.setPath(path);
			item.setSize(file.length());
			item.setSavePcClient();
			DBMgr.getInstance(context).addTableUniqueSaveOrUpdate(ExportedImageItem.class, item);
//			FLog.i(TAG, "addToDb  time " + item.getDate());
			IPhotoManager photoMgr = PhotoManagerFactory.getInstance(context, PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
			photoMgr.setClearMemoryCache();
		} catch (Exception e) {
			FLog.e(TAG, "addToDb throw error", e);
		}
		return true;
	}
	private boolean addToCurrentExportedDb(String path){
		try {
			CurrentExportedImageItem item = new CurrentExportedImageItem();
			File file = new File(path);
			item.setDate(file.lastModified());
			item.setPath(path);
			item.setSize(file.length());
			item.setSavePcClient();
			DBMgr.getInstance(context).addTableUniqueSaveOrUpdate(CurrentExportedImageItem.class, item);
		} catch (Exception e) {
			FLog.e(TAG, "addToDb throw error", e);
		}
		return true;
	}
	private HttpEntity generateReplyMsg(HttpResponse response) {
		HttpEntity entity = null;
		try {
			new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream)
						throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(
							outstream, "GB2312");
					String resp = "Upload data succeed";

					writer.write(resp);
					writer.flush();
				}
			});
			response.setStatusCode(HTTP_ERROR_CODE_SUCCEED);
			response.setHeader("Content-Type", "text/html");
		} catch (Exception e) {
			FLog.e(TAG, "generateReplyMsg throw error", e);
		}
		return entity;
	}
}
