/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

import android.app.Activity;
import android.app.Dialog;

/**
 *
 */
public abstract class OneDriveAuthManager {
	static final String[] SCOPES = {"wl.signin",
		"wl.basic",
		"wl.offline_access",
		"wl.skydrive_update",
		"wl.contacts_create",
		"wl.contacts_skydrive",
		"wl.contacts_photos",
		"wl.emails"};
	
	public abstract void initialize(final OneDriveAuthListener listener, final Object userObject);
	
	/**
	 * Logs in an user.
     *
     * login call {@link #initialize(OneDriveAuthListener, Object)} first.
     * If initialize failed, then displays a {@link Dialog} that will prompt the
     * user for a username and password, and ask for consent to use scopes.
     * A {@link OneDriveAuthSession} will be returned by calling
     * {@link OneDriveAuthListener#onAuthComplete(OneDriveAuthStatus, OneDriveAuthSession, Object)}.
     * Otherwise, the {@link OneDriveAuthListener#onAuthError(Exception, Object)} will be
     * called. These methods will be called on the main/UI thread.
     *
	 * @param activity {@link Activity} instance to display the Login dialog on.
	 * @param listener called on either completion or error during the login process.
	 * @param userObject arbitrary object that is used to determine the caller of the method.
	 * @throws NullPointerException if the activity is null.
	 */
	public abstract void login(final Activity activity, final OneDriveAuthListener listener, final Object userObject);
	
	public abstract void logout(final OneDriveAuthListener listener, final Object userObject);
	
	public abstract OneDriveAuthSession getOneDriveAuthSession();
	
	public abstract OneDriveManager getOneDriveManager();
	
	public abstract boolean isLogin();
}
