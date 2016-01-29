package com.microsoft.onedriveaccess.model;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

public class LargeItem {

	@SerializedName("createdBy")
	public IdentitySet CreatedBy;
	@SerializedName("createdDateTime")
	public Date CreatedDateTime;
	@SerializedName("cTag")
	public String CTag;
	@SerializedName("description")
	public String Description;
	@SerializedName("eTag")
	public String ETag;
	@SerializedName("lastModifiedBy")
	public IdentitySet LastModifiedBy;
	@SerializedName("lastModifiedDateTime")
	public Date LastModifiedDateTime;
	@SerializedName("name")
	public String Name;
	@SerializedName("parentReference")
	public ItemReference ParentReference;
	@SerializedName("size")
	public Long Size;
	@SerializedName("webUrl")
	public String WebUrl;
	@SerializedName("file")
	public File File;
	@SerializedName("fileSystemInfo")
	public SystemInfo FileSystemInfo;
}
