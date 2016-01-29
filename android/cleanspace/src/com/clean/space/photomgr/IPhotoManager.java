package com.clean.space.photomgr;

import java.util.List;

// 获取图片管理接口类
public interface IPhotoManager {

	// 所有数据按照时间排序
	public static String SORT_TYPE_TIME = "date";
	
	// 数据按照大小排序
	public static String SORT_TYPE_SIZE = "size";
	
	// 排序方式
	public static String ORDER_BY_DESC = "desc";
	
	// 排序方式
	public static String ORDER_BY_ASC = "asc";
	
	// 设置监听事件,当异步扫描获取到文件时会通知该监听器
	public void setPhotoManagerListener(IPhotoManagerListener listener);

	// 获取已扫描的文件大小
	public long getScannedPhotoSize();
	
	//开始扫描,是一个异步的过程,需要设置监听器获取异步回调事件
	public boolean startScan(String sortType, String orderType, int photoCount);

	// 停止扫描
	public boolean stopScan();

	// 删除列表
	public boolean deletePhotos(List<?> photos);

	// 停止删除
	public void cancelDelete();
	
	// 设置清空缓存命令
	public void setClearMemoryCache();

	// 同步获取图片列表,耗时比较长,不建议在UI线程调用
	public List<?> getPhotosSync(final String sortType, final String orderType, int photoCount) ;
}
