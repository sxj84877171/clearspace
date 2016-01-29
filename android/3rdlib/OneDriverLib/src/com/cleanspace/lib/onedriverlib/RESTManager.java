/**
 * 
 */
package com.cleanspace.lib.onedriverlib;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;
import android.text.TextUtils;

import com.cleanspace.lib.util.NetDate;
import com.microsoft.onedriveaccess.ODConnection;
import com.microsoft.onedriveaccess.model.Drive;
import com.microsoft.onedriveaccess.model.Folder;
import com.microsoft.onedriveaccess.model.Item;
import com.microsoft.onedriveaccess.model.LargeItem;
import com.microsoft.onedriveaccess.model.Permission;
import com.microsoft.onedriveaccess.model.SharingLink;
import com.microsoft.onedriveaccess.model.UploadFragmentSession;
import com.microsoft.onedriveaccess.model.UploadSession;

/**
 *
 */
public class RESTManager extends OneDriveManager {

	// 1M, larger than 1M will transfer as large file
	private static final long MAX_SIZE = 1 * 1024;

	// 1M, max to 60MB
	private static final int FRAGMENT_SIZE = 20 * 1024 * 1024;

	// 10GB is large file limitation
	private static final long MAX_LARGE_FILE_SIZE = 10L * 1024 * 1024 * 1024; 
	
	private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
	
	private final RESTAuthManager mAuth;
	private final ODConnection mConnection;
	
	private static final OneDriveOperationListener NULL_LISTENER;
	private static final OneDriveListener NULL_PROGRESS_LISTENER;

	static
	{
		NULL_LISTENER = new OneDriveOperationListener() {
			@Override
			public void onSuccess(final Object object) {
			}

			@Override
			public void onFailure(final Exception exception) {
			}
			
		};
		
		NULL_PROGRESS_LISTENER = new OneDriveListener(){
			@Override
			public void onEnterTransfer() {

			}

			@Override
			public void onSuccess(final Object object) {
			}

			@Override
			public void onFailure(final Exception exception) {
			}

			@Override
			public void onProgress(final long totalBytes, final long bytesTransfered) {
			}
			
		};
	}
	
	RESTManager(final RESTAuthManager authManager) {
		mAuth = authManager;
		mConnection = new ODConnection(mAuth.getAuthClient());
		mConnection.setVerboseLogcatOutput(false);
	}
	
	@Override
	public Object getDrive() throws Exception {
		if (isLogin()) {
			return mConnection.getService().getDrive();
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}
	
	@Override
	public void getDriveAsync(final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().getDrive(new Callback<Drive>() {

				@Override
				public void failure(RetrofitError error) {
					callback.onFailure(error);
				}

				@Override
				public void success(Drive object, Response response) {
					callback.onSuccess(object);
				}

			});
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}
	
	@Override
	public Object getItemById(final String itemId, final Map<String, String> options) throws Exception {
		if (isLogin()) {
			return mConnection.getService().getItemId(itemId, options);
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}
	
	@Override
	public void getItemByIdAsync(final String itemId, final Map<String, String> options, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().getItemId(itemId, options, new Callback<Item>() {

				@Override
				public void failure(RetrofitError error) {
					callback.onFailure(error);
				}

				@Override
				public void success(Item object, Response response) {
					callback.onSuccess(object);
				}

			});
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}
	
	@Override
	public Object getItemByPath(final String path, final Map<String, String> options)
			throws Exception {
		if (isLogin()) {
			return mConnection.getService().getItemPath(path, options);
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}

	@Override
	public void getItemByPathAsync(final String path, final Map<String ,String> options, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().getItemPath(path, options, new Callback<Item>() {
				@Override
				public void success(Item item, Response response) {
					callback.onSuccess(item);
				}

				@Override
				public void failure(RetrofitError error) {
					callback.onFailure(error);
				}
			});
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}

	@Override
	public Object getSharedItem() throws Exception {
		if (isLogin()) {
			return mConnection.getService().getSharedItem();
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}

	@Override
	public void getSharedItemAsync(final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().getSharedItem(new Callback<Item>() {

				@Override
				public void failure(RetrofitError error) {
					callback.onFailure(error);
				}

				@Override
				public void success(Item object, Response response) {
					callback.onSuccess(object);
				}

			});
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}

	@Override
	public Object createFolder(final String itemId, final Object item) throws Exception{
		if (item instanceof Item) {
			final Item newItem = (Item)item;
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().createFolder(itemId, newItem);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_ITEM);
		}
	}
	
	@Override
	public void createFolderAsync(final String itemId, final Object item, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (item instanceof Item) {
			final Item newItem = (Item)item;
			if (isLogin()) {
				
				mConnection.getService().createFolder(itemId, newItem, new Callback<Item>() {

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
					}

					@Override
					public void success(Item object, Response response) {
						callback.onSuccess(object);
					}

				});
			} else {
				callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		} else {
			callback.onFailure(new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_ITEM));
		}
	}
	
	@Override
	public Object createFolderRecursion(final String path, final Object item) throws Exception {
		if (item instanceof Item) {
			final Item newItem = (Item)item;
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().createFolderRecursion(path, newItem);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_ITEM);
		}
	}

	@Override
	public void createFolderRecursionAsync(final String path, final Object item, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (item instanceof Item) {
			final Item newItem = (Item)item;
			if (isLogin()) {
				mConnection.getService().createFolderRecursion(path, newItem, new Callback<Item>() {

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
					}

					@Override
					public void success(Item object, Response response) {
						callback.onSuccess(object);
					}

				});
			} else {
				callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		} else {
			callback.onFailure(new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_ITEM));
		}
	}
	
