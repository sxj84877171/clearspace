

package com.clean.space.network.http;

import java.io.IOException;
import java.net.Socket;

import org.apache.http.HttpException;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

import android.content.Context;

import com.clean.space.Constants;
import com.clean.space.log.FLog;

public class HttpThread extends Thread {
	
	private static final String ALL_PATTERN = "*";
	private static final String TAG = "HttpThread";
	
	private Context context = null;
	
	public BasicHttpProcessor httproc;
	public HttpService httpserv = null;
	private BasicHttpContext httpContext = null;
	private HttpRequestHandlerRegistry reg = null;
	
	Socket soket = null;
	
	public HttpThread(Context ctx, Socket soket, String threadName) {
		
		this.setContext(ctx);
		this.soket = soket;
		this.setName(threadName);
		httproc = new BasicHttpProcessor();
		httpContext = new BasicHttpContext();
		
		httproc.addInterceptor(new ResponseDate());
	    httproc.addInterceptor(new ResponseServer());
	    httproc.addInterceptor(new ResponseContent());
	    httproc.addInterceptor(new ResponseConnControl());
	    
	    httpserv = new HttpService(httproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
	    
	    reg = new HttpRequestHandlerRegistry();

		reg.register(ALL_PATTERN, new HomeCommandHandler(ctx));
		reg.register(Constants.DOWNLOAD_STOP_BY_PC, new CmdCommandHandler(ctx, false));
		reg.register(Constants.DOWNLOAD_PATTERN, new DownloadCommandHandler(ctx, false));
		reg.register(Constants.DOWNLOAD_COMPLETE_PATTERN, new DownloadCompleteCommandHandler(ctx, false));
		httpserv.setHandlerResolver(reg);
		
	}

	public void run(){
		DefaultHttpServerConnection httpserver = new DefaultHttpServerConnection();
		try {
			httpserver.bind(this.soket, new BasicHttpParams());
			httpserv.handleRequest(httpserver, httpContext);
		} catch (IOException e) {
			FLog.e(TAG, "Exception in HttpThread.java:can't bind", e);
		} catch (HttpException e) {
			FLog.e(TAG, "Exception in HttpThread.java:handle request", e);
		} catch (Exception e){
			FLog.e(TAG, "debug : error again !", e);
		}
		finally {		
			try {
				httpserver.close();
			} catch (IOException e) {
				System.err.println("Excetion in HttpThread.java:can't shutdown");
				e.printStackTrace();
			}
			 
		}
	}
	
	public void setContext(Context context) {
		this.context = context;
	}
	
	public Context getContext() {
		return context;
	}
	
}
