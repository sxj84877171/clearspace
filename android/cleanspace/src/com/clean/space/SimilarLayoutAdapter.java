package com.clean.space;

import java.util.ArrayList;
import java.util.List;

import com.clean.space.protocol.SimilarImageItem;
import com.clean.space.ui.listener.AdapterListener;
import com.clean.space.ui.listener.SimilarAdapterListener;
import com.clean.space.util.ScreenUtil;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.LinearLayout.LayoutParams;

public class SimilarLayoutAdapter extends ArrayAdapter<List<Object>> implements
		OnScrollListener {
	private Context mContext;
	private ListView mListView;
	private int mResource;
	private List<List<Object>> mList;
	private List<ThumbnailAdapter> mAdapterList;
	private SimilarAdapterListener mListener;

	public SimilarLayoutAdapter(Context context, ListView listView, int resource) {
		super(context, resource);

		mContext = context;
		mListView = listView;
		mListView.setOnScrollListener(this);
		mResource = resource;
		mList = new ArrayList<List<Object>>();
		mAdapterList = new ArrayList<ThumbnailAdapter>();
	}

	@Override
	public int getCount() {
		return mList.size();
	}

	@Override
	public List<Object> getItem(int position) {
		return mList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = null;
		if (convertView == null) {
			view = (View) LayoutInflater.from(mContext)
					.inflate(mResource, null);
		} else {
			view = (View) convertView;
		}
		final List<Object> list = mList.get(position);
		final ThumbnailAdapter adapter = mAdapterList.get(position);

		GridView gridView = (GridView) view.findViewById(R.id.gridview);
//		if(gridView.getAdapter() == null || !gridView.getAdapter().equals(adapter)){
			gridView.setAdapter(adapter);
			gridView.invalidateViews();
			adapter.setListener(new AdapterListener() {
				
				@Override
				public void onItemClick(int position, boolean selected) {
					if (mListener != null) {
						SimilarImageItem item = (SimilarImageItem) list
								.get(position);
						mListener.onItemClick(item.getPath(), selected);
					}
				}
				
				@Override
				public void onItemLongClick(String path,int position,int a) {
					mListener.onItemLongClick(path,position,5);
				}
			});
//		}
		setGridViewItemSize(gridView, adapter, list.size());

		return view;
	}

	private void setGridViewItemSize(GridView gridView,
			ThumbnailAdapter adapter, int count) {
		DisplayMetrics metric = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(metric);

		int pxWidth = metric.widthPixels - ScreenUtil.dp2px(mContext, 30); // marginLeft
																			// &
																			// marginRight
		int min = ScreenUtil.dp2px(mContext, 80);
		int max = ScreenUtil.dp2px(mContext, 120);

		int i = 1;
		for (; i < 16; i++) {
			int prePx = pxWidth / i;
			if ((prePx > min) && (prePx < max)) {
				pxWidth = prePx;
				break;
			}
		}

		// set item width & height
		gridView.setColumnWidth(pxWidth);
		adapter.setItemSize(pxWidth, pxWidth);

		// set gridview height
		int cellHeight = pxWidth + 20;
		int rows = (count / i) + (((count % i) != 0) ? 1 : 0);
		int totalHeight = rows * cellHeight;

		LayoutParams params = (LayoutParams) gridView.getLayoutParams();
		params.width = params.width;
		params.height = totalHeight;
		gridView.setLayoutParams(params);
	}

	public void setListener(SimilarAdapterListener listener) {
		mListener = listener;
	}

	public void addSimilarImages(List<Object> list) {
		mList.add(list);

		ThumbnailAdapter adapter = new ThumbnailAdapter(mContext,
				R.layout.gridview_item_photo, ThumbnailAdapter.TYPE_SIMILAR);
		adapter.setList(list);
		mAdapterList.add(adapter);

//		if (shouldNotifyChange()) {
			notifyDataSetChanged();
//		}
	}

	private boolean shouldNotifyChange() {
		if (mList.size() < 3) {
			return false;
		}

		if ((mFirstVisibleItem + mVisibleItemCount + 1) >= mList.size()) {
			return true;
		}

		return false;
	}

	public List<List<Object>> getSelectedList() {
		List<List<Object>> selectedLists = new ArrayList<List<Object>>();
		for (int i = 0; i < mAdapterList.size(); i++) {
			selectedLists.add(mAdapterList.get(i).getSelectedList());
		}

		return selectedLists;
	}

	public long getSelectedSize() {
		long size = 0;
		for (ThumbnailAdapter adapter : mAdapterList) {
			size += adapter.getSelectedSize();
		}

		return size;
	}

	public int getAllSelectedListCount() {
		int count = 0;
		for (ThumbnailAdapter adapter : mAdapterList) {
			if (adapter.isClickedItem() && (adapter.getSelectedCount() == adapter.getCount())) {
				count++;
			}
		}

		return count;
	}
	
	public int getIndexOfAllSelectedList() {
		for (int i=0; i<mAdapterList.size(); i++) {
			if (mAdapterList.get(i).getSelectedCount() == mAdapterList.get(i).getCount()) {
				return i;			}
		}
		
		return -1;
	}

	public long getTotalSize() {
		long size = 0;
		for (ThumbnailAdapter adapter : mAdapterList) {
			size += adapter.getTotalSize();
		}

		return size;
	}

	public int getSelectedItemCount() {
		int count = 0;
		for (ThumbnailAdapter adapter : mAdapterList) {
			count += adapter.getSelectedCount();
		}

		return count;
	}

	public void deleteSelectedList(List<String> list) {
		if (list == null) {
			return;
		}

		boolean isNeedRefresh = true;
		for (int index = mAdapterList.size() - 1; index >=0 ; index--) {
			if ((index > (mFirstVisibleItem - 1))
					&& (index < (mFirstVisibleItem + mVisibleItemCount + 1))) {
				isNeedRefresh = true;
			}

			mAdapterList.get(index).deleteSimilarPhotoesById(list, true);
			if (mAdapterList.get(index).getCount() <= 1) {
				ThumbnailAdapter thumbnailAdapter = mAdapterList.get(index);
				List<Object> allItems = thumbnailAdapter.getAllItems();
				for (Object object : allItems) {
					SimilarImageItem imageItem = (SimilarImageItem) object;
					// 记录剩余的一张相似照片的path
					UserSetting.setString(mContext, "_path", imageItem.getPath());
				}
				
				mAdapterList.remove(index);
				mList.remove(index);
			} else {
				mList.get(index).clear();
				mList.get(index).addAll(mAdapterList.get(index).getAllItems());
			}
		}

		if (isNeedRefresh) {
			notifyDataSetChanged();
		}
	}

	public void selectItemByPath(String path, boolean selected) {
		if (path != null) {
			for (int index = 0; index < mAdapterList.size(); index++) {
				if (mAdapterList.get(index).selectItemByPath(path, selected)) {
					if ((index > (mFirstVisibleItem - 1))
							&& (index < (mFirstVisibleItem + mVisibleItemCount + 1))) {
						notifyDataSetChanged();
					}
				}
			}
		}
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
}
