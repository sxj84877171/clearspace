package com.clean.space.ui;

import com.clean.space.Constants;
import com.clean.space.R;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.ui.listener.SimilarFinderListener;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.view.View;

public class SimilarProgressBar extends RelativeLayout implements
		View.OnClickListener {
	private Context mContext;
	private int mMaxLength;
	private RelativeLayout mSimilarProgress;
	private LinearLayout mProgressState;
	private int mProgressMax;
	private int mProgressValue;
	private TextView mProgressText;
	private TextView mSimilarSize;
	private ImageView mShowSimilarPhoto;
	private SimilarFinderListener mFinderListener;
	private boolean mIsOpenSimilarBlob;

	public SimilarProgressBar(Context context) {
		super(context);

		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.progress_find_similar,
				this, true);

		mSimilarProgress = (RelativeLayout) findViewById(R.id.similar_progress);
		mProgressState = (LinearLayout) findViewById(R.id.progress_state);
		mProgressMax = 100;
		mProgressValue = 0;

		mProgressText = (TextView) findViewById(R.id.similar_message);
		mSimilarSize = (TextView) findViewById(R.id.found_size_info);
		mSimilarSize.setOnClickListener(this);
		mShowSimilarPhoto = (ImageView) findViewById(R.id.show_similar_icon);
		mShowSimilarPhoto.setOnClickListener(this);
		mIsOpenSimilarBlob = false;

		DisplayMetrics metric = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(metric);
		mMaxLength = metric.widthPixels;
	}

	public SimilarProgressBar(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		mContext = context;
		LayoutInflater.from(mContext).inflate(R.layout.progress_find_similar,
				this, true);

		mSimilarProgress = (RelativeLayout) findViewById(R.id.similar_progress);
		mProgressState = (LinearLayout) findViewById(R.id.progress_state);
		mProgressMax = 100;
		mProgressValue = 0;

		mProgressText = (TextView) findViewById(R.id.similar_message);
		mSimilarSize = (TextView) findViewById(R.id.found_size_info);
		mSimilarSize.setOnClickListener(this);
		mShowSimilarPhoto = (ImageView) findViewById(R.id.show_similar_icon);
		mShowSimilarPhoto.setOnClickListener(this);
		mIsOpenSimilarBlob = false;

		DisplayMetrics metric = new DisplayMetrics();
		((Activity) mContext).getWindowManager().getDefaultDisplay()
				.getMetrics(metric);
		mMaxLength = metric.widthPixels;
	}

	public void setMax(int max) {
		mProgressMax = max;
	}

	public void setProgress(int progress) {
		mProgressValue = progress;
		if (mProgressValue == mProgressMax) {
			if (mIsOpenSimilarBlob) {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_toppart_corner_bg);
			} else {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_corner_bg);
			}
		} else {
			if (mIsOpenSimilarBlob) {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_topleftpart_corner_bg);
			} else {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_leftpart_corner_bg);
			}
		}

		LayoutParams params = (RelativeLayout.LayoutParams) mProgressState
				.getLayoutParams();
		params.width = mMaxLength * mProgressValue / mProgressMax;
	}

	public void setText(CharSequence text) {
		mProgressText.setText(text);
	}

	public void setSimilarPhotoSizeText(CharSequence text) {
		mSimilarSize.setText(text);
	}

	public void showSimilarPhoto() {
		mShowSimilarPhoto.setImageResource(R.drawable.btn_down_open_normal);
		mSimilarProgress.setBackgroundResource(R.drawable.white_corner_bg);
		if (mProgressValue == mProgressMax) {
			mProgressState.setBackgroundResource(R.drawable.similar_progress_corner_bg);
		} else {
			mProgressState.setBackgroundResource(R.drawable.similar_progress_leftpart_corner_bg);
		}
	}
	
	public void allSimilarPhotoDeleted() {
		setSimilarPhotoSizeText("0 B");
		mShowSimilarPhoto.setImageResource(R.drawable.btn_down_open_normal);		
		mProgressState.setBackgroundResource(R.drawable.white_corner_bg);
	}

	public void foundSimilarPhoto() {
		mSimilarProgress.setBackgroundResource(R.drawable.white_corner_bg);

		if (mProgressValue == mProgressMax) {
			if (mIsOpenSimilarBlob) {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_toppart_corner_bg);
			} else {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_corner_bg);
			}
		} else {
			if (mIsOpenSimilarBlob) {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_topleftpart_corner_bg);
			} else {
				mProgressState
						.setBackgroundResource(R.drawable.similar_progress_leftpart_corner_bg);
			}
		}
	}

	public void setFinderListener(SimilarFinderListener finderListener) {
		mFinderListener = finderListener;
	}

	/**
	 * 添加友盟
	 * @param eventid ID
	 */
	private void addUMENG(String eventid) {
		if(eventid != ""){
			StatisticsUtil.getDefaultInstance(mContext).onEventCount(eventid);
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.found_size_info:
		case R.id.show_similar_icon: {
			if (mFinderListener != null) {
				
				addUMENG(Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_UNEXPORT_SIMILAR);
				
				mIsOpenSimilarBlob = mFinderListener.showSimilarPhotoes();

				if (mIsOpenSimilarBlob) {
					mShowSimilarPhoto
							.setImageResource(R.drawable.btn_up_close_normal);
					mSimilarProgress
							.setBackgroundResource(R.drawable.white_toppart_corner_bg);

					if (mProgressValue == mProgressMax) {
						mProgressState
								.setBackgroundResource(R.drawable.similar_progress_toppart_corner_bg);
					} else {
						mProgressState
								.setBackgroundResource(R.drawable.similar_progress_topleftpart_corner_bg);
					}
				} else {
					mShowSimilarPhoto
							.setImageResource(R.drawable.btn_down_open_normal);
					mSimilarProgress
							.setBackgroundResource(R.drawable.white_corner_bg);
					if (mProgressValue == mProgressMax) {
						mProgressState
								.setBackgroundResource(R.drawable.similar_progress_corner_bg);
					} else {
						mProgressState
								.setBackgroundResource(R.drawable.similar_progress_leftpart_corner_bg);
					}
				}
			}

			break;
		}
		}

	}
}
