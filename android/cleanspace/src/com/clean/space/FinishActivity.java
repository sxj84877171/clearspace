package com.clean.space;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.clean.space.log.FLog;
import com.clean.space.network.discover.WifiApManager;
import com.clean.space.network.http.RankManager;
import com.clean.space.network.http.RankManager.SortRet;
import com.clean.space.notification.CleanSpaceNotificationManager;
import com.clean.space.onedriver.OneDriveUploadManage;
import com.clean.space.photomgr.ExportedPhotoManager;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.IPhotoManagerListener;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.protocol.PCClientItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.ui.CartoonTextView;
import com.clean.space.util.FileUtil;
import com.clean.space.util.KeepUserNetwork;
import com.clean.space.util.SpaceUtil;
import com.clean.space.util.Tools;

/**
 * @Des 处理完成的界面
 */
@SuppressLint("NewApi")
public class FinishActivity extends Activity {

	private static final String TAG = "FinishActivity";
	private TextView export_result = null;
	// private TextView clearInfo_free_size_ = null;
	private CartoonTextView speed;
	private CartoonTextView speedLike;
	private CartoonTextView totalNum2;
	private CartoonTextView totalNum4;
	private CartoonTextView totalSize2;
	private CartoonTextView totalSize4;
	private Button finish;
	private TextView exportLocation_finish, speed_danwei;
	private TextView locationBold;
	private LinearLayout clearInfo_hasmore_size;
	private Button share;
	private Button delete_export_;// 删除刚才导出的照片的按钮
	private TextView look_and_delete_des;// 查看后删除按钮
	private TextView dia_continue_delete;// 弹出对话框确认删除按钮
	private TextView dia_cancle_delete;// 弹出对话框确认删除按钮
	private View dial_delete_item;
	private GestureDetector gestureDetector;
	private LinearLayout confirm_dia_des_root;
	private LinearLayout confirm_dia_anniu_root;
	private TextView del_info_cancle;// 正在删除动态数据的布局
	private LinearLayout del_info;
	private TextView del_info_num;
	private IPhotoManager manager;
	private ImageView speed_icon;

	public static final int DANWEI = 1024;

