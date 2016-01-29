package com.clean.space.network.http;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;

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
import android.os.Bundle;

import com.clean.space.Constants;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.notification.CleanSpaceNotificationManager;
import com.clean.space.protocol.CleanFileStatusPkg;

public class DownloadCommandHandlerBase implements HttpRequestHandler {

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

	public DownloadCommandHandlerBase(Context ctx, Boolean bFromWeb) {
		this.context = ctx;
	}

	public void handle(HttpRequest req, HttpResponse resp, HttpContext arg2)
			throws HttpException, IOException {
		return;
	}

	private HttpEntity getEntityFromUri(String uri, final HttpResponse response) {
		return null;
	}

	public HttpEntity generateErrorMsg(HttpResponse response, int errCode) {
		HttpEntity entity = new EntityTemplate(new ContentProducer() {
			public void writeTo(final OutputStream outstream)
					throws IOException {
				OutputStreamWriter writer = new OutputStreamWriter(outstream,
						"GB2312");
				String resp = "<html>"
						+ "<head><title>ERROR : NOT FOUND</title></head>"
						+ "<body>"
						+ "<center><h1>FILE OR DIRECTORY NOT FOUND !</h1></center>"
						+ "<p>Sorry, file or directory you request not available<br />"
						+ "Contact your administrator<br />" + "</p>"
						+ "</body></html>";

				writer.write(resp);
				writer.flush();
			}
		});
		response.setHeader("Content-Type", "text/html");
		response.setStatusCode(errCode);
		return entity;
	}

	public void sendToUi(long fileSize, String action) {
		try {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putLong("fileSize", fileSize);
			intent.putExtras(bundle);
			intent.setAction(action);
			context.sendBroadcast(intent);
		} catch (Exception e) {
			FLog.e(TAG, "sendToUi throw error", e);
		}
	}

	public void refreshHandlerNumber() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(context);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			String strHandleNumber = pkg.getHandlePicNumber();
			pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_ING);
			long number = 1;
			if (null != strHandleNumber) {
				number = Long.parseLong(strHandleNumber) + 1;
			}
			pkg.setHandlePicNumber(String.valueOf(number));
			sendProcess(pkg);
			boolean writeRet = UserSetting.setClearStatusInfo(context,
					pkg.toJson());

			// FLog.i(TAG, "writeToSetting download complete filenumber "
			// + number);
		} catch (NumberFormatException e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		}
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
//		DecimalFormat df = new DecimalFormat("#.##%");
		
		if (total <= number) {
			CleanSpaceNotificationManager.getInstance()
					.sendExportProcessFinshNotification(context);
		} else {
			CleanSpaceNotificationManager.getInstance()
					.sendExportProcessNotification(context,
							String.format("%1$.2f", (100.0 * number) / total) + "%");
//							df.format();
		}
	}

	public void refreshCleanedSpace(long fileSize,long totalSize,long currrentSize) {
		try {
			String clearStatus = UserSetting.getClearStausInfo(context);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_ING);

			String strClearSpace = pkg.getCleanedSpace();
			long clearedSpace = 0;
			if (null != strClearSpace) {
				clearedSpace = Long.parseLong(strClearSpace);
			}
			clearedSpace += fileSize;
			pkg.setClearedSpace(String.valueOf(clearedSpace));
			pkg.setCurrentFileSize(""+ totalSize);
			pkg.setCurrentTransferSize(""+ currrentSize);
			if (pkg.getStart() <= 0) {
				pkg.setStart(System.currentTimeMillis());
			}
			pkg.setEnd(System.currentTimeMillis());

			boolean writeRet = UserSetting.setClearStatusInfo(context,
					pkg.toJson());

			// FLog.i(TAG, "writeToSetting download complete filenumber "
			// + number);
		} catch (NumberFormatException e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "writeToSetting throw error", e);
		}
	}
}
