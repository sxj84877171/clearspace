package com.clean.space;

import android.content.Context;
import android.graphics.Bitmap;

import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class ImageLoaderManager {
	private static ImageLoader mImageLoader = null;
	private static DisplayImageOptions mOptions = null;
	
	public static ImageLoader getImageLoader(Context context) {
		if (mImageLoader == null) {
			ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(
					context);
			config.threadPriority(Thread.NORM_PRIORITY - 2);
			config.denyCacheImageMultipleSizesInMemory();
			config.discCacheSize(100 * 1024 * 1024); //
			config.memoryCacheExtraOptions(1600, 2400);
			config.memoryCache(new LruMemoryCache(10 * 1024 * 1024));
			config.tasksProcessingOrder(QueueProcessingType.LIFO);

			mImageLoader = ImageLoader.getInstance();
			mImageLoader.init(config.build());
		}
		
		return mImageLoader;
	}

	public static DisplayImageOptions getImageOptions() {
		if (mOptions == null) {
			mOptions = new DisplayImageOptions.Builder()
					.showImageOnLoading(R.drawable.loading_image)
					.showImageOnFail(R.drawable.load_image_fail) // 设置图片加载/解码过程中错误时候显示的图片
					.cacheInMemory(false)// 设置下载的图片是否缓存在内存中
					.cacheOnDisc(false)// 设置下载的图片是否缓存在SD卡中
					.considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
					.imageScaleType(ImageScaleType.EXACTLY)// 设置图片以如何的编码方式显示
					.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
					.resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
					.build();// 构建完成
		}
		return mOptions;
	}
}
