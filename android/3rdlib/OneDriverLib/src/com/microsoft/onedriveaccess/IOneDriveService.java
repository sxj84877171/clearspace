package com.microsoft.onedriveaccess;

import com.microsoft.onedriveaccess.model.Drive;
import com.microsoft.onedriveaccess.model.Item;
import com.microsoft.onedriveaccess.model.LargeItem;
import com.microsoft.onedriveaccess.model.Permission;
import com.microsoft.onedriveaccess.model.SharingLink;
import com.microsoft.onedriveaccess.model.UploadFragmentSession;
import com.microsoft.onedriveaccess.model.UploadSession;

import java.util.Map;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.DELETE;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.PATCH;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.QueryMap;
import retrofit.mime.TypedByteArray;
import retrofit.mime.TypedFile;
import retrofit.mime.TypedOutput;

/**
 * Service interface that will connect to OneDrive
 */
public interface IOneDriveService {

    /**
     * The ID of the root folder for a OneDrive
     */
    String ROOT_FOLDER_ID = "root";

    /**
     * Gets the default drive
     * @param driveCallback The callback when the drive has been retrieved
     */
    @GET("/v1.0/drive")
    @Headers("Accept: application/json")
    void getDrive(final Callback<Drive> driveCallback);

    /**
     * Gets the default drive
     */
    @GET("/v1.0/drive")
    @Headers("Accept: application/json")
    Drive getDrive();

    /**
     * Gets the specified drive
     * @param driveId the id of the drive to be retrieved
     * @param driveCallback The callback when the drive has been retrieved
     */
    @GET("/v1.0/drives/{drive-id}")
    @Headers("Accept: application/json")
    void getDrive(@Path("drive-id") final String driveId, final Callback<Drive> driveCallback);

    /**
     * Gets the root of the default drive
     * @param rootCallback The callback when the root has been retrieved
     */
    @GET("/v1.0/drives/root")
    @Headers("Accept: application/json")
    void getMyRoot(final Callback<Item> rootCallback);

    /**
     * Gets shared items
     * @param itemCallback The callback when shared items has been retrieved
     */
    @GET("/v1.0/drive/shared")
    @Headers("Accept: application/json")
    void getSharedItem(final Callback<Item> itemCallback);

    /**
     * Gets shared items
     */
    @GET("/v1.0/drive/shared")
    @Headers("Accept: application/json")
    Item getSharedItem();

    /**
     * Gets an item
     * @param itemId the item id
     * @param itemCallback The callback when the item has been retrieved
     */
    @GET("/v1.0/drive/items/{item-id}/")
    @Headers("Accept: application/json")
    void getItemId(@Path("item-id") final String itemId, final Callback<Item> itemCallback);

    /**
     * Gets an item
     * @param itemId the item id
     */
    @GET("/v1.0/drive/items/{item-id}/")
    @Headers("Accept: application/json")
    Item getItemId(@Path("item-id") final String itemId);

    /**
     * Gets an item with options
     * @param itemId the item id
     * @param options parameter options for this request
     * @param itemCallback The callback when the item has been retrieved
     */
    @GET("/v1.0/drive/items/{item-id}/")
    @Headers("Accept: application/json")
    void getItemId(@Path("item-id") final String itemId,
                   @QueryMap Map<String, String> options,
                   final Callback<Item> itemCallback);
    
    /**
     * Gets an item with options
     * @param itemId the item id
     * @param options parameter options for this request
     */
    @GET("/v1.0/drive/items/{item-id}/")
    @Headers("Accept: application/json")
    Item getItemId(@Path("item-id") final String itemId,
                   @QueryMap Map<String, String> options);
    
    /**
     * Gets an item with options
     * @param path the item path
     * @param options parameter options for this request
     * @param itemCallback The callback when the item has been retrieved
     */
    @GET("/v1.0/drive/root:/{item-path}:/")
    @Headers("Accept: application/json")
    void getItemPath(@Path("item-path") final String path,
                     @QueryMap Map<String, String> options,
                     final Callback<Item> itemCallback);

    /**
     * Gets an item with options
     * @param path the item path
     * @param options parameter options for this request
     */
    @GET("/v1.0/drive/root:/{item-path}:/")
    @Headers("Accept: application/json")
    Item getItemPath(@Path("item-path") final String path,
                   @QueryMap Map<String, String> options);

