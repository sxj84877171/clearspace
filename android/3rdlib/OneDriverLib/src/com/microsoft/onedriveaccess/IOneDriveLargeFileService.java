package com.microsoft.onedriveaccess;

import com.microsoft.onedriveaccess.model.LargeItem;
import com.microsoft.onedriveaccess.model.UploadFragmentSession;
import com.microsoft.onedriveaccess.model.UploadSession;

import retrofit.Callback;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedOutput;

/**
 * Service interface that will connect to OneDrive
 */
public interface IOneDriveLargeFileService {

    @POST("/v1.0/drive/items/{item-id}:/{file-name}:/upload.createSession")
    UploadSession createUploadSession(@Path("item-id") final String itemId,
                       @Path("file-name") final String fileName);
    
    @POST("v1.0/drive/root:/{item-path}:/upload.createSession")
    UploadSession createUploadSession(@Path("item-path") final String itemPath);
    
    @PUT("/up/{upload-fragment}")
    UploadFragmentSession uploadFragment(@Path("upload-fragment") final String uploadFragment, 
    				@Header("Content-Range") final String contentRange, 
    				@Body final TypedOutput file);
    
    @PUT("/up/{upload-fragment}")
    LargeItem uploadLastFragment(@Path("upload-fragment") final String uploadFragment, 
			@Header("Content-Range") final String contentRange, 
			@Body final TypedOutput file);
    
    @PUT("/up/{upload-fragment}")
    void uploadFragment(@Path("upload-fragment") final String uploadFragment, 
    				@Header("Content-Range") final String contentRange, 
    				@Body final TypedOutput file,
    				final Callback<UploadSession> callback);
    
    @GET("/up/{upload-fragment}")
    UploadFragmentSession getUploadSessionStatus(@Path("upload-fragment") final String uploadFragment);
    
    @GET("/up/{upload-fragment}")
    void getUploadSessionStatus(@Path("upload-fragment") final String uploadFragment, 
    				final Callback<UploadFragmentSession> callback);
    
    @DELETE("/up/{upload-fragment}")
    int abortUploadSession(@Path("upload-fragment") final String uploadFragment);
    
    @DELETE("/up/{upload-fragment}")
    void abortUploadSession(@Path("upload-fragment") final String uploadFragment, 
    				final Callback<Integer> callback);
}
