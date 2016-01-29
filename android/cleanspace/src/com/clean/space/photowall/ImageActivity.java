package com.clean.space.photowall;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.clean.space.CleanPhotoFragment;
import com.clean.space.ExportPhotoFragment;
import com.clean.space.R;
import com.clean.space.ThumbnailAdapter;
import com.clean.space.log.FLog;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;
import com.clean.space.protocol.SimilarImageItem;
import com.clean.space.util.FileUtils;

/**
 * 可以左右滑动的照片墙
 */
public class ImageActivity extends Activity implements OnPageChangeListener {

	private static final String TAG = ImageActivity.class.getSimpleName();

	/**
	 * 用于管理图片的滑动
	 */
	private ViewPager viewPager;

	/**
	 * 显示当前图片的页数
	 */
	private TextView pageText;
//	private ExportedPhotoManager mExportedPhotoMgr;

	private List<Object> list = new ArrayList<Object>();
	private List<ViewItem> mList = new ArrayList<ViewItem>();

	private int imageType;

	// private SimilarPhotoManager mSimilarPhotoMgr;
	// private UnexportPhotoManager mAlbumPhotoMgr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();

		initData();
	}

	private void initView() {
		setContentView(R.layout.activity_image);

		pageText = (TextView) findViewById(R.id.page_text);
		viewPager = (ViewPager) findViewById(R.id.view_pager);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return super.onTouchEvent(event);

	}

	private void initData() {
		final ViewPagerAdapter adapter = new ViewPagerAdapter();
		final int imagePosition = getIntent().getIntExtra("image_position", 0);
		imageType = getIntent().getIntExtra("image_type", 0);

		FLog.d(TAG, "imagePosition: " + imagePosition);

		if (imageType == CleanPhotoFragment.TYPE_CURRENT_EXPORTED) {
			FLog.d(TAG, "imageType: " + imageType);

//			mExportedPhotoMgr = (ExportedPhotoManager) PhotoManagerFactory
//					.getInstance(this, PhotoManagerFactory.PHOTO_MGR_EXPORTED);
//
//			mExportedPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
//					IPhotoManager.ORDER_BY_ASC, 0);
			
			if (list != null) {
				list.clear();
			}
			list = CleanPhotoFragment.listCurrExp;
			System.out.println(list.size());
//			list = (List<Object>) mExportedPhotoMgr
//					.getCurrenExportedPhotosSync(IPhotoManager.SORT_TYPE_TIME,
//							IPhotoManager.ORDER_BY_ASC, 0);
			loop();

		} else if (imageType == ThumbnailAdapter.TYPE_CLEAR) {
//			mExportedPhotoMgr = (ExportedPhotoManager) PhotoManagerFactory
//					.getInstance(this, PhotoManagerFactory.PHOTO_MGR_EXPORTED);
//			FLog.d(TAG, "导出的所有照片:imageType: " + imageType);
//			mExportedPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
//					IPhotoManager.ORDER_BY_ASC, 0);

			if (list != null) {
				list.clear();
			}

			list = CleanPhotoFragment.listAllExp;
			
//			list = (List<Object>) mExportedPhotoMgr
//					.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
//							IPhotoManager.ORDER_BY_ASC, 0);
			loop();

		} else if (imageType == 5) {
			// mSimilarPhotoMgr = (SimilarPhotoManager) PhotoManagerFactory
			// .getInstance(this, PhotoManagerFactory.PHOTO_MGR_SIMILAR);
			if (list != null) {
				list.clear();
			}

			list = ExportPhotoFragment.similarList;
			FLog.d(TAG, TAG + "-------------相似照片5: " + list.size());

			if (mList != null) {
				mList.clear();
			}

			for (int i = 0; i < list.size(); i++) {
				SimilarImageItem item = (SimilarImageItem) list.get(i);
				ViewItem viewItem = new ViewItem();
				if (FileUtils.isFileExist(item.getPath())) {
					viewItem.setPath(item.getPath());
					if (!mList.contains(viewItem)) {
						mList.add(viewItem);
					}
				} else {
					Log.w(TAG, "文件不存在: " + item.getPath());
				}
			}
			// mSimilarPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
			// IPhotoManager.ORDER_BY_ASC, 0);

		} else if (imageType == 4) {
			// mAlbumPhotoMgr = (UnexportPhotoManager) PhotoManagerFactory
			// .getInstance(this, PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);

			if (list != null) {
				list.clear();
			}

			list = ExportPhotoFragment.albumList;

			FLog.d(TAG, TAG + "---------相册照片4: " + list.size());

			if (mList != null) {
				mList.clear();
			}

			for (int i = 0; i < list.size(); i++) {
				FileItem item = (FileItem) list.get(i);
				ViewItem viewItem = new ViewItem();
				if (FileUtils.isFileExist(item.getPath())) {
					viewItem.setPath(item.getPath());
					if (!mList.contains(viewItem)) {
						mList.add(viewItem);
					}
				} else {
					Log.w(TAG, "文件不存在: " + item.getPath());
				}
			}

			// mAlbumPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
			// IPhotoManager.ORDER_BY_ASC, 0);
		} else {
			FLog.d(TAG, "类型出错了,imageType: " + imageType);

		}
		setAdapter(adapter, imagePosition);
	}

	/** 遍历添加数据 */
	private void loop() {

		if (mList != null) {
			mList.clear();
		}

		for (int i = 0; i < list.size(); i++) {
			ExportedImageItem item = (ExportedImageItem) list.get(i);
			ViewItem viewItem = new ViewItem();
			if (FileUtils.isFileExist(item.getPath())) {
				viewItem.setPath(item.getPath());
				if (!mList.contains(viewItem)) {
					mList.add(viewItem);
				}
			} else {
				Log.w(TAG, "文件不存在: " + item.getPath());
			}
		}
	}

	private void setAdapter(final ViewPagerAdapter adapter, int imagePosition) {
		viewPager.setAdapter(adapter);
		viewPager.setCurrentItem(imagePosition);
		viewPager.setOnPageChangeListener(this);
		viewPager.setEnabled(false);
		// 设定当前的页数和总页数
		pageText.setText((imagePosition + 1) + "/" + mList.size());
	}

	/**
	 * TODO<ViewPager的适配器>
	 */
	class ViewPagerAdapter extends PagerAdapter {

		@SuppressLint("InflateParams")
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// String imagePath = getImagePath(Images.imageUrls[position]);
			String imagePath = mList.get(position).getPath();
			// Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

			Bitmap bm;

			bm = getCompressedBitmap(imagePath);

			View view = LayoutInflater.from(ImageActivity.this).inflate(
					R.layout.image_layout, null);
			OnlyImageView onlyImageView = (OnlyImageView) view
					.findViewById(R.id.image_view);
			onlyImageView.setImageBitmap(bm);
			container.addView(view, 0);
			return view;
		}

		@SuppressWarnings("deprecation")
		private Bitmap getCompressedBitmap(String imagePath) {
			Bitmap bm;
			BitmapFactory.Options opt = new BitmapFactory.Options();
			// 获取一个不占内存的bitmap -> 获取到这个图片的原始宽度和高度
			opt.inJustDecodeBounds = true;
			bm = BitmapFactory.decodeFile(imagePath, opt);

			// 获取到这个图片的原始宽度和高度
			int picWidth = opt.outWidth;
			int picHeight = opt.outHeight;

			// 获取屏的宽度和高度
			WindowManager windowManager = getWindowManager();
			Display display = windowManager.getDefaultDisplay();
			int screenWidth = display.getWidth();
			int screenHeight = display.getHeight();

			// isSampleSize是表示对图片的缩放程度，2 -> 1/2
			opt.inSampleSize = 1;
			// 根据屏的大小和图片大小计算出缩放比例
			if (picWidth > picHeight) {
				if (picWidth > screenWidth)
					opt.inSampleSize = (int) (picWidth / screenWidth + 3.5f);
			} else {
				if (picHeight > screenHeight)

					opt.inSampleSize = (int) (picHeight / screenHeight + 3.5f);
			}
			// System.out.println("opt.inSampleSize: " +opt.inSampleSize);
			// 生成一个真正带有缩放的Bitmap
			opt.inJustDecodeBounds = false;

			try {
				bm = BitmapFactory.decodeFile(imagePath, opt);

				if (bm == null) {
					bm = BitmapFactory.decodeResource(getResources(),
							R.drawable.ic_launcher, opt);
				}

			} catch (Exception e) {
				Log.e(TAG, "bitmap///////存溢出了", e);
			}
			return bm;
		}

		@Override
		public int getCount() {
			return mList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View view = (View) object;
			container.removeView(view);
		}
	}

	@Override
	public void onPageScrollStateChanged(int arg0) {

	}

	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {

	}

	@Override
	public void onPageSelected(int currentPage) {
		// 每当页数发生改变时重新设定一遍当前的页数和总页数
		pageText.setText((currentPage + 1) + "/" + mList.size());
	}

	/**
	 * 获取图片的本地存储路径。
	 * 
	 * @param imageUrl
	 *            图片的URL地址。
	 * @return 图片的本地存储路径。
	 */
	@SuppressWarnings("unused")
	private String getImagePath(String imageUrl) {
		int lastSlashIndex = imageUrl.lastIndexOf("/");
		String imageName = imageUrl.substring(lastSlashIndex + 1);
		String imageDir = Environment.getExternalStorageDirectory().getPath()
				+ "/PhotoWallFalls/";
		File file = new File(imageDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		String imagePath = imageDir + imageName;
		return imagePath;
	}

	@Override
	public void onBackPressed() {
		finish();
	}
}
