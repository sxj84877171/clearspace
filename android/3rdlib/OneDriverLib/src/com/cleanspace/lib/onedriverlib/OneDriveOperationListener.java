/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

/**
 *
 */
public interface OneDriveOperationListener {
	  public abstract void onSuccess(final Object object);
	  public abstract void onFailure(final Exception exception);
}
