package com.clean.space;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.method.ScrollingMovementMethod;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.onekeyshare.OnekeyShare;

import com.clean.space.UpdateModule.UpdateListener;
import com.clean.space.log.FLog;
import com.clean.space.network.discover.DiscoverManager;
import com.clean.space.network.discover.WifiApManager;
import com.clean.space.network.http.Server;
import com.clean.space.photomgr.AllPhotoManager;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.FileUtil;
import com.clean.space.util.ReadMataDataUtil;
import com.clean.space.util.SpaceUtil;

/**
 * @Des 主界面
 */
public class MainActivity extends Activity {

	private final String TAG = "MainActivity";

	private TextView PicSize;
	private TextView freeSize;
	// private TextView export_danwei;
	private TextView wantClearSize;
	private RelativeLayout clearSizeGradeFrag_;
	private ListView listViewClearSizeGrade;
	private LinearLayout selectClearSizeRoot;
	private MyAdapater mAdapter;
	private final int SCAN_SUCCEED = 1;
	private Button outAndClear;
	private Button justExport;

	private TextView all_pic_size_danwei;
	private TextView freespace_of_phone_danwei;
	// private TextView look_for_selected;
	private RelativeLayout main_root;
	private RelativeLayout main_circle_spaceinfo_root;
	private ImageView menu;
	private View include_menu;
	private View dialog_;
	private TextView dialog_update;
	private TextView dialog_cancle;
	private TextView v;
	private TextView describe;
	private ImageView en_iv1;
	private ImageView en_iv2;
	private TextView en_tv1;
	private TextView en_tv2;
	private View menu_root;
	// String[] sizeGrade = { "0.5", "1.0", "2.0", "4.0", "8.0" };
	String[] sizeGrade = null;

	public long fileSize = 0;
	public long fileNumber = 0;
	private GestureDetector gestureDetector;
	private int mPrevSelectedPos = 3;

	private ScaleAnimation circleAnimation;
	private LinearLayout share;
	private LinearLayout feelback;
	private LinearLayout about;
	private LinearLayout exit;
	private LinearLayout developer;

	private View feedbackPanel;
	private View backFeedback;
	private EditText feedbackMessage;
	private Button feedbackSubmit;
	private BaseApplication application = null;

	private TextView seleted_size;// 选择的大小
	private TextView seleted_size_danwei;// 选择的大小

	private TextView developerModel;// 长按开启开发者模式
	private ImageView cleanIv;

	private long oneMounthSize = 0;
	private long threeMounthSize = 0;
	private long sixMounthSize = 0;
	private long oneYearSize = 0;
	private long stayTime = 0;

	// public long getWantClearSize() {
	// long size = fileSize;
	// try {
	// String clear = (String) wantClearSize.getText();
	// final String gb = "GB";
	// if (clear.contains(gb)) {
	// clear = clear.replace(gb, "");
	// size = (long) (Double.parseDouble(clear) * 1024 * 1024 * 1024);
	// }
	// } catch (Exception e) {
	// FLog.e(TAG, "getWantClearSize throw error", e);
	// }
	// return size;
	// }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		try {
			application = (BaseApplication) getApplication();

			getDatasFromResourse();

			checkUpdate();

			initView();

			initDeveloperMethod();

			initFeedbackPanelView();

			initMainRootAnimation();

			initCircleAnimation();

			initEvent();
			StatisticsUtil.getInstance(getApplicationContext(),
					StatisticsUtil.TYPE_UMENG).onCreate();

			// Intent intent = getIntent();

			// fileSize = intent.getLongExtra("fileSize", 0);
			// fileNumber = intent.getLongExtra("fileNumber", 0);

