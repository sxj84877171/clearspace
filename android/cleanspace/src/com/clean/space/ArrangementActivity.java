package com.clean.space;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.clean.space.log.FLog;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.IPhotoManagerListener;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.protocol.ArrangeImageItem;
import com.clean.space.protocol.ImageItem;
import com.clean.space.util.FileUtils;
import com.clean.space.util.SpaceUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;

public class ArrangementActivity extends Activity implements
		IPhotoManagerListener {

	private final String TAG = "ArrangementActivity";
	private View back;
	private TextView allPhotoDesc;
	private TextView deletePhotoNum;
	private ListView categoryListView;
	private TextView deleteButton;

	List<ImageItem> mListDelImage = new ArrayList<ImageItem>();
	private ArrayList<ImageItem> mSimilarImagesList = new ArrayList<ImageItem>();
	private List<ArrangeImageItem> categoryData;
	List<List<ArrangeImageItem>> mCategoryData = new ArrayList<List<ArrangeImageItem>>();
	private int chooseNum = 0;
	private CategoryAdapter mCategoryAdt = null;
	private long mTotalCheckImageSize = 0;
	public static Map<String, Bitmap> gridviewBitmapCaches = new HashMap<String, Bitmap>();
	private ImageLoader mImageLoader = null;
	private String mSimilarFile = Constants.APP_ROOT_PATH + "similarfile.json";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();

		initListener();

		initData();

		initImageLoader(this);
	}

	private void initData() {

		Intent intent = getIntent();
		long fileSize = intent.getLongExtra("fileSize", 0);
		long fileNumber = intent.getLongExtra("fileNumber", 0);
		String currSize = convertSize(fileSize);
//		allPhotoDesc.setText(String.format(
//				getString(R.string.arrange_all_photo), fileNumber, currSize));

//		deletePhotoNum.setText(String.format(
//				getString(R.string.arrange_delete_photo_num), chooseNum));
		deleteButton.setText(String.format(
				getString(R.string.arrange_delete_button_1), chooseNum * 10
						+ "MB"));

	}

	private String convertSize(long fileSize) {
		String currSize = SpaceUtil.convertSize(fileSize);
		if (fileSize < 1024 * 1024) {
			currSize += getString(R.string.Dwsize_kb);
		} else if (fileSize >= 1024 * 1024 && fileSize < 1024 * 1024 * 1024) {
			currSize += getString(R.string.Dwsize_mb);
		} else {
			currSize += getString(R.string.Dwsize);
		}
		;
		return currSize;
	}

	private void initAdapter() {
		String saveData = FileUtils.readFromFile(mSimilarFile);
		Gson gson = new Gson();
		Type listType = new TypeToken<ArrayList<ArrangeImageItem>>() {
		}.getType();
		// categoryData = gson.fromJson(saveData, listType);

		categoryData.clear();
		if (null == categoryData) {
			categoryData = new ArrayList<ArrangeImageItem>();
		}
		ArrangeImageItem c = new ArrangeImageItem();
		c.setType(1);
		categoryData.add(c);
		// generateTestData();
		mCategoryAdt = new CategoryAdapter(this, categoryData);
		categoryListView.setAdapter(mCategoryAdt);
	}

	private void initListener() {
		back.setOnClickListener(listener);
		deleteButton.setOnClickListener(listener);

	}

	private void initView() {
		setContentView(R.layout.arrangement);

		back = findViewById(R.id.back_layout);
		allPhotoDesc = (TextView) findViewById(R.id.arrage_all_photo);
		deletePhotoNum = (TextView) findViewById(R.id.delete_photo_num);
		categoryListView = (ListView) findViewById(R.id.category_listview);
		deleteButton = (TextView) findViewById(R.id.delete_button);
	}

	private OnClickListener listener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (v.getId() == R.id.back_layout) {
				Intent intent = new Intent(ArrangementActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
			if (v.getId() == R.id.delete_button) {
				deleteButton.setText(R.string.cancle);
				for (ImageItem item : mListDelImage) {
					try {
						if (FileUtils.isFileExist(item.getPath())) {
							FileUtils.deleteFile(item.getPath());
						}
					} catch (Exception e) {
						FLog.e(TAG,
								"OnClickListener delete file " + item.getPath());
					}
				}
				refresh();
			}
		}
	};

	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent(ArrangementActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	};

	class CategoryAdapter extends BaseAdapter {

		Context mContext = null;

		public CategoryAdapter(Context context, List<ArrangeImageItem> datas) {
			if (datas == null)
				throw new NullPointerException();
			this.datas = datas;
			mContext = context;
		}

		private List<ArrangeImageItem> datas;

		@Override
		public int getCount() {
			return datas.size();
		}

		@Override
		public Object getItem(int position) {
			return datas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ArrangeImageItem item = datas.get(position);
			if (item.getType() == 0) {
				convertView = getLayoutInflater().inflate(
						R.layout.arrangement_listview_item, null);
				GridView gridView = (GridView) convertView;
				gridView.setAdapter(new GridViewAdapter(mContext, datas
						.get(position)));
			}
			if (item.getType() > 0) {
				convertView = getLayoutInflater().inflate(
						R.layout.arrange_ca_title, null);
				TextView textView = (TextView) convertView;
				if (item.getType() == 1) {
					textView.setText(R.string.export_similar_message);
				}
				if (item.getType() == 2) {
					// textView.setText(R.string.other_photo);
				}
				if (item.getType() == 3) {
					textView.setText(R.string.all_photo);
				}
			}
			return convertView;
		}

	}

	public class GridViewHolder {
		ImageView imageview_thumbnail;
		ImageView imageViewChoose;
		ImageView favorite;
	}

	class GridViewAdapter extends BaseAdapter {

		private ArrangeImageItem data;
		private LayoutInflater mLayoutInflater = null;
		// 可以根据实际情况修改
		private int width = 250;
		private int height = 250;

		public GridViewAdapter(Context context, ArrangeImageItem data) {
			if (data == null)
				throw new NullPointerException();
			this.data = data;

			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		public int getCount() {
			return 1;
		}

		@Override
		public Object getItem(int position) {
			return data;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			GridViewHolder viewHolder = null;
			if (convertView == null) {
				viewHolder = new GridViewHolder();
				convertView = mLayoutInflater.inflate(
						R.layout.arrange_list_grid_item, null);
				viewHolder.imageview_thumbnail = (ImageView) convertView
						.findViewById(R.id.imageview_thumbnail);
				viewHolder.imageViewChoose = (ImageView) convertView
						.findViewById(R.id.imageview_thumbnail_backgroud);
				viewHolder.favorite = (ImageView) convertView
						.findViewById(R.id.favorite);

				final ImageItem item = data;
				try {
					String path = "file://" + item.getPath();
					if (FileUtils.isFileExist(item.getPath())) {
						viewHolder.imageview_thumbnail
								.setScaleType(ScaleType.CENTER_CROP);
						try {
							DisplayImageOptions options = configDisplayImageOpt();
							mImageLoader.displayImage(path,
									viewHolder.imageview_thumbnail, options);
						} catch (Exception e) {
							FLog.e(TAG, "getView displayimage throw error", e);
						}
					} else {
						viewHolder.imageview_thumbnail.setVisibility(View.GONE);
						viewHolder.imageViewChoose.setVisibility(View.GONE);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				int state = 0;
				if (position == 0) {
					state = data.getState();
				} else {
					state = data.getState();
				}
				if (state == 0) {
					viewHolder.imageViewChoose.setVisibility(View.GONE);
				} else {
					viewHolder.imageViewChoose.setVisibility(View.VISIBLE);
					viewHolder.imageViewChoose
							.setImageResource(R.drawable.btn_checked);
				}

				final GridViewHolder viewHolderTemp = viewHolder;
				viewHolderTemp.imageview_thumbnail
						.setOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {
								if (viewHolderTemp.imageViewChoose
										.getVisibility() == View.GONE) {
									viewHolderTemp.imageViewChoose
											.setVisibility(View.VISIBLE);
									viewHolderTemp.imageViewChoose
											.setImageResource(R.drawable.btn_checked);
									mTotalCheckImageSize += item.getSize();
									chooseNum++;
									mListDelImage.add(item);
								} else {
									viewHolderTemp.imageViewChoose
											.setVisibility(View.GONE);
									chooseNum--;
									mTotalCheckImageSize -= item.getSize();
									mListDelImage.remove(item);
								}
//								deletePhotoNum.setText(String
//										.format(getString(R.string.arrange_delete_photo_num),
//												chooseNum));

								deleteButton.setText(String
										.format(getString(R.string.arrange_delete_button_1),
												mTotalCheckImageSize / 1024
														/ 1024 + "MB"));
								if (position == 0) {
									data.setState(data.getState() == 0 ? 1 : 0);
								} else {
									int state = data.getState() == 0 ? 1 : 0;
									data.setState(state);
								}
							}
						});
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (GridViewHolder) convertView.getTag();
			}
			return convertView;
		}

		private DisplayImageOptions configDisplayImageOpt() {
			DisplayImageOptions options;
			options = new DisplayImageOptions.Builder()
					.showImageOnFail(R.drawable.ic_launcher) // 设置图片加载/解码过程中错误时候显示的图片
					.cacheInMemory(true)// 设置下载的图片是否缓存在内存中
					.cacheOnDisc(true)// 设置下载的图片是否缓存在SD卡中
					.considerExifParams(true) // 是否考虑JPEG图像EXIF参数（旋转，翻转）
					.imageScaleType(ImageScaleType.EXACTLY_STRETCHED)// 设置图片以如何的编码方式显示
					.bitmapConfig(Bitmap.Config.RGB_565)// 设置图片的解码类型//
					.resetViewBeforeLoading(true)// 设置图片在下载前是否重置，复位
					.displayer(new RoundedBitmapDisplayer(20))// 是否设置为圆角，弧度为多少
					.displayer(new FadeInBitmapDisplayer(100))// 是否图片加载好后渐入的动画时间
					.build();// 构建完成
			return options;
		}

	}

	private void startFindSimilarImage() {
		try {
			IPhotoManager engine = PhotoManagerFactory.getInstance(this,
					PhotoManagerFactory.PHOTO_MGR_SIMILAR);
			engine.setPhotoManagerListener(this);
			engine.startScan(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_ASC, 0);
		} catch (Exception e) {
			FLog.e(TAG, "scanPC throw error", e);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				FLog.i(TAG, "OpenCV loaded successfully");
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onResume() {
		super.onResume();

		if (!OpenCVLoader.initDebug()) {
			FLog.d(TAG,
					"Internal OpenCV library not found. Using OpenCV Manager for initialization");
			OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this,
					mLoaderCallback);
		} else {
			FLog.d(TAG, "OpenCV library found inside package. Using it!");
			mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
		}
		initAdapter();
		startFindSimilarImage();
	}

	// @Override
	// public void onPhotos(final ImageItem item) {
	// runOnUiThread(new Runnable() {
	// public void run() {
	// addSimilarImage(item);
	// }
	// });
	//
	// }

	private void addSimilarImage(ImageItem item) {
		try {
			if (item != null) {
				int index = mSimilarImagesList.indexOf(item);
				if (index < 0) {
					mSimilarImagesList.add(item);
					ArrangeImageItem similar = new ArrangeImageItem(item);
					if (!categoryData.contains(similar)) {
						similar.setHist(null);
						categoryData.add(similar);
					}
				}
			}

			refresh();
		} catch (Exception e) {
			FLog.e(TAG, "addSimilarImage throw error!", e);
		}
	}

	private void refresh() {
		// do something
		mCategoryAdt.notifyDataSetChanged();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();

		// saveSimilarImage();
	}

	private void saveSimilarImage() {
		Gson gson = new Gson();
		String json = gson.toJson(categoryData);
		FileUtils.writeFile(mSimilarFile, json);
	}

	// @Override
	// public void findSimilarFinish(final List<ImageItem> otherImage) {
	// runOnUiThread(new Runnable() {
	// public void run() {
	// if (null != otherImage) {
	// ArrangeImageItem c1 = new ArrangeImageItem();
	// c1.setType(2);
	// categoryData.add(c1);
	//
	// ArrangeImageItem images = new ArrangeImageItem();
	// for (ImageItem item : otherImage) {
	// item.setHist(null);
	// images.getSimilarList().add(item);
	// }
	// if (!categoryData.contains(images)) {
	// categoryData.add(images);
	// }
	// refresh();
	// }
	// }
	// });
	// }

	public void initImageLoader(Context context) {
		ImageLoaderConfiguration.Builder config = new ImageLoaderConfiguration.Builder(
				context);
		config.threadPriority(Thread.NORM_PRIORITY - 2);
		config.denyCacheImageMultipleSizesInMemory();
		config.discCacheSize(100 * 1024 * 1024); //
		config.tasksProcessingOrder(QueueProcessingType.LIFO);
		// config.writeDebugLogs(); // Remove for release app

		mImageLoader = ImageLoader.getInstance();

		// Initialize ImageLoader with configuration.
		mImageLoader.init(config.build());
	}

	@Override
	public void onDeletePhotosProgress(long deletedSize, double percent) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPhoto(List<?> photos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeletePhoto(List<?> photos) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onScanFinished() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onPhotosSize(long size) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onDeleteFinished() {
		// TODO Auto-generated method stub

	}
}