	@Override
	public Object updateItem(final String itemId, final Object updatedItem) throws Exception {
		if (updatedItem instanceof Item) {
			final Item newItem = (Item)updatedItem;
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().updateItemId(itemId, newItem);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_ITEM);
		}
	}

	@Override
	public void updateItemAsync(final String itemId, final Object updatedItem, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (updatedItem instanceof Item) {
			final Item newItem = (Item)updatedItem;
			if (isLogin()) {
				mConnection.getService().updateItemId(itemId, newItem, new Callback<Item>() {

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
					}

					@Override
					public void success(Item object, Response response) {
						callback.onSuccess(object);
					}

				});
			} else {
				callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		} else {
			callback.onFailure(new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_ITEM));
		}
	}

	@Override
	public Object uploadById(final String itemId, final String fileName, final File file)
	{
		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(file, "file");
		if (isLogin()) {
			return mConnection.getService().createItemId(itemId, fileName, new TypedFile(APPLICATION_OCTET_STREAM, file));
		}
		return null;
	}

	@Override
	public Object uploadById(final String itemId,final String fileName, final File file, final TransferController controller) throws Exception {
		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(file, "file");

		if(controller!=null&&controller.isReTransfer()&&controller.getSessionKey()!=null){
			return resumeUpload(controller.getSessionKey(),file,controller);
		}

		final OneDriveListener callback = controller.getOneDriveListener() == null ? NULL_PROGRESS_LISTENER : controller.getOneDriveListener();
		if (isLogin()) {
			if (file.length() < MAX_SIZE) {
				return mConnection.getService().createItemId(itemId, fileName,
						new ProgressTypedFile(APPLICATION_OCTET_STREAM, file, file.length(), callback));

//				final Map<String, String> options = new HashMap<String, String>();
//				options.put("@name.conflictBehavior", "rename");
//
//				return mConnection.getService().createItemId(parentId, fileName, options,
//						new TypedByteArray(APPLICATION_OCTET_STREAM, fileInMemory));
			} else if(file.length() < MAX_LARGE_FILE_SIZE) {
				// >1M file
				UploadSession session = mConnection.getService().createUploadSession(itemId, fileName);
				if(session != null) {
					return uploadLargeFile(session.getUploadSession(), 0, file, controller);
				}
			} else {
				// FILE TOO LARGE
				callback.onFailure(new Exception("File Too Large"));
			}
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
		
		return null;
	}


	/***
	 *
	 * 	upload file to onedrive. file size should be small than 100M.
	 *
	 * @param itemId location to upload to.
	 * @param fileName
	 * @param file contents of the upload.
	 * @param listener called on completion, on progress, or on an error of the upload request.
	 */

	@Override
	public void uploadByIdAsync(final String itemId, final String fileName, final File file, final OneDriveProgressListener listener) {

		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(file, "file");

		final OneDriveProgressListener callback = listener == null ? NULL_PROGRESS_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().createItemId(itemId, fileName,
					new ProgressTypedFile(APPLICATION_OCTET_STREAM, file, file.length(), callback),
					new Callback<Item>() {

						@Override
						public void failure(RetrofitError error) {
							callback.onFailure(error);
						}

						@Override
						public void success(Item object, Response response) {
							callback.onSuccess(object);
						}

					});
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}
	
	@Override
	public Object uploadById(final String itemId, final String fileName, final byte[] contents) throws Exception {
		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(contents, "contents");
		
		if (isLogin()) {

			return mConnection.getService().createItemId(itemId, fileName, new TypedByteArray(APPLICATION_OCTET_STREAM, contents));

		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}

	/**
	 * upload file bytes to onedrive. don't call this function if file size over 100MB.
	 * @param itemId location to upload to.
	 * @param fileName file name
	 * @param contents contents of the upload.
	 * @param listener called on completion, on progress, or on an error of the upload request.
	 */
	@Override
	public void uploadByIdAsync(final String itemId, final String fileName, final byte[] contents, final OneDriveProgressListener listener) {
		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(contents, "contents");
		
		final OneDriveProgressListener callback = listener == null ? NULL_PROGRESS_LISTENER : listener;
		if (isLogin()) {
			try {
				mConnection.getService().createItemId(itemId, fileName, new ProgressTypedByteArray(APPLICATION_OCTET_STREAM, contents, contents.length, callback),
						new Callback<Item>() {

							@Override
							public void failure(RetrofitError error) {
								callback.onFailure(error);
							}

							@Override
							public void success(Item object, Response response) {
								callback.onSuccess(object);
							}

				});
			} catch (Exception e) {
				callback.onFailure(e);
			}
		}else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}
	
	private String getParentFolderPath(String path) {
		String parentFolderPath = "";
		int lastIndex = path.lastIndexOf('/');
		if(lastIndex == path.length() - 1) {
			lastIndex = path.substring(0, path.length() - 1).lastIndexOf('/');
		}
		if(lastIndex == -1) {
			parentFolderPath = "";
		} else {
			parentFolderPath = path.substring(0, lastIndex);
		}
		return parentFolderPath;
	}
	
	private String getFolderName(String path) {
		String[] p = path.split("/");
		String name = p[p.length - 1];
		if(TextUtils.isEmpty(name) && p.length > 1) {
			name = p[p.length - 2];
		}
		return name;
	}

	@Override
	public Object uploadByPath(String path, final String fileName, final File file, final TransferController controller) throws Exception {
		OneDriveUtils.assertNotNullOrEmpty(path, "path");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(file, "file");

		if(controller!=null&&controller.isReTransfer()&&controller.getSessionKey()!=null){
			return resumeUpload(controller.getSessionKey(),file,controller);
		}

		final OneDriveListener callback = controller.getOneDriveListener() == null ? NULL_PROGRESS_LISTENER : controller.getOneDriveListener();
		if (isLogin()) {
			if(file.length() < MAX_LARGE_FILE_SIZE) {
				Item item = null;
				try {
					item = (Item) getItemByPath(path, null);
				} catch(Exception e) {
					e.printStackTrace();
				}
				
				if(item == null) {
					final Item newItem = new Item();
			        newItem.Name = getFolderName(path);
			        newItem.Folder = new Folder();
					item = mConnection.getService().createFolderRecursion(getParentFolderPath(path), newItem);
				}
				if(item != null) {
					UploadSession session = mConnection.getService().createUploadSession(item.Id, fileName);
					String sessionKey=session.getUploadSession();
					controller.setSessionKey(sessionKey);
					if(session != null) {
						Object obj = uploadLargeFile(sessionKey, 0, file, controller);
						callback.onSuccess(obj);
						return obj;
					} else {
						callback.onFailure(new Exception("Create Upload Session Failure"));
					}
				} else {
					callback.onFailure(new Exception("no such a path on onedrive"));
				}
			} else {
				// FILE TOO LARGE
				callback.onFailure(new Exception("File Too Large"));
			}
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
		
		return null;
	}


	@Override
	public void uploadByPathAsync(final String path, final String fileName, final File file, final OneDriveProgressListener listener) {
		OneDriveUtils.assertNotNullOrEmpty(path, "path");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(file, "file");
		
		final OneDriveProgressListener callback = listener == null ? NULL_PROGRESS_LISTENER : listener;
		if (isLogin()) {
			if (file.length() < MAX_SIZE) {
				try {
					mConnection.getService().createItemPath(path, fileName,
							new ProgressTypedFile(APPLICATION_OCTET_STREAM, file, file.length(), listener),
							new Callback<Item>() {

								@Override
								public void failure(RetrofitError error) {
									callback.onFailure(error);
								}

								@Override
								public void success(Item object, Response response) {
									callback.onSuccess(object);
								}

							});
				} catch (Exception e) {
					callback.onFailure(e);
				}
			} else {
				//TODO >1M file
			}
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}

	@Override
	public Object uploadByPath(final String path, final String fileName, final byte[] contents) throws Exception {
		OneDriveUtils.assertNotNullOrEmpty(path, "path");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(contents, "contents");
		
		if (isLogin()) {
			if (contents.length < MAX_SIZE) {
				return mConnection.getService().createItemPath(path, fileName, new TypedByteArray(APPLICATION_OCTET_STREAM, contents));
			} else {
				//TODO >1M file
			}
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
		
		return null;
	}

	@Override
	public void uploadByPathAsync(final String path, final String fileName, final byte[] contents, final OneDriveProgressListener listener) {
		OneDriveUtils.assertNotNullOrEmpty(path, "path");
		OneDriveUtils.assertNotNullOrEmpty(fileName, "fileName");
		OneDriveUtils.assertNotNull(contents, "contents");
		
		final OneDriveProgressListener callback = listener == null ? NULL_PROGRESS_LISTENER : listener;
		if (isLogin()) {
			if (contents.length < MAX_SIZE) {
				try {
					mConnection.getService().createItemPath(path, fileName, new ProgressTypedByteArray(APPLICATION_OCTET_STREAM, contents, contents.length, listener),
							new Callback<Item>() {

								@Override
								public void failure(RetrofitError error) {
									callback.onFailure(error);
								}

								@Override
								public void success(Item object, Response response) {
									callback.onSuccess(object);
								}
						
					});
				} catch (Exception e) {
					callback.onFailure(e);
				}
			} else {
				//TODO >1M file
			}
		}else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}

	@Override
	public Object resumeUpload(final String uploadSessionKey, final File file, final TransferController controller) throws Exception {
		UploadFragmentSession session = mConnection.getService().getUploadSessionStatus(uploadSessionKey);
		NetDate netDate = new NetDate();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(netDate.getUTCNetworkDate());
		if(session != null && !session.isExpired(calendar.getTime())) {
			List<String> ranges = session.NextExpectedRanges;
			long start = 0;
			if(ranges != null && ranges.size() > 0) {
				String range = ranges.get(0);
				String[] r = range.split("-");
				if(r.length > 0) {
					start = Long.parseLong(r[0]);
				}
			}
			return uploadLargeFile(uploadSessionKey, start, file, controller);
		}
		return null;
	}
	
	private LargeItem uploadLargeFile(String session, long start, File file, TransferController controller) throws Exception {
		long fileSize = file.length();
		long remind = fileSize - start;
		long startOffset = 0;
		long endOffset = 0;
		int count = (int) (remind / FRAGMENT_SIZE);
		count += remind % FRAGMENT_SIZE > 0 ? 1 : 0;
		LargeItem item = null;
		if(controller.getOneDriveListener()!=null){
			controller.getOneDriveListener().onEnterTransfer();
		}
		for (int i = 0; i < count; i++) {
			
			// break if user cancel upload
			if(controller.isCancel()) {
				try{
					abortUploadSession(session);
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			}
			
			startOffset = i * FRAGMENT_SIZE + start;
			endOffset = startOffset + FRAGMENT_SIZE - 1;
			if (endOffset >= fileSize) {
				endOffset = fileSize - 1;
			}

			int repeatCount=controller.getRepeatCount();
			//Error retry 5
			do{
				try{
					if(i != count - 1) {
						uploadFragment(session, startOffset, endOffset, file, controller.getOneDriveProgressListener());
					} else {
						item = (LargeItem) uploadLastFragment(session, startOffset, endOffset, file, controller.getOneDriveProgressListener());
					}
					repeatCount=controller.getRepeatCount();
					break;
				}catch (Exception e){
					e.printStackTrace();
					if(repeatCount ==0 && !controller.isCancel()){
						throw e;
					}
					repeatCount--;
					try{
						Thread.sleep((10*(controller.getRepeatCount()-repeatCount))*1000);
					}catch (Exception e2){
					}

				}
			}while (repeatCount>=0 && !controller.isCancel());
		}
		return item;
	}

	@Override
	public void download(String saveTo, String itemId, boolean isCover, OneDriveProgressListener listener) throws Exception {
		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		
		if (isLogin()) {
			Item item = mConnection.getService().getItemId(itemId);
			Request request = new Request("GET", item.Content_downloadUrl, null, null);
			Response response = mConnection.getHttpClient().execute(request);
			if(response.getStatus() == 200) {
				TypedInput input = response.getBody();
				new DownloadWrapper(saveTo, input, listener, isCover).start();
			}
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}

	@Override
	public void downloadAsync(final String saveTo, final String itemId, final boolean isCover, final OneDriveProgressListener listener) {
		OneDriveUtils.assertNotNullOrEmpty(itemId, "itemId");
		
		final OneDriveProgressListener callback = listener == null ? NULL_PROGRESS_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().getItemId(itemId, new Callback<Item>() {

				@Override
				public void failure(RetrofitError error) {
					callback.onFailure(error);
				}

				@Override
				public void success(Item object, Response response) {
					String s = object.Content_downloadUrl;
					Request request = new Request("GET", s, null, null);
					try {
						Response resp = mConnection.getHttpClient().execute(request);
						if(resp.getStatus() == 200) {
							TypedInput input = resp.getBody();
							new DownloadWrapper(saveTo, input, callback, isCover).startAsync();
						}
					} catch (IOException e) {
						listener.onFailure(e);
					}
					callback.onSuccess(object);
				}
			});
		} else {
			if(listener != null)
			{
				listener.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		}
	}
	
	@Override
	public Object createLink(final String itemId, final Object link) throws Exception {
		if (link instanceof SharingLink) {
			final SharingLink type = (SharingLink)link;
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().createLink(itemId, type);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_LINK);
		}
	}

	@Override
	public void createLinkAsync(final String itemId, final Object link, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (link instanceof SharingLink) {
			final SharingLink type = (SharingLink)link;
			if (isLogin()) {
				
				mConnection.getService().createLink(itemId, type, new Callback<Permission>() {

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
					}

					@Override
					public void success(Permission object, Response response) {
						callback.onSuccess(object);
					}
					
				});
			} else {
				callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		} else {
			callback.onFailure(new IllegalArgumentException(OneDriveErrorMessages.ILLEGAL_LINK));
		}
	}
	
	@Override
	public Object createUploadSession(String path, String fileName) throws Exception {
		if (!TextUtils.isEmpty(path) && !TextUtils.isEmpty(fileName)) {
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().createUploadSession(path, fileName);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER);
		}
	}

	@Override
	public int abortUploadSession(String uploadSession) throws Exception {
		if (!TextUtils.isEmpty(uploadSession)) {
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().abortUploadSession(uploadSession);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER);
		}
	}

	@Override
	public void abortUploadSessionAsync(String uploadSession, OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (!TextUtils.isEmpty(uploadSession)) {
			if (isLogin()) {
				mConnection.getService().abortUploadSession(uploadSession, new Callback<Integer>() {

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
					}

					@Override
					public void success(Integer object, Response response) {
						callback.onSuccess(object);
					}
					
				});
			} else {
				callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		} else {
			callback.onFailure(new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER));
		}
	}

	@Override
	public Object getUploadStatus(String uploadSession) throws Exception {
		if (!TextUtils.isEmpty(uploadSession)) {
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				return mConnection.getService().getUploadSessionStatus(uploadSession);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER);
		}
	}

	@Override
	public void getUploadStatusAsync(String uploadSession, OneDriveOperationListener listener) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object uploadFragment(String uploadSession, long start, long end, File file, OneDriveProgressListener listener) throws Exception {
		if (!TextUtils.isEmpty(uploadSession)) {
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				String range = String.format(Locale.getDefault(), "bytes %d-%d/%d", start, end, file.length());
				TypedOutput typedFile = new FragmentTypedFile(APPLICATION_OCTET_STREAM, file, start, end, listener);
				return mConnection.getLargeFileService().uploadFragment(uploadSession, range, typedFile);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER);
		}
	}
	
	@Override
	public Object uploadLastFragment(String uploadSession, long start, long end, File file, OneDriveProgressListener listener) throws Exception {
		if (!TextUtils.isEmpty(uploadSession)) {
			if (!isLogin()) {
				throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
			} else {
				String range = String.format(Locale.getDefault(), "bytes %d-%d/%d", start, end, file.length());
				TypedOutput typedFile = new FragmentTypedFile(APPLICATION_OCTET_STREAM, file, start, end, listener);
				return mConnection.getLargeFileService().uploadLastFragment(uploadSession, range, typedFile);
			}
		} else {
			throw new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER);
		}
	}

	@Override
	public void uploadFragmentAsync(String uploadFragment, long start, long end, File file,
			OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (!TextUtils.isEmpty(uploadFragment)) {
			if (isLogin()) {
				String range = String.format(Locale.getDefault(), "bytes %d-%d/%d", start, end, file.length());
				TypedOutput typedFile = new FragmentTypedFile(APPLICATION_OCTET_STREAM, file, start, end, null);
				mConnection.getService().uploadFragment(uploadFragment, range, typedFile, new Callback<UploadSession>() {

					@Override
					public void failure(RetrofitError error) {
						callback.onFailure(error);
					}

					@Override
					public void success(UploadSession object, Response response) {
						callback.onSuccess(object);
					}
					
				});
			} else {
				callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
			}
		} else {
			callback.onFailure(new IllegalArgumentException(OneDriveErrorMessages.EMPTY_PARAMETER));
		}
	}
	

	@Override
	public Object getItemChildren(final String itemId) throws Exception {
		if (isLogin()) {
			return mConnection.getService().getItemChildren(itemId);
		} else {
			throw new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN);
		}
	}

	@Override
	public void getItemChildrenAsync(final String itemId, final OneDriveOperationListener listener) {
		final OneDriveOperationListener callback = listener == null ? NULL_LISTENER : listener;
		if (isLogin()) {
			mConnection.getService().getItemChildren(itemId, new Callback<Item>() {
				
				@Override
				public void failure(RetrofitError error) {
					callback.onFailure(error);
				}

				@Override
				public void success(Item object, Response response) {
					callback.onSuccess(object);
				}
				
			});
		} else {
			callback.onFailure(new OneDriveException(OneDriveErrorMessages.NOT_SIGNIN));
		}
	}

	private boolean isLogin() {
		return mAuth.isLogin();
	}
	
	private static class ProgressTypedByteArray extends TypedByteArray {
		private final long mTotalSize;
		private final OneDriveProgressListener mListener;
		
		public ProgressTypedByteArray(String mimeType, byte[] bytes, final long totalSize, final OneDriveProgressListener listener) {
			super(mimeType, bytes);
			
			mTotalSize = totalSize;
			mListener = listener;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			super.writeTo(new ProgressOutputStream(out, mTotalSize, mListener));
		}
    }
	
	private static class ProgressOutputStream extends FilterOutputStream {
//		private static final int ONE_PIECE = 8 * 1024; //8K
		
		private final long mTotalSize;
		private long mTransferedSize;
		private final OneDriveProgressListener mListener;

		public ProgressOutputStream(OutputStream out, final long totalSize, final OneDriveProgressListener listener) {
			super(out);
			
			mTotalSize = totalSize;
			mTransferedSize = 0;
			mListener = listener;
		}

		@Override
		public void write(byte[] buffer, int offset, int length)
				throws IOException {
//			int bytesTransferred = 0, bytesSending = 0, bytesRemain = 0;
//			while ((bytesRemain = length-bytesTransferred) > 0) {
//				bytesSending = bytesRemain > ONE_PIECE ? ONE_PIECE : bytesRemain;
//				out.write(buffer, offset+bytesTransferred, bytesSending);
//				bytesTransferred += bytesSending;
//				
//				mTransferedSize += bytesSending;
//				notifyListener();
//			}
			
			out.write(buffer, offset, length);
			mTransferedSize += length;
			notifyListener();
		}

		@Override
		public void write(int oneByte) throws IOException {
			out.write(oneByte);
			mTransferedSize += 1;
			notifyListener();
		}

		@Override
		public void write(byte[] buffer) throws IOException {
//			int offset = 0, length = 0 ,remain = 0;
//			while ((remain = buffer.length-offset) > 0) {
//				length = remain > ONE_PIECE ? ONE_PIECE : remain;
//				out.write(buffer, offset, length);
//				offset += length;
//				
//				mTransferedSize += length;
//				notifyListener();
//			}
			
			out.write(buffer);
			mTransferedSize += buffer.length;
			notifyListener();
		}
		
		private void notifyListener() {
			mListener.onProgress(mTotalSize, mTransferedSize);
		}
    }
	
	private static class ProgressTypedFile extends TypedFile {
		
		private final long mTotalSize;
		private final OneDriveProgressListener mListener;
		
		public ProgressTypedFile(String mimeType, File file, final long totalSize, final OneDriveProgressListener listener) {
			super(mimeType, file);
			
			mTotalSize = totalSize;
			mListener = listener;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			super.writeTo(new ProgressOutputStream(out, mTotalSize, mListener));
		}
	}
	
	/**
	 * upload fragment output wrapper
	 */
	private static class FragmentTypedFile implements TypedOutput {
		
		private final static int BUFF_SIZE = 4096;
		
		private long start = -1;
		private long end = -1;
		
		private final OneDriveProgressListener mListener;
		
		private File file;
		
		public FragmentTypedFile(String mimeType, File file, long start, long end, final OneDriveProgressListener listener) {
			this.file = file;
			this.start = start;
			this.end = end;
			this.mListener = listener;
		}

		@Override
		public void writeTo(OutputStream out) throws IOException {
			FileInputStream input = new FileInputStream(file);
			long totalBytes = file.length();
			long offset = 0;
			if(start != -1) {
				input.skip(start);
				offset = start;
			}
			byte[] b = new byte[BUFF_SIZE];
			try {
				while(true) {
					int length = input.read(b);
					if(length == -1) {
						break;
					}
					
					if(end != -1 && (offset + length) > end + 1) {
						length = (int) (end + 1 - offset);
					}
					out.write(b, 0, length);
					offset += length;
					if(mListener != null) {
						mListener.onProgress(totalBytes, offset);
					}
					if(offset == end + 1) {
						out.flush();
						break;
					}
				}
			} catch(Exception e) {
				e.printStackTrace();
				throw new IOException(e);
			} finally {
				input.close();
			}
			
		}
		
		@Override
		public long length() {
			return end + 1 - start ;
		}

		@Override
		public String mimeType() {
			return APPLICATION_OCTET_STREAM;
		}

		@Override
		public String fileName() {
			return file.getName();
		}
		
	}
	
	/**
	 * wrap download's implementation
	 */
	private class DownloadWrapper {
		
		private static final int ERROR_FILE_EXISTED = 0x1;
		private static final int ERROR_CREATE_FOLDER_FAIL = 0x2;
		
		private final static int BUFF_SIZE = 4096;
		
		private TypedInput input;
		private OneDriveProgressListener listener;
		private String saveTo;
		private boolean isCover = false;
		
		DownloadWrapper(String saveTo, TypedInput input, OneDriveProgressListener listener, boolean isCover) {
			this.saveTo = saveTo;
			this.input = input;
			this.listener = listener;
			this.isCover = isCover;
		}
		
		int createFolderIfNotExist() {
			File f = new File(saveTo);
			if(f.exists() && !isCover) {
				return ERROR_FILE_EXISTED;
			}
			
			if(f.exists() && isCover) {
				f.delete();
			}
			
			File parentFile = f.getParentFile();
			if(parentFile != null && !parentFile.exists()) {
				boolean b = parentFile.mkdirs();
				if(!b) {
					return ERROR_CREATE_FOLDER_FAIL;
				}
			}
			return 0;
		}
		
		void start() throws Exception {
			int i = createFolderIfNotExist();
			switch(i) {
			case ERROR_FILE_EXISTED:
				throw new Exception("File existed.");
			case ERROR_CREATE_FOLDER_FAIL:
				throw new Exception("Create Folder failure.");
			}
			
			File f = new File(saveTo);
			FileOutputStream output = new FileOutputStream(f);
			InputStream in = null;
			byte[] buff = new byte[BUFF_SIZE];
			int length = -1;
			long totalSize = 0;
			long progress = 0;
			try {
				in = input.in();
				totalSize = input.length();
				while((length = in.read(buff)) != -1) {
					output.write(buff, 0, length);
					progress += length;
					if(listener != null) {
						listener.onProgress(totalSize, progress);
					}
				}
				if(listener != null) {
					listener.onSuccess(f);
				}
			} catch(Exception e) {
				if(listener != null) {
					listener.onFailure(e);
				}
			} finally {
				output.close();
				if(in != null) {
					in.close();
				}
			}
		}
		
		void startAsync() {
			new Thread() {
				public void run() {
					try {
						start();
					} catch(Exception e) {
						if(listener != null) {
							listener.onFailure(e);
						}
					}
				}
			}.start();
		}
	}
	
}
