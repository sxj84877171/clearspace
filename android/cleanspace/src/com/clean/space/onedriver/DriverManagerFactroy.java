package com.clean.space.onedriver;

import android.app.Activity;
import android.content.Context;

import com.clean.space.Constants;
import com.clean.space.R;
import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.clean.space.util.ReadMataDataUtil;
import com.cleanspace.lib.onedriverlib.OneDriveAuthListener;
import com.cleanspace.lib.onedriverlib.OneDriveAuthManager;
import com.cleanspace.lib.onedriverlib.OneDriveAuthSession;
import com.cleanspace.lib.onedriverlib.OneDriveAuthStatus;
import com.cleanspace.lib.onedriverlib.OneDriveManager;
import com.cleanspace.lib.onedriverlib.OneDriveManagerFactory;
import com.cleanspace.lib.onedriverlib.OneDriveOperationListener;

/**
 * https://account.live.com/developers/applications/appsettings/000000004c175804<br>
 * 客户端 ID:<br>
 * 000000004C175804<br>
 * 客户端密钥(v1):<br>
 * cG16f2FrpuNrmxZdmSoDhbPEnq4Csh4t<br>
 * 
 * @author Elvis
 * 
 */
public class DriverManagerFactroy {

	public static final String TAG = DriverManagerFactroy.class.getSimpleName();

	public static final int ONEDRIVE_LOGOUT = 0;
	public static final int ONEDRIVE_LOGIN = 1;
	public static final int ONEDRIVE_LOGIN_OVERTIME = 2;

	private String loginUserName;
	private boolean isInitialize = false;

	private OneDriveAuthListener listener;
	public String getLoginUserName() {
		return loginUserName;
	}

	public void setLoginUserName(String loginUserName) {
		this.loginUserName = loginUserName;
	}

	private DriverManagerFactroy() {
	}

	private static DriverManagerFactroy instance;

	public static DriverManagerFactroy getInstance() {
		if (instance == null) {
			synchronized (DriverManagerFactroy.class) {
				if (instance == null) {
					instance = new DriverManagerFactroy();
				}
			}
		}
		return instance;
	}

	private OneDriveAuthManager manager;

	public synchronized OneDriveAuthManager getOneDriveAuthManager(
			Context context) {
		if (manager == null) {
			String appId = ReadMataDataUtil.getOneDriveApplicationId(context);
			if(appId == null){
				throw new RuntimeException("Manfiest file not OneDrive application id.");
			}
			FLog.i(TAG, "application id:" + appId);
			manager = OneDriveManagerFactory.getOneDriveAuthManager(context,appId
					, OneDriveManagerFactory.TYPE_REST);
			manager.initialize(new OneDriveAuthListener() {

				@Override
				public void onAuthError(Exception exception, Object userObject) {
					FLog.i(TAG, "OneDriveManagerFactory initialize ok,but it is fail.");
					isInitialize = true;
					if(listener != null){
						FLog.i(TAG, "OneDriveManagerFactory auto login.and onAuthError.");
						listener.onAuthError(exception, userObject);
					}
				}

				@Override
				public void onAuthComplete(OneDriveAuthStatus status,
						OneDriveAuthSession session, Object userObject) {
					FLog.i(TAG, "OneDriveManagerFactory initialize ok,login success.");
					isInitialize = true;
					if(listener != null){
						FLog.i(TAG, "OneDriveManagerFactory auto login.and onAuthComplete.");
						listener.onAuthComplete(status,session, userObject);
					}

				}
			}, context.getString(R.string.one_drive_string));
		}
		return manager;
	}

	private OneDriveManager driveManager;

	public OneDriveManager getOneDriveManager() {
		if (driveManager == null) {
			if (manager != null) {
				driveManager = manager.getOneDriveManager();
			}
		}
		return driveManager;
	}

	public void getDriveAsync(OneDriveOperationListener listener) {
		if (getOneDriveManager() != null) {
			driveManager.getDriveAsync(listener);
		}
	}

	public int isLogin(Context context) {
		int ret = ONEDRIVE_LOGOUT;
		String userName = UserSetting.getString(context,
				Constants.CLOUD_ONE_DRIVER, null);
		FLog.i(TAG, "save userName:" + userName);
		if (null != userName && !"".equals(userName)) {
			ret = getOneDriveAuthManager(context).isLogin() ? ONEDRIVE_LOGIN
					: ONEDRIVE_LOGIN_OVERTIME;
		}
		return ret;
	}

	public void login(final Activity context,
			final OneDriveAuthListener listener) {
		this.listener = listener ;
		if (isInitialize) {
			FLog.i(TAG, "direct login.");
			getOneDriveAuthManager(context).login(context,
					new OneDriveAuthListener() {
						@Override
						public void onAuthError(Exception exception,
								Object userObject) {
							FLog.i(TAG, "login onAuthError:" + exception);
							listener.onAuthError(exception, userObject);
						}

						@Override
						public void onAuthComplete(OneDriveAuthStatus status,
								OneDriveAuthSession session, Object userObject) {
							FLog.i(TAG, "login onAuthComplete:" + status);
							listener.onAuthComplete(status, session, userObject);
						}
					}, context.getString(R.string.one_drive_string));
		}
	}
}
