// Auto-generated by OneDriveClassify on Wednesday, March 25, 2015
//    from https://api.onedrive.com/v1.0/$metadata

package com.microsoft.onedriveaccess.model;

import java.util.Date;
import java.util.List;

import com.google.gson.annotations.SerializedName;

public class UploadFragmentSession {

   @SerializedName("expirationDateTime")
   public Date ExpirationDateTime;
   @SerializedName("nextExpectedRanges")
   public List<String> NextExpectedRanges;

   public boolean isExpired(Date time) {
	   return ExpirationDateTime.getTime() <= time.getTime();
   }
}
