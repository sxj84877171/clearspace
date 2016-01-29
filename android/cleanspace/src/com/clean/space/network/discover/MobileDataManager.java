package com.clean.space.network.discover;

import java.lang.reflect.Method;
import java.net.Socket;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Handler;

import com.clean.space.log.FLog;

public class MobileDataManager {
	
	public static final String TAG = MobileDataManager.class.getSimpleName();

	private Handler handler = null;
	private boolean isEnable = false;
	private boolean dataEnable = false;
	private Context mContext ;
	private MobileDataManager() {
	}

	private static MobileDataManager instance = new MobileDataManager();
	
	public static MobileDataManager getInstance(){
		return instance ;
	}
	
	public void enableMobileData(Context context){
		mContext = context ;
		isEnable = true;
		if(handler == null){
			handler = new Handler(mContext.getMainLooper());
		}
		if(dataEnable){
		try {
			Socket socket = new Socket("www.baidu.com",80);
			if(handler != null){
				handler.removeCallbacks(task);
			}
		} catch (Exception e) {
			setDataConnectionState(true, context);
			if(handler != null){
				handler.postDelayed(task, 2000);
			}
		}
		}
		
	}
	
	public void disableMobileData(Context context){
		mContext = context; 
		isEnable = false;
		try {
			Socket socket = new Socket("www.baidu.com",80);
			setDataConnectionState(false, context);
			dataEnable = true;
		} catch (Exception e) {
			/*if(handler != null){
				handler.removeCallbacks(task);
			}*/
			dataEnable = false;
		}
		if(handler != null){
			handler.postDelayed(task, 2000);
		}
	}
	
	public void setDataConnectionState(boolean state,Context mContext) {
		FLog.i(TAG, "setDataConnectionState:" + state);
		ConnectivityManager connectivityManager = null;
		Class connectivityManagerClz = null;
		try {
			connectivityManager = (ConnectivityManager) mContext
					.getSystemService("connectivity");
			connectivityManagerClz = connectivityManager.getClass();
			Method method = connectivityManagerClz.getMethod(
					"setMobileDataEnabled", new Class[] { boolean.class });
			method.invoke(connectivityManager, state);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private Runnable task = new Runnable() {
		
		@Override
		public void run() {
			if(handler != null){
				handler.postDelayed(task, 2000);
			}
			if(isEnable){
				enableMobileData(mContext);
			}else{
				disableMobileData(mContext);
			}
		}
	};
}
