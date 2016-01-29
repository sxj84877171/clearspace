package com.clean.space.photomgr;

import android.content.Context;

public class PhotoManagerFactory {
	private final String TAG = "PhotoManagerFactory";
	public final static int PHOTO_MGR_ALL = 100;
	public final static int PHOTO_MGR_EXPORTED = 101;
	public final static int PHOTO_MGR_UNEXPORTED = 102;
	public final static int PHOTO_MGR_SIMILAR = 103;
	private static AllPhotoManager mAllPhotoMgr = null;
	private static ExportedPhotoManager mExportedPhotoMgr = null;
	private static UnexportPhotoManager mUnexportPhotoMgr = null;
	private static SimilarPhotoManager mSimilarPhotoMgr = null;
	/*
	 * 修改管理photo的类,把photo对象都变成静态的,避免每次调用startscan都重新去扫描
	 * (修改为:当用户调用的时候startscan,获取上次的listimage,遍历判断文件是否存在,如果不存在则删除该对象,而后通知UI,同时启动扫描线程,不停的扫描文件,获取新增的文件,完成后,通知UI).
	 */
	public static IPhotoManager getInstance(Context context, int mrgtype) {  
		switch(mrgtype){
		// 所有相片
		case PHOTO_MGR_ALL:
		{
			if(null == mAllPhotoMgr){
				mAllPhotoMgr = new AllPhotoManager(context);
			}
			return mAllPhotoMgr;
		}
            
		// 获取已经导出的相片
		case PHOTO_MGR_EXPORTED:
		{
			if(null == mExportedPhotoMgr){
				mExportedPhotoMgr = new ExportedPhotoManager(context);
			}
			return mExportedPhotoMgr;
		}
            
        // 获取未导出的相片
		case PHOTO_MGR_UNEXPORTED:
		{
			if(null == mUnexportPhotoMgr){
				mUnexportPhotoMgr = new UnexportPhotoManager(context);
			}
			return mUnexportPhotoMgr;
		}
        // 获取相似相片
		case PHOTO_MGR_SIMILAR:
		{
			if(null == mSimilarPhotoMgr){
				mSimilarPhotoMgr = new SimilarPhotoManager(context);
			}
			return mSimilarPhotoMgr;
		}
		default:
			break;
		
		}
        return null;
	}
}
