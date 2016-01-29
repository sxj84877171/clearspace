package com.clean.space.ui;

import java.util.List;

import com.clean.space.ThumbnailAdapter;
import com.clean.space.ui.listener.AdapterListener;
import com.clean.space.util.ScreenUtil;
import com.clean.space.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.GridView;
import android.widget.LinearLayout;

public class SimlilarPhotoLayout extends LinearLayout {
	private Context mContext;
	private GridView mGridView;
	private ThumbnailAdapter mAdapter;
	private int mColumnNum;
	private int mcellHeight;

	public SimlilarPhotoLayout(Context context) {
		super(context);

		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.layout_similar_photo,
				this, true);

		mGridView = (GridView) findViewById(R.id.gridview);
		mAdapter = new ThumbnailAdapter(mContext, R.layout.gridview_item_photo,
				ThumbnailAdapter.TYPE_SIMILAR);
		mGridView.setAdapter(mAdapter);

		// set item size of gridview
		setGridViewItemSize(mGridView);
	}

	public SimlilarPhotoLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.layout_similar_photo,
				this, true);

		mGridView = (GridView) findViewById(R.id.gridview);
		mAdapter = new ThumbnailAdapter(mContext, R.layout.gridview_item_photo,
				ThumbnailAdapter.TYPE_SIMILAR);
		mGridView.setAdapter(mAdapter);

		// set item size of gridview
		setGridViewItemSize(mGridView);
	}

	private void setGridViewItemSize(GridView gridView) {
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
		mColumnNum = i;
		mcellHeight = pxWidth + 20;

		mGridView.setColumnWidth(pxWidth);
		mAdapter.setItemSize(pxWidth, pxWidth);
	}

	public void setList(List<Object> list) {		
		int count = list.size();
		int rows = (count / mColumnNum) + (((count % mColumnNum) != 0)?1:0);
		int totalHeight = rows * mcellHeight;
		
		LayoutParams params = (LayoutParams) mGridView.getLayoutParams();
		params.width = params.width;
		params.height = totalHeight;
		mGridView.setLayoutParams(params);
		
		mAdapter.setList(list);
	}
	
	public void setListener(AdapterListener listener) {
		mAdapter.setListener(listener);
	}
}
