package com.microsoft.onedriveaccess;

import com.microsoft.authenticate.AuthClient;
import com.microsoft.onedriveaccess.BuildConfig;

import retrofit.RequestInterceptor;

/**
 * Produces Request interceptor for OneDrive service requests
 */
final class InterceptorFactory {

    /**
     * Default Constructor
     */
    private InterceptorFactory() {
    }

    /**
     * Creates an instance of the request interceptor
     * @param authClient The credentials used by the interceptor
     * @return The interceptor object
     */
    public static RequestInterceptor getRequestInterceptor(final AuthClient authClient) {
        return new RequestInterceptor() {

            @Override
            public void intercept(final RequestFacade request) {
                if (authClient.getSession().willExpireInSecs(30)) {//refresh session when session will expire in 30s.
            		authClient.getSession().refresh();
            	}
            	
                request.addHeader("Authorization", "bearer " + authClient.getSession().getAccessToken());
                request.addHeader("User-Agent", "OneDriveSDK Android " + BuildConfig.VERSION_NAME);
            }
        };
    }
}
