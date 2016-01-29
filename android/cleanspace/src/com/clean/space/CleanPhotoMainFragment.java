package com.clean.space;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.IPhotoManagerListener;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.protocol.FileItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.FileUtils;
import com.clean.space.util.SpaceUtil;

/**
 * A placeholder fragment containing a simple view.
 */
@SuppressLint("NewApi")
public class CleanPhotoMainFragment extends Fragment {
	private static final String TAG = CleanPhotoMainFragment.class
			.getSimpleName();
	int mCount = 0;
	long mSize = 0;
	public IPhotoManager unexportManager = null;
	public IPhotoManager exportManager = null;

	private GridView mExportedGridView;
	private GridView mUnExportGridView;
	private CleanMainFragImageAdapter mExportedAdapter;
	private CleanMainFragImageAdapter mUnExportAdapter;

	ImageView mBack;
	TextView mTitle;
	Button mBtnBack;

	ImageView mNotCleanIcon;
	TextView mNotCleanText;
	ImageView mUnExportIcon;
	TextView mUnExportText;

	LinearLayout mExportedlayout;
	TextView mTextNoCleaned;
	TextView mTextCleaned;
	ImageButton mBtnCleaned;
	LinearLayout mUnExportlayout;
	TextView mTextNoUnExport;
	TextView mTextUnExported;
	ImageButton mBtnUnClean;
//	TextView mTvsummery;
	
	TextView photo_clean_size;
	TextView photo_clean_size_danwei;
	TextView photo_clean_number;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.clean_photo_fragment_main,
				container, false);

		unexportManager = PhotoManagerFactory.getInstance(getActivity(),
				PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
		unexportManager.setPhotoManagerListener(new UnexportManagerListener());
		unexportManager.startScan(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_ASC, 0);

		exportManager = PhotoManagerFactory.getInstance(getActivity(),
				PhotoManagerFactory.PHOTO_MGR_EXPORTED);
		exportManager.setPhotoManagerListener(new ExportManagerListener());
		exportManager.startScan(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_ASC, 0);
		mBack = (ImageView) v.findViewById(R.id.back);
		mBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gotTagFromScan();
				getActivity().finish();
				
			}

		});
		mTitle = (TextView) v.findViewById(R.id.title);
		mTitle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gotTagFromScan();
				getActivity().finish();
			}
		});
		mBtnBack = (Button) v.findViewById(R.id.btn_back);
		mBtnBack.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				gotTagFromScan();
				getActivity().finish();
			}
		});

		mNotCleanIcon = (ImageView) v.findViewById(R.id.exported_icon);
		mNotCleanIcon.setOnClickListener(cleanedPhotoListener);
		mNotCleanText = (TextView) v.findViewById(R.id.notCleanText);
		mNotCleanText.setOnClickListener(cleanedPhotoListener);
		mUnExportIcon = (ImageView) v.findViewById(R.id.unexport_icon);
		mUnExportIcon.setOnClickListener(unExportedPhotoListener);
		mUnExportText = (TextView) v.findViewById(R.id.unExportText);
		mUnExportText.setOnClickListener(unExportedPhotoListener);

		mExportedlayout = (LinearLayout) v.findViewById(R.id.exportedLayout);
		mTextNoCleaned = (TextView) v.findViewById(R.id.CleanedPhotoNoFileText);
		mTextCleaned = (TextView) v.findViewById(R.id.CleanedPhotoText);
		mBtnCleaned = (ImageButton) v.findViewById(R.id.startCleanedPhotoBtn);
		mUnExportlayout = (LinearLayout) v.findViewById(R.id.UnexportedLayout);
		mTextNoUnExport = (TextView) v
				.findViewById(R.id.unCleanPhotoNoFileText);
		mTextUnExported = (TextView) v.findViewById(R.id.unCleanPhotoText);
		mBtnUnClean = (ImageButton) v.findViewById(R.id.startUnCleanPhotoBtn);