    /**
     * Deletes an item
     * @param itemId the item id
     * @param callback The callback when the delete has been finished
     */
    @DELETE("/v1.0/drive/items/{item-id}/")
    void deleteItemId(@Path("item-id") final String itemId, final Callback<Response> callback);

    /**
     * Updates an item
     * @param itemId the item id
     * @param updatedItem the updated item
     * @param itemCallback The callback when the item has been retrieved
     */
    @PATCH("/v1.0/drive/items/{item-id}/")
    @Headers("Accept: application/json")
    void updateItemId(@Path("item-id") final String itemId, @Body Item updatedItem, final Callback<Item> itemCallback);

    /**
     * Updates an item
     * @param itemId the item id
     * @param updatedItem the updated item
     */
    @PATCH("/v1.0/drive/items/{item-id}/")
    @Headers("Accept: application/json")
    Item updateItemId(@Path("item-id") final String itemId, @Body Item updatedItem);

    /**
     * Creates a Folder on OneDrive
     * @param itemId the item id
     * @param newItem The item to create
     * @param itemCallback The callback when the item has been retrieved
     */
    @POST("/v1.0/drive/items/{item-id}/children")
    @Headers("Accept: application/json")
    void createFolder(@Path("item-id") final String itemId,
                      @Body Item newItem,
                      final Callback<Item> itemCallback);
    
    /**
     * Creates a Folder on OneDrive
     * @param itemId the item id
     * @param newItem The item to create
     */
    @POST("/v1.0/drive/items/{item-id}/children")
    @Headers("Accept: application/json")
    Object createFolder(@Path("item-id") final String itemId,
                      @Body Item newItem);
    
    /**
     * Creates a Folder on OneDrive
     * @param path the item path
     * @param newItem The item to create
     * @param itemCallback The callback when the item has been retrieved
     */
    @POST("/v1.0/drive/root:/{parent-path}:/children")
    @Headers("Accept: application/json")
    void createFolderRecursion(@Path("parent-path") final String path,
                      @Body Item newItem,
                      final Callback<Item> itemCallback);
    
    /**
     * Creates a Folder on OneDrive
     * @param path the item path
     * @param newItem The item to create
     */
    @POST("/v1.0/drive/root:/{parent-path}:/children")
    @Headers("Accept: application/json")
    Item createFolderRecursion(@Path("parent-path") final String path,
                      @Body Item newItem);

    /**
     * Creates a file on OneDrive
     * @param itemId the item id
     * @param fileName The name of the file that is being created
     * @param fileBody the contents of the file
     * @param itemCallback The callback when the item has been retrieved
     */
    @PUT("/v1.0/drive/items/{item-id}/children/{file-name}/content")
    void createItemId(@Path("item-id") final String itemId,
                      @Path("file-name") final String fileName,
                      @Body TypedByteArray fileBody,
                      final Callback<Item> itemCallback);
    
    /**
     * Creates a file on OneDrive
     * @param itemId the item id
     * @param fileName The name of the file that is being created
     * @param fileBody the contents of the file
     */
    @PUT("/v1.0/drive/items/{item-id}/children/{file-name}/content")
    Item createItemId(@Path("item-id") final String itemId,
                      @Path("file-name") final String fileName,
                      @Body TypedByteArray fileBody);
    
    /**
     * Creates a file on OneDrive
     * @param itemId the item id
     * @param fileName The name of the file that is being created
     * @param file the file
     * @param itemCallback The callback when the item has been retrieved
     */
    @PUT("/v1.0/drive/items/{item-id}/children/{file-name}/content")
    void createItemId(@Path("item-id") final String itemId,
                      @Path("file-name") final String fileName,
                      @Body TypedFile file,
                      final Callback<Item> itemCallback);
    
    /**
     * Creates a file on OneDrive
     * @param itemId the item id
     * @param fileName The name of the file that is being created
     * @param file the file
     */
    @PUT("/v1.0/drive/items/{item-id}/children/{file-name}/content")
    Item createItemId(@Path("item-id") final String itemId,
                      @Path("file-name") final String fileName,
                      @Body TypedFile file);
    
