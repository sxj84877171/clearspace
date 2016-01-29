/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

import java.io.File;
import java.util.Map;

/**
 *
 */
public abstract class OneDriveManager {
	
	public abstract Object getDrive() throws Exception;
	
	public abstract void getDriveAsync(final OneDriveOperationListener listener);
	
	public abstract Object getItemById(final String itemId, final Map<String, String> options) throws Exception;
	
	public abstract void getItemByIdAsync(final String itemId, final Map<String, String> options, final OneDriveOperationListener listener);
	
	public abstract Object getItemByPath(final String path, final Map<String, String> options) throws Exception;

	public abstract void getItemByPathAsync(final String path, final Map<String ,String> options, final OneDriveOperationListener listener);
	
	public abstract Object getSharedItem() throws Exception;
	
	public abstract void getSharedItemAsync(final OneDriveOperationListener listener);
	
	public abstract Object createFolder(final String itemId, final Object item) throws Exception;
	
	public abstract void createFolderAsync(final String itemId, final Object item, final OneDriveOperationListener listener);
	
	public abstract Object createFolderRecursion(final String path, final Object item) throws Exception;
	
	public abstract void createFolderRecursionAsync(final String path, final Object item, final OneDriveOperationListener listener);
	
	public abstract Object updateItem(final String itemId, final Object updatedItem) throws Exception;
	
	public abstract void updateItemAsync(final String itemId, final Object updatedItem, final OneDriveOperationListener listener);
	
	/**
	 * Upload large file - create upload
	 * @param path
	 * @param fileName
	 * @return 
	 */
	public abstract Object createUploadSession(final String path, final String fileName) throws Exception;
	
	/**
	 * Upload large file - abort upload
	 * @param uploadSession
	 * @return 
	 */
	public abstract int abortUploadSession(final String uploadSession) throws Exception;
	
	/**
	 * Upload large file - abort upload
	 * @param uploadSession
	 * @param listener
	 */
	public abstract void abortUploadSessionAsync(final String uploadSession, final OneDriveOperationListener listener);
	
	/**
	 * Upload large file - get upload status
	 * @param uploadSession
	 * @return
	 */
	public abstract Object getUploadStatus(final String uploadSession) throws Exception;
	
	/**
	 * Upload large file - get upload status
	 * @param uploadSession
	 * @param listener
	 */
	public abstract void getUploadStatusAsync(final String uploadSession, final OneDriveOperationListener listener);
	
	/**
	 * Upload large file - upload fragment
	 * @param uploadSession
	 * @param start
	 * @param end
	 * @param file
	 */
	public abstract Object uploadFragment(final String uploadSession, final long start, final long end, final File file, final OneDriveProgressListener listener) throws Exception;
	
	/**
	 * Upload large file - upload fragment
	 * @param uploadSession
	 * @param start
	 * @param end
	 * @param file
	 * @param listener
	 */
	public abstract void uploadFragmentAsync(final String uploadSession, final long start, final long end, final File file, final OneDriveOperationListener listener);
	
	public abstract Object uploadLastFragment(String uploadSession, long start, long end, final File file, final OneDriveProgressListener listener) throws Exception;
	
	public abstract Object resumeUpload(final String uploadSession, final File file, final TransferController controller) throws Exception;


	/**
	 * uploadFile on synchronous mode without progress control.
	 * @param itemId onedrive's object id
	 * @param fileName file name to upload	 *
	 * @param file file object to upload
	 * @return onedrive object related to the uploaded file
	 * @throws Exception
	 */
	public abstract Object uploadById(final String itemId, final String fileName, final File file) throws Exception;

	/**
	 * Uploads a resource by performing a synchronous HTTP PUT
     * 
     * <p>If a file with the same name exists the upload will replace.
     *
	 * @param itemId location to upload to.
     * @param fileName name of the new resource.
     * @param file contents of the upload.
	 * @throws NullPointerException if the path, fileName or file is null.
	 * @throws IllegalArgumentException if the path or fileName is empty.
	 * @throws OneDriveException if not log in.
	 */
	public abstract Object uploadById(final String itemId, final String fileName, final File file, final TransferController controller) throws Exception;
	
	/**
     * Uploads a resource by performing an asynchronous HTTP PUT.
     *
     * {@link OneDriveProgressListener#onSuccess(Object)} will be called on
     * success.
     * {@link OneDriveProgressListener#onProgress(long, long)} will be called
     * on upload progress.
     * Otherwise,
     * {@link OneDriveProgressListener#onFailure(Exception)}
     * will be called.
     *
     * @param itemId location to upload to.
     * @param fileName name of the new resource.
     * @param file contents of the upload.
     * @param listener called on completion, on progress, or on an error of the upload request.
     */
	public abstract void uploadByIdAsync(final String itemId, final String fileName, final File file, final OneDriveProgressListener listener);
	
	/**
	 * Uploads a resource by performing a synchronous HTTP PUT
     * 
     * <p>If a file with the same name exists the upload will replace.
     *
	 * @param itemId location to upload to.
     * @param fileName name of the new resource.
     * @param contents contents of the upload.
	 * @throws NullPointerException if the path, fileName or contents is null.
	 * @throws IllegalArgumentException if the path or fileName is empty.
	 * @throws OneDriveException if not log in.
	 */
	public abstract Object uploadById(final String itemId, final String fileName, final byte[] contents) throws Exception;
	
	/**
     * Uploads a resource by performing an asynchronous HTTP PUT.
     *
     * {@link OneDriveProgressListener#onSuccess(Object)} will be called on
     * success.
     * {@link OneDriveProgressListener#onProgress(long, long)} will be called
     * on upload progress.
     * Otherwise,
     * {@link OneDriveProgressListener#onFailure(Exception)}
     * will be called.
     *
     * @param itemId location to upload to.
     * @param fileName name of the new resource.
     * @param contents contents of the upload.
     * @param listener called on completion, on progress, or on an error of the upload request.
     */
	public abstract void uploadByIdAsync(final String itemId, final String fileName, final byte[] contents, final OneDriveProgressListener listener);
	
	public abstract Object uploadByPath(final String path, final String fileName, final File file, final TransferController listener) throws Exception;
	
	public abstract void uploadByPathAsync(final String path, final String fileName, final File file, final OneDriveProgressListener listener);
	
	public abstract Object uploadByPath(final String path, final String fileName, final byte[] contents) throws Exception;
	
	public abstract void uploadByPathAsync(final String path, final String fileName, final byte[] contents, final OneDriveProgressListener listener);
	
	public abstract void download(final String saveTo, final String itemId, final boolean isCover, final OneDriveProgressListener listener) throws Exception;
	
	public abstract void downloadAsync(final String saveTo, final String itemId, final boolean isCover, final OneDriveProgressListener listener); 
	
	public abstract Object createLink(final String itemId, final Object link) throws Exception;
	
	public abstract void createLinkAsync(final String itemId, final Object link, final OneDriveOperationListener listener);
	
	public abstract Object getItemChildren(final String itemId) throws Exception;
	
	public abstract void getItemChildrenAsync(final String itemId, final OneDriveOperationListener listener);
}