//		mTvsummery = (TextView) v.findViewById(R.id.summery);
		photo_clean_size = (TextView) v.findViewById(R.id.photo_clean_size);
		photo_clean_size_danwei = (TextView) v.findViewById(R.id.photo_clean_size_danwei);
		photo_clean_number = (TextView) v.findViewById(R.id.photo_clean_number);

		mExportedGridView = (GridView) v.findViewById(R.id.gridview1);
		mExportedAdapter = new CleanMainFragImageAdapter(getActivity());
		mExportedGridView.setAdapter(mExportedAdapter);
		mExportedGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				addUMENG(Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_EXPORTED_UNCLEAN);
				
				getFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container,
								new CleanPhotoFragment(
										CleanPhotoFragment.TYPE_ALL_EXPORTED))
						.commit();
			}
		});

		mUnExportGridView = (GridView) v.findViewById(R.id.gridview2);
		mUnExportAdapter = new CleanMainFragImageAdapter(getActivity());
		mUnExportGridView.setAdapter(mUnExportAdapter);
		mUnExportGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				addUMENG(Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_UNEXPORT);
				
				getFragmentManager()
						.beginTransaction()
						.add(R.id.fragment_container, new ExportPhotoFragment())
						.commit();
			}

		});

		setPhotoSummery(0, 0);

		mTextCleaned.setOnClickListener(cleanedPhotoListener);
		mBtnCleaned.setOnClickListener(cleanedPhotoListener);
		mTextUnExported.setOnClickListener(unExportedPhotoListener);
		mBtnUnClean.setOnClickListener(unExportedPhotoListener);
		return v;
	}
	
	private void gotTagFromScan() {
		String tag = UserSetting.getString(getActivity(), Constants.COMEFROMSCAN, "");
		if("scan".equals(tag)){
			Intent main = new Intent(getActivity(),MainActivity.class);
			startActivity(main);
			UserSetting.setString(getActivity(), Constants.COMEFROMSCAN, "");
		}
	}
	
	/**
	 * 添加友盟
	 * @param eventid ID
	 */
	private void addUMENG(String eventid) {
		Activity activity = getActivity();
		if(null != activity && eventid != ""){
			StatisticsUtil.getInstance(this.getActivity(),
					StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
		}
	}
	
	OnClickListener cleanedPhotoListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mExportedAdapter.getCount() == 0) {
				return;
			}
			
			addUMENG(Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_EXPORTED_UNCLEAN);
			
			getFragmentManager()
					.beginTransaction()
					.add(R.id.fragment_container,
							new CleanPhotoFragment(
									CleanPhotoFragment.TYPE_ALL_EXPORTED))
					.commit();
		}
	};

	OnClickListener unExportedPhotoListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (mUnExportAdapter.getCount() == 0) {
				return;
			}
			
			addUMENG(Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_UNEXPORT);
			
			getFragmentManager().beginTransaction()
					.add(R.id.fragment_container, new ExportPhotoFragment())
					.commit();
		}
	};

	synchronized public void setPhotoSummery(int count, long size) {
		mCount += count;
		mSize += size;

		//TODO
//		String template = getString(R.string.cleaned_photos_summery);
//		String summery = String.format(template, mCount,
//				SpaceUtil.convertSize(mSize, true));
//		mTvsummery.setText(summery);
		
		String s = SpaceUtil.convertSize(mSize, true);// 10MB
		String sSize = s.substring(0, s.length() - 2);
		String sSizeDanwei = s.substring(s.length() - 2);
		
		photo_clean_size.setText(sSize);
		Shader shader = new LinearGradient(0, 0, 0, 150, Color.WHITE,
				Color.parseColor("#add1ff"), TileMode.CLAMP);
		photo_clean_size.getPaint().setShader(shader);
		
		photo_clean_size_danwei.setText(sSizeDanwei);
		photo_clean_number.setText(String.format(getString(R.string.photo_clean_number),mCount));
	}
	
	@Override
	public void onResume() {
		Activity activity = getActivity();
		if(activity != null){

			StatisticsUtil.getDefaultInstance(activity).onResume();
		}
		super.onResume();
	}
	
	@Override
	public void onPause() {
		Activity activity = getActivity();
		if(activity != null){
			StatisticsUtil.getDefaultInstance(activity).onPause();
		}
		super.onPause();
	}
	
	class ExportManagerListener implements IPhotoManagerListener {
		long mSize = 0;

		@Override
		public void onDeletePhotosProgress(long deletedSize, double percent) {
		}

		@Override
		public void onPhoto(final List<?> photos) {
			if (photos.size() > 0) {
				final List<FileItem> list = new ArrayList<FileItem>();
				for (Object object : photos) {
					FileItem item = (FileItem) object;
					if (FileUtils.isFileExist(item.getPath())) {
						list.add(item);
					} else {
						Log.e(TAG, "file not exist:" + item.getPath());
					}
				}

				if (list.size() == 0) {
					return;
				}

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						List<FileItem> items = null;
						if (mExportedAdapter.getCount() < 4) {
							if ((4 - mExportedAdapter.getCount()) >= photos
									.size()) {
								items = list;
							} else {
								items = list.subList(0,
										4 - mExportedAdapter.getCount());
							}
							mExportedAdapter.addImages(items);
						}

						mExportedlayout.setVisibility(View.VISIBLE);
						mTextNoCleaned.setVisibility(View.GONE);
						mTextCleaned.setVisibility(View.VISIBLE);
						mBtnCleaned.setVisibility(View.VISIBLE);

						setPhotoSummery(list.size(), 0);
					}

				});

			}
		}

		@Override
		public void onDeletePhoto(List<?> photos) {
		}

		@Override
		public void onPhotosSize(final long size) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mSize += size;
					String str = SpaceUtil.convertSize(mSize, true);
					mTextCleaned.setText(String.format(
							getString(R.string.clean_and_size), str));
					setPhotoSummery(0, size);
				}
			});
		}

		@Override
		public void onScanFinished() {
		}

		@Override
		public void onDeleteFinished() {
			
			
		}
	}

	class UnexportManagerListener implements IPhotoManagerListener {
		long mSize = 0;

		@Override
		public void onDeletePhotosProgress(long deletedSize, double percent) {
		}

		@Override
		public void onPhoto(final List<?> photos) {
			if (photos.size() > 0) {
				final List<FileItem> list = new ArrayList<FileItem>();
				for (Object object : photos) {
					FileItem item = (FileItem) object;
					if (FileUtils.isFileExist(item.getPath())) {
						list.add(item);
					} else {
						Log.e(TAG, "file not exist:" + item.getPath());
					}
				}

				if (list.size() == 0) {
					return;
				}

				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						List<FileItem> items = null;
						if (mUnExportAdapter.getCount() < 4) {
							if ((4 - mUnExportAdapter.getCount()) >= list
									.size()) {
								items = list;
							} else {
								items = list.subList(0,
										4 - mUnExportAdapter.getCount());
							}
							mUnExportAdapter.addImages(items);
						}

						mUnExportlayout.setVisibility(View.VISIBLE);
						mTextNoUnExport.setVisibility(View.GONE);
						mTextUnExported.setVisibility(View.VISIBLE);
						mBtnUnClean.setVisibility(View.VISIBLE);

						setPhotoSummery(list.size(), 0);
					}

				});

			}
		}

		@Override
		public void onDeletePhoto(List<?> photos) {
		}

		@Override
		public void onPhotosSize(final long size) {
			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					mSize += size;
					String str = SpaceUtil.convertSize(mSize, true);
					mTextUnExported.setText(String.format(
							getString(R.string.clean_and_size), str));
					setPhotoSummery(0, size);
				}
			});
		}

		@Override
		public void onScanFinished() {
		}

		@Override
		public void onDeleteFinished() {
			
			
		}
	}

}
