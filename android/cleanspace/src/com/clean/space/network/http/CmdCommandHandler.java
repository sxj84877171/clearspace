package com.clean.space.network.http;

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

import com.clean.space.Constants;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.protocol.CleanFileStatusPkg;

public class CmdCommandHandler extends DownloadCommandHandlerBase implements HttpRequestHandler {

	private Context context = null;
	private static final String TAG = "CmdCommandHandler";
	private final int HTTP_ERROR_CODE_SUCCEED = 200;

	public CmdCommandHandler(Context ctx, Boolean bFromWeb) {
		super(ctx, bFromWeb);
		this.context = ctx;
	}

	public void handle(HttpRequest req, HttpResponse resp, HttpContext arg2)
			throws HttpException, IOException {
		String uriString = "";
		try {
			
			writeToSetting();
			sendToUi();
			HttpEntity entity = generateReplyMsg(resp);
			if (null != entity) {
				resp.setEntity(entity);
			}
		} catch (Exception e) {
			FLog.e(TAG, "handle throw error" + uriString, e);
		}

	}
	private void writeToSetting() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(context);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_INTERRUPT);
			boolean writeRet = UserSetting
					.setClearStatusInfo(context, pkg.toJson());

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
			context.sendBroadcast(intent);
		} catch (Exception e) {
			FLog.e(TAG, "sendToUi throw error", e);
		}
	}
	private HttpEntity generateReplyMsg(HttpResponse response) {
		HttpEntity entity = null;
		try {
			new EntityTemplate(new ContentProducer() {
				public void writeTo(final OutputStream outstream)
						throws IOException {
					OutputStreamWriter writer = new OutputStreamWriter(
							outstream, "GB2312");
					String resp = "send data succeed";

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
