/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

/**
 *
 */
public class OneDriveAuthSession {
	private final String mAccessToken;
	private final String mRefreshToken;
	
	OneDriveAuthSession(String accessToken, String refreshToken) {
		mAccessToken = accessToken;
		mRefreshToken = refreshToken;
	}
	
	public String getAccessToken() {
		return mAccessToken;
	}
	
	public String getRefreshToken() {
		return mRefreshToken;
	}
}
