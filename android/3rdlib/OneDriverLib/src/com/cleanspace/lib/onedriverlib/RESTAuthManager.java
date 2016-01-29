/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

import java.util.Arrays;

import android.app.Activity;
import android.content.Context;

import com.microsoft.authenticate.AuthClient;
import com.microsoft.authenticate.AuthException;
import com.microsoft.authenticate.AuthListener;
import com.microsoft.authenticate.AuthSession;
import com.microsoft.authenticate.AuthStatus;
import com.microsoft.onedriveaccess.OneDriveOAuthConfig;

/**
 *
 */
public class RESTAuthManager extends OneDriveAuthManager {
	
	private static final OneDriveAuthListener NULL_AUTH_LISTENER = new OneDriveAuthListener() {

		@Override
		public void onAuthComplete(final OneDriveAuthStatus status, final OneDriveAuthSession session, final Object userObject) {
		}

		@Override
		public void onAuthError(final Exception exception, final Object userObject) {
		}
		
	};
	
	private final AuthClient mAuthClient;
	private final RESTManager mOneDriveManager;
	
	RESTAuthManager(final Context appContext, final String clientId) {
		mAuthClient = new AuthClient(appContext, OneDriveOAuthConfig.getInstance(), clientId);
		mOneDriveManager = new RESTManager(this);
	}
	
	@Override
	public void initialize(final OneDriveAuthListener listener, final Object userObject) {
		final OneDriveAuthListener callback = listener == null ? NULL_AUTH_LISTENER : listener;
		if (!isLogin()) {
			mAuthClient.initialize(new AuthListener() {

				@Override
				public void onAuthComplete(final AuthStatus status, final AuthSession session, final Object userState) {
					callback.onAuthComplete(convertStatus(status), convertSession(session), userState);
				}

				@Override
				public void onAuthError(final AuthException exception, final Object userState) {
					callback.onAuthError(exception, userState);
				}
				
			}, userObject);
		} else {
			callback.onAuthComplete(OneDriveAuthStatus.CONNECTED, convertSession(mAuthClient.getSession()), userObject);
		}
	}
	
	@Override
	public void login(final Activity activity, final OneDriveAuthListener listener, final Object userObject) {
		OneDriveUtils.assertNotNull(activity, "activity");
		
		final OneDriveAuthListener callback = listener == null ? NULL_AUTH_LISTENER : listener;
		initialize(new OneDriveAuthListener() {

			@Override
			public void onAuthComplete(final OneDriveAuthStatus status, final OneDriveAuthSession session, final Object userObject) {
				if (status != OneDriveAuthStatus.CONNECTED) {
					mAuthClient.login(activity, Arrays.asList(SCOPES), new AuthListener() {
						@Override
						public void onAuthComplete(final AuthStatus status, final AuthSession session, final Object userState) {
							callback.onAuthComplete(convertStatus(status), convertSession(session), userState);
						}

						@Override
						public void onAuthError(final AuthException exception, final Object userState) {
							callback.onAuthError(exception, userState);
						}
					}, userObject);
				} else {
					callback.onAuthComplete(status, session, userObject);
				}
			}

			@Override
			public void onAuthError(final Exception exception, final Object userObject) {
				callback.onAuthError(exception, userObject);
			}
			
		}, userObject);
	}

	@Override
	public void logout(final OneDriveAuthListener listener, final Object userObject) {
		final OneDriveAuthListener callback = listener == null ? NULL_AUTH_LISTENER : listener;
		mAuthClient.logout(new AuthListener() {

			@Override
			public void onAuthComplete(final AuthStatus status, final AuthSession session, final Object userState) {
				callback.onAuthComplete(convertStatus(status), convertSession(session), userState);
			}

			@Override
			public void onAuthError(final AuthException exception, final Object userState) {
				callback.onAuthError(exception, userState);
			}
			
		}, userObject);
	}
	
	@Override
	public OneDriveAuthSession getOneDriveAuthSession() {
		return isLogin() ? convertSession(mAuthClient.getSession()) : null;
	}

	@Override
	public OneDriveManager getOneDriveManager() {
		return mOneDriveManager;
	}
	
	@Override
	public boolean isLogin() {
		if (mAuthClient != null && mAuthClient.getSession() != null && !mAuthClient.getSession().isExpired()//MS not set session expired when logout.
				&& mAuthClient.getSession().getAccessToken() != null) {
			return true;
		}
		return false;
	}
	
	private OneDriveAuthStatus convertStatus(final AuthStatus status) {
		if (status == AuthStatus.CONNECTED) {
			return OneDriveAuthStatus.CONNECTED;
		}
		if (status == AuthStatus.NOT_CONNECTED) {
			return OneDriveAuthStatus.NOT_CONNECTED;
		}
		return OneDriveAuthStatus.UNKNOWN;
	}
	
	private OneDriveAuthSession convertSession(final AuthSession session) {
		return session == null? null : new OneDriveAuthSession(session.getAccessToken(), session.getRefreshToken());
	}
	
	AuthClient getAuthClient() {
		return mAuthClient;
	}
}
