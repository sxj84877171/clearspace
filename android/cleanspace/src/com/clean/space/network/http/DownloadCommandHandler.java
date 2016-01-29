package com.clean.space.network.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.FileEntity;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.clean.space.Constants;
import com.clean.space.R;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.statistics.StatisticsUtil;

public class DownloadCommandHandler extends DownloadCommandHandlerBase implements HttpRequestHandler {

	private Context context = null;
	private String host = "localhost";
	private String FOLDER_SHARE_PATH = Constants.FOLDER_SHARE_PATH;
	private static final String TAG = "DownloadCommandHandler";
	private long curtime = 0l;
	private int HTTP_ERROR_CODE_UNACCEPTABLE = 406;
	private int HTTP_CONTINUE_DOWNLOAD = 206;
	private int HTTP_ERROR_CODE_READ_FILE_FAILED = 403;
	private int HTTP_ERROR_CODE_IS_DIRECTORY = 408;
	private int mFileOffset = 0;

	public DownloadCommandHandler(Context ctx, Boolean bFromWeb) {
		super(ctx, bFromWeb);
		this.context = ctx;
	}

	public void handle(HttpRequest req, HttpResponse resp, HttpContext arg2)
			throws HttpException, IOException {

		String uriString = "";
		try {
			analyHttpRequest(req);
			uriString = req.getRequestLine().getUri()
					.substring(Constants.DOWNLOAD_PATTERN.length());
			HttpEntity entity = getEntityFromUri(uriString, resp);
			if (null != entity) {
				resp.setEntity(entity);
			}
		} catch (Exception e) {
			FLog.e(TAG, "handle throw error" + uriString, e);
		}

	}

	private HttpEntity getEntityFromUri(String uri, final HttpResponse response) {

		HttpEntity entity = null;
		try {
			String contentType = "text/html";
			String filepath = FOLDER_SHARE_PATH;

			if (uri.equalsIgnoreCase("/") || uri.length() <= 0) {
				filepath = FOLDER_SHARE_PATH + "/";
			} else {
				filepath = FOLDER_SHARE_PATH  + URLDecoder.decode(uri);
			}
			FLog.i(TAG, "filepath : " + filepath);

			final File file = new File(filepath);

			if (file.isDirectory()) {
				entity = super.generateErrorMsg(response,
						HTTP_ERROR_CODE_IS_DIRECTORY);
			} else if (file.exists()) {
				
				writePath(filepath);
				
				contentType = URLConnection.guessContentTypeFromName(file
						.getAbsolutePath());
				
				entity = new ContinueTransferFileEntity(file, response, contentType);

				// 把需要下载的文件加入实际下载文件列表
				if(!Server.mRealTransferfileList.contains(filepath)){
					Server.mRealTransferfileList.add(filepath);
					String eventid = Constants.UMENG.NETWORK_TRANSFER.TRANSFER_FILE_FROM_ZERO;
					StatisticsUtil.getInstance(context, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
				}
				super.sendToUi(0, Constants.PC_START_DOWNLOAD);
				if (mFileOffset > 0) {
					response.setStatusCode(HTTP_CONTINUE_DOWNLOAD);
					String eventid = Constants.UMENG.NETWORK_TRANSFER.USED_TRANSFER_CONTINUE_DOWNLOAD;
					StatisticsUtil.getInstance(context, StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
				}
				response.setHeader("Content-Type", contentType);
			} else {
				entity = super.generateErrorMsg(response,
						HTTP_ERROR_CODE_IS_DIRECTORY);
			}
		} catch (Exception e) {
			FLog.e(TAG, "getEntityFromUri throw error", e);
		}

		return entity;
	}

	/**
	 * 实时将路径写入sp,供export界面读取
	 * @param filepath 当前正导出的文件路径
	 */
	private void writePath(String filepath) {
		String path = filepath.substring(filepath.lastIndexOf("/") + 1);
		if (!"filelist.json".equals(path)) {
			UserSetting.setString(context, Constants.PATH, path);
		}
	}

	// 支持断点续传功能
	private class ContinueTransferFileEntity extends FileEntity{

		HttpResponse mResponse = null;
		public ContinueTransferFileEntity(File file, HttpResponse response, String contentType) {
			super(file, contentType);
			mResponse = response;
		}

		@Override
		public void writeTo(OutputStream outstream)
				throws IOException {
			if (outstream == null) {
				throw new IllegalArgumentException(
						"Output stream may not be null");
			}
			InputStream instream = new FileInputStream(this.file);
			try {
				byte[] tmp = new byte[1024*10];
				int actuallyTransferSize = 0;
				int currentSize = 0; 
				
				if (mFileOffset > 0) {
					try {
						// 支持断点续传,根据http协议跳到指定文件流
						long actuallySkip = instream.skip(mFileOffset);
					} catch (Exception e) {
						FLog.e(TAG, "instream.skip throw error", e);
					}
				}
				while ((actuallyTransferSize = instream.read(tmp)) != -1) {
					outstream.write(tmp, 0, actuallyTransferSize);
					
					Server.mLastRecvTime = System
							.currentTimeMillis();
					currentSize += actuallyTransferSize ;
					// 把文件下载进度实时通知界面
					refreshCleanedSpace(actuallyTransferSize,file.length(),currentSize);
					sendToUi(actuallyTransferSize, Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
					// FLog.d(TAG, "正在运行已经传输大小:" + size);
				}
				outstream.flush();
			} catch (Exception etx) {
				FLog.e(TAG, "getEntityFromUri throw error", etx);
				mResponse.setStatusCode(HTTP_ERROR_CODE_UNACCEPTABLE);

				// UdpClient.send("192.168.173.1", "{ }");
			} finally {
				instream.close();
			}
		}
	
	}

	// 分析http头的range字段,获取文件偏移量(目前只获取文件偏移量,并不获取长度)
	private void analyHttpRequest(HttpRequest req) {
		String rangeValue = "";
		try {
			this.host = req.getFirstHeader("Host").getValue();
			
			// 解析断点续传的文件偏移量
			Header range = req.getFirstHeader("Range");
			if (null != range) {
				rangeValue = range.getValue();
				rangeValue = rangeValue.replace("bytes=", "");
				rangeValue = rangeValue.replace("-", "");
				if (!rangeValue.isEmpty()) {
					mFileOffset = Integer.parseInt(rangeValue);
				}
			}
		} catch (NumberFormatException e) {
			FLog.e(TAG, "analyHttpRequest NumberFormatException throw error" + rangeValue, e);
		} catch (Exception e) {
			FLog.e(TAG, "analyHttpRequest throw error" + rangeValue, e);
		}
	}
}
