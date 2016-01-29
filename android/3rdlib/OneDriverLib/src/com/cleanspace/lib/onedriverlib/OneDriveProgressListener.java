/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

/**
 *
 */
public interface OneDriveProgressListener extends OneDriveOperationListener {
	public abstract void onProgress(final long totalBytes, final long bytesTransferred);
}