	private Handler mHandler = new Handler();
	private Runnable btnStateTask = new Runnable() {

		@Override
		public void run() {
			// 判断是否已经全部删除,true全部删除,false没有全部删除.
			boolean b = UserSetting.getBoolean(getApplicationContext(),
					Constants.DELETEALL, false);
			if (b) {
				checkDeleteInformation();
				UserSetting.setBoolean(getApplicationContext(),
						Constants.DELETEALL, false);
			}
			mHandler.postDelayed(this, Constants.CHECK_BUTTON_STATE);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();

		initData();

		initEvent();

		resetCleanStatus();

		// disconnect softAp connection
		KeepUserNetwork.getInstance(this).restoreOldNetWork();
		//
		WifiApManager.getInstance(this).disableSoftAp();

		CleanSpaceNotificationManager.getInstance()
				.sendExportProcessFinshNotification(this);
	}

	/**
	 * 完成分享的界面.
	 */
	protected void checkDeleteInformation() {
		dial_delete_item.setVisibility(View.GONE);
		delete_export_.setVisibility(View.GONE);
		share.setVisibility(View.VISIBLE);
		look_and_delete_des.setVisibility(View.GONE);
		setClickble();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		resetCleanStatus();
		UserSetting.setLong(FinishActivity.this, Constants.REMAIN, 0);
		mHandler.removeCallbacks(btnStateTask);
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onDestroy();
	}

	@Override
	protected void onPause() {
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		remain = UserSetting.getLong(FinishActivity.this, Constants.REMAIN, 0);

		resetScanState();

		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onResume();

		new Thread() {
			public void run() {
				FileUtil.saveNeedSharedPhoto2File(FinishActivity.this,
						Constants.SHAREPATH);
			}
		}.start();

		super.onResume();
	}

	/**
	 * 设置监听,重新扫描最新的数据.
	 */
	private void resetScanState() {
		if (delInfo == null) {
			delInfo = new GotDelInfo();
		}

		if (manager == null) {
			manager = PhotoManagerFactory.getInstance(FinishActivity.this,
					PhotoManagerFactory.PHOTO_MGR_EXPORTED);
		}

		manager.setPhotoManagerListener(delInfo);
		manager.startScan(IPhotoManager.SORT_TYPE_TIME,
				IPhotoManager.ORDER_BY_ASC, 0);// 扫描最新数据
	}

	@Override
	public void onBackPressed() {
		if (dial_delete_item.getVisibility() == View.VISIBLE) {
			dial_delete_item.setVisibility(View.GONE);
			setClickble();
		} else {
			jump2MainActivity();
		}
	}

	@SuppressLint("SdCardPath")
	private void showShare() {
		try {
			ShareSDK.initSDK(this);
			OnekeyShare oks = new OnekeyShare();
			// 关闭sso授权
			oks.disableSSOWhenAuthorize();
			// 分享时Notification的图标和文字 2.5.9以后的版本不调用此方法
			// oks.setNotification(R.drawable.ic_launcher,
			// getString(R.string.app_name));
			// title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
			oks.setTitle(getString(R.string.app_name));
			// titleUrl是标题的网络链接，仅在人人网和QQ空间使用
			oks.setTitleUrl("http://dworkstudio.com/");
			// text是分享文本，所有平台都需要这个字段
			oks.setText(getString(R.string.share));
			// imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
			oks.setImagePath(Constants.SHAREPATH);// 确保SDcard下面存在此张图片
			// url仅在微信（包括好友和朋友圈）中使用
			oks.setUrl("http://dworkstudio.com/");
			// comment是我对这条分享的评论，仅在人人网和QQ空间使用
			// oks.setComment("我是测试评论文本");
			// site是分享此内容的网站名称，仅在QQ空间使用
			oks.setSite(getString(R.string.app_name));
			// siteUrl是分享此内容的网站地址，仅在QQ空间使用
			oks.setSiteUrl("http://dworkstudio.com/");

			// 启动分享GUI
			oks.show(this);
		} catch (Exception e) {
			FLog.e(TAG, "showShare throw error", e);
		}
	}

	private void initEvent() {
		try {

			OnClickListener listener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					String eventID = "";

					if (v != null) {

						switch (v.getId()) {

						case R.id.done:// 清理完成
							resetCleanStatus();
							jump2MainActivity();
							break;

						case R.id.share:// 分享
							eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_PULL2FRIENADS;
							showShare();
							// Intent share1 = new Intent(Intent.ACTION_SEND);
							//
							// share1.setType("text/plain");
							// share1.putExtra(Intent.EXTRA_SUBJECT,
							// R.string.app_name);
							// share1.putExtra(Intent.EXTRA_TEXT,
							// getString(R.string.share));
							// share1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							// share1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
							//
							// startActivity(Intent.createChooser(share1,
							// getTitle()));
							break;

						case R.id.delete_export_:// 删除刚才导出的照片的按钮
							eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_CLEANALLPIC;
							dial_delete_item.setVisibility(View.VISIBLE);
							del_info.setVisibility(View.GONE);
							setClickble();
							break;

						case R.id.look_and_delete_des:// 查看后删除
							// 查看已导出照片
							Intent lookExported = new Intent(
									FinishActivity.this,
									LookExportPhotoActivity.class);
							startActivity(lookExported);
							mHandler.removeCallbacks(btnStateTask);
							mHandler.postDelayed(btnStateTask,
									Constants.CHECK_BUTTON_STATE);
							eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_CLEAN_AFTER_LOOK;
							break;

						case R.id.continue_delete:// (弹出对话框) 继续 删除按钮
													// dia_continue_delete
							del_info.setVisibility(View.VISIBLE);
							del_info_cancle.setVisibility(View.VISIBLE);
							confirm_dia_des_root.setVisibility(View.GONE);
							confirm_dia_anniu_root.setVisibility(View.GONE);

							setClickble();
							ExportedPhotoManager photoMgr = (ExportedPhotoManager) PhotoManagerFactory
									.getInstance(
											getApplicationContext(),
											PhotoManagerFactory.PHOTO_MGR_EXPORTED);
							photoMgr.setCancelDelete(false);// true:不删除,false:删除.
							manager.deletePhotos(photoMgr
									.getCurrenExportedPhotosSync(
											IPhotoManager.SORT_TYPE_TIME,
											IPhotoManager.ORDER_BY_ASC, 0));
							break;

						case R.id.cancle_delete:// (弹出对话框) 取消 删除按钮
												// dia_cancle_delete,点击取消回到完成完成界面
							dial_delete_item.setVisibility(View.GONE);
							// delete_export_.setVisibility(View.VISIBLE);
							// share.setVisibility(View.GONE);
							setClickble();
							break;

						case R.id.dia_deleting_root_cancle:// 正在删除时候点击取消 回到完成界面
							eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_DELETING_CANCLE;
							manager.cancelDelete();
							checkDeleteInformation();
							break;

						default:
							break;
						}
					}
					if (!"".equals(eventID)) {
						StatisticsUtil.getDefaultInstance(
								getApplicationContext()).onEventCount(eventID);
					}
				}

			};

			finish.setOnClickListener(listener);
			share.setOnClickListener(listener);
			delete_export_.setOnClickListener(listener);// 删除刚才导出的照片的按钮
			look_and_delete_des.setOnClickListener(listener);// 查看后删除
			dia_continue_delete.setOnClickListener(listener);// (弹出对话框) 确认 删除按钮
			dia_cancle_delete.setOnClickListener(listener);//
			del_info_cancle.setOnClickListener(listener);//

		} catch (Exception e) {
			FLog.e(TAG, "initEvent throw error", e);
		}

	}

	private void jump2MainActivity() {
		Intent intent = new Intent(FinishActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void resetCleanStatus() {
		CleanFileStatusPkg pkg = new CleanFileStatusPkg();
		pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_STOP);
		UserSetting.setClearStatusInfo(this, pkg.toJson());

	}

	private void initData() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			String handlerNumber = pkg.getHandlePicNumber();
			if (null == handlerNumber) {
				handlerNumber = "0";
			}
			initRankData(pkg);

			// 2[张]|
			// String s = String.format(getString(R.string.Dwpic),
			// handlerNumber);
			// int index = s.indexOf("[");
			// int index2 = s.indexOf("]");
			//
			// SpannableStringBuilder builder = new SpannableStringBuilder(s);
			// AbsoluteSizeSpan span = new AbsoluteSizeSpan(32);// 设置字体大小
			//
			// builder.setSpan(span, index + 1, index + 2,
			// Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			//
			// builder.replace(index, index + 1, "");
			// builder.delete(index2 - 1, index2 + 1);

			String result = getString(R.string.clear_title_cur_exported_);
			result = String.format(result, handlerNumber
					+ getString(R.string.Dwpic), SpaceUtil.getFreeSpace()
					+ getString(R.string.Dwsize));
			export_result.setText(result);

			manager = PhotoManagerFactory.getInstance(FinishActivity.this,
					PhotoManagerFactory.PHOTO_MGR_EXPORTED);
			// dealInfoStr = (String) clearInfo_has_out_pic_num_.getText();
			// dealInfo = Integer.parseInt(dealInfoStr);// 实际处理的照片数目
			dealInfo = Integer.parseInt(handlerNumber);// 实际处理的照片数目

			if (dealInfo == 0) {// 跳到finish界面,如果实际导出照片为零,那么显示完成分享界面.
				checkDeleteInformation();
			}

		} catch (NumberFormatException e) {
			FLog.e(TAG, "initData throw error", e);
		} catch (Exception e) {
			FLog.e(TAG, "initData throw error", e);
		}
	}

	private void initView() {
		setContentView(R.layout.finish);

		try {
			initHandlePicTip();

			initRankView();

			export_result = (TextView) this.findViewById(R.id.export_result);
			share = (Button) findViewById(R.id.share);

			delete_export_ = (Button) findViewById(R.id.delete_export_);// 删除刚才导出的照片
			look_and_delete_des = (TextView) findViewById(R.id.look_and_delete_des);// 查看后删除的按钮

			dial_delete_item = findViewById(R.id.dial_delete_item);// 确认是否删除的
																	// 对话框
			dia_continue_delete = (TextView) findViewById(R.id.continue_delete);// 弹出对话框确认删除按钮
			dia_cancle_delete = (TextView) findViewById(R.id.cancle_delete);// 弹出对话框取消删除按钮

			confirm_dia_des_root = (LinearLayout) findViewById(R.id.confirm_dia_root);// 确认是否继续删除描述的对话框
			confirm_dia_anniu_root = (LinearLayout) findViewById(R.id.confirm_dia_anniu_root);// 确认是否继续删除下面两个按钮的布局

			del_info = (LinearLayout) findViewById(R.id.dia_deleting_root);// 正在删除动态数据的布局
			del_info_cancle = (TextView) findViewById(R.id.dia_deleting_root_cancle);// 正在删除动态数据取消的布局
			del_info_num = (TextView) findViewById(R.id.del_num);// 正在删除动态数据

			// boolean outAndClearinfo = UserSetting.getBoolean(
			// getApplicationContext(), Constants.CHECK_EXPORT_AND_CLEAN,
			// false);
			// if (!outAndClearinfo) {
			// clearInfo_hasmore_size.setBackgroundColor(Color
			// .parseColor("#2c77d9"));
			// }

			finish = (Button) this.findViewById(R.id.done);

			exportLocation_finish = (TextView) findViewById(R.id.exportLocation_finish);
			int type = UserSetting.getInt(getApplicationContext(),
					Constants.SELECTTPYE, 0);

			if (type != PCClientItem.DISCOVER_BY_ONEDRIVE) {
				String PCName = UserSetting.getString(getApplicationContext(),
						Constants.SELECTEDPCNAME, "");
				exportLocation_finish.setText(String.format(
						getString(R.string.alert_export_location), PCName));
			} else {
				exportLocation_finish
						.setText(getString(R.string.one_drive_string)
								+ File.separator
								+ OneDriveUploadManage.FOLDER_NAME
								+ File.separator + android.os.Build.MODEL
								+ File.separator);
			}

			gestureDetector = new GestureDetector(FinishActivity.this,
					onGestureListener);

		} catch (Exception e) {
			FLog.e(TAG, "initData throw error", e);
		}
	}

	private void initRankView() {
		speed = (CartoonTextView) findViewById(R.id.speed);
		speedLike = (CartoonTextView) findViewById(R.id.speed_like);
		totalNum2 = (CartoonTextView) findViewById(R.id.total_num_2);
		totalNum4 = (CartoonTextView) findViewById(R.id.total_num_4);
		totalSize2 = (CartoonTextView) findViewById(R.id.total_size_2);
		totalSize4 = (CartoonTextView) findViewById(R.id.total_size_4);
		speed_icon = (ImageView) findViewById(R.id.speed_icon);
		speed_danwei = (TextView) findViewById(R.id.speed_danwei);
	}

	private void setClickble() {
		int visibility = dial_delete_item.getVisibility();
		if (visibility == View.VISIBLE) {
			finish.setClickable(false);
			finish.setEnabled(false);
			delete_export_.setClickable(false);
			delete_export_.setEnabled(false);
			look_and_delete_des.setClickable(false);
			look_and_delete_des.setEnabled(false);
		} else {
			finish.setClickable(true);
			finish.setEnabled(true);
			delete_export_.setClickable(true);
			delete_export_.setEnabled(true);
			look_and_delete_des.setClickable(true);
			look_and_delete_des.setEnabled(true);
		}
	}

	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (del_info_cancle.getVisibility() == View.VISIBLE) {// 正在删除时,onGestureListener失效
				return true;
			}

			dial_delete_item.setVisibility(View.GONE);
			setClickble();
			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			if (del_info_cancle.getVisibility() == View.VISIBLE) {// 正在删除时,onGestureListener失效
				return true;
			}
			dial_delete_item.setVisibility(View.GONE);
			setClickble();
			return super.onDown(e);
		}
	};

	private void initRankData(final CleanFileStatusPkg pkg) {
		final SortRet localData = RankManager.getInstance().getLocalData(this);
		String cleanSpace = pkg.getCleanedSpace();
		if (null == cleanSpace) {
			cleanSpace = "0";
		}
		long space = Long.parseLong(cleanSpace);

		long s = (pkg.getEnd() - pkg.getStart()) / 1000;// ArithmeticException
		s = s <= 0 ? 1 : s;
		final long speed = space / s;
		localData.setSpeed(speed);
		/*
		 * localData.getSortdata().setTotal_cleanphotocount(
		 * localData.getSortdata().getTotal_cleanphotocount() +
		 * Long.parseLong(pkg.getHandlePicNumber()));
		 */
		localData.getSortdata().setTotal_transfersize(
				localData.getSortdata().getTotal_transfersize()
						+ Long.parseLong(pkg.getCleanedSpace()));

		if (localData.getSortdata().getTotal_transfersize_rank() == 0) {
			localData.getSortdata().setTotal_transfersize_rank(
					10000000 - Long.parseLong(pkg.getCleanedSpace()));
		}

		/*
		 * localData.getSortdata().setTotal_cleanspace(
		 * localData.getSortdata().getTotal_cleanspace() +
		 * Long.parseLong(pkg.getCleanedSpace()));
		 */

		localData.getSortdata().setTotal_transfercount(
				localData.getSortdata().getTotal_transfercount()
						+ Long.parseLong(pkg.getHandlePicNumber()));

		if (localData.getSortdata().getTotal_transfer_count_rank() == 0) {
			localData.getSortdata().setTotal_transfer_count_rank(
					(10000 - Long.parseLong(pkg.getHandlePicNumber())));
		}

		localData.getSortdata().setTotal_transfersize_rank(
				Long.parseLong(pkg.getCleanedSpace()) / 1000);
		// initRankDataView(localData,true);
		setSpeedText(speed, true);
		initRankDataView(localData);

		new Thread() {
			public void run() {
				final SortRet netData = RankManager.getInstance().uploadData(
						Tools.getAndroidDeviceID(getApplicationContext()),
						pkg.getCleanedSpace(), pkg.getHandlePicNumber(),
						"" + speed);
				netData.setSpeed(speed);
				if (netData.getStatus() >= 0) {
					runOnUiThread(new Runnable() {
						@Override
						public void run() {
							initRankDataView(netData);
						}
					});
					RankManager.getInstance().saveSortRetData(
							getApplicationContext(), netData);
				} else {
					RankManager.getInstance().saveSortRetData(
							getApplicationContext(), localData);
					RankManager.getInstance().saveunUploadData(
							getApplicationContext(), netData.getMessage());
				}
				RankManager.getInstance()
						.updateOldData(getApplicationContext());
			}
		}.start();
	}

	private void initRankDataView(SortRet localData) {

		if (localData != null && localData.getSortdata() != null) {
			double percent = (double) (localData.getSortdata()
					.getTotal_speed_rank() - localData.getSortdata()
					.getYour_speed_rank())
					/ localData.getSortdata().getTotal_speed_rank();
			if (percent >= 1.0) {
				percent = 0.999;
			}
			DecimalFormat df = new DecimalFormat("#%");
			setRankData(localData.getSpeed(), df.format(percent));
			totalNum2.setCartoonText(localData.getSortdata()
					.getTotal_transfercount()+"");
//			totalNum2.setCartoonText(localData.getSortdata()
//					.getTotal_transfercount() + getString(R.string.Dwpic));
			totalNum4.setCartoonText(""
					+ localData.getSortdata().getYour_transfer_count_rank());
			totalSize2.setCartoonText(SpaceUtil.convertSize(localData
					.getSortdata().getTotal_transfersize(), false));
			totalSize4.setCartoonText(""
					+ localData.getSortdata().getYour_transfersize_rank());
		}
	}

	private void setRankData(long speedNum, String percent) {
		String result = getString(R.string.finish_des_chijiao);
		String temp = "";
		if (speedNum >= 8 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_boying);
			temp = getString(R.string.finish_name_yuzhou);
			speed_icon.setImageResource(R.drawable.img_title_10);
		} else if (speedNum >= 7 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_boying);
			temp = getString(R.string.finish_name_changzheng);
			speed_icon.setImageResource(R.drawable.img_title_9);
		} else if (speedNum >= 6 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_boying);
			temp = getString(R.string.finish_name_boying);
			speed_icon.setImageResource(R.drawable.img_title_8);
		} else if (speedNum >= 5 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_sangtana);
			temp = getString(R.string.finish_name_falali);
			speed_icon.setImageResource(R.drawable.img_title_7);
		} else if (speedNum >= 4 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_sangtana);
			temp = getString(R.string.finish_name_sangta);
			speed_icon.setImageResource(R.drawable.img_title_6);
		} else if (speedNum >= 3 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_yadong);
			temp = getString(R.string.finish_name_xialipu);
			speed_icon.setImageResource(R.drawable.img_title_5);
		} else if (speedNum >= 2 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_yadong);
			temp = getString(R.string.finish_name_yadong);
			speed_icon.setImageResource(R.drawable.img_title_4);
		} else if (speedNum >= 1 * DANWEI * DANWEI) {
			result = getString(R.string.finish_des_yongjiu);
			temp = getString(R.string.finish_name_yongjiu);
			speed_icon.setImageResource(R.drawable.img_title_3);
		} else if (speedNum >= 200 * DANWEI) {
			result = getString(R.string.finish_des_moca);
			temp = getString(R.string.finish_name_moca);
			speed_icon.setImageResource(R.drawable.img_title_2);
		} else {
			result = getString(R.string.finish_des_chijiao);
			temp = getString(R.string.finish_name_chijiao);
			speed_icon.setImageResource(R.drawable.img_title_1);
		}
		result = String.format(result, percent, temp);
		SpannableStringBuilder builder = new SpannableStringBuilder(result);
		ForegroundColorSpan span = new ForegroundColorSpan(getResources()
				.getColor(R.color.yellow_speed));// 文字颜色
		ForegroundColorSpan span1 = new ForegroundColorSpan(getResources()
				.getColor(R.color.yellow_speed));
		StyleSpan sty = new StyleSpan(Typeface.BOLD_ITALIC); // 粗体
		int start = result.indexOf(temp);
		int end = start + temp.length();
		int start1 = result.indexOf(percent);
		int end1 = start1 + percent.length();
		if (end > start) {
			builder.setSpan(sty, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			builder.setSpan(span, start, end,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		if (end1 > start1) {
			builder.setSpan(span1, start1, end1,
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		speedLike.setText(builder);
	}

	private void setSpeedText(long speedNum, boolean isNeed) {
		if (speedNum > DANWEI * DANWEI) {
			double d = (double) speedNum / (double) (DANWEI * DANWEI);
			String re = ((int) (d * 10)) / 10 + "." + ((int) (d * 10)) % 10;
			if (isNeed) {
				speed.setCartoonText(re);
			} else {
				speed.setText(re);
			}
			speed_danwei.setText(getString(R.string.Dwsize_mb_speed));
		} else if (speedNum > DANWEI) {
			double d = (double) speedNum / (double) (DANWEI);
			String re = ((int) (d * 10)) / 10 + "." + ((int) (d * 10)) % 10;
			if (isNeed) {
				speed.setCartoonText(re);
			} else {
				speed.setText(re);
			}
			speed_danwei.setText(getString(R.string.Dwsize_kb_speed));
		} else {
			if (isNeed) {
				speed.setCartoonText("" + speedNum);
			} else {
				speed.setText("" + speedNum);
			}
			speed_danwei.setText(getString(R.string.Dwsize_b_speed));
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (null != gestureDetector) {
			gestureDetector.onTouchEvent(event);
		}
		return super.onTouchEvent(event);
	}

	private void initHandlePicTip() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			int status = pkg.getCleanStatus();
			String dealTip = getString(R.string.deal_pic_done);
			// if (CleanFileStatusPkg.CLEAN_INTERRUPT == status) {
			// dealTip = getString(R.string.interrupt_deal_pic_done);
			// }

			TextView dealPicTV = (TextView) this
					.findViewById(R.id.deal_pic_tip);
			dealPicTV.setText(dealTip);
		} catch (Exception e) {
			FLog.e(TAG, "initHandlePicTip throw error", e);
		}
	}

	private List<?> exportPhotos;

	/**
	 * export到finish界面,实际处理的照片数目的字符串形式.
	 */
	private String dealInfoStr;

	/**
	 * export到finish界面,实际处理的照片数目.
	 */
	private int dealInfo;

	/**
	 * 删除照片的监听器
	 */
	private GotDelInfo delInfo;

	/**
	 * 查看后删除,剩余个数
	 */
	private long remain;

	class GotDelInfo implements IPhotoManagerListener {

		@Override
		public void onDeletePhotosProgress(long deletedSize, double percent) {

		}

		@Override
		public void onPhoto(List<?> photos) {
			exportPhotos = photos;
			// number = exportPhotos.size();
			FLog.d(TAG, "要处理的个数 : " + exportPhotos.size());
		}

		@Override
		public void onDeletePhoto(final List<?> photos) {
			if (remain == 0) {
				remain = dealInfo;
			}
			--remain;
			FLog.d(TAG, "要处理的照片总数:" + exportPhotos.size() + "---还剩:" + remain);
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					del_info_num.setText(String.format(
							getString(R.string.delete), remain + ""));
					if (remain <= 0) {
						checkDeleteInformation();
					}
				}
			});
		}

		@Override
		public void onPhotosSize(long size) {

		}

		@Override
		public void onScanFinished() {

		}

		@Override
		public void onDeleteFinished() {
			// TODO Auto-generated method stub

		}

	}
}
