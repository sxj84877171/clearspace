/**
 * 
 */
package com.cleanspace.lib.onedriverlib;


/**
 *
 */
public interface OneDriveAuthListener {
	void onAuthComplete(final OneDriveAuthStatus status, final OneDriveAuthSession session, final Object userObject);

    void onAuthError(final Exception exception, final Object userObject);
}
