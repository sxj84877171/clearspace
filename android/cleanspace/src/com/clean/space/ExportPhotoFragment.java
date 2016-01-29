package com.clean.space;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
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
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.IPhotoManagerListener;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.photomgr.SimilarPhotoManager;
import com.clean.space.photomgr.UnexportPhotoManager;
import com.clean.space.photowall.ImageActivity;
import com.clean.space.protocol.FileItem;
import com.clean.space.protocol.SimilarImageItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.ui.AlertDeleteSimilarListDialog;
import com.clean.space.ui.HandleProgressBar;
import com.clean.space.ui.SimilarProgressBar;
import com.clean.space.ui.listener.AdapterListener;
import com.clean.space.ui.listener.BackHandledFragment;
import com.clean.space.ui.listener.SimilarAdapterListener;
import com.clean.space.ui.listener.SimilarFinderListener;
import com.clean.space.util.FileUtil;
import com.clean.space.util.ScreenUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class ExportPhotoFragment extends BackHandledFragment implements
		View.OnClickListener, OnTouchListener {
	private static final String TAG = ExportPhotoFragment.class.getSimpleName();

	private HandleProgressBar mClearProgressBar;
	private SimilarProgressBar mSimilarProgressBar;
	private LinearLayout mSimilarLayout;
	private GridView mAlbumGridView;
	private ThumbnailAdapter mAlbumAdapter;
	private ListView mSimilarListView;
	private SimilarLayoutAdapter mSimilarAdapter;

	private TextView mTitle;
	private TextView mExportInfo;
	private ImageView mSortOptionIcon;
	private TextView mSortSelectedText;
	private LinearLayout mSortOptionsList;
	private TextView mSortOption1;
	private TextView mSortOption2;
	private TextView mSortOption3;
	private TextView mSortOption4;
	private ImageView mAlbumPhotoIcon;
	private TextView mAlbumPhotoSize;
	private RelativeLayout mAlbumOptionsLayout;
	private LinearLayout mAlbumContent;

	private ImageView mBtnPrevious;
	private Button mBtnDelete;
	private Button mBtnCancel;
	private Button mBtnBack;

	private LinearLayout mBigImageViewLayout;
	private ImageView mBigImageView;

	private SimilarPhotoManager mSimilarPhotoMgr;
	private UnexportPhotoManager mAlbumPhotoMgr;
	private long mTotalSelectedSize;
	private long mDeletedSize;
	private long mSimilarTotalSize;
	private long mAlbumTotalSize;
	private long mTotalSize;
	private int mTotalCount;
	private int mTotalSelectedCount;
	private int mDeleteSelectedCount;
	private List<Object> mDeletedList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_export_photo, container,
				false);
		view.setOnTouchListener(this);

		configProgress(view);
		configSortOptionsList(view);
		configSimilarListView(view);
		configGridView(view);
		showBigImageView(view);
		otherConfig(view);

		getPhoto();

		return view;
	}

	@Override
	public void onClick(View v) {
		String eventID = "";
		switch (v.getId()) {
		case R.id.back:
		case R.id.title:
		case R.id.back_button: {
			mSimilarPhotoMgr.setPhotoManagerListener(null);
			mSimilarPhotoMgr.stopScan();
			mAlbumPhotoMgr.setPhotoManagerListener(null);
			mAlbumPhotoMgr.stopScan();

			FragmentTransaction transaction = getFragmentManager()
					.beginTransaction();
			transaction.remove(this);
			transaction.add(R.id.fragment_container,
					new CleanPhotoMainFragment());
			transaction.commit();
			break;
		}
		case R.id.sort_list_icon:
		case R.id.sort_selected_item: {
			if ((mAlbumContent.getVisibility() == View.GONE)
					|| (mAlbumAdapter.getCount() == 0)) {
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
			synchronized (ExportPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption1.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAlbumAdapter
						.sort(Constants.SORT_TYPE_DATE_ASC);
				resetAlbumListForPhotowall(sort);
			}

			break;
		}
		case R.id.sort_option2: {
			synchronized (ExportPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption2.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAlbumAdapter
						.sort(Constants.SORT_TYPE_DATE_DESC);
				resetAlbumListForPhotowall(sort);
			}

			break;
		}
		case R.id.sort_option3: {
			synchronized (ExportPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption3.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAlbumAdapter
						.sort(Constants.SORT_TYPE_SIZE_DESC);
				resetAlbumListForPhotowall(sort);
			}

			break;
		}
		case R.id.sort_option4: {
			synchronized (ExportPhotoFragment.this) {
				mSortSelectedText.setText(mSortOption4.getText());
				mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
				mSortOptionsList.setVisibility(View.GONE);

				List<Object> sort = mAlbumAdapter
						.sort(Constants.SORT_TYPE_SIZE_ASC);
				resetAlbumListForPhotowall(sort);
			}

			break;
		}
		case R.id.show_album_photo_icon:
		case R.id.album_photo_size_info: {

			eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_UNEXPORT_ALBUM;

			mSortOptionsList.setVisibility(View.GONE);

			if (mAlbumAdapter.getCount() == 0) {
				return;
			}

			synchronized (ExportPhotoFragment.this) {
				if (mAlbumContent.getVisibility() == View.GONE) {
					mAlbumPhotoIcon
							.setImageResource(R.drawable.btn_up_close_normal);
					mAlbumOptionsLayout
							.setBackgroundResource(R.drawable.white_toppart_corner_bg);
					mAlbumContent.setVisibility(View.VISIBLE);
				} else {
					mAlbumPhotoIcon
							.setImageResource(R.drawable.btn_down_open_normal);
					mAlbumOptionsLayout
							.setBackgroundResource(R.drawable.white_corner_bg);
					mAlbumContent.setVisibility(View.GONE);
				}
			}

			break;
		}
		case R.id.delete_button: {
			synchronized (ExportPhotoFragment.this) {
				int allDeleteCount = mSimilarAdapter.getAllSelectedListCount();
				if (allDeleteCount > 0) {
					final AlertDeleteSimilarListDialog dialog = new AlertDeleteSimilarListDialog(
							getActivity());
					dialog.setListCount(allDeleteCount);
					dialog.setPositiveButton(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();

							mBtnDelete.setVisibility(View.GONE);
							mBtnCancel.setVisibility(View.VISIBLE);
							mSimilarListView.setClickable(false);
							mAlbumGridView.setClickable(false);

							mDeletedSize = 0;
							mDeleteSelectedCount = 0;
							mClearProgressBar.setMax(mTotalSelectedSize);
							mAlbumPhotoMgr.setCancelDelete(false);
							mAlbumPhotoMgr.deletePhotos(mAlbumAdapter
									.getSelectedList());
							mSortOptionsList.setVisibility(View.GONE);
						}
					});
					dialog.setNegativeButton(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							dialog.dismiss();

							int index = mSimilarAdapter
									.getIndexOfAllSelectedList();
							if (index < 0) {
								return;
							}

							mSimilarProgressBar.showSimilarPhoto();
							mSimilarLayout.setVisibility(View.VISIBLE);
							mSimilarListView.smoothScrollToPosition(index);
						}
					});
					dialog.show();
				} else {
					mBtnDelete.setVisibility(View.GONE);
					mBtnCancel.setVisibility(View.VISIBLE);
					mSimilarListView.setClickable(false);
					mAlbumGridView.setClickable(false);

					mDeletedSize = 0;
					mDeleteSelectedCount = 0;
					mClearProgressBar.setMax(mTotalSelectedSize);
					mAlbumPhotoMgr.setCancelDelete(false);
					mAlbumPhotoMgr
							.deletePhotos(mAlbumAdapter.getSelectedList());
					mSortOptionsList.setVisibility(View.GONE);
				}
			}

			break;
		}
		case R.id.cancel_button: {
			synchronized (ExportPhotoFragment.this) {
				mSimilarPhotoMgr.cancelDelete();
				mAlbumPhotoMgr.cancelDelete();
				mSimilarListView.setClickable(true);
				mAlbumGridView.setClickable(true);

				mBtnCancel.setVisibility(View.GONE);
				mTotalSelectedSize = mAlbumAdapter.getSelectedSize();
				if (mAlbumAdapter.getSelectedCount() != 0) {
					mBtnDelete.setVisibility(View.VISIBLE);
					mBtnBack.setVisibility(View.GONE);

					mBtnDelete.setText(String.format(
							getString(R.string.clear_delete_btn),
							FileUtil.formatFileSize(mTotalSelectedSize)));
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
		}

		if (!"".equals(eventID)) {
			StatisticsUtil.getInstance(this.getActivity(),
					StatisticsUtil.TYPE_UMENG).onEventCount(eventID);
		}
	}

	/**
	 * 重置PhotoWall \ Album的数据
	 * 
	 * @param sort
	 */
	private void resetAlbumListForPhotowall(List<Object> sort) {
		if (albumList != null) {
			albumList.clear();
			albumList.addAll(sort);
		}
	}

	@Override
	public void onResume() {
		Activity activity = getActivity();
		if (activity != null) {
			StatisticsUtil.getInstance(activity, StatisticsUtil.TYPE_UMENG)
					.onResume();
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		Activity activity = getActivity();
		if (activity != null) {
			StatisticsUtil.getInstance(activity, StatisticsUtil.TYPE_UMENG)
					.onPause();
		}
		super.onPause();
	}

	@Override
	public void onDestroy() {
		Activity activity = getActivity();
		if (activity != null) {
			StatisticsUtil.getInstance(activity, StatisticsUtil.TYPE_UMENG)
					.onDestroy();
		}
		super.onDestroy();
	}

	private void configProgress(View parentView) {
		mClearProgressBar = (HandleProgressBar) parentView
				.findViewById(R.id.export_progress);

		String message = String.format(
				getString(R.string.export_analyze_message), 0);
		mClearProgressBar.setText(message);

		mClearProgressBar.setProgressColor(1,
				getResources().getColor(R.color.export_delete_progress));

		// ---------------------------
		mSimilarProgressBar = (SimilarProgressBar) parentView
				.findViewById(R.id.similar_progress_bar);
		mSimilarProgressBar.setText(getString(R.string.export_similar_finding));

		mSimilarProgressBar.foundSimilarPhoto();
		mSimilarProgressBar.setFinderListener(new SimilarFinderListener() {

			@Override
			public boolean showSimilarPhotoes() {
				synchronized (ExportPhotoFragment.this) {
					if (mSimilarAdapter.getCount() == 0) {
						return false;
					}

					if (mSimilarLayout.getVisibility() == View.GONE) {
						mSimilarLayout.setVisibility(View.VISIBLE);
						return true;
					} else {
						mSimilarLayout.setVisibility(View.GONE);
						return false;
					}
				}
			}
		});
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

	private void configSimilarListView(View parentView) {
		mSimilarListView = (ListView) parentView
				.findViewById(R.id.similar_listview);
		mSimilarAdapter = new SimilarLayoutAdapter(getActivity(),
				mSimilarListView, R.layout.layout_similar_photo);
		mSimilarListView.setAdapter(mSimilarAdapter);
		mSimilarAdapter.setListener(new SimilarAdapterListener() {

			@Override
			public void onItemClick(String id, boolean isSelected) {
				synchronized (ExportPhotoFragment.this) {
					mSimilarAdapter.selectItemByPath(id, isSelected);
					mAlbumAdapter.selectItemByPath(id, isSelected);

					mTotalSelectedSize = mAlbumAdapter.getSelectedSize();
					mTotalSelectedCount = mAlbumAdapter.getSelectedCount();
					mClearProgressBar.setText(String.format(
							getString(R.string.export_select_delete),
							mTotalSelectedCount));

					if (isSelected) {
						mBtnDelete.setVisibility(View.VISIBLE);
						mBtnBack.setVisibility(View.GONE);

						mBtnDelete.setText(String.format(
								getString(R.string.clear_delete_btn),
								FileUtil.formatFileSize(mTotalSelectedSize)));
					} else {
						if (mTotalSelectedCount == 0) {
							mBtnDelete.setVisibility(View.GONE);
							mBtnBack.setVisibility(View.VISIBLE);
						} else {
							mBtnDelete.setText(String
									.format(getString(R.string.clear_delete_btn),
											FileUtil.formatFileSize(mTotalSelectedSize)));
						}
					}

					mExportInfo.setText(String.format(
							getString(R.string.export_total_info), ""
									+ mTotalCount,
							FileUtil.formatFileSize(mTotalSize)));

					mSortOptionsList.setVisibility(View.GONE);
				}
			}

			@Override
			public void onItemAllDeleted() {
				mSimilarProgressBar
						.setText(getString(R.string.export_no_similar_photo));
				mSimilarProgressBar.allSimilarPhotoDeleted();
				mSimilarLayout.setVisibility(View.GONE);
			}

			@Override
			public void onItemLongClick(String path, int position, int type) {
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), ImageActivity.class);
				int index = position;

				if (similarListDel != null) {
					similarList.removeAll(similarListDel);
					similarListDel.clear();
				}

				String string = UserSetting.getString(getActivity(), "_path",
						"");

				// 相似照片删除后剩余一张,获取这一张的path,在similarList中删除.
				List<Object> objects = new ArrayList<Object>();

				for (Object obj : similarList) {
					String p = ((SimilarImageItem) obj).getPath();
					if (path.equals(p)) {
						index = similarList.indexOf(obj);
					}

					if (!TextUtils.isEmpty(string) && p.equals(string)) {
						objects.add(obj);
						UserSetting.setString(getActivity(), "_path", "");
					}
				}

				if (objects != null) {
					similarList.removeAll(objects);
					objects.clear();
				}

				intent.putExtra("image_position", index);
				intent.putExtra("image_type", 5);
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
				// .getImageOptions(),
				// mImageLoadingListener);
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

	private void configGridView(View parentView) {
		mAlbumGridView = (GridView) parentView.findViewById(R.id.gridview);
		mAlbumAdapter = new ThumbnailAdapter(getActivity(),
				R.layout.gridview_item_photo, ThumbnailAdapter.TYPE_ALBUM);
		mAlbumGridView.setAdapter(mAlbumAdapter);
		mAlbumGridView.setOnScrollListener(mAlbumAdapter);

		// set item size of gridview
		setGridViewItemSize(mAlbumGridView);
		mAlbumAdapter.setListener(new AdapterListener() {

			@Override
			public void onItemClick(int position, boolean selected) {
				synchronized (ExportPhotoFragment.this) {
					FileItem item = (FileItem) mAlbumAdapter.getItem(position);
					mSimilarAdapter.selectItemByPath(item.getPath(), selected);
					mAlbumAdapter.selectItemByPath(item.getPath(), selected);

					mTotalSelectedSize = mAlbumAdapter.getSelectedSize();
					mTotalSelectedCount = mAlbumAdapter.getSelectedCount();
					mClearProgressBar.setText(String.format(
							getString(R.string.export_select_delete),
							mTotalSelectedCount));

					if (selected) {
						mBtnDelete.setVisibility(View.VISIBLE);
						mBtnBack.setVisibility(View.GONE);

						mBtnDelete.setText(String.format(
								getString(R.string.clear_delete_btn),
								FileUtil.formatFileSize(mTotalSelectedSize)));
					} else {
						if (mTotalSelectedCount == 0) {
							mBtnDelete.setVisibility(View.GONE);
							mBtnBack.setVisibility(View.VISIBLE);
						} else {
							mBtnDelete.setText(String
									.format(getString(R.string.clear_delete_btn),
											FileUtil.formatFileSize(mTotalSelectedSize)));
						}
					}

					mExportInfo.setText(String.format(
							getString(R.string.export_total_info), ""
									+ mTotalCount,
							FileUtil.formatFileSize(mTotalSize)));

					mSortOptionsList.setVisibility(View.GONE);
				}
			}

			@Override
			public void onItemLongClick(String path, int position, int type) {
				
				if (mSortOptionsList.getVisibility() == View.VISIBLE) {
					mSortOptionIcon.setImageResource(R.drawable.icon_sort_down);
					mSortOptionsList.setVisibility(View.GONE);
				}
				
				Intent intent = new Intent(getActivity()
						.getApplicationContext(), ImageActivity.class);
				intent.putExtra("image_position", position);
				intent.putExtra("image_type", 4);
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
				// .getImageOptions(),
				// mImageLoadingListener);
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

	private void showBigImageView(View parentView) {
		mBigImageViewLayout = (LinearLayout) parentView
				.findViewById(R.id.show_big_photo_layout);
		mBigImageViewLayout.setOnClickListener(this);
		mBigImageView = (ImageView) parentView
				.findViewById(R.id.show_big_photo);
		mBigImageView.setOnClickListener(this);
	}

	private void setGridViewItemSize(GridView gridView) {
		DisplayMetrics metric = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(metric);

		int pxWidth = metric.widthPixels - ScreenUtil.dp2px(getActivity(), 30); // marginLeft
																				// &
																				// marginRight
		int min = ScreenUtil.dp2px(getActivity(), 80);
		int max = ScreenUtil.dp2px(getActivity(), 120);
		for (int i = 1; i < 16; i++) {
			int prePx = pxWidth / i;
			if ((prePx > min) && (prePx < max)) {
				pxWidth = prePx;
				break;
			}
		}

		mAlbumGridView.setColumnWidth(pxWidth);
		mAlbumAdapter.setItemSize(pxWidth, pxWidth);
	}

	private void otherConfig(View parentView) {
		mTitle = (TextView) parentView.findViewById(R.id.title);
		mTitle.setOnClickListener(this);
		mExportInfo = (TextView) parentView.findViewById(R.id.export_info);
		mExportInfo.setText(String.format(
				getString(R.string.export_total_info), "0", "0 B"));

		mSimilarLayout = (LinearLayout) parentView
				.findViewById(R.id.similar_photo_blob);

		mAlbumPhotoIcon = (ImageView) parentView
				.findViewById(R.id.show_album_photo_icon);
		mAlbumPhotoIcon.setOnClickListener(this);
		mAlbumPhotoSize = (TextView) parentView
				.findViewById(R.id.album_photo_size_info);
		mAlbumPhotoSize.setOnClickListener(this);
		mAlbumOptionsLayout = (RelativeLayout) parentView
				.findViewById(R.id.export_alubum_operations);
		mAlbumContent = (LinearLayout) parentView
				.findViewById(R.id.album_photo_content);

		mBtnPrevious = (ImageView) parentView.findViewById(R.id.back);
		mBtnPrevious.setOnClickListener(this);
		mBtnDelete = (Button) parentView.findViewById(R.id.delete_button);
		mBtnDelete.setOnClickListener(this);
		mBtnCancel = (Button) parentView.findViewById(R.id.cancel_button);
		mBtnCancel.setOnClickListener(this);
		mBtnBack = (Button) parentView.findViewById(R.id.back_button);
		mBtnBack.setOnClickListener(this);
	}

	/**
	 * 相似照片列表
	 */
	public static List<Object> similarList = new ArrayList<Object>();

	/**
	 * 相册照片列表
	 */
	public static List<Object> albumList;

	/**
	 * 删除的集合
	 */
	public static List<Object> similarListDel;

	public void getPhoto() {
		mTotalSelectedSize = 0;
		mSimilarTotalSize = 0;
		mAlbumTotalSize = 0;
		mTotalSize = 0;
		mTotalCount = 0;
		mTotalSelectedCount = 0;
		mDeleteSelectedCount = 0;
		mDeletedList = new ArrayList<Object>();

		mAlbumPhotoMgr = (UnexportPhotoManager) PhotoManagerFactory
				.getInstance(getActivity(),
						PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
		mSimilarPhotoMgr = (SimilarPhotoManager) PhotoManagerFactory
				.getInstance(getActivity(),
						PhotoManagerFactory.PHOTO_MGR_SIMILAR);

		mSimilarPhotoMgr.setPhotoManagerListener(new IPhotoManagerListener() {

			@Override
			public void onDeletePhotosProgress(final long deletedSize,
					double percent) {
				try {
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mSimilarTotalSize -= deletedSize;

								mSimilarProgressBar.setSimilarPhotoSizeText(FileUtil
										.formatFileSize(mSimilarTotalSize));
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onPhoto(List<?> photos) {
				if (photos != null) {
					try {
						// if(similarList != null){
						// similarList.clear();
						// }
						for (Object object : photos) {
							if (!similarList.contains(object)) {
								similarList.add(object);
							}
						}
						final List<Object> newList = new ArrayList<Object>(
								photos);
						synchronized (ExportPhotoFragment.this) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
									mSimilarProgressBar
											.setText(getString(R.string.export_similar_message));

									mSimilarAdapter.addSimilarImages(newList);
								}
							});
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onDeletePhoto(final List<?> photos) {
				try {
					final List<Object> newList = new ArrayList<Object>(
							(List<Object>) photos);
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mDeleteSelectedCount -= photos.size();
								// mSimilarAdapter.deleteSelectedList(newList);
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onPhotosSize(final long size) {
				try {
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mSimilarTotalSize += size;
								mSimilarProgressBar.setSimilarPhotoSizeText(FileUtil
										.formatFileSize(mSimilarTotalSize));
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onScanFinished() {
				try {
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Log.i(TAG, "Similar scan finish!");
								mClearProgressBar
										.setText(getString(R.string.export_analyze_finish));

								if (mSimilarTotalSize == 0) {
									mSimilarProgressBar
											.setText(getString(R.string.export_no_similar_photo));
								}
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onDeleteFinished() {
				// TODO Auto-generated method stub
			}
		});
		mSimilarPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_ASC, 0);

		mAlbumPhotoMgr.setPhotoManagerListener(new IPhotoManagerListener() {

			@Override
			public void onDeletePhotosProgress(final long deletedSize,
					final double percent) {
				try {
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mAlbumTotalSize -= deletedSize;
								mTotalSize -= deletedSize;
								mDeletedSize += deletedSize;
								mClearProgressBar.setProgress(1, mDeletedSize);

								mAlbumPhotoSize.setText(FileUtil
										.formatFileSize(mAlbumTotalSize));
								mExportInfo.setText(String
										.format(getString(R.string.export_deleted_size),
												FileUtil.formatFileSize(mDeletedSize)));

								if (percent == 100.0) {
									mBtnCancel.setVisibility(View.GONE);
									mBtnBack.setVisibility(View.VISIBLE);
									mClearProgressBar
											.setText(getString(R.string.export_delete_finish));
								}
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onPhoto(List<?> photos) {
				if (photos != null) {
					try {
						if (albumList != null) {
							albumList.clear();
						}
						albumList = (List<Object>) photos;
						final List<Object> newList = new ArrayList<Object>(
								(List<Object>) photos);

						synchronized (ExportPhotoFragment.this) {
							getActivity().runOnUiThread(new Runnable() {

								@Override
								public void run() {
//									if (mAlbumAdapter.getCount() == 0) {
//										mAlbumPhotoIcon
//												.setImageResource(R.drawable.btn_up_close_normal);
//										mAlbumOptionsLayout
//												.setBackgroundResource(R.drawable.white_toppart_corner_bg);
//										mAlbumContent
//												.setVisibility(View.GONE);
//									}else{
//										mAlbumContent
//										.setVisibility(View.VISIBLE);
//									}

									mAlbumAdapter.addObjects(newList);

									mTotalCount += newList.size();
									mExportInfo.setText(String
											.format(getString(R.string.export_total_info),
													"" + mTotalCount,
													FileUtil.formatFileSize(mTotalSize)));
								}
							});
						}
					} catch (IllegalStateException e) {
						e.printStackTrace();
					}

				}
			}

			@Override
			public void onDeletePhoto(final List<?> photos) {
				try {
					final List<Object> newList = new ArrayList<Object>(
							(List<Object>) photos);

					if (photos != null) {
						for (Object object : photos) {
							if (similarList.contains(object)) {
								similarList.remove(object);
							}
						}
					}

					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mDeleteSelectedCount += photos.size();
								mClearProgressBar.setText(String
										.format(getString(R.string.export_deleted_count),
												mDeleteSelectedCount,
												mTotalSelectedCount));

								mDeletedList.addAll(newList);
								if (mTotalSelectedCount != mDeleteSelectedCount) {
									return;
								}

								mTotalCount -= mDeletedList.size();

								mAlbumAdapter.deletePhotoes(mDeletedList, true);
								mAlbumPhotoSize.setText(FileUtil
										.formatFileSize(mAlbumAdapter
												.getTotalSize()));

								mSimilarAdapter
										.deleteSelectedList(getPathList(mDeletedList));
								mSimilarProgressBar
										.setSimilarPhotoSizeText(FileUtil
												.formatFileSize(mSimilarAdapter
														.getTotalSize()));

								mDeletedList.clear();

								mBtnCancel.setVisibility(View.GONE);
								mBtnBack.setVisibility(View.VISIBLE);
								mClearProgressBar
										.setText(getString(R.string.export_delete_finish));

								if (mSimilarAdapter.getCount() == 0) {
									mSimilarLayout.setVisibility(View.GONE);
									mSimilarProgressBar
											.allSimilarPhotoDeleted();
								}
								if (mAlbumAdapter.getCount() == 0) {
									mAlbumPhotoIcon
											.setImageResource(R.drawable.btn_down_open_normal);
									mAlbumOptionsLayout
											.setBackgroundResource(R.drawable.white_corner_bg);
									mAlbumContent.setVisibility(View.GONE);
								}
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onPhotosSize(final long size) {
				try {
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mAlbumTotalSize += size;
								mTotalSize += size;

								mExportInfo.setText(String.format(
										getString(R.string.export_total_info),
										"" + mTotalCount,
										FileUtil.formatFileSize(mTotalSize)));
								mAlbumPhotoSize.setText(FileUtil
										.formatFileSize(mAlbumTotalSize));
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}

			}

			@Override
			public void onScanFinished() {
				try {
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								mExportInfo.setText(String.format(
										getString(R.string.export_total_info),
										"" + mTotalCount,
										FileUtil.formatFileSize(mTotalSize)));
								
								if (mAlbumAdapter.getCount() == 0) {
									mAlbumPhotoIcon
											.setImageResource(R.drawable.btn_up_close_normal);
									mAlbumOptionsLayout
											.setBackgroundResource(R.drawable.white_toppart_corner_bg);
									mAlbumContent
											.setVisibility(View.GONE);
								}else{
									mAlbumContent
									.setVisibility(View.VISIBLE);
								}
								
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
					synchronized (ExportPhotoFragment.this) {
						getActivity().runOnUiThread(new Runnable() {

							@Override
							public void run() {
								if (mDeletedList.size() > 0) {
									mAlbumAdapter.deletePhotoes(mDeletedList,
											true);
									mDeletedList.clear();

									mClearProgressBar.setText(String
											.format(getString(R.string.export_select_delete),
													"0"));
								}
							}
						});
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}

		});

		mAlbumPhotoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_ASC, 0);
	}

	private List<String> getPathList(List<Object> list) {
		List<String> newList = new ArrayList<String>();
		for (Object object : list) {
			FileItem item = (FileItem) object;
			newList.add(item.getPath());
		}

		return newList;
	}

	@Override
	public boolean onBackPressed() {
		try {
			if (mBigImageViewLayout.getVisibility() == View.VISIBLE) {
				mBigImageViewLayout.setVisibility(View.GONE);
				return true;
			} else {
				mSimilarPhotoMgr.setPhotoManagerListener(null);
				mSimilarPhotoMgr.stopScan();
				mAlbumPhotoMgr.setPhotoManagerListener(null);
				mAlbumPhotoMgr.stopScan();

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
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		mSortOptionsList.setVisibility(View.GONE);
		return true;
	}

	private ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {

		@Override
		public void onLoadingStarted(String imageUri, View view) {
		}

		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			String fileName = FileUtil.getFileName(imageUri);
			if (FileUtil.isVideo(getActivity(), fileName)) {
				mBigImageView.setScaleType(ScaleType.CENTER_CROP);

				String loacation = imageUri.substring(7); // start with file://
				Bitmap thumBitmap = FileUtil.getImageThumbnail(getActivity(),
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
		}

		@Override
		public void onLoadingCancelled(String imageUri, View view) {
		}
	};
}