			// if (0 == fileSize) {
			// scanFile();
			// } else {
			// updateUi();
			// }
			// setSelectExportSizeList();

		} catch (Exception e) {
			FLog.e(TAG, "getWantClearSize throw error", e);
		}
	}

	/**
	 * 微信分享,需要打包安装后测试.
	 */
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

	/**
	 * 获取字符串数组:一个月前的,三个月前的.....
	 */
	private void getDatasFromResourse() {
		sizeGrade = getResources().getStringArray(R.array.datasGrade);
	}

	long[] size = new long[5];

	private void initView() {
		setContentView(R.layout.main);
		try {

			seleted_size = (TextView) findViewById(R.id.seleted_size);
			seleted_size_danwei = (TextView) findViewById(R.id.seleted_size_danwei);

			main_root = (RelativeLayout) findViewById(R.id.main_root);// 整个界面的跟布局
			main_circle_spaceinfo_root = (RelativeLayout) findViewById(R.id.main_circle_spaceinfo_root);// 环形内存信息的跟布局
			selectClearSizeRoot = (LinearLayout) findViewById(R.id.tv_wantClearSize_root);// 要清理大小的跟布局
			main_circle_spaceinfo_root.setVisibility(View.INVISIBLE);
			menu = (ImageView) findViewById(R.id.menu);
			include_menu = findViewById(R.id.include_menu);
			menu_root = findViewById(R.id.menu_root);
			developerModel = (TextView) findViewById(R.id.app_name);// 长按显示开发者模式

			// 更新对话框
			dialog_ = findViewById(R.id.dialog);
			dialog_update = (TextView) findViewById(R.id.dialog_update);
			dialog_cancle = (TextView) findViewById(R.id.dialog_cancle);
			v = (TextView) findViewById(R.id.v);
			// dialog_listview = (ListView) findViewById(R.id.dialog_listview);
			describe = (TextView) findViewById(R.id.describe);

			en_iv1 = (ImageView) findViewById(R.id.en_iv1);
			en_iv2 = (ImageView) findViewById(R.id.en_iv2);
			en_tv1 = (TextView) findViewById(R.id.en_tv1);
			en_tv2 = (TextView) findViewById(R.id.en_tv2);

			cleanIv = (ImageView) findViewById(R.id.clear_change_size);// 切换出listView的小图标

			share = (LinearLayout) findViewById(R.id.menu_share);
			feelback = (LinearLayout) findViewById(R.id.feelback);
			about = (LinearLayout) findViewById(R.id.about);
			exit = (LinearLayout) findViewById(R.id.exit);
			developer = (LinearLayout) findViewById(R.id.developer);// 开发者模式

			PicSize = (TextView) findViewById(R.id.all_pic_size);// 照片总大小
			freeSize = (TextView) findViewById(R.id.freespace_of_phone);// 存储空间剩余
			all_pic_size_danwei = ((TextView) findViewById(R.id.all_pic_size_danwei));// 照片总大小的单位
			freespace_of_phone_danwei = ((TextView) findViewById(R.id.freespace_of_phone_danwei));// 存储空间剩余的单位

			wantClearSize = ((TextView) findViewById(R.id.tv_wantClearSize));// 要腾出空间
			clearSizeGradeFrag_ = (RelativeLayout) findViewById(R.id.select_grade_fraglatout);// listView跟布局

			listViewClearSizeGrade = (ListView) findViewById(R.id.listview);// listView
			mPrevSelectedPos = UserSetting.getInt(MainActivity.this,
					"position", mPrevSelectedPos);

			countSelectedByDate(mPrevSelectedPos);

			listViewClearSizeGrade.setSelection(mPrevSelectedPos);

			justExport = (Button) findViewById(R.id.just_export);// 仅导出
			outAndClear = (Button) findViewById(R.id.Btn_clear);

			gestureDetector = new GestureDetector(MainActivity.this,
					onGestureListener);

		} catch (Exception e) {
		}
	}

	private void countSelectedByDate(int selete) {
		String eventID = "";
		switch (selete) {
		case 0:
			eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_SELECT_BEFORE_ONE_MON_EXPORT;
			break;

		case 1:
			eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_SELECT_BEFORE_THREE_MON_EXPORT;
			break;

		case 2:
			eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_SELECT_BEFORE_SIX_MON_EXPORT;
			break;

		case 3:
			eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_SELECT_BEFORE_ONE_YEAR_EXPORT;
			break;

		case 4:
			eventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_SELECT_BEFORE_ALL_EXPORT;
			break;

		default:
			break;
		}

		if (!"".equals(eventID)) {

			StatisticsUtil.getDefaultInstance(getApplicationContext())
					.onEventCount(eventID);
		}
	}

	private void initMainRootAnimation() {
		Animation animation = AnimationUtils.loadAnimation(MainActivity.this,
				R.anim.main_down2up);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				main_circle_spaceinfo_root.startAnimation(circleAnimation);
				main_circle_spaceinfo_root.setVisibility(View.VISIBLE);
			}
		});

		main_root.startAnimation(animation);

	}

	private void initCircleAnimation() {
		circleAnimation = new ScaleAnimation(0f, 1f, 0f, 1f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		circleAnimation.setDuration(400);
		circleAnimation.setFillAfter(true);

	}

	private void setSelectExportSizeList() {
		initExportSizeList();
		if (newData.size() == 1) {
			selectClearSizeRoot.setEnabled(false);
			selectClearSizeRoot.setClickable(false);
			cleanIv.setVisibility(View.GONE);
			wantClearSize.setTextSize(20f);
			wantClearSize.setText(newData.get(0));
			// export_danwei.setVisibility(View.GONE);
		}
		newData.clear();
	}

	private void jump2ScanningActivity() {
		Intent intent = new Intent(MainActivity.this, ScanningActivity.class);

		// UserSetting.setWantCleanSize(MainActivity.this, getWantClearSize());
		// intent.putExtra("wantClearSize", getWantClearSize());// 传入要清理的数据大小
		startActivity(intent);
		finish();
	}

	private void initEvent() {
		try {
			selectClearSizeRoot.setEnabled(true);
			selectClearSizeRoot.setClickable(true);
			exit.setClickable(true);
			OnClickListener clickListener = new OnClickListener() {

				Context context = getApplicationContext();

				@Override
				public void onClick(View v) {
					String eventId = "";
					if (v != null) {
						switch (v.getId()) {

						case R.id.Btn_clear:// 清理的界面
							Intent intent = new Intent(MainActivity.this,
									CleanPhotoActivity.class);
							startActivity(intent);
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_MANAGE_PHOTO;
							break;

						case R.id.just_export:// 仅导出
							UserSetting.setBoolean(MainActivity.this,
									Constants.CHECK_EXPORT_AND_CLEAN, false);
							jump2ScanningActivity();
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_EXPORT;
							break;

						case R.id.tv_wantClearSize_root:
							clearSizeGradeFrag_.setVisibility(View.VISIBLE);
							listViewClearSizeGrade.setVisibility(View.VISIBLE);
							initExportSizeList();
							break;

						case R.id.menu:// 主界面/左侧菜单menu按钮.
							popMenu();
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_MENU;
							break;
						case R.id.menu_share:// 分享
							popMenu();
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
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_SHARE;
							break;
						case R.id.exit:// 退出
							application.setShowSetNotification(true);
							Intent service = new Intent(MainActivity.this,
									Server.class);
							stopService(service);
							putSharedPreferencesKeyValue(
									Constants.SETTING_KEY_EXIT,
									Constants.SETTING_VALUE_EXIT);
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_EXIT;
							finish();
							break;
						case R.id.feelback:// 反馈.
							feedbackPanel.setVisibility(View.VISIBLE);
							hideLeftMenu();// 隐藏左侧菜单与否
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_FEEDBACK;
							break;

						case R.id.feedback_submit:// 提交.
							hideInputMethod();// 隐藏输入法
							String message = feedbackMessage.getText()
									.toString();
							if (message != null && message.length() > 0) {
								feedbackSubmit.setText(R.string.committing);
								feedbackSubmit.setEnabled(false);
								feebackToServer(message);
							} else {
								Toast.makeText(getApplication(),
										R.string.menu_help_reply_tip,
										Toast.LENGTH_SHORT).show();
							}
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_FEEDBACK_SUBMIT;
							break;

						case R.id.feedback_action_bar:
							hideInputMethod();
							feedbackPanel.setVisibility(View.GONE);
							break;

						case R.id.about:// 关于
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_MENU_ABOUT;
							AlertDialog.Builder builder = new AlertDialog.Builder(
									MainActivity.this);
							builder.setTitle(R.string.about);
							String strMsg = String.format(
									getString(R.string.about_message),
									getVersionCode());
							builder.setMessage(strMsg);
							builder.setCancelable(false);
							builder.setPositiveButton(R.string.ok, null);

							final AlertDialog dialog = builder.create();
							dialog.show();
							break;
						case R.id.developer:// 开发者模式
							String appStore = ReadMataDataUtil.getChannel(MainActivity.this);
							
							UpdateModule.getInstance().developerUpdate( MainActivity.this,appStore,getResources().getConfiguration().locale);
							eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_MENU_DEVELOP;
							break;
						case R.id.dialog_update:// 更新

							dialog_.setVisibility(View.GONE);
							setTrueForDialogVisible();
							UpdateModule.getInstance().startUpdate();
							StatisticsUtil
									.getDefaultInstance(MainActivity.this)
									.onEventCount(
											Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_UPDATA_P);
							break;

						case R.id.dialog_cancle:// 取消更新
							dialog_.setVisibility(View.GONE);
							setTrueForDialogVisible();
							StatisticsUtil
									.getDefaultInstance(MainActivity.this)
									.onEventCount(
											Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_UPDATA_N);
							break;
						default:
							break;
						}
					}

					if (!"".equals(eventId)) {
						StatisticsUtil.getDefaultInstance(
								getApplicationContext()).onEventCount(eventId);
					}
				}
			};

			justExport.setOnClickListener(clickListener);
			outAndClear.setOnClickListener(clickListener);
			selectClearSizeRoot.setOnClickListener(clickListener);
			// icon_more_down.setOnClickListener(clickListener);
			// icon_more_down_up.setOnClickListener(clickListener);
			menu.setOnClickListener(clickListener);
			// look_for_selected.setOnClickListener(clickListener);

			share.setOnClickListener(clickListener);
			feelback.setOnClickListener(clickListener);
			about.setOnClickListener(clickListener);
			exit.setOnClickListener(clickListener);
			developer.setOnClickListener(clickListener);

			feedbackPanel.setOnClickListener(clickListener);
			backFeedback.setOnClickListener(clickListener);
			feedbackMessage.setOnClickListener(clickListener);
			feedbackSubmit.setOnClickListener(clickListener);

			dialog_update.setOnClickListener(clickListener);
			dialog_cancle.setOnClickListener(clickListener);

			circleAnimation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {

				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					setButtonDefaultClickble();
				}
			});

		} catch (Exception e) {
			FLog.e(TAG, "initEvent throw error", e);
		}
	}

	private void setButtonDefaultClickble() {
		int int1 = UserSetting.getInt(getApplicationContext(), "position", 3);
		long selectedL = size[int1];
		setCleanData(int1);
		UserSetting.setLong(getApplicationContext(), "cleansize", selectedL);
		UserSetting.setInt(getApplicationContext(), "position", int1);
		String s = SpaceUtil.convertSize(selectedL, 0);
		seleted_size.setText(s);
		setDanwei(selectedL, seleted_size_danwei);

		setClickble();
	}

	private void hideInputMethod() {
		InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		View focus = MainActivity.this.getCurrentFocus();
		if (focus != null) {
			imm.hideSoftInputFromWindow(MainActivity.this.getCurrentFocus()
					.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);// 仅关闭输入法
		}
	}

	private void hideLeftMenu() {
		if (include_menu.getVisibility() == View.VISIBLE) {
			dismissMenu();
			// menu.setVisibility(View.VISIBLE);
			menu.setImageResource(R.drawable.btn_menu);
			selectClearSizeRoot.setEnabled(true);
			selectClearSizeRoot.setClickable(true);
		}
	}

	private void initFeedbackPanelView() {
		feedbackPanel = findViewById(R.id.feedback_panel_activity);
		backFeedback = findViewById(R.id.feedback_action_bar);
		feedbackMessage = (EditText) findViewById(R.id.feedback_message);
		feedbackSubmit = (Button) findViewById(R.id.feedback_submit);

	}

	@SuppressLint("InflateParams")
	private void showExitWarmDialog() {
		View root = getLayoutInflater().inflate(R.layout.exit, null);
		final AlertDialog dialog = new AlertDialog.Builder(this).create();
		dialog.show();
		dialog.getWindow().setContentView(root);
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String eventid = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_SETTING_EXIT_GUI;
				if (v.getId() == R.id.exit_program) {
					Intent service = new Intent(MainActivity.this, Server.class);
					stopService(service);
					putSharedPreferencesKeyValue(Constants.SETTING_KEY_EXIT,
							Constants.SETTING_VALUE_EXIT);
					eventid = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_SETTING_EXIT_SERVICE;
				}
				StatisticsUtil.getDefaultInstance(MainActivity.this)
						.onEventCount(eventid);
				finish();
				dialog.dismiss();
			}

		};
		dialog.getWindow().findViewById(R.id.exit_program)
				.setOnClickListener(listener);
		dialog.getWindow().findViewById(R.id.back_to_screen)
				.setOnClickListener(listener);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void putSharedPreferencesKeyValue(String key, String value) {
		SharedPreferences sh = getSharedPreferences(Constants.LENOVOID,
				Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
		Editor editor = sh.edit();
		editor.putString(key, value);
		editor.apply();
		editor.commit();
	}

	@SuppressLint("SimpleDateFormat")
	protected void feebackToServer(final String message) {
		new Thread() {
			public void run() {
				String sMobileId = android.os.Build.MODEL;
				SimpleDateFormat sDateFormat = new SimpleDateFormat(
						"yyyyMMddHHmmssSSS");
				File zipFile = new File(FLog.LOG_PATH + "cleanspace"
						+ getVersionCode() + "_"
						+ sDateFormat.format(new Date()) + ".zip");
				boolean success = false;
				if (!zipFile.exists()) {
					try {
						zipFile.createNewFile();
						FLog.zipFile(
								new File(FLog.LOG_PATH
										+ FLog.ACTIVITY_LOG_FILE.replace("$1",
												getVersionCode())), zipFile);
						success = FeedbackNetHelp.feedback(sMobileId, message,
								zipFile);
						zipFile.delete();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				final boolean s = success;
				runOnUiThread(new Runnable() {
					public void run() {
						String tips = s ? getString(R.string.commint_success)
								: getString(R.string.commint_fail);
						if (s) {
							feedbackMessage.setText("");
							feedbackPanel.setVisibility(View.GONE);

						}
						feedbackSubmit.setText(R.string.commit);
						feedbackSubmit.setEnabled(true);
						Toast.makeText(getBaseContext(), tips,
								Toast.LENGTH_SHORT).show();
					}

				});
			}
		}.start();
	}

	private String getVersionCode() {
		String version = "";
		try {
			PackageManager manager = this.getPackageManager();
			PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
			FLog.e(TAG, "getVersionCode", e);
		}
		return version;
	}

	protected void popMenu() {
		if (include_menu.getVisibility() == View.VISIBLE) {
			dismissMenu();
			// menu.setVisibility(View.VISIBLE);
			menu.setImageResource(R.drawable.btn_menu);
			selectClearSizeRoot.setEnabled(true);
			selectClearSizeRoot.setClickable(true);
			outAndClear.setClickable(true);
			outAndClear.setEnabled(true);
			// if(newData.size() == 1){
			// selectClearSizeRoot.setEnabled(false);
			// selectClearSizeRoot.setClickable(false);
			// }else{
			// selectClearSizeRoot.setEnabled(true);
			// selectClearSizeRoot.setClickable(true);
			// }
		} else {
			displayMenu();
			menu.setImageResource(R.drawable.btn_menu_empty);
			selectClearSizeRoot.setEnabled(false);
			selectClearSizeRoot.setClickable(false);
			// menu.setVisibility(View.INVISIBLE);
			// selectClearSizeRoot.setEnabled(false);
			// selectClearSizeRoot.setClickable(false);
		}
	}

	private void displayMenu() {
		Animation animation = AnimationUtils
				.loadAnimation(this, R.anim.menu_in);
		include_menu.setVisibility(View.VISIBLE);
		menu_root.startAnimation(animation);
		// menu_root.setVisibility(View.VISIBLE);

		outAndClear.setClickable(false);
		outAndClear.setEnabled(false);
		justExport.setClickable(false);
		justExport.setEnabled(false);
		// icon_more_down.setClickable(false);
		// icon_more_down.setEnabled(false);

	}

	boolean Up = true;

	private void dismissMenu() {
		menu.setImageResource(R.drawable.btn_menu);
		if (include_menu.getVisibility() == View.VISIBLE) {
			Animation animation = AnimationUtils.loadAnimation(this,
					R.anim.menu_out);
			animation.setAnimationListener(new AnimationListener() {

				@Override
				public void onAnimationStart(Animation animation) {
					Up = false;
				}

				@Override
				public void onAnimationRepeat(Animation animation) {

				}

				@Override
				public void onAnimationEnd(Animation animation) {
					Up = true;
					include_menu.setVisibility(View.GONE);
				}
			});
			menu_root.startAnimation(animation);
			outAndClear.setClickable(true);
			outAndClear.setEnabled(true);
			setClickble();

		}
	}

	private void setClickble() {
		String text = (String) seleted_size.getText();
		if ("0.0".equals(text)) {
			justExport.setClickable(false);
			justExport.setEnabled(false);
			justExport.setBackgroundResource(R.drawable.btn_white_line_disable);
			justExport.setTextColor(Color.parseColor("#30FFFFFF"));
		} else {
			justExport.setClickable(true);
			justExport.setEnabled(true);
			justExport.setBackgroundResource(R.drawable.scan_btn_selector);
			justExport.setTextColor(Color.parseColor("#FFFFFF"));
		}
	}

	@Override
	public void onBackPressed() {
		if (clearSizeGradeFrag_.getVisibility() == View.VISIBLE
				&& include_menu.getVisibility() == View.VISIBLE) {
			clearSizeGradeFrag_.setVisibility(View.GONE);
			dismissMenu();
			menu.setImageResource(R.drawable.btn_menu);
			selectClearSizeRoot.setEnabled(true);
			selectClearSizeRoot.setClickable(true);
			return;
		} else if (dialog_.getVisibility() == View.VISIBLE) {
			if(dialog_cancle.getVisibility() != View.GONE){
				dialog_.setVisibility(View.GONE);
				setTrueForDialogVisible();
			}

		} else if (clearSizeGradeFrag_.getVisibility() == View.VISIBLE) {
			clearSizeGradeFrag_.setVisibility(View.GONE);
			return;
		} else if (feedbackPanel.getVisibility() == View.VISIBLE) {
			feedbackPanel.setVisibility(View.GONE);
			return;
		} else if (include_menu.getVisibility() == View.VISIBLE) {
			dismissMenu();
			// menu.setVisibility(View.VISIBLE);
			menu.setImageResource(R.drawable.btn_menu);
			selectClearSizeRoot.setEnabled(true);
			selectClearSizeRoot.setClickable(true);
			return;
		} else {
			super.onBackPressed();
		}

	}

	List<String> newData = new ArrayList<String>();

	protected void initExportSizeList() {
		// try {
		// newData.clear();
		// String picSizeGrade = SpaceUtil.convertToGb(fileSize, 1);
		// for (String item : sizeGrade) {
		// if (item.compareTo(picSizeGrade) < 0) {
		// newData.add(item + "GB");
		// }
		// }
		// newData.add("所有" + "(" + SpaceUtil.convertSize(fileSize, 1) + ""
		// + PicSize_danwei_info + ")");
		// } catch (Exception e) {
		// FLog.e(TAG, "initExportSizeList throw error", e);
		// }

		newData.clear();

		for (int i = 0; i < sizeGrade.length; i++) {
			newData.add(sizeGrade[i]);
		}

		if (mAdapter == null) {
			mAdapter = new MyAdapater();
		}
		listViewClearSizeGrade.setAdapter(mAdapter);
		listViewClearSizeGrade.setDividerHeight(0);
		listViewClearSizeGrade
				.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						clearSizeGradeFrag_.setVisibility(View.GONE);
						mPrevSelectedPos = position;
						setCleanData(position);
						String s = setSeletedSize(position);

						if ("0.0".equals(s)) {
							justExport.setClickable(false);
							justExport.setEnabled(false);
							justExport
									.setBackgroundResource(R.drawable.btn_white_line_disable);
							justExport.setTextColor(Color
									.parseColor("#30FFFFFF"));
						} else {
							justExport.setClickable(true);
							justExport.setEnabled(true);
							justExport
									.setBackgroundResource(R.drawable.scan_btn_selector);
							justExport.setTextColor(Color.parseColor("#FFFFFF"));
						}
					}

				});
	}

	private void setCleanData(int position) {
		String wantClearSizeStr = "";
		if ("zh".equals(language)) {
			switch (position) {
			case 0:
				wantClearSizeStr = getString(R.string.all_photo);

				break;

			case 1:
				wantClearSizeStr = getString(R.string.one_month);

				break;
			case 2:
				wantClearSizeStr = getString(R.string.three_month);

				break;
			case 3:
				wantClearSizeStr = getString(R.string.half_year);

				break;
			case 4:
				wantClearSizeStr = getString(R.string.one_year);

				break;
			default:
				break;
			}
			wantClearSize.setText(wantClearSizeStr);
		} else {
			wantClearSize.setText(sizeGrade[position]);
		}
	}

	private class MyAdapater extends BaseAdapter {

		@Override
		public int getCount() {
			return newData.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@SuppressLint("ViewHolder")
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			TextView v = (TextView) View.inflate(MainActivity.this,
					R.layout.main_listview_item, null);
			String strSize = newData.get(position);
			v.setText(strSize);

			return v;
		}

	}
	private void checkUpdate() {
		String appStore = ReadMataDataUtil.getChannel(MainActivity.this);
		UpdateModule.getInstance().autoUpate(MainActivity.this,appStore,getResources().getConfiguration().locale);
		UpdateModule.getInstance().registerUpdateListener(new UpdateListener() {
			
			@Override
			public void onUpdate(final String newVersion,final String oldVersion,final String desc,
					final boolean force) {
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						dialog_.setVisibility(View.VISIBLE);
						setFalseForDialogVisible();
						serUpdateInfo(newVersion,desc);
						if(force){
							if(dialog_cancle != null){
								dialog_cancle.setVisibility(View.GONE);
							}
						}
						
					}
				});
				
			}
		});
	}



	private void setFalseForDialogVisible() {
		selectClearSizeRoot.setEnabled(false);
		selectClearSizeRoot.setClickable(false);
		outAndClear.setClickable(false);
		outAndClear.setEnabled(false);
		justExport.setClickable(false);
		justExport.setEnabled(false);
		menu.setClickable(false);
		menu.setEnabled(false);
	}

	private void setTrueForDialogVisible() {
		selectClearSizeRoot.setEnabled(true);
		selectClearSizeRoot.setClickable(true);
		outAndClear.setClickable(true);
		outAndClear.setEnabled(true);
		justExport.setClickable(true);
		justExport.setEnabled(true);
		menu.setClickable(true);
		menu.setEnabled(true);
	}

	protected void serUpdateInfo(String version ,String description) {
		v.setText(String.format(getString(R.string.v), version));
		// description =
		// "1.选择你的选择\n2.选择你的选择\n3.选择你的选择\n4.选择你的选择\n5.选择你的选择\n6.选择你的选择\n7.选择你的选择";
		describe.setText(description);
		describe.setMovementMethod(new ScrollingMovementMethod());// 设置TextView可滚动.
	}

	public void updateUi() {

		String freeSpaceAndSize = SpaceUtil.getFreeSpace2();
		System.out.println(freeSpaceAndSize);
		String[] split = freeSpaceAndSize.split("/");
		String freeSpace = split[0];
		String size = split[1];
		long parseLong = Long.parseLong(size);

		PicSize.setText(SpaceUtil.convertSize(fileSize, 1));
		freeSize.setText(freeSpace);

		setDanwei(fileSize, all_pic_size_danwei);
		setDanwei(parseLong, freespace_of_phone_danwei);

		setButtonDefaultClickble();

		setSelectExportSizeList();

		// String s = setSeletedSize(mPrevSelectedPos);

		// scanFinish();
	}

	private void setDanwei(long l, TextView danwei) {
		if (l < 1024 * 1024) {
			danwei.setText(R.string.Dwsize_kb);

		} else if (l >= 1024 * 1024 && l < 1024 * 1024 * 1024) {
			danwei.setText(R.string.Dwsize_mb);

		} else {
			danwei.setText(R.string.Dwsize);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onStart();
	}

	/**
	 * 获取系统的当前语言环境,非中文状态下,隐藏左侧菜单部分内容.
	 */
	private void initLocalLanguage() {
		Locale locale = getResources().getConfiguration().locale;
		language = locale.getLanguage();
		FLog.i(TAG, "当前语言环境: " + language);
		if (!"zh".equals(language)) {

			if (en_iv1.getVisibility() == View.VISIBLE) {
				en_iv1.setVisibility(View.GONE);
				en_iv2.setVisibility(View.GONE);
				en_tv1.setVisibility(View.GONE);
				en_tv2.setVisibility(View.GONE);
			}
		} else {
			en_iv1.setVisibility(View.VISIBLE);
			en_iv2.setVisibility(View.VISIBLE);
			en_tv1.setVisibility(View.VISIBLE);
			en_tv2.setVisibility(View.VISIBLE);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onResume();
		// setSelectExportSizeList();

		scanFile();
		DiscoverManager.getInstance(getApplicationContext()).start();

		// 解决用户取消导出进入主界面,softap依然打开的情况
		WifiApManager.getInstance(this).disableSoftAp();

		initLocalLanguage();

		new Thread() {
			public void run() {
				FileUtil.saveNeedSharedPhoto2File(MainActivity.this,
						Constants.SHAREPATH);
			}
		}.start();
	}

	@Override
	protected void onPause() {
		super.onPause();
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		UserSetting.setBoolean(MainActivity.this, Constants.DOWNLOAD_START,
				true);
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onDestroy();
		
		UpdateModule.getInstance().unRegisterUpdateListener();
	}

	@Override
	protected void onStop() {
		super.onStop();
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onStop();
	}

	// private void scanFinish() {
	// mainScanning.setVisibility(View.INVISIBLE);// 扫描进度条消失
	// }

	private void scanFile() {
		new Thread(new Runnable() {

			public void run() {
				try {
					AllPhotoManager photoMgr = (AllPhotoManager) PhotoManagerFactory
							.getInstance(MainActivity.this,
									PhotoManagerFactory.PHOTO_MGR_ALL);
					photoMgr.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_DESC, 0);
					fileSize = photoMgr.getFileSize();
					fileNumber = photoMgr.getFileNumber();

					oneMounthSize = photoMgr.get1MounthSize();
					threeMounthSize = photoMgr.get3MounthSize();
					sixMounthSize = photoMgr.get6MounthSize();
					oneYearSize = photoMgr.get12MounthSize();

					size[0] = fileSize;
					size[1] = oneMounthSize;
					size[2] = threeMounthSize;
					size[3] = sixMounthSize;
					size[4] = oneYearSize;

					Message msg = new Message();
					msg.what = SCAN_SUCCEED;
					mHandler.sendMessage(msg);
				} catch (Exception e) {
					FLog.e(TAG, "scanFile throw error", e);
				}
			}
		}).start();
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case SCAN_SUCCEED:
				updateUi();
				break;

			default:
				break;
			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		gestureDetector.onTouchEvent(event);
		return super.onTouchEvent(event);
	}

	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {

			float x = e2.getX() - e1.getX();
			float y = e2.getY() - e1.getY();

			if (x > 30 && Math.abs(x) > Math.abs(y) && Math.abs(x) > 100) {
				if (include_menu.getVisibility() != View.VISIBLE) {
					displayMenu();
					menu.setImageResource(R.drawable.btn_menu_empty);
					return true;
				}
			}

			if (x < 0 && include_menu.getVisibility() == View.VISIBLE
					&& Up == true && Math.abs(x) > Math.abs(y)
					&& Math.abs(x) > 80) {
				dismissMenu();
				menu.setImageResource(R.drawable.btn_menu);
				selectClearSizeRoot.setEnabled(true);
				selectClearSizeRoot.setClickable(true);
				return true;
			}

			return false;
		}

		@Override
		public boolean onDown(MotionEvent e) {
			displayList();
			// if (include_menu.getVisibility() == View.VISIBLE && Up == true) {
			// dismissMenu();
			// menu.setImageResource(R.drawable.btn_menu);
			// selectClearSizeRoot.setEnabled(true);
			// selectClearSizeRoot.setClickable(true);
			// return true;
			// }

			if (dialog_.getVisibility() == View.VISIBLE) {
				dialog_.setVisibility(View.GONE);
				setTrueForDialogVisible();
			}
			return super.onDown(e);
		}
	};

	// private UpdateAdapter uAdapter;

	private String version;

	private String description;

	private String language;

	private void displayList() {
		if (null != listViewClearSizeGrade
				&& listViewClearSizeGrade.getVisibility() == View.VISIBLE) {
			listViewClearSizeGrade.setVisibility(View.GONE);
		}
	}

	private void initDeveloperMethod() {
		developerModel.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				if (developer.getVisibility() == View.VISIBLE) {
					developer.setVisibility(View.GONE);
					Toast.makeText(MainActivity.this, R.string.developer_close,
							Toast.LENGTH_SHORT).show();
				} else {
					developer.setVisibility(View.VISIBLE);
					Toast.makeText(MainActivity.this, R.string.developer_open,
							Toast.LENGTH_SHORT).show();
				}
				// checkConnectState();
				return false;
			}

		});
	}

	private String setSeletedSize(int position) {
		long selectedL = size[position];
		UserSetting.setLong(getApplicationContext(), "cleansize", selectedL);
		UserSetting.setInt(getApplicationContext(), "position", position);
		String s = SpaceUtil.convertSize(selectedL, 0);
		seleted_size.setText(s);
		setDanwei(selectedL, seleted_size_danwei);
		setClickble();
		return s;
	}

}
