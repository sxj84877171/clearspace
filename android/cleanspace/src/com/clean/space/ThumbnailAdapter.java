package com.clean.space;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;
import com.clean.space.protocol.ItemComparator;
import com.clean.space.protocol.SimilarImageItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.ui.listener.AdapterListener;
import com.clean.space.util.FileUtil;
import com.clean.space.util.FileUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ThumbnailAdapter extends ArrayAdapter<Object> implements
		OnScrollListener {
	private static final String TAG = ThumbnailAdapter.class.getSimpleName();

	public static final int TYPE_CLEAR = 1;
	public static final int TYPE_SIMILAR = 2;
	public static final int TYPE_ALBUM = 3;

	private Context mContext;
	private int mResource;
	private int mType;
	private List<Object> mList;
	private Map<String, Boolean> mSelectedMap;
	private List<String> mClickedList;
	private boolean mIsMultiple;
	private int mItemWidth;
	private int mItemHeight;
	private AdapterListener mListener;
	private boolean mIsLongClick;

	private static ImageLoader mImageLoader;
	private static DisplayImageOptions mOptions;

	public ThumbnailAdapter(Context context, int resource, int type) {
		super(context, resource);

		mContext = context;
		mResource = resource;
		mType = type;

		mList = new ArrayList<Object>();
		mSelectedMap = new HashMap<String, Boolean>();
		mClickedList = new ArrayList<String>();
		mIsMultiple = false;

		mIsLongClick = false;

		initImageLoader(mContext);
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public Object getItem(int position) {
		return mList.get(position);
	}

	public List<Object> getAllItems() {
		return new ArrayList<Object>(mList);
	}

	private class ViewHolder {
		ImageView selectMark;
		ImageView image;
		ImageView image_computer;
		ImageView image_yun;
		LinearLayout export;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = (View) LayoutInflater.from(mContext).inflate(
					mResource, null);
			viewHolder = new ViewHolder();
			viewHolder.image = (ImageView) convertView.findViewById(R.id.image);
			viewHolder.selectMark = (ImageView) convertView
					.findViewById(R.id.checkbox);
			viewHolder.image_computer = (ImageView) convertView
					.findViewById(R.id.export_computer);
			viewHolder.image_yun = (ImageView) convertView
					.findViewById(R.id.export_yun);
			viewHolder.export = (LinearLayout) convertView
					.findViewById(R.id.export);

			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final ImageView image = viewHolder.image;
		final ImageView selectMark = viewHolder.selectMark;

		final ImageView image_computer = viewHolder.image_computer;
		final ImageView image_yun = viewHolder.image_yun;
		final LinearLayout export = viewHolder.export;

		final LayoutParams params = new LayoutParams(mItemWidth, mItemHeight);

		convertView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					String path = "";
					switch (mType) {
					case TYPE_CLEAR: {
						ExportedImageItem item = (ExportedImageItem) mList
								.get(position);
						path = item.getPath();
						break;
					}
					case TYPE_SIMILAR: {
						SimilarImageItem item = (SimilarImageItem) mList
								.get(position);
						path = item.getPath();
						break;
					}
					case TYPE_ALBUM: {
						FileItem item = (FileItem) mList.get(position);
						path = item.getPath();
						break;
					}
					}

					mListener.onItemLongClick(path, position, mType);

					// statistics
					String eventid = Constants.UMENG.ARRANGE_PHOTO.SHOW_BIG_PICTURE;
					StatisticsUtil.getInstance(mContext,
							StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
				}
				// mIsLongClick = true;
				// return false;
			}
		});

		switch (mType) {
		case TYPE_CLEAR: {
			final ExportedImageItem item = (ExportedImageItem) mList
					.get(position);
			setImageThumbnail(image, item.getPath(), item);

			params.setMargins(10, 10, 10, 10);
			image.setLayoutParams(params);

			export.setVisibility(View.VISIBLE);
			if (item.isSaveOneDrive()) {
				image_yun.setVisibility(View.VISIBLE);
			} else {
				image_yun.setVisibility(View.GONE);
			}

			if (item.isSavePcClient()) {
				image_computer.setVisibility(View.VISIBLE);
			} else {
				image_computer.setVisibility(View.GONE);
			}

			if (mIsMultiple) {
				selectMark.setVisibility(View.VISIBLE);

				if (mSelectedMap.get(item.getPath())) {
					selectMark
							.setBackgroundResource(R.drawable.btn_checked_big);
				} else {
					selectMark
							.setBackgroundResource(R.drawable.btn_checked_big_not);
				}
			} else {
				selectMark
						.setBackgroundResource(R.drawable.btn_checked_big_not);
				// selectMark.setVisibility(View.GONE);
			}

			selectMark.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// if (mIsLongClick) {
					// mIsLongClick = false;
					// return;
					// }

					setMultipleSelectMode(true);
					itemClick(position);

					if (mSelectedMap.get(item.getPath())) {
						selectMark
								.setBackgroundResource(R.drawable.btn_checked_big);
					} else {
						selectMark
								.setBackgroundResource(R.drawable.btn_checked_big_not);
					}
				}
			});
			break;
		}
		case TYPE_SIMILAR: {
			selectMark.setVisibility(View.VISIBLE);
			final SimilarImageItem item = (SimilarImageItem) mList
					.get(position);
			setImageThumbnail(image, item.getPath(), item);

			params.setMargins(10, 10, 10, 10);
			image.setLayoutParams(params);

			if (mSelectedMap.get(item.getPath())) {
				selectMark.setImageResource(R.drawable.btn_checked_big);
			} else {
				selectMark
						.setBackgroundResource(R.drawable.btn_checked_big_not);
			}

			selectMark.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// if (mIsLongClick) {
					// mIsLongClick = false;
					// return;
					// }
					if (!mSelectedMap.get(item.getPath())) {
						selectMark.setImageResource(R.drawable.btn_checked_big);
						// selectMark.setVisibility(View.VISIBLE);
					} else {
						selectMark
								.setImageResource(R.drawable.btn_checked_big_not);
						// selectMark.setVisibility(View.GONE);
					}

					itemClick(position);
				}
			});

			break;
		}
		case TYPE_ALBUM: {
			selectMark.setVisibility(View.VISIBLE);
			final FileItem item = (FileItem) mList.get(position);

			setImageThumbnail(image, item.getPath(), item);
			params.setMargins(10, 10, 10, 10);
			image.setLayoutParams(params);

			if (mSelectedMap.get(item.getPath()) == null) {
				Log.i(TAG,
						"path:" + item.getPath() + ", mlist size:"
								+ mList.size() + ", map size:"
								+ mSelectedMap.size());
			}
			if (mSelectedMap.get(item.getPath())) {
				selectMark.setImageResource(R.drawable.btn_checked_big);
			} else {
				selectMark.setImageResource(R.drawable.btn_checked_big_not);
				// selectMark.setVisibility(View.GONE);
			}

			selectMark.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// if (mIsLongClick) {
					// mIsLongClick = false;
					// return;
					// }

					if (!mSelectedMap.get(item.getPath())) {
						selectMark.setImageResource(R.drawable.btn_checked_big);
						// selectMark.setVisibility(View.VISIBLE);

					} else {
						// selectMark.setVisibility(View.GONE);
						selectMark
								.setImageResource(R.drawable.btn_checked_big_not);

					}

					// Log.e(TAG, "date:" + item.getDate());

					itemClick(position);
				}
			});

			break;
		}
		}
		return convertView;
	}

	private HashMap<Integer, View> viewMap;

	private void initImageLoader(Context context) {
		mImageLoader = ImageLoaderManager.getImageLoader(getContext());
		mOptions = ImageLoaderManager.getImageOptions();
	}

	private void setImageThumbnail(ImageView imageView, String filePath,
			FileItem item) {
		try {
			String path = "file://" + filePath;
			if (FileUtils.isFileExist(filePath)) {
				imageView.setScaleType(ScaleType.CENTER_CROP);
				try {
//					if (item.getRef() != null && item.getRef().get() != null) {
//						imageView.setImageBitmap(item.getRef().get());
//					} else {
						mImageLoader.displayImage(path, imageView, mOptions,
								new ImageLoadingListenerImpl(item));
//					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				Log.e(TAG, "file not exist:" + path);
				imageView.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setList(List<Object> objects) {
		if (objects == null) {
			return;
		}

		mList.clear();
		// mList.addAll(objects);

		mSelectedMap.clear();
		switch (mType) {
		case TYPE_CLEAR: {
			for (int i = 0; i < objects.size(); i++) {
				ExportedImageItem item = (ExportedImageItem) objects.get(i);
				if (FileUtils.isFileExist(item.getPath())) {
					mList.add(objects.get(i));
					mSelectedMap.put(item.getPath(), false);
				} else {
					Log.e(TAG, "Clear, file not exist:" + item.getPath());
				}
			}
			break;
		}
		case TYPE_SIMILAR: {
			for (int i = 0; i < objects.size(); i++) {
				SimilarImageItem item = (SimilarImageItem) objects.get(i);
				if (FileUtils.isFileExist(item.getPath())) {
					mList.add(objects.get(i));
					mSelectedMap.put(item.getPath(), false);
				} else {
					Log.e(TAG, "Similar, file not exist:" + item.getPath());
				}
			}
			break;
		}
		case TYPE_ALBUM: {
			for (int i = 0; i < objects.size(); i++) {
				FileItem item = (FileItem) objects.get(i);
				if (FileUtils.isFileExist(item.getPath())) {
					mList.add(objects.get(i));
					mSelectedMap.put(item.getPath(), false);
				} else {
					Log.e(TAG, "Album, file not exist:" + item.getPath());
				}
			}
			break;
		}
		}

		notifyDataSetChanged();
	}

	public void addObjects(List<Object> objects) {
		if (objects == null) {
			return;
		}

		if (mList != null) {
			mList.clear();
		}

		/*
		 * for (int i = 0; i < objects.size(); i++) { mList.add(objects.get(i));
		 * }
		 */

		switch (mType) {
		case TYPE_CLEAR: {
			for (int i = 0; i < objects.size(); i++) {
				ExportedImageItem item = (ExportedImageItem) objects.get(i);
				if (FileUtils.isFileExist(item.getPath())) {
					mList.add(objects.get(i));
					mSelectedMap.put(item.getPath(), false);
				} else {
					Log.e(TAG, "Clear, file not exist:" + item.getPath());
				}
			}
			break;
		}
		case TYPE_SIMILAR: {
			for (int i = 0; i < objects.size(); i++) {
				SimilarImageItem item = (SimilarImageItem) objects.get(i);
				if (FileUtils.isFileExist(item.getPath())) {
					mList.add(objects.get(i));
					mSelectedMap.put(item.getPath(), false);
				} else {
					Log.e(TAG, "Similar, file not exist:" + item.getPath());
				}
			}
			break;
		}
		case TYPE_ALBUM: {
			for (int i = 0; i < objects.size(); i++) {
				FileItem item = (FileItem) objects.get(i);
				if (FileUtils.isFileExist(item.getPath())) {
					mList.add(objects.get(i));
					mSelectedMap.put(item.getPath(), false);
				} else {
					Log.e(TAG, "Album, file not exist:" + item.getPath());
				}
			}
			break;
		}
		}

		// notifyDataSetChanged();
	}

	public void deleteSelectedPhoto() {
		Iterator<Object> iteratorValue = mList.iterator();

		boolean isDeleted = false;
		while (iteratorValue.hasNext()) {
			switch (mType) {
			case TYPE_CLEAR: {
				ExportedImageItem item = (ExportedImageItem) iteratorValue
						.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					mSelectedMap.remove(item.getPath());
					iteratorValue.remove();

					isDeleted = true;
				}
				break;
			}
			case TYPE_SIMILAR: {
				SimilarImageItem item = (SimilarImageItem) iteratorValue.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					mSelectedMap.remove(item.getPath());
					iteratorValue.remove();

					isDeleted = true;
				}
				break;
			}
			case TYPE_ALBUM: {
				FileItem item = (FileItem) iteratorValue.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					mSelectedMap.remove(item.getPath());
					iteratorValue.remove();

					isDeleted = true;
				}
				break;
			}
			}
		}

		// if (isDeleted) {
		notifyDataSetChanged();
		// }
	}

	public void deletePhotoes(List<Object> list, boolean isNotify) {
		Log.w(TAG, "delete size:" + list.size());

		boolean isDeleted = false;
		switch (mType) {
		case TYPE_CLEAR: {
			for (Object object : list) {
				Iterator<Object> iterator = mList.iterator();
				while (iterator.hasNext()) {
					ExportedImageItem item = (ExportedImageItem) iterator
							.next();
					ExportedImageItem deleteItem = (ExportedImageItem) object;
					if (item.getPath().equals(deleteItem.getPath())) {
						Log.w(TAG, "delete file:" + deleteItem.getPath());
						mSelectedMap.remove(deleteItem.getPath());
						iterator.remove();

						isDeleted = true;
						break;
					}
				}
			}

			break;
		}
		case TYPE_SIMILAR: {
			for (Object object : list) {
				Iterator<Object> iterator = mList.iterator();
				while (iterator.hasNext()) {
					SimilarImageItem item = (SimilarImageItem) iterator.next();
					SimilarImageItem deleteItem = (SimilarImageItem) object;
					if (item.getPath().equals(deleteItem.getPath())) {
						mSelectedMap.remove(deleteItem.getPath());
						iterator.remove();

						isDeleted = true;
						break;
					}
				}
			}
			break;
		}
		case TYPE_ALBUM: {
			for (Object object : list) {
				Iterator<Object> iterator = mList.iterator();
				while (iterator.hasNext()) {
					FileItem item = (FileItem) iterator.next();
					FileItem deleteItem = (FileItem) object;
					if (item.getPath().equals(deleteItem.getPath())) {
						mSelectedMap.remove(deleteItem.getPath());
						iterator.remove();
						isDeleted = true;
						break;
					}
				}
			}

			break;
		}
		}

		// if (isDeleted && isNotify) {
		notifyDataSetChanged();
		// }
	}

	public void deleteSimilarPhotoesById(List<String> list, boolean isNotify) {
		boolean isDeleted = false;
		for (String path : list) {
			Iterator<Object> iterator = mList.iterator();
			while (iterator.hasNext()) {
				SimilarImageItem item = (SimilarImageItem) iterator.next();
				if (item.getPath().equals(path)) {
					iterator.remove();
					mSelectedMap.remove(path);
					isDeleted = true;
					break;
				}
			}
		}
		// if (isDeleted && isNotify) {
		notifyDataSetChanged();
		// }
	}

	public long getSelectedSize() {
		long totalSize = 0;
		Iterator<Object> iteratorValue = mList.iterator();

		while (iteratorValue.hasNext()) {
			switch (mType) {
			case TYPE_CLEAR: {
				ExportedImageItem item = (ExportedImageItem) iteratorValue
						.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					totalSize += item.getSize();
				} else {
					// Log.e(TAG, "false:" + item.getPath());
				}
				break;
			}
			case TYPE_SIMILAR: {
				SimilarImageItem item = (SimilarImageItem) iteratorValue.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					totalSize += item.getSize();
				}
				break;
			}
			case TYPE_ALBUM: {
				FileItem item = (FileItem) iteratorValue.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					totalSize += item.getSize();
				}
				break;
			}
			}
		}

		return totalSize;
	}

	public long getTotalSize() {
		long totalSize = 0;
		Iterator<Object> iteratorValue = mList.iterator();

		while (iteratorValue.hasNext()) {
			switch (mType) {
			case TYPE_CLEAR: {
				ExportedImageItem item = (ExportedImageItem) iteratorValue
						.next();
				totalSize += item.getSize();
				break;
			}
			case TYPE_SIMILAR: {
				SimilarImageItem item = (SimilarImageItem) iteratorValue.next();
				totalSize += item.getSize();
				break;
			}
			case TYPE_ALBUM: {
				FileItem item = (FileItem) iteratorValue.next();
				totalSize += item.getSize();
				break;
			}
			}
		}

		return totalSize;
	}

	public int getSelectedCount() {
		int count = 0;
		Iterator<Entry<String, Boolean>> iterator = mSelectedMap.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, Boolean> entry = iterator.next();
			if (entry.getValue()) {
				count++;
			}
		}

		return count;
	}

	public boolean isAllSelected() {
		Iterator<Entry<String, Boolean>> iterator = mSelectedMap.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, Boolean> entry = iterator.next();
			if (!entry.getValue()) {
				return false;
			}
		}

		return true;
	}

	public List<Object> getSelectedList() {
		List<Object> list = new ArrayList<Object>();

		Iterator<Object> iteratorValue = mList.iterator();
		while (iteratorValue.hasNext()) {
			switch (mType) {
			case TYPE_CLEAR: {
				ExportedImageItem item = (ExportedImageItem) iteratorValue
						.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					list.add(item);
				}
				break;
			}
			case TYPE_SIMILAR: {
				SimilarImageItem item = (SimilarImageItem) iteratorValue.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					list.add(item);
				}
				break;
			}
			case TYPE_ALBUM: {
				FileItem item = (FileItem) iteratorValue.next();
				boolean isSelected = mSelectedMap.get(item.getPath());
				if (isSelected) {
					list.add(item);
				}
				break;
			}
			}
		}

		return list;
	}

	public void setListener(AdapterListener listener) {
		mListener = listener;
	}

	public void setItemSize(int width, int height) {
		mItemWidth = width;
		mItemHeight = height;
	}

	public void setMultipleSelectMode(boolean isMultiple) {
		if (mIsMultiple == isMultiple) {
			return;
		}

		mIsMultiple = isMultiple;
		notifyDataSetChanged();
	}

	public void allSelected(boolean isSelected) {
		Iterator<Entry<String, Boolean>> iterator = mSelectedMap.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, Boolean> entry = iterator.next();
			entry.setValue(isSelected);
		}

		notifyDataSetChanged();
	}

	public boolean selectItemByPath(String path, boolean selected) {
		if (path != null) {
			if (mSelectedMap.containsKey(path)) {
				if (mSelectedMap.get(path) == selected) {
					return false;
				}

				Log.e(TAG, "select file:" + path + ", value:" + selected);
				mSelectedMap.put(path, selected);

				if (mVisibleItemCount == 0) {
					notifyDataSetChanged();
				} else {
					int index = getItemIndex(path);
					if ((index > (mFirstVisibleItem - 1))
							&& (index < (mFirstVisibleItem + mVisibleItemCount + 1))) {
						notifyDataSetChanged();
					}
				}

				return true;
			}
		}

		return false;
	}

	private int getItemIndex(String path) {
		switch (mType) {
		case TYPE_CLEAR: {
			for (int i = 0; i < mList.size(); i++) {
				ExportedImageItem item = (ExportedImageItem) mList.get(i);
				if (item.getPath().equals(path)) {
					return i;
				}
			}
			break;
		}
		case TYPE_SIMILAR: {
			for (int i = 0; i < mList.size(); i++) {
				SimilarImageItem item = (SimilarImageItem) mList.get(i);
				if (item.getPath().equals(path)) {
					return i;
				}
			}
			break;
		}
		case TYPE_ALBUM: {
			for (int i = 0; i < mList.size(); i++) {
				FileItem item = (FileItem) mList.get(i);
				if (item.getPath().equals(path)) {
					return i;
				}
			}
			break;
		}
		}

		return -1;
	}

	public void itemClick(int position) {
		if (mList.size() - 1 < position) {
			return;
		}

		String key = "";
		switch (mType) {
		case TYPE_CLEAR: {
			ExportedImageItem item = (ExportedImageItem) mList.get(position);
			key = item.getPath();
			break;
		}
		case TYPE_SIMILAR: {
			SimilarImageItem item = (SimilarImageItem) mList.get(position);
			key = item.getPath();
			break;
		}
		case TYPE_ALBUM: {
			FileItem item = (FileItem) mList.get(position);
			key = item.getPath();
			break;
		}
		}

		if (mSelectedMap.get(key)) {
			mSelectedMap.put(key, false);
			removeClickedItem(key);
		} else {
			mSelectedMap.put(key, true);
			addClickedItem(key);
		}

		if (mListener != null) {
			Log.e(TAG, "File path" + key);
			mListener.onItemClick(position, mSelectedMap.get(key));
		}
	}

	private void addClickedItem(String key) {
		if (key == null) {
			return;
		}

		for (String path : mClickedList) {
			if (key.equals(path)) {
				return;
			}
		}

		mClickedList.add(key);
	}

	private void removeClickedItem(String key) {
		if (key == null) {
			return;
		}

		Iterator<String> iterator = mClickedList.iterator();
		while (iterator.hasNext()) {
			String path = iterator.next();
			if (key.equals(path)) {
				iterator.remove();
				return;
			}
		}
	}

	public boolean isClickedItem() {
		if (mClickedList.size() > 0) {
			return true;
		}

		return false;
	}

	public List<Object> sort(int sortType) {
		mList = ItemComparator.sort(mList, sortType);
		notifyDataSetChanged();
		return mList;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	private int mFirstVisibleItem = 0;
	private int mVisibleItemCount = 0;

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mFirstVisibleItem = firstVisibleItem;
		mVisibleItemCount = visibleItemCount;
	}

	/*
	 * private ImageLoadingListener mImageLoadingListener = new
	 * ImageLoadingListener() {
	 * 
	 * @Override public void onLoadingStarted(String imageUri, View view) { }
	 * 
	 * @Override public void onLoadingFailed(String imageUri, View view,
	 * FailReason failReason) { String fileName =
	 * FileUtil.getFileName(imageUri); if (FileUtil.isVideo(mContext, fileName))
	 * { // ((ImageView) // view).setImageResource(R.drawable.video_loading);
	 * String loacation = imageUri.substring(7); // start with file:// Bitmap
	 * thumBitmap = FileUtil.getImageThumbnail(mContext, loacation); if
	 * (thumBitmap != null) { ((ImageView) view).setImageBitmap(thumBitmap); }
	 * else { ((ImageView) view) .setImageResource(R.drawable.video_loading); }
	 * } }
	 * 
	 * @Override public void onLoadingComplete(String imageUri, View view,
	 * Bitmap loadedImage) { }
	 * 
	 * @Override public void onLoadingCancelled(String imageUri, View view) { }
	 * };
	 */

	class ImageLoadingListenerImpl implements ImageLoadingListener {

		private FileItem item;

		public ImageLoadingListenerImpl(FileItem item) {
			this.item = item;
		}

		@Override
		public void onLoadingStarted(String imageUri, View view) {

		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			String fileName = FileUtil.getFileName(imageUri);
			if (FileUtil.isVideo(mContext, fileName)) {
				// ((ImageView)
				// view).setImageResource(R.drawable.video_loading);
				String loacation = imageUri.substring(7); // start with file://
				Bitmap thumBitmap = FileUtil.getImageThumbnail(mContext,
						loacation);
				if (thumBitmap != null) {
					((ImageView) view).setImageBitmap(thumBitmap);
				} else {
					((ImageView) view)
							.setImageResource(R.drawable.video_loading);
				}
			}

		}

		@Override
		public void onLoadingComplete(String imageUri, View view,
				Bitmap loadedImage) {
			if (item != null) {
				item.setRef(new WeakReference<Bitmap>(loadedImage));
			}
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {

		}

	}
}
