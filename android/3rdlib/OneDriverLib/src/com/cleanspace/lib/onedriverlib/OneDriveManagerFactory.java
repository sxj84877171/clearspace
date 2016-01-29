/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

import android.content.Context;


/**
 *
 */
public class OneDriveManagerFactory {
	
	public static final int TYPE_REST = 0;
	
	/**
	 * construct a OneDriveAuthManager to auth and get OneDriveManager.
	 * 
	 * @param context Context of the Application.
	 * @param clientId The client_id of Application to login to.
	 * @param type Type of OneDriveAuthManager. now only TYPE_REST.
	 * @return the OneDriveAuthManager object.
	 * @throws NullPointerException if the context or clientId is null.
	 * @throws IllegalArgumentException if the clientId is empty.
	 */
	public static OneDriveAuthManager getOneDriveAuthManager(final Context context, final String clientId, final int type) {
		OneDriveUtils.assertNotNull(context, "context");
		OneDriveUtils.assertNotNullOrEmpty(clientId, "clientId");
		
		if (type == TYPE_REST) {
			return new RESTAuthManager(context.getApplicationContext(), clientId);
		}
		return null;
	}
}
