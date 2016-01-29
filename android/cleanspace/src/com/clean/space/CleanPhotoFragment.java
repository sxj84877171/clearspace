package com.clean.space;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clean.space.log.FLog;
import com.clean.space.photomgr.ExportedPhotoManager;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.IPhotoManagerListener;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.photowall.ImageActivity;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.ui.AlertDeleteDialog;
import com.clean.space.ui.HandleProgressBar;
import com.clean.space.ui.listener.AdapterListener;
import com.clean.space.ui.listener.BackHandledFragment;
import com.clean.space.util.FileUtil;
import com.clean.space.util.FileUtils;
import com.clean.space.util.ScreenUtil;

public class CleanPhotoFragment extends BackHandledFragment implements
		View.OnClickListener, OnTouchListener {
	private static final String TAG = CleanPhotoFragment.class.getSimpleName();
	public static final int TYPE_ALL_EXPORTED = 1;
	public static final int TYPE_CURRENT_EXPORTED = 2;

	private int mType;
	private TextView mTopTitle;
	private HandleProgressBar mClearProgressBar;
	private boolean mIsSelectAll;
	private ImageView mSelectAll;
	private TextView mSelectAllText;
	private GridView mGridView;
	private ThumbnailAdapter mAdapter;
	private ExportedPhotoManager mExportedPhotoMgr;

	private ImageView mSortOptionIcon;
	private TextView mSortSelectedText;
	private LinearLayout mSortOptionsList;
	private TextView mSortOption1;
	private TextView mSortOption2;
	private TextView mSortOption3;
	private TextView mSortOption4;

	private ImageView mBtnPrevious;
	private Button mBtnDelete;
	private Button mBtnCancel;
	private Button mBtnBack;

	private LinearLayout mBigImageViewLayout;
	private ImageView mBigImageView;

	private int mTotalSelectedCount;
	private int mDeleteSelectedCount;
	private List<Object> mDeletedList;

	public CleanPhotoFragment(int type) {
		mType = type;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_clear_photo, container,
				false);
		view.setOnTouchListener(this);

		configProgress(view);
		configSelectAll(view);
		configSortOptionsList(view);
		configGridView(view);
		showBigImageView(view);
		otherConfig(view);

		showExportedPhotoes();

		scanListAgain();

		return view;
	}

	@Override
	public void onClick(View v) {

		String eventid = "";
		switch (v.getId()) {
		case R.id.back:
		case R.id.top_title:
		case R.id.back_button: {
			mExportedPhotoMgr.setPhotoManagerListener(null);
			mExportedPhotoMgr.stopScan();
			if (mType == TYPE_CURRENT_EXPORTED) {
				getActivity().finish();
			} else {
				FragmentTransaction transaction = getFragmentManager()
						.beginTransaction();
				transaction.remove(this);
				transaction.add(R.id.fragment_container,
						new CleanPhotoMainFragment());
				transaction.commit();
			}
			break;
		}
		case R.id.select_all:
		case R.id.select_all_text: {
			synchronized (CleanPhotoFragment.this) {
				if (mIsSelectAll) {
					mIsSelectAll = false;
					mSelectAll.setImageResource(R.drawable.btn_not_check);

					mAdapter.setMultipleSelectMode(false);
					mAdapter.allSelected(false);

					mClearProgressBar
							.setText(getString(R.string.clear_need_select_message));
				} else {
					mIsSelectAll = true;
					mSelectAll.setImageResource(R.drawable.btn_checked);

					mAdapter.setMultipleSelectMode(true);
					mAdapter.allSelected(true);
				}

				int selectedCount = mAdapter.getSelectedCount();
				mClearProgressBar.setText(String.format(
						getString(R.string.clear_message), selectedCount));
				mBtnDelete.setText(String.format(
						getString(R.string.clear_delete_btn),
						FileUtil.formatFileSize(mAdapter.getSelectedSize())));

				if (mIsSelectAll && (selectedCount != 0)) {
					mBtnDelete.setVisibility(View.VISIBLE);
					mBtnBack.setVisibility(View.GONE);
				} else {
					mBtnDelete.setVisibility(View.GONE);
					mBtnBack.setVisibility(View.VISIBLE);
				}
			}

			break;
		}
		case R.id.sort_list_icon:
		case R.id.sort_selected_item: {
			if (mAdapter.getCount() == 0) {
				return;
			}

			if (mSortOptionsList.getVisibility() == View.GONE) {
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_up);
				mSortOptionsList.setVisibility(View.VISIBLE);
			} else {
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);
			}
			break;
		}
		/* sort options */
		case R.id.sort_option1: {
			synchronized (CleanPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption1.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAdapter.sort(Constants.SORT_TYPE_DATE_ASC);
				resetExportListForPhotowall(sort, mType);
				
				eventid = Constants.UMENG.ARRANGE_PHOTO.SORT_BY_TIME_ASC;
			}

			break;
		}
		case R.id.sort_option2: {
			synchronized (CleanPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption2.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAdapter.sort(Constants.SORT_TYPE_DATE_DESC);
				resetExportListForPhotowall(sort, mType);
				eventid = Constants.UMENG.ARRANGE_PHOTO.SORT_BY_TIME_DESC;
			}

			break;
		}
		case R.id.sort_option3: {
			synchronized (CleanPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption3.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAdapter.sort(Constants.SORT_TYPE_SIZE_DESC);
				resetExportListForPhotowall(sort, mType);
				eventid = Constants.UMENG.ARRANGE_PHOTO.SORT_BY_SIZE_DESC;
			}

			break;
		}
		case R.id.sort_option4: {
			synchronized (CleanPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption4.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAdapter.sort(Constants.SORT_TYPE_SIZE_ASC);
				resetExportListForPhotowall(sort, mType);
				eventid = Constants.UMENG.ARRANGE_PHOTO.SORT_BY_SIZE_ASC;
			}

			break;
		}
		case R.id.delete_button: {
			synchronized (CleanPhotoFragment.this) {
				String location = UserSetting.getString(getActivity(),
						Constants.SELECTEDPCNAME, "");

				final AlertDeleteDialog dialog = new AlertDeleteDialog(
						getActivity());
				dialog.setLocation(String.format(
						getString(R.string.alert_export_location), location));
				dialog.setPositiveButton(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();

						mDeleteSelectedCount = 0;
						mTotalSelectedCount = mAdapter.getSelectedCount();

						List<Object> list = mAdapter.getSelectedList();
						for (Object object : list) {
							ExportedImageItem item = (ExportedImageItem) object;
							Log.i(TAG, "To be deleted:" + item.getPath());
						}
						mExportedPhotoMgr.setCancelDelete(false);
						mExportedPhotoMgr.deletePhotos(list);

						mGridView.setClickable(false);
						mSelectAll.setClickable(false);
						mSelectAllText.setClickable(false);
						mBtnDelete.setVisibility(View.GONE);
						mBtnCancel.setVisibility(View.VISIBLE);
					}
				});
				dialog.setNegativeButton(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						dialog.dismiss();
					}
				});
				dialog.show();
			}

			break;
		}
		case R.id.cancel_button: {
			synchronized (CleanPhotoFragment.this) {
				mExportedPhotoMgr.cancelDelete();
				mGridView.setClickable(true);
				mSelectAll.setClickable(true);
				mSelectAllText.setClickable(true);

				// scanListAgain();

				mBtnCancel.setVisibility(View.GONE);
				if (mAdapter.getSelectedCount() != 0) {
					mBtnDelete.setVisibility(View.VISIBLE);
					mBtnBack.setVisibility(View.GONE);

					mBtnDelete
							.setText(String.format(
									getString(R.string.clear_delete_btn),
									FileUtil.formatFileSize(mAdapter
											.getSelectedSize())));
				} else {
					mBtnDelete.setVisibility(View.GONE);
					mBtnBack.setVisibility(View.VISIBLE);
				}

				mSortOptionsList.setVisibility(View.GONE);
			}

			break;
		}
		case R.id.show_big_photo_layout:
		case R.id.show_big_photo: {
			mBigImageViewLayout.setVisibility(View.GONE);
		}
			StatisticsUtil.getInstance(this.getActivity(),
					StatisticsUtil.TYPE_UMENG).onEventCount(eventid);
		}

	}

	/**
	 * 重置PhotoWall \ Export的数据
	 * 
	 * @param sort
	 */
	private void resetExportListForPhotowall(List<Object> sort, int type) {

		if (type == CleanPhotoFragment.TYPE_CURRENT_EXPORTED) {
			if (listCurrExp != null) {
				listCurrExp.clear();
				listCurrExp.addAll(sort);
			}
		} else {
			if (listAllExp != null) {
				listAllExp.clear();
				listAllExp.addAll(sort);
			}
		}
	}

	/**
	 * 本次删除之后,剩余要处理的文件个数.
	 */
	private void getRemainCountAfterDelete() {

		int selectedCount = mAdapter.getSelectedCount();
		if (selectedCount == 0) {
			mSelectAll.setImageResource(R.drawable.btn_not_check);
		}

		if (mType == TYPE_CURRENT_EXPORTED) {// finish界面用,查看后删除的个数.
			long totalSize = mAdapter.getCount();// 删除之后还剩下的数据,如果为零,那么全部删除.
			boolean b = (totalSize == 0) ? true : false;
			UserSetting.setBoolean(getActivity(), Constants.DELETEALL, b);
			FLog.d("FinishActivity", "本次删除之后还剩余: " + totalSize);
			UserSetting.setLong(getActivity(), Constants.REMAIN, totalSize);
			if (totalSize == 0) {// 完全删除后返回到finish界面.
				Activity activity = getActivity();
				if (activity != null
						&& activity instanceof LookExportPhotoActivity) {
					activity.finish();
				}
			}
		}
	}

	private void configProgress(View parentView) {
		mClearProgressBar = (HandleProgressBar) parentView
				.findViewById(R.id.clear_progress);

		String message = String.format(getString(R.string.clear_message), 0);
		mClearProgressBar.setText(message);

		// mClearProgressBar.setProgressColor(getResources().getColor(R.color.clear_clear_progress));
		// mClearProgressBar.setMax(100);
		// mClearProgressBar.setProgress(50);

		// mClearProgressBar.setProgressColor(1,
		// getResources().getColor(R.color.clear_content));
		// mClearProgressBar.setMax(1, 100);
		// mClearProgressBar.setProgress(1, 30);
	}

	private void configSelectAll(View parentView) {
		mIsSelectAll = false;
		mSelectAll = (ImageView) parentView.findViewById(R.id.select_all);
		mSelectAll.setOnClickListener(this);
		mSelectAllText = (TextView) parentView
				.findViewById(R.id.select_all_text);
		mSelectAllText.setOnClickListener(this);
	}

	private void configSortOptionsList(View parentView) {
		mSortOptionIcon = (ImageView) parentView
				.findViewById(R.id.sort_list_icon);
		mSortOptionIcon.setOnClickListener(this);
		mSortSelectedText = (TextView) parentView
				.findViewById(R.id.sort_selected_item);
		mSortSelectedText.setOnClickListener(this);

		mSortOptionsList = (LinearLayout) parentView
				.findViewById(R.id.sort_options);
		mSortOption1 = (TextView) parentView.findViewById(R.id.sort_option1);
		mSortOption1.setOnClickListener(this);
		mSortOption2 = (TextView) parentView.findViewById(R.id.sort_option2);
		mSortOption2.setOnClickListener(this);
		mSortOption3 = (TextView) parentView.findViewById(R.id.sort_option3);
		mSortOption3.setOnClickListener(this);
		mSortOption4 = (TextView) parentView.findViewById(R.id.sort_option4);
		mSortOption4.setOnClickListener(this);
	}

	private void configGridView(View parentView) {
		mGridView = (GridView) parentView.findViewById(R.id.gridview);
		mAdapter = new ThumbnailAdapter(getActivity(),
				R.layout.gridview_item_photo, ThumbnailAdapter.TYPE_CLEAR);
		mGridView.setAdapter(mAdapter);

		// set item size of gridview
		setGridViewItemSize(mGridView);

		mAdapter.setListener(new AdapterListener() {

			@Override
			public void onItemClick(int position, boolean selected) {
				mClearProgressBar.setText(String.format(
						getString(R.string.clear_message),
						mAdapter.getSelectedCount()));
				mBtnDelete.setText(String.format(
						getString(R.string.clear_delete_btn),
						FileUtil.formatFileSize(mAdapter.getSelectedSize())));

				mSortOptionsList.setVisibility(View.GONE);

				if (!selected) {
					mIsSelectAll = false;
					mSelectAll.setImageResource(R.drawable.btn_not_check);

					if (mAdapter.getSelectedCount() == 0) {
						mBtnDelete.setVisibility(View.GONE);
						mBtnBack.setVisibility(View.VISIBLE);
					} else {
						mSelectAll
								.setImageResource(R.drawable.btn_checked_part);
					}
				} else {
					if (mAdapter.isAllSelected()) {
						mIsSelectAll = true;
						mSelectAll.setImageResource(R.drawable.btn_checked);
					} else {
						mSelectAll
								.setImageResource(R.drawable.btn_checked_part);
					}

					mBtnDelete.setVisibility(View.VISIBLE);
					mBtnBack.setVisibility(View.GONE);
				}
			}

			@Override
			public void onItemLongClick(String path, int position, int type) {
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), ImageActivity.class);

				if(mSortOptionsList.getVisibility() == View.VISIBLE){
					mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
					mSortOptionsList.setVisibility(View.GONE);
				}
				
				intent.putExtra("image_position", position);
				intent.putExtra("image_type", mType);
				startActivity(intent);

				// try {
				// String fullPath = "file://" + path;
				// if (FileUtils.isFileExist(path)) {
				// mBigImageView.setScaleType(ScaleType.FIT_CENTER);
				// try {
				// ImageLoaderManager.getImageLoader(getActivity())
				// .displayImage(
				// fullPath,
				// mBigImageView,
				// ImageLoaderManager
				// .getImageOptions());
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				// } else {
				// mBigImageView.setVisibility(View.GONE);
				// }
				// } catch (Exception e) {
				// e.printStackTrace();
				// }
				//
				// mBigImageViewLayout.setVisibility(View.VISIBLE);
			}
		});
	}

	public static List<Object> listCurrExp = new ArrayList<Object>();
	public static List<Object> listAllExp = new ArrayList<Object>();

	/**
	 * @desc 正在删除过程中,点击取消删除,为适配器重新设置数据.
	 */
	@SuppressWarnings("unchecked")
	private void scanListAgain() {
		if (mType == TYPE_ALL_EXPORTED) {
			mExportedPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
					IPhotoManager.ORDER_BY_ASC, 0);

		} else {
			List<Object> list = (List<Object>) mExportedPhotoMgr
					.getCurrenExportedPhotosSync(IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_ASC, 0);

			if (listCurrExp != null) {
				listCurrExp.clear();
			}

			listCurrExp = list;

			mAdapter.setList(list);
			onClick(mSelectAll);
		}
	}

	private void setGridViewItemSize(GridView gridView) {
		DisplayMetrics metric = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);

		int pxWidth = metric.widthPixels - ScreenUtil.dp2px(getActivity(), 20); // marginLeft
																				// &
																				// marginRight
		int min = ScreenUtil.dp2px(getActivity(), 80);
		int max = ScreenUtil.dp2px(getActivity(), 120);
		for (int i = 1; i < 64; i++) {
			int prePx = pxWidth / i;
			if ((prePx > min) && (prePx < max)) {
				pxWidth = prePx;
				break;
			}
		}

		mGridView.setColumnWidth(pxWidth);
		mAdapter.setItemSize(pxWidth, pxWidth);
	}

	private void showBigImageView(View parentView) {
		mBigImageViewLayout = (LinearLayout) parentView
				.findViewById(R.id.show_big_photo_layout);
		mBigImageViewLayout.setOnClickListener(this);
		mBigImageView = (ImageView) parentView
				.findViewById(R.id.show_big_photo);
		mBigImageView.setOnClickListener(this);
	}

	private void otherConfig(View parentView) {
		mTopTitle = (TextView) parentView.findViewById(R.id.top_title);
		if (mType == TYPE_ALL_EXPORTED) {

		} else {
			mTopTitle.setText(R.string.clear_title_cur_exported);
		}
		mTopTitle.setOnClickListener(this);

		mBtnPrevious = (ImageView) parentView.findViewById(R.id.back);
		mBtnPrevious.setOnClickListener(this);
		mBtnDelete = (Button) parentView.findViewById(R.id.delete_button);
		mBtnDelete.setOnClickListener(this);
		mBtnCancel = (Button) parentView.findViewById(R.id.cancel_button);
		mBtnCancel.setOnClickListener(this);
		mBtnDelete.setText(String.format(getString(R.string.clear_delete_btn),
				"0 M"));
		mBtnBack = (Button) parentView.findViewById(R.id.back_button);
		mBtnBack.setOnClickListener(this);

		TextView locationLabel = (TextView) parentView
				.findViewById(R.id.exported_loacation);
		String location = UserSetting.getString(getActivity(),
				Constants.SELECTEDPCNAME, "");
		locationLabel.setText(String.format(
				getString(R.string.alert_export_location), location));
	}

	/**
	 * finish界面点击"查看后删除"按钮,进入选择删除界面,判断是否删除了照片,false进去后没删除,true删除了照片
	 */
	@SuppressWarnings("unused")
	private boolean deletetag = false;
	private List<Object> list;

	private void showExportedPhotoes() {
		mTotalSelectedCount = 0;
		mDeleteSelectedCount = 0;
		mDeletedList = new ArrayList<Object>();

		mExportedPhotoMgr = (ExportedPhotoManager) PhotoManagerFactory
				.getInstance(getActivity(),
						PhotoManagerFactory.PHOTO_MGR_EXPORTED);

		mExportedPhotoMgr.setPhotoManagerListener(new IPhotoManagerListener() {

			@Override
			public void onDeletePhotosProgress(long deletedSize,
					final double percent) {
				try {
					synchronized (CleanPhotoFragment.this) {
						Activity curAct = CleanPhotoFragment.this.getActivity();
						curAct.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Log.i(TAG, "Delete percent:" + percent);
								mClearProgressBar.setProgress(percent / 100);

								if (percent == 100.0) {
									mGridView.setClickable(true);
									mSelectAll.setClickable(true);
									mSelectAllText.setClickable(true);
									mBtnCancel.setVisibility(View.GONE);
									mBtnBack.setVisibility(View.VISIBLE);
									mClearProgressBar
											.setText(getString(R.string.clear_need_select_message));
								}
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onPhoto(List<?> photos) {
				if (photos != null) {
					try {
						list = new ArrayList<Object>(
								(List<ExportedImageItem>) photos);

						if (listAllExp != null) {
							listAllExp.clear();
						}

						listAllExp = (List<Object>) photos;

						synchronized (CleanPhotoFragment.this) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									mAdapter.addObjects(list);
								}
							});
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onDeletePhoto(List<?> photos) {
				if (photos != null) {
					try {
						final List<Object> list = new ArrayList<Object>(
								(List<Object>) photos);
						Log.i(TAG, "Delete size:" + list.size());
						synchronized (CleanPhotoFragment.this) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									mDeleteSelectedCount += list.size();
									mDeletedList.addAll(list);

									mClearProgressBar.setText(String
											.format(getString(R.string.clear_deleteing),
													mDeleteSelectedCount,
													mTotalSelectedCount));

									resetClearProgressBarText();

									if (mDeleteSelectedCount != mTotalSelectedCount) {
										return;
									}

									mAdapter.deletePhotoes(mDeletedList, true);

									mDeletedList.clear();

									mGridView.setClickable(true);
									mSelectAll.setClickable(true);
									mSelectAllText.setClickable(true);
									mBtnCancel.setVisibility(View.GONE);
									mBtnBack.setVisibility(View.VISIBLE);
									mClearProgressBar
											.setText(getString(R.string.clear_need_select_message));

									// getRemainCountAfterDelete();
								}

							});
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onPhotosSize(long size) {
				//
			}

			@Override
			public void onScanFinished() {
				try {
					synchronized (CleanPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								onClick(mSelectAll);
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onDeleteFinished() {
				try {
					synchronized (CleanPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (mDeletedList.size() > 0) {
									mAdapter.deletePhotoes(mDeletedList, true);
									mDeletedList.clear();

									if (mAdapter.getSelectedCount() == 0) {
										mClearProgressBar.setText(String
												.format(getString(R.string.clear_need_select_message),
														"0"));
									} else {
										int selectedCount = mAdapter
												.getSelectedCount();
										mClearProgressBar.setText(String
												.format(getString(R.string.clear_message),
														selectedCount));
									}
								}
								getRemainCountAfterDelete();
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

		});

		// scanListAgain();
	}

	/**
	 * 正在删除过程中,点击取消,初始化描述文本: mClearProgressBar.setText(message);
	 */
	private void resetClearProgressBarText() {
		if (mAdapter != null && mAdapter.getSelectedCount() == 0) {
			mClearProgressBar
					.setText(getString(R.string.clear_need_select_message));
		}
	}

	@Override
	public boolean onBackPressed() {
		try {
			if (mBigImageViewLayout.getVisibility() == View.VISIBLE) {
				mBigImageViewLayout.setVisibility(View.GONE);
				return true;
			} else {
				mExportedPhotoMgr.setPhotoManagerListener(null);
				mExportedPhotoMgr.stopScan();
				if (mType == TYPE_CURRENT_EXPORTED) {
					getActivity().finish();
				} else {
					FragmentManager fragmentManager = getFragmentManager();
					if (fragmentManager != null) {
						FragmentTransaction transaction = fragmentManager
								.beginTransaction();
						transaction.remove(this);
						transaction.add(R.id.fragment_container,
								new CleanPhotoMainFragment());
						transaction.commit();
						return true;
					}
					return false;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mSortOptionsList.setVisibility(View.GONE);
		return true;
	}
}
