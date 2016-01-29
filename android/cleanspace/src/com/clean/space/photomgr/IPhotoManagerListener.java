package com.clean.space.photomgr;

import java.util.List;

public interface IPhotoManagerListener {

	// 删除进度
	public void onDeletePhotosProgress(long deletedSize, double percent);

	// 新增相片
	public void onPhoto(List<?> photos);
	
	// 删除相片
	public void onDeletePhoto(List<?> photos);

	// 新增相片大小
	public void onPhotosSize(long size);

	// 扫描完成
	public void onScanFinished();
	
	// 删除相片完成
	public void onDeleteFinished();
	
}