    /**
     * Creates a file on OneDrive
     * @param itemId the item id
     * @param fileName The name of the file that is being created
     * @param options parameter options for this request
     * @param fileBody the contents of the file
     */
    @PUT("/v1.0/drive/items/{item-id}/children/{file-name}/content")
    Item createItemId(@Path("item-id") final String itemId,
                      @Path("file-name") final String fileName,
                      @QueryMap Map<String, String> options,
                      @Body TypedByteArray fileBody);
    
    /**
     * Creates a file on OneDrive
     * @param path the path
     * @param fileName The name of the file that is being created
     * @param fileBody the contents of the file
     * @param itemCallback The callback when the item has been retrieved
     */
    @PUT("/v1.0/drive/root:/{parent-path}/{file-name}:/content")
    void createItemPath(@Path("parent-path") final String path,
                      @Path("file-name") final String fileName,
                      @Body TypedByteArray fileBody,
                      final Callback<Item> itemCallback);
    
    /**
     * Creates a file on OneDrive
     * @param path the path
     * @param fileName The name of the file that is being created
     * @param fileBody the contents of the file
     */
    @PUT("/v1.0/drive/root:/{parent-path}/{file-name}:/content")
    Item createItemPath(@Path("parent-path") final String path,
                      @Path("file-name") final String fileName,
                      @Body TypedByteArray fileBody);
    
    /**
     * Creates a file on OneDrive
     * @param path the path
     * @param fileName The name of the file that is being created
     * @param file the file
     * @param itemCallback The callback when the item has been retrieved
     */
    @PUT("/v1.0/drive/root:/{parent-path}/{file-name}:/content")
    void createItemPath(@Path("parent-path") final String path,
                      @Path("file-name") final String fileName,
                      @Body TypedFile file,
                      final Callback<Item> itemCallback);
    
    /**
     * Creates a file on OneDrive
     * @param path the path
     * @param fileName The name of the file that is being created
     * @param file the file
     */
    @PUT("/v1.0/drive/root:/{parent-path}/{file-name}:/content")
    Item createItemPath(@Path("parent-path") final String path,
                      @Path("file-name") final String fileName,
                      @Body TypedFile file);
    
    @POST("/v1.0/drive/root:/{path_to_item}:/upload.createSession")
    UploadSession createUploadSession(@Path("path_to_item") final String path);
    
    @POST("/v1.0/drive/root:/{path_to_item}:/upload.createSession")
    void createUploadSession(@Path("path_to_item") final String path,
                       final Callback<UploadSession> sessionCallback);

    @POST("/v1.0/drive/items/{parent_item_id}:/{file-name}:/upload.createSession")
    UploadSession createUploadSession(@Path("parent_item_id") final String parentItemId,
                       @Path("file-name") final String fileName);

    /**
     * Creates a large file upload session
     * @param parentItemId The item id
     * @param fileName The name of the file that is being created
     * @param sessionCallback the callback when the session has been created
     */
    @POST("/v1.0/drive/items/{parent_item_id}:/{file-name}:/upload.createSession")
    void createUploadSession(@Path("parent_item_id") final String parentItemId,
                       @Path("file-name") final String fileName,
                       final Callback<UploadSession> sessionCallback);
    
    @PUT("/up/{upload-fragment}")
    UploadFragmentSession uploadFragment(@Path("upload-fragment") final String uploadFragment, 
    				@Header("Content-Range") final String contentRange, 
    				@Body final TypedOutput file);
    
    @PUT("/up/{upload-fragment}")
    LargeItem uploadLastFragment(@Path("upload-fragment") final String uploadFragment, 
			@Header("Content-Range") final String contentRange, 
			@Body final TypedFile file);
    
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
    
    @GET("/v1.0/drive/items/{item-id}/content")
    void download(@Path("item-id") final String itemId, final Callback<Response> callback);
    
    @POST("/v1.0/drive/items/{item-id}/action.createLink")
    void createLink(@Path("item-id") final String itemId, @Body SharingLink link, final Callback<Permission> callback);
    
    @POST("/v1.0/drive/items/{item-id}/action.createLink")
    Permission createLink(@Path("item-id") final String itemId, @Body SharingLink link);
    
    @GET("/v1.0/drive/items/{item-id}/children")
    void getItemChildren(@Path("item-id") final String itemId, final Callback<Item> callback);
    
    @GET("/v1.0/drive/items/{item-id}/children")
    Item getItemChildren(@Path("item-id") final String itemId);
}
