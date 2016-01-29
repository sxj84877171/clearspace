package com.microsoft.onedriveaccess;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.android.MainThreadExecutor;
import retrofit.client.OkClient;
import retrofit.converter.Converter;

import com.squareup.okhttp.OkHttpClient;


/**
 * Abstract type defining configurable aspects of the ODConnection
 */
public abstract class AbstractODConnection {
	
	static final int CONNECT_TIMEOUT_MILLIS = 15 * 1000; // 15s
	static final int READ_TIMEOUT_MILLIS = 120 * 1000; // 120s
	static final int MAX_THREAD_NUMBER = 4;
	
	static final int LARGE_FILE_READ_TIMEOUT_MILLIS = 2;//2 hours
	static final int LARGE_FILE_WRITE_TIMEOUT_MILLIS = 2;//2 hours
	
	private final OkClient mHttpClient;
	private final OkClient mLargeFileHttpClient;
	
	private RestAdapter mAdapter;
	
	private RestAdapter mLargeFileAdapter;
	
	public AbstractODConnection() {
		OkHttpClient httpClient = new OkHttpClient();
		httpClient.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		httpClient.setReadTimeout(READ_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		mHttpClient = new OkClient(httpClient);
		
		OkHttpClient largeFileHttpClient = new OkHttpClient();
		largeFileHttpClient.setConnectTimeout(CONNECT_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
		largeFileHttpClient.setReadTimeout(LARGE_FILE_READ_TIMEOUT_MILLIS, TimeUnit.HOURS);
		largeFileHttpClient.setWriteTimeout(LARGE_FILE_WRITE_TIMEOUT_MILLIS, TimeUnit.HOURS);
		mLargeFileHttpClient = new OkClient(largeFileHttpClient);
	}

    /**
     * Creates an instance of the IOneDriveService
     *
     * @return The IOneDriveService
     */
    public IOneDriveService getService() {
    	if (mAdapter == null) {
    		mAdapter = new RestAdapter.Builder()
	            .setLogLevel(getLogLevel())
	            .setClient(mHttpClient)
	            .setEndpoint(getEndpoint())
	            .setConverter(getConverter())
	            .setRequestInterceptor(getInterceptor())
	            .setExecutors(Executors.newFixedThreadPool(MAX_THREAD_NUMBER), new MainThreadExecutor())
	            .build();
    	}
    	
        return mAdapter.create(IOneDriveService.class);
    }
    
    /**
     * Creates an instance of the IOneDriveService
     *
     * @return The IOneDriveService
     */
    public IOneDriveLargeFileService getLargeFileService() {
    	if (mLargeFileAdapter == null) {
    		mLargeFileAdapter = new RestAdapter.Builder()
	            .setLogLevel(getLogLevel())
	            .setClient(mLargeFileHttpClient)
	            .setEndpoint(getEndpoint())
	            .setConverter(getConverter())
	            .setRequestInterceptor(getInterceptor())
	            .setExecutors(Executors.newFixedThreadPool(MAX_THREAD_NUMBER), new MainThreadExecutor())
	            .build();
    	}
    	
        return mLargeFileAdapter.create(IOneDriveLargeFileService.class);
    }
    
    /**
     * The {@link retrofit.RequestInterceptor} to use for this connection
     *
     * @return the interceptor
     */
    protected abstract RequestInterceptor getInterceptor();

    /**
     * The {@link retrofit.RestAdapter.LogLevel} to use for this connection
     *
     * @return the log level
     */
    protected abstract RestAdapter.LogLevel getLogLevel();

    /**
     * The endpoint used by this connection
     *
     * @return the url to use
     */
    protected abstract String getEndpoint();

    /**
     * The {@link retrofit.converter.Converter} to use for parsing webservice responses
     *
     * @return the converter
     */
    protected abstract Converter getConverter();
    
    public OkClient getHttpClient() {
    	return mHttpClient;
    }
}
