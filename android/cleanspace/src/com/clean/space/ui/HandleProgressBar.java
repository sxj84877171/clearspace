package com.clean.space.ui;

import java.util.ArrayList;
import java.util.List;

import com.clean.space.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class HandleProgressBar extends RelativeLayout {
	private Context mContext;
	private int mMaxLength;
	private List<LinearLayout> mProgressStateList;
	private List<Long> mProgressMaxList;
	private TextView mProgressText;

	public HandleProgressBar(Context context) {
		super(context);

		mContext = context;
	}

	public HandleProgressBar(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		mContext = context;
		LayoutInflater.from(mContext)
				.inflate(R.layout.progress_bar, this, true);

		mProgressStateList = new ArrayList<LinearLayout>(5);
		mProgressStateList
				.add((LinearLayout) findViewById(R.id.progress_state1));
		mProgressStateList
				.add((LinearLayout) findViewById(R.id.progress_state2));
		mProgressMaxList = new ArrayList<Long>();
		mProgressMaxList.add(100L);
		mProgressMaxList.add(100L);

		mProgressText = (TextView) findViewById(R.id.progress_text);

		DisplayMetrics metric = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(metric);
		mMaxLength = metric.widthPixels;
	}

	public void setMax(long max) {
		setMax(0, max);
	}

	public void setMax(int barIndex, long max) {
		mProgressMaxList.set(barIndex, max);
	}

	public void setProgress(long progress) {
		setProgress(0, progress);
	}

	public void setProgress(int barIndex, int progress) {
		LayoutParams params = (RelativeLayout.LayoutParams) mProgressStateList
				.get(barIndex).getLayoutParams();
		params.width = (int) (mMaxLength * progress / mProgressMaxList.get(barIndex));
		mProgressStateList.get(barIndex).setLayoutParams(params);
	}

	public void setProgress(int barIndex, double percent) {
		LayoutParams params = (RelativeLayout.LayoutParams) mProgressStateList
				.get(barIndex).getLayoutParams();
		params.width = (int) (mMaxLength * percent);
		mProgressStateList.get(barIndex).setLayoutParams(params);
	}

	public void setProgress(double percent) {
		setProgress(0, percent);
	}

	public void setProgressColor(int resourceId) {
		setProgressColor(0, resourceId);
	}

	public void setProgressColor(int barIndex, int resourceId) {
		mProgressStateList.get(barIndex).setBackgroundColor(resourceId);
	}

	public void setBackgroundColor(int resourceId) {
		setBackgroundColor(resourceId);
	}

	public void setText(CharSequence text) {
		mProgressText.setText(text);
	}

	public void setTextColor(int resourceId) {
		mProgressText.setTextColor(resourceId);
	}
}
