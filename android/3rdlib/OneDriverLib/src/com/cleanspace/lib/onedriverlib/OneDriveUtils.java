/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

import android.text.TextUtils;


/**
 *
 */
final class OneDriveUtils {
	/**
     * Checks to see if the passed in Object is null, and throws a
     * NullPointerException if it is.
     *
     * @param object to check
     * @param parameterName name of the parameter that is used in the exception message
     * @throws NullPointerException if the Object is null
     */
    static void assertNotNull(Object object, String parameterName) {
        if (object == null) {
            final String message = String.format(OneDriveErrorMessages.NULL_PARAMETER, parameterName);
            throw new NullPointerException(message);
        }
    }
    
    static void assertNotNullOrEmpty(String parameter, String parameterName) {
        assertNotNull(parameter, parameterName);

        if (TextUtils.isEmpty(parameter)) {
            final String message = String.format(OneDriveErrorMessages.EMPTY_PARAMETER, parameterName);
            throw new IllegalArgumentException(message);
        }
    }
}
