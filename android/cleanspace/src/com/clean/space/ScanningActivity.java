package com.clean.space;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clean.space.db.DBMgr;
import com.clean.space.exportrule.ExportByDateTime;
import com.clean.space.log.FLog;
import com.clean.space.network.discover.ConnectManager;
import com.clean.space.network.discover.ConnectManager.StateChanager;
import com.clean.space.network.discover.DiscoverManager;
import com.clean.space.network.discover.DiscoverManager.Receiver;
import com.clean.space.network.discover.WifiApManager;
import com.clean.space.network.http.ServerManager;
import com.clean.space.onedriver.DriverManagerFactroy;
import com.clean.space.onedriver.OneDriveUploadManage;
import com.clean.space.photomgr.AllPhotoManager;
import com.clean.space.photomgr.IPhotoManager;
import com.clean.space.photomgr.PhotoManagerFactory;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.protocol.CurrentExportedImageItem;
import com.clean.space.protocol.FileItem;
import com.clean.space.protocol.PCClientItem;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.GifView;
import com.clean.space.util.KeepUserNetwork;
import com.cleanspace.lib.onedriverlib.OneDriveAuthListener;
import com.cleanspace.lib.onedriverlib.OneDriveAuthManager;
import com.cleanspace.lib.onedriverlib.OneDriveAuthSession;
import com.cleanspace.lib.onedriverlib.OneDriveAuthStatus;
import com.cleanspace.lib.onedriverlib.OneDriveManagerFactory;
import com.cleanspace.lib.onedriverlib.OneDriveOperationListener;
import com.microsoft.onedriveaccess.model.Drive;

/**
 * 从DiscoverManager读出已发现的记录存放到mConnectedServers集合中<br>
 * 判断上次是否存在有已连接过的电脑<br>
 * 判断连接过的电脑是否存在已发现列表中<br>
 * 
 * 已发现列表为空 则显示搜素动画<br>
 * 已发现列表不为空 则显示列表<br>
 * 已发现列表中存在上次连接过的电脑，
 * 
 * @Des 寻找电脑的界面
 */
public class ScanningActivity extends Activity implements Receiver {

	private static final String TAG = ScanningActivity.class.getSimpleName();

	public static final int CHOOSE = 1;
	public static final int NOCHOOSE = 0;
	public static final int FIRSTTAG = 1;
	public static final int THIRDTAG = 3;
	public static final int SELECTEDITEM = 2;// 自动选择的item
	public static final int TIMEDELAY = 5 * 1000;// 没有电脑时动画延时显示时间
	// public static final int AUTOTIMEDELAY = 15 * 1000;// 有电脑时动画延时显示时间

	private ImageView tagProgressBar;// 标题栏pb
	// private ImageView select_pc;
	private LinearLayout tagBack;// 标题栏返回按钮

	private TextView sanningYourPC_Des;// 寻找PC描述
	private ListView PCList;// 电脑列表
	private Button btnStart;

	private RelativeLayout main_circle_spaceinfo;
	private RelativeLayout pc_backgroud;// 点击开始, 连接电脑的item
	private ImageView connect_pc_icon;
	private TextView currLinkNetInfo;// 当前连接网络的信息
	private TextView currLinkNetInfo1;
	private TextView castDes;
	private TextView PCName;
	private View retry;
	private View clean;
	private List<PCClientItem> adapterDatas = new ArrayList<PCClientItem>();
	private List<PCClientItem> mConnectedServers = new ArrayList<PCClientItem>();
	private MyListAdapater mListAdapater;
	private PCClientItem chooseItem;

	private RotateAnimation ra;
	private ConnectManager manager;
	private String selectedLast;
	private int type = PCClientItem.OTHER;
	private ProgressBar pb;

	private GifView findGif;
	private GifView not_find_one;
	private GifView not_find_two;

	private View driver;
	private TextView login;
	private TextView loginDesc;
	private TextView logout;
	private ImageView driveChoose;

	private Handler myHandler = new Handler();

	/**
	 * 显示的寻找电脑的根布局
	 */
	private RelativeLayout findIconDowmRoot;

	/**
	 * 隐藏的寻找电脑的根布局
	 */
	private RelativeLayout findIconDowmRoot_1;

	/**
	 * true 表示列表隐藏 false 表示列表显示
	 */
	private boolean isHide = false;
	private Runnable stateTask = new Runnable() {

		@Override
		public void run() {
			// 修改如果数据源没有变化，则无需通知UI更新
			if (checkLivePCClients()) {
				refresh();
			}
			myHandler.postDelayed(this, Constants.CHECK_PC_LIVE_TIME);
		}
	};

	private Runnable findGifTask = new Runnable() {

		@Override
		public void run() {
			not_find_one.setVisibility(View.GONE);
			not_find_two.setVisibility(View.VISIBLE);

			if (findGifTask != null) {
				myHandler.removeCallbacks(findGifTask);
				findGifTask = null;
			}

		}
	};

	// private ImageView hide_pc_list_pic;// 附近电脑旁边的小图标
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();// 初始化控件

		SetConnectedWifiName();// 获取当前WiFi名称

		initData();

		initTagProgressBar();

		startTagarAnimation();

		initEvent();// 初始化事件

		scanPC();

		autoUpScanInfo();

	}

	/**
	 * 延时15s弹出寻找电脑的信息
	 */
	protected void autoUpScanInfo() {
		myHandler.postDelayed(runtask, TIMEDELAY);
	}

	/**
	 * 定时任务延时15s显示gifview
	 */
	private Runnable runtask = new Runnable() {
		@Override
		public void run() {
			not_find_one.setVisibility(View.VISIBLE);
			initFindGifView();
			if (runtask != null) {
				myHandler.removeCallbacks(runtask);
				runtask = null;
			}
		}
	};

	//
	// private boolean auto = false;
	// Runnable runtask = new Runnable() {
	// @Override
	// public void run() {
	// if (mConnectedServers.size() == 0 && !tagClick
	// && pc_backgroud.getVisibility() != View.VISIBLE) {
	// playFindAnimation();
	// } else if (mConnectedServers.size() != 0 && !tagClick
	// && pc_backgroud.getVisibility() != View.VISIBLE) {
	// auto = true;
	// playFindAnimation();
	// } else {
	// myHandler.removeCallbacks(runtask);
	// }
	// }
	// };

	private void initFindGifView() {
		myHandler.postDelayed(findGifTask, 2600);
	}

	/**
	 * 开启标题栏旋转的动画
	 */
	private void startTagarAnimation() {
		tagProgressBar.startAnimation(ra);
	}

	/**
	 * 初始化可以一直旋转的动画
	 */
	private void initTagProgressBar() {
		ra = new RotateAnimation(0, 359, Animation.RELATIVE_TO_SELF, 0.5f,
				Animation.RELATIVE_TO_SELF, 0.5f);

		ra.setDuration(1000);
		ra.setStartOffset(0);
		ra.setRepeatCount(-1);
		ra.setRepeatMode(Animation.INFINITE);

		ra.setInterpolator(new Interpolator() {

			@Override
			public float getInterpolation(float input) {

				return input;
			}
		});

	}

	/**
	 * 初始化数据
	 */
	private void initData() {

		try {
			selectedLast = UserSetting.getString(getApplicationContext(),
					Constants.SELECTEDPCNAME, "");

			if (mListAdapater == null) {
				mListAdapater = new MyListAdapater();
			}
			PCList.setAdapter(mListAdapater);
			PCList.setDividerHeight(0);
			// PCList.setOnItemClickListener(listitemListener);
			mConnectedServers = DiscoverManager.getInstance(this)
					.getOnlinePcItem();
			refresh();
			setDefaulChoose();
		} catch (Exception e) {
			FLog.e(TAG, "initData throw error");
		}
	}

	/**
	 * 设置默认选中的电脑,如果没有,不选中任何一台电脑
	 */
	private void setDefaulChoose() {
		type = UserSetting.getInt(getApplicationContext(),
				Constants.SELECTTPYE, PCClientItem.OTHER);
		for (PCClientItem item : adapterDatas) {
			if (item.getItemType() == SELECTEDITEM) {
				isHide = item.isOnline();
				if (isHide && type != PCClientItem.DISCOVER_BY_ONEDRIVE) {
					item.setStatus(CHOOSE);
				} else {
					item.setStatus(NOCHOOSE);
				}
			}
		}
		if (type == PCClientItem.DISCOVER_BY_ONEDRIVE) {
			if (DriverManagerFactroy.getInstance().isLogin(getContext()) == DriverManagerFactroy.ONEDRIVE_LOGIN) {
				PCClientItem item = new PCClientItem();
				item.setName(getString(R.string.one_drive_string));
				item.setType(PCClientItem.DISCOVER_BY_ONEDRIVE);
				chooseItem = item;
				setDriveFuncVisible(driveChoose);
			}
		}
		refresh();
	}

	private OnItemClickListener listitemListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			clickListener(position);
		}
	};

	/**
	 * 点击开始btn,隐藏寻找或者PClist列表,显示连接电脑的画面
	 */
	private void showPcItem() {
		// select_pc.startAnimation(ra);
		pc_backgroud.setVisibility(View.VISIBLE);
		tagProgressBar.clearAnimation();
		tagProgressBar.setVisibility(View.INVISIBLE);
		main_circle_spaceinfo.setVisibility(View.GONE);
		PCName.setText(chooseItem.getPcname());
		btnStart.setText(R.string.cancle);
		driver.setVisibility(View.GONE);

		if (chooseItem.getType() == PCClientItem.DISCOVER_BY_ONEDRIVE) {
			connect_pc_icon.setImageResource(R.drawable.icon_logo_onedrive);
		} else {
			connect_pc_icon.setImageResource(R.drawable.icon_computer_white);
		}
	}

	@SuppressWarnings("unused")
	private void unshowPcItem() {
		// select_pc.clearAnimation();
		// select_pc.setVisibility(View.GONE);
		pc_backgroud.setVisibility(View.GONE);
		tagProgressBar.startAnimation(ra);
		tagProgressBar.setVisibility(View.VISIBLE);
		main_circle_spaceinfo.setVisibility(View.VISIBLE);
		btnStart.setText(R.string.start);
	}

	/**
	 * 同步并刷新数据
	 */
	private void refresh() {
		addFirstTag();
		syncAdapterDatas();
		addThirdTag();
		confirmHide();
		isVisibility();
		// hideFind();
		SetConnectedWifiName();
		castBtnState();
		mListAdapater.notifyDataSetChanged();
	}

	// boolean t = false;
	//
	// /**
	// * 在没有手动点击,并且寻找电脑信息的消息框显示的时候,隐藏该消息框,只执行一次,参考tagClick,t,auto等参数
	// */
	// private void hideFind() {
	// if (!tagClick && !t && mConnectedServers.size() != 0
	// && findIconDowmRoot.getVisibility() == View.VISIBLE && !auto) {
	// t = true;
	// playFindAnimation();
	// }
	// }

	/**
	 * 切换btn的状态:用户选择了要传输的电脑,并且该电脑在线,btn可点击,否则不可点击,如果用户上次选择的电脑在线，自动帮用户选中
	 */
	private void castBtnState() {
		boolean choose = false;
		for (PCClientItem item : adapterDatas) {
			if (item.getStatus() == CHOOSE) {
				choose = true;
			}
		}

		// add by elvis 判断是否选择了云
		if (!choose) {
			choose = driveChoose.getVisibility() == View.VISIBLE;
		}

		// add by elvis 如果用户没有选择任何item，而用户上次选择的电脑在线，则帮用户选中。
		if (!choose) {
			for (PCClientItem item : adapterDatas) {
				if (chooseItem != null) {
					String tempSelected = chooseItem.getPcname();
					if (item.isOnline()
							&& item.getPcname().equals(selectedLast)
							&& tempSelected.equals(selectedLast)) {
						item.setStatus(CHOOSE);
						chooseItem = PCClientItem.parse(item.toJson());
						choose = true;
					}
				}
			}
		}

		if (choose) {
			btnStart.setBackgroundResource(R.drawable.scan_btn_selector);
			btnStart.setTextColor(Color.parseColor("#ffffff"));
			btnStart.setClickable(true);
			btnStart.setEnabled(true);
		} else {
			if (pc_backgroud.getVisibility() != View.VISIBLE) {
				btnStart.setBackgroundResource(R.drawable.btn_white_line_disable);
				btnStart.setTextColor(Color.parseColor("#30FFFFFF"));
				btnStart.setClickable(false);
				btnStart.setEnabled(false);
			}
		}
	}

	/**
	 * 判断是否隐藏附近的电脑列表,参考参数isHide
	 */
	private void confirmHide() {

		if (!isHide) {
			for (PCClientItem item : mConnectedServers) {
				int indexOf = adapterDatas.indexOf(item);
				if (indexOf < 0) {
					adapterDatas.add(item);
				}
			}
		} else {
			// 隐藏列表,不是前3个,删除后面的数据
			List<PCClientItem> items = new ArrayList<PCClientItem>();
			for (PCClientItem item : adapterDatas) {
				if (item.getItemType() != FIRSTTAG
						&& item.getItemType() != THIRDTAG) {
					if (!item.getPcname().equals(selectedLast)) {
						items.add(item);
					}
				}
			}
			for (PCClientItem pcClientItem : items) {
				adapterDatas.remove(pcClientItem);
			}
		}
	}

	/**
	 * 设置真实数据默认没有被选中,详情见clickListener()方法,只选中用户选择的
	 */
	private void cleanReal() {
		for (PCClientItem item : adapterDatas) {
			item.setStatus(NOCHOOSE);
		}
	}

	/**
	 * 添加"附近的电脑"标签
	 */
	private void addThirdTag() {
		if (!isExsitItem(THIRDTAG)) {
			// 添加附近的电脑item
			PCClientItem item = new PCClientItem();
			item.setItemType(THIRDTAG);
			adapterDatas.add(item);
		}
	}

	/**
	 * 同步并更新真实数据,如果电脑已经离线,那么删除(上次选中的电脑除外)
	 */
	private void syncAdapterDatas() {
		// 更新adapter里面的pc都是在线的,或者是上次电脑显示为离线
		List<PCClientItem> temp = new ArrayList<PCClientItem>();
		for (PCClientItem item : adapterDatas) {
			if (item.getItemType() != THIRDTAG
					&& item.getItemType() != FIRSTTAG) {
				int indexOf = mConnectedServers.indexOf(item);
				if (indexOf < 0) {
					if (!item.getPcname().equals(selectedLast)) {
						// adapterDatas.remove(item);
						temp.add(item);
					} else {
						item.setOnline(false);
						item.setStatus(NOCHOOSE);
					}
				}
			}
		}

		for (PCClientItem pcClientItem : temp) {
			adapterDatas.remove(pcClientItem);// 删除不再线的集合
		}

		// 同步真实数据 mConnectedServers...
		for (PCClientItem item : mConnectedServers) {
			int indexOf = adapterDatas.indexOf(item);
			if (indexOf >= 0) {
				adapterDatas.get(indexOf).setOnline(true);
				adapterDatas.get(indexOf).setIp(item.getIp());
				if (item.getSoftAp() != null) {
					adapterDatas.get(indexOf).setSoftAp(item.getSoftAp());
				}
				adapterDatas.get(indexOf).setType(item.getType());
				if (item.getClient() != null) {
					adapterDatas.get(indexOf).setClient(item.getClient());
				}
			}

			// 同步选择的数据，如果选择的数据已经更新，则更新的选择的数据中去
			if (chooseItem != null
					&& item.getName().equals(chooseItem.getPcname())) {
				chooseItem.setIp(item.getIp());
				chooseItem.setType(item.getType());
				chooseItem.setRectime(item.getRectime());
				if (item.getSoftAp() != null) {
					chooseItem.setSoftAp(item.getSoftAp());
				}
				if (item.getClient() != null) {
					chooseItem.setClient(item.getClient());
				}
			}
		}

	}

	/**
	 * 如果上次有选择过的电脑,那么添加"上次连接的电脑"标签和上次连接的电脑数据,否则不添加
	 */
	private void addFirstTag() {

		if (!"".equals(selectedLast)) {// 存在,上次选择的电脑
			if (!isExsitItem(FIRSTTAG)) {
				PCClientItem item = new PCClientItem();
				// add 上次连接过的电脑item tag
				item.setItemType(FIRSTTAG);
				adapterDatas.add(0, item);

				// add 上次连接的电脑 数据
				item = new PCClientItem();
				String selectedLastTime = UserSetting.getString(
						getApplicationContext(), selectedLast, "");
				String ip = UserSetting.getString(getApplicationContext(),
						Constants.SELECTEDPCIP, "");
				item.setIp(ip);
				item.setPcname(selectedLast);
				item.setItemType(SELECTEDITEM);
				item.setLastTransferTime(selectedLastTime);

				chooseItem = PCClientItem.parse(item.toJson());

				adapterDatas.add(1, item);
			}
		} else {// 不存在,上次选择的电脑

			List<PCClientItem> temp = new ArrayList<PCClientItem>();
			for (PCClientItem item : adapterDatas) {
				int itemType = item.getItemType();
				if (itemType == SELECTEDITEM || itemType == FIRSTTAG) {
					temp.add(item);
				}
			}

			for (PCClientItem pcClientItem : temp) {
				adapterDatas.remove(pcClientItem);
			}
		}
	}

	/**
	 * 根据真实数据(mConnectedServers)是否为空,控制搜索界面,电脑列表的显示与隐藏
	 */
	private void isVisibility() {
		if (mConnectedServers.size() == 0) {
			// 隐藏列表
			PCList.setVisibility(View.GONE);
			// 搜素界面
			sanningYourPC_Des.setVisibility(View.VISIBLE);
			findGif.setVisibility(View.VISIBLE);
			if (driveChoose.getVisibility() != View.VISIBLE) {
				btnStart.setBackgroundResource(R.drawable.btn_white_line_disable);
				btnStart.setTextColor(Color.parseColor("#30FFFFFF"));
				btnStart.setClickable(false);
				btnStart.setEnabled(false);
			}
		} else {
			// 搜素界面
			// 显示列表
			sanningYourPC_Des.setVisibility(View.GONE);
			findGif.setVisibility(View.GONE);
			PCList.setVisibility(View.VISIBLE);
		}
	}

	/** 根据item type 类型判断是否存在listview 列表中 */
	private boolean isExsitItem(int i) {
		for (PCClientItem item : adapterDatas) {
			int itemType = item.getItemType();
			if (itemType == i) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 如果在autoUpScanInfo()执行之前,用户手动触发了寻找电脑消息框的提示,那么autoUpScanInfo()不执行
	 */
	private boolean tagClick = false;

	/**
	 * 初始化事件,设置监听事件
	 */
	private void initEvent() {

		try {
			tagBack.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					StatisticsUtil
							.getDefaultInstance(getApplicationContext())
							.onEventCount(
									Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_BACK);
					onBackPressed();
				}
			});

			btnStart.setOnClickListener(new OnClickListener() {

				@SuppressLint("SimpleDateFormat")
				@Override
				public void onClick(View v) {
					String EventID = "";
					String text = (String) btnStart.getText();
					if (text != null && text.equals(getString(R.string.cancle))) {
						EventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_CANCLE;
					} else {
						EventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_START;
					}

					String connFail = (String) castDes.getText();
					if (getString(R.string.casterror).equals(connFail)
							&& connFail != null && text != null
							&& text.equals(getString(R.string.cancle))) {
						EventID = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_CONN_FAILE_CANCLE;
					}

					if (!"".equals(EventID)) {
						StatisticsUtil.getDefaultInstance(
								getApplicationContext()).onEventCount(EventID);
					}

					if (manager != null) {
						WifiApManager.getInstance(getContext()).disableSoftAp();

						onBackPressed();
						return;
					}
					// resetHideImage();
					setProgress();
					showPcItem();
					saveConnectedPCInfo();
					resetCleanStatus();
					doExport();
					// startUDPDiscover();
					launchExportAct();
				}

			});

			OnClickListener animationListener = new OnClickListener() {

				@Override
				public void onClick(View v) {
					tagClick = true;
					playFindAnimation();
				}
			};
			findIconDowmRoot.setOnClickListener(animationListener);
			not_find_two.setOnClickListener(animationListener);

			retry.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					retry.setVisibility(View.GONE);
					clean.setVisibility(View.GONE);
					castDes.setText(R.string.connect);
					StatisticsUtil
							.getDefaultInstance(getApplicationContext())
							.onEventCount(
									Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_RETRY);
					if (checkConnectType()) {
						setProgress();
						launchExportAct();
					} else {
						castDes.setText(R.string.device_offline);
						retry.setVisibility(View.VISIBLE);
						clean.setVisibility(View.VISIBLE);
					}
				}
			});

			clean.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getContext(),
							CleanPhotoActivity.class);
					// intent.putExtra("page",
					// Constants.PAGE_EXPORTPHOTOFRAGMENT);
					intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
					startActivity(intent);
					UserSetting.setString(ScanningActivity.this,
							Constants.COMEFROMSCAN, "scan");
					finish();
				}
			});

			driver.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (DriverManagerFactroy.getInstance()
							.isLogin(getContext()) == DriverManagerFactroy.ONEDRIVE_LOGIN) {
						PCClientItem item = new PCClientItem();
						item.setName(getString(R.string.one_drive_string));
						item.setType(PCClientItem.DISCOVER_BY_ONEDRIVE);
						chooseItem = item;
						setDriveFuncVisible(driveChoose);
						cleanReal();
						refresh();
					}
				}
			});

			driver.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (DriverManagerFactroy.getInstance()
							.isLogin(getContext()) == DriverManagerFactroy.ONEDRIVE_LOGIN) {
						setDriveFuncVisible(logout);
					}
					return true;
				}
			});

			login.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					setDriveFuncVisible(null);
					loginDesc.setText(R.string.one_drive_login_desc_3);
					DriverManagerFactroy.getInstance()
							.getOneDriveAuthManager(getContext())
							.login(getContext(), new OneDriveAuthListener() {

								@Override
								public void onAuthError(Exception exception,
										Object userObject) {
									FLog.i(TAG, "exception:" + exception
											+ ",userObject:" + userObject);
									loginDesc
											.setText(R.string.login_fail);
									setDriveFuncVisible(login);
								}

								@Override
								public void onAuthComplete(
										OneDriveAuthStatus status,
										OneDriveAuthSession session,
										Object userObject) {
									FLog.i(TAG, "status:" + status
											+ ",session:" + session
											+ ",userObject:" + userObject);
									setDriveFuncVisible(null);
									initDriveText();

								}
							}, getString(R.string.one_drive_string));
				}
			});

			logout.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					DriverManagerFactroy.getInstance()
							.getOneDriveAuthManager(getContext())
							.logout(null, getString(R.string.one_drive_string));
					loginDesc.setText(R.string.one_drive_login_desc);
					UserSetting.setString(getContext(),
							Constants.CLOUD_ONE_DRIVER, null);
					setDriveFuncVisible(login);
				}
			});
		} catch (Exception e) {
			FLog.e(TAG, "initEvent throw error");
		}

	}

	private void setDriveFuncVisible(View view) {
		login.setVisibility(View.GONE);
		logout.setVisibility(View.GONE);
		driveChoose.setVisibility(View.GONE);
		if (view != null) {
			if (view.getId() == login.getId()) {
				login.setVisibility(View.VISIBLE);
			}
			if (view.getId() == logout.getId()) {
				logout.setVisibility(View.VISIBLE);
			}
			if (view.getId() == driveChoose.getId()) {
				driveChoose.setVisibility(View.VISIBLE);
			}
		}
	}

	private ScanningActivity getContext() {
		return this;
	}

	/**
	 * 判断选择的电脑是否在线,更新选择电脑的信息,返回true在线,false不在线
	 */
	protected boolean checkConnectType() {

		List<PCClientItem> onlinePcItem = DiscoverManager.getInstance(this)
				.getOnlinePcItem();
		int indexOf = onlinePcItem.indexOf(chooseItem);
		if (indexOf >= 0) {
			PCClientItem temp = onlinePcItem.get(indexOf);
			chooseItem.setType(temp.getType());
			chooseItem.setIp(temp.getIp());
			if (temp.getSoftAp() != null) {
				chooseItem.setSoftAp(temp.getSoftAp());
			}
			if (temp.getClient() != null) {
				chooseItem.setClient(temp.getClient());
			}
			return true;
		}
		if(chooseItem.getType() == PCClientItem.DISCOVER_BY_ONEDRIVE){
			return true;
		}
		return false;

		// TODO: 如果增加这个判断，如果在同一局域网，但是ssid不一样，会被切换链接方式。例如 sw-netgear-5G
		// sw-netgear
		// if (chooseItem.getType() != PCClientItem.SOFTAP_DISCOVER) {
		// if (chooseItem.getClient() != null
		// && chooseItem.getClient().getNet_id() != null) {
		// WifiManager wifiManager = (WifiManager)
		// getSystemService(Context.WIFI_SERVICE);
		// if (wifiManager.getConnectionInfo() != null) {
		// if (!wifiManager.getConnectionInfo().getSSID()
		// .equals(chooseItem.getClient().getNet_id())) {
		// chooseItem.setType(PCClientItem.SOFTAP_DISCOVER);
		// }
		// FLog.i(TAG, "now android ssid is "+
		// wifiManager.getConnectionInfo().getSSID() + "and pc net ssid is" +
		// chooseItem.getClient().getNet_id());
		// }
		// }
		// }
	}

	boolean up = true;

	/**
	 * 弹出或者隐藏寻找电脑的消息框
	 */
	private void playFindAnimation() {
		// 播放动画
		Object upTag = findIconDowmRoot.getTag();
		if (upTag != null) {
			up = Boolean.parseBoolean(upTag.toString());
		}
		findIconDowmRoot.setTag(!up);

		final boolean isup = up;
		int resId = up ? R.anim.pc_down : R.anim.pc_up;
		Animation animation = AnimationUtils.loadAnimation(getContext(), resId);
		animation.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				if (isup) {
					findIconDowmRoot_1.setVisibility(View.INVISIBLE);
					findIconDowmRoot.setVisibility(View.VISIBLE);
					// findIconDowmRoot
					// .setBackgroundResource(R.color.pc_down_color);
					// findIconDown.setImageResource(R.drawable.btn_find_hide);
					findIconDowmRoot.setClickable(true);
					findIconDowmRoot.setEnabled(true);
				} else {
					findIconDowmRoot.setVisibility(View.INVISIBLE);
					findIconDowmRoot_1.setVisibility(View.VISIBLE);
					// findIconDowmRoot_1.setClickable(true);
					// findIconDowmRoot_1.setEnabled(true);
					not_find_two.setClickable(true);
					not_find_two.setEnabled(true);
				}
			}
		});
		if (up) {
			not_find_two.setClickable(false);
			not_find_two.setEnabled(false);
			findIconDowmRoot_1.startAnimation(animation);
			findIconDowmRoot_1.setClickable(false);
			findIconDowmRoot_1.setEnabled(false);
		} else {
			// findIconDowmRoot.setBackgroundResource(R.color.pc_up_color);
			// findIconDown.setImageResource(R.drawable.btn_find_show);
			findIconDowmRoot.startAnimation(animation);
			findIconDowmRoot.setClickable(false);
			findIconDowmRoot.setEnabled(false);
		}
	}

	private int progress = 0;

	/**
	 * 设置连接某一台具体电脑的进度,在页面destroy的时候要remove掉task
	 */
	protected void setProgress() {
		progress = 0;
		myHandler.removeCallbacks(task);
		myHandler.postDelayed(task, 10);
	}

	Runnable task = new Runnable() {
		@Override
		public void run() {
			++progress;
			pb.setProgress(progress);
			if (progress < 195) {
				myHandler.postDelayed(this, 250);
			}
		}
	};

	/**
	 * 寻找电脑
	 */
	private void scanPC() {
		try {
			DiscoverManager.getInstance(this).registerReceiver(this);
			DiscoverManager.getInstance(this).start();
		} catch (Exception e) {
			FLog.e(TAG, "scanPC throw error", e);
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onStart();
	}

	@Override
	protected void onPause() {
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onResume();
		super.onResume();
		myHandler.removeCallbacks(stateTask);
		myHandler.postDelayed(stateTask, Constants.CHECK_PC_LIVE_TIME);

		int lin = DriverManagerFactroy.getInstance().isLogin(this);
		if (lin == DriverManagerFactroy.ONEDRIVE_LOGIN) {
			setDriveFuncVisible(null);
			String text = UserSetting.getString(getContext(),
					Constants.CLOUD_ONE_DRIVER, null);
			DriverManagerFactroy.getInstance().setLoginUserName(text);
			loginDesc.setText(text);
			FLog.i(TAG, "has login.");
		} else {
			setDriveFuncVisible(login);
			int resId = lin == DriverManagerFactroy.ONEDRIVE_LOGIN_OVERTIME ? R.string.one_drive_login_desc_2
					: R.string.one_drive_login_desc;
			loginDesc.setText(resId);
			FLog.i(TAG, getString(resId));
		}
	}

	@Override
	protected void onStop() {
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onStop();
		myHandler.removeCallbacks(stateTask);
		super.onStop();
	}

	// 定时类任务(runnable), 界面destroy的时候要关掉
	@Override
	protected void onDestroy() {
		FLog.i(TAG, "onDestroy");
		if (manager != null) {
			manager.destroy();
		}
		if (mReceiver != null) {
			unregisterReceiver(mReceiver);
			mReceiver = null;
		}

		if (runtask != null) {
			myHandler.removeCallbacks(runtask);
			runtask = null;
		}

		if (findGifTask != null) {
			myHandler.removeCallbacks(findGifTask);
			findGifTask = null;
		}
		progress = 200;
		super.onDestroy();
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onDestroy();
	}

	/**
	 * 初始化界面,控件
	 */
	private void initView() {
		setContentView(R.layout.scanning_pc);

		PCList = (ListView) findViewById(R.id.list);

		tagProgressBar = (ImageView) findViewById(R.id.tag_pb_small);
		// select_pc = (ImageView) findViewById(R.id.select_pc);
		tagBack = (LinearLayout) findViewById(R.id.backRoot);

		pb = (ProgressBar) findViewById(R.id.pb);

		sanningYourPC_Des = (TextView) findViewById(R.id.pic_list_des);
		// findIconDown = (ImageView) findViewById(R.id.finicondown);
		findIconDowmRoot = (RelativeLayout) findViewById(R.id.link_pc_with_net_des);// 显示的布局
		findIconDowmRoot_1 = (RelativeLayout) findViewById(R.id.link_pc_with_net_des_1);// 隐藏的布局
		main_circle_spaceinfo = (RelativeLayout) findViewById(R.id.main_circle_spaceinfo);
		retry = findViewById(R.id.retry);
		clean = findViewById(R.id.clean_);
		pc_backgroud = (RelativeLayout) findViewById(R.id.pc_backgroud_);
		connect_pc_icon = (ImageView) findViewById(R.id.connect_pc_icon);
		btnStart = (Button) findViewById(R.id.Btn_start);
		currLinkNetInfo = (TextView) findViewById(R.id.tv_link_net_);
		currLinkNetInfo1 = (TextView) findViewById(R.id.tv_link_net_1);
		castDes = (TextView) findViewById(R.id.cast_des);

		PCName = (TextView) findViewById(R.id.PCName);
		currLinkNetInfo.getPaint().setFakeBoldText(true);
		currLinkNetInfo1.getPaint().setFakeBoldText(true);
		findGif = (GifView) findViewById(R.id.find_gif);

		btnStart.setEnabled(false);
		btnStart.setClickable(false);
		btnStart.setBackgroundResource(R.drawable.btn_white_line_disable);

		not_find_one = (GifView) findViewById(R.id.not_find_one);
		not_find_two = (GifView) findViewById(R.id.not_find_two);

		t1 = (TextView) findViewById(R.id.tv1);
		t2 = (TextView) findViewById(R.id.tv2);
		t3 = (TextView) findViewById(R.id.tv3);
		t4 = (TextView) findViewById(R.id.tv4);

		driver = findViewById(R.id.driver);
		login = (TextView) findViewById(R.id.login);
		driveChoose = (ImageView) findViewById(R.id.select_drive);
		logout = (TextView) findViewById(R.id.logout);
		loginDesc = (TextView) findViewById(R.id.user_info);

		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getLanguage();

		if ("zh".equals(language)) {// 中文显示格式
			initScanAnim(getString(R.string.s1), 1);
			initScanAnim(getString(R.string.s2), 2);
		}
	}

	/**
	 * 一个TextView要显示的字符串,设置不同颜色.
	 * 
	 * @param s
	 *            要显示的字符串
	 * @param i
	 */
	private void initScanAnim(String s, int i) {

		try {
			SpannableStringBuilder builder = new SpannableStringBuilder(s);

			int index1 = s.indexOf("[");
			int index2 = s.indexOf("]");

			ForegroundColorSpan span = new ForegroundColorSpan(
					Color.parseColor("#74CCFF"));// 文字颜色
			StyleSpan sty = new StyleSpan(Typeface.BOLD_ITALIC); // 粗体

			builder.setSpan(span, index1 + 1, index2,
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);
			builder.setSpan(sty, index1 + 1, index2,
					Spannable.SPAN_INCLUSIVE_INCLUSIVE);

			if (i == 1) {
				builder.replace(index1, index1 + 1, "");
				builder.replace(index2 - 1, index2, "");
				t1.setText(builder);
				t3.setText(builder);

			} else {
				builder.replace(index1, index1 + 1, "");
				builder.delete(index2 - 1, index2 + 1);
				t2.setText(builder);
				t4.setText(builder);
			}
		} catch (Exception e) {
			FLog.e(TAG, "initScanAnim throw error", e);
		}
	}

	/**
	 * 时时的添加找到的电脑到mConnectedServers,如果已经存在那么更新信息
	 * 
	 * @param server
	 */
	private void fillServerList(PCClientItem server) {
		boolean add = false;
		if (server != null) {

			int index = mConnectedServers.indexOf(server);
			if (index < 0) {
				// 添加到列表中
				add = true;
				server.setOnline(true);
				mConnectedServers.add(server);
			} else {
				// 在列表中，已存在对应该机器。
				PCClientItem item = mConnectedServers.get(index);
				if (server.getType() <= item.getType()) {
					// 已存在发现的机器类型发现等级低于当前等级，更新当前等级类型及ip
					item.setType(server.getType());
					item.setIp(server.getIp());
					item.setRectime(server.getRectime());
					if (server.getSoftAp() != null) {
						item.setSoftAp(server.getSoftAp());
					}
					if (server.getClient() != null) {
						item.setClient(server.getClient());
					}
				} else {
					// 低于当前等级类型，查询上次该类型是否超时，则切换到低等级类型
					if (server.getRectime() - item.getRectime() > Constants.CHECK_PC_LIVE_TIME) {
						item.setIp(server.getIp());
						item.setType(server.getType());
						item.setRectime(server.getRectime());
					}
					if (server.getSoftAp() != null) {
						item.setSoftAp(server.getSoftAp());
					}
					if (server.getClient() != null) {
						item.setClient(server.getClient());
					}
				}
				item.setOnline(true);
			}
		}
		// FLog.i(TAG, "new pcname:" + server.toJson());
		if (checkLivePCClients() || add) {
			refresh();
		}

	}

	private synchronized boolean checkLivePCClients() {
		boolean ret = false;
		// 超时检测
		List<PCClientItem> temp = new ArrayList<PCClientItem>();
		for (PCClientItem item : mConnectedServers) {
			if (System.currentTimeMillis() - item.getRectime() > Constants.CHECK_PC_LIVE_TIME) {
				temp.add(item);
			}
		}

		for (PCClientItem item : temp) {
			FLog.i(TAG, item.getPcname() + " remove.");
			mConnectedServers.remove(item);
			ret = true;
		}
		return ret;
	}

	class MyListAdapater extends BaseAdapter {

		@Override
		public int getCount() {
			return adapterDatas.size();
		}

		@Override
		public Object getItem(int position) {
			return adapterDatas.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			final int itemType = adapterDatas.get(position).getItemType();
			boolean online = adapterDatas.get(position).isOnline();
			boolean hide = adapterDatas.get(position).isHide();
			String lastTime = UserSetting.getString(getApplicationContext(),
					adapterDatas.get(position).getPcname(), "");
			if (itemType == FIRSTTAG) {
				TextView tv = (TextView) View.inflate(getContext(),
						R.layout.scan_textview_item, null);
				return tv;
			}
			if (itemType == THIRDTAG) {
				return getThridItemView(position);
			}
			// 创建新的view视图.
			convertView = View
					.inflate(getContext(), R.layout.pclist_item, null);
			final ViewHolder mholder = getViewHolder(convertView);
			initOnlineAndHideState(position, online, hide, lastTime, mholder);
			initChooseState(position, lastTime, mholder);
			initPcItemListener(position, convertView, itemType, mholder);

			return convertView;
		}
	}

	private class ViewHolder {
		TextView PCName;
		RelativeLayout pc_backgroud;
		ImageView PCPic;
		ImageView selectPCTag;
		LinearLayout finishTimeRoot;
		TextView last_finishTime;
		TextView offline;
		TextView cancle;
	}

	@Override
	public void addAnnouncedServers(final PCClientItem servers) {
		runOnUiThread(new Runnable() {
			public void run() {
				fillServerList(servers);
			}
		});

	}

	private void resetCleanStatus() {
		CleanFileStatusPkg pkg = new CleanFileStatusPkg();
		pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_STOP);
		UserSetting.setClearStatusInfo(this, pkg.toJson());
	}

	private void launchExportAct() {
		// DiscoverManager.getInstance(this).stop();
		myHandler.removeCallbacks(stateTask);
		registerSuccessReceiver();
		saveConnectInfo();
		connect();
	}

	private void saveConnectInfo() {
		if (chooseItem.getType() != PCClientItem.DISCOVER_BY_ONEDRIVE) {
			String mSelectPCName = chooseItem.getPcname();
			String mSelectPCIp = chooseItem.getIp();

			UserSetting.setString(getApplicationContext(),
					Constants.SELECTEDPCNAME, mSelectPCName);
			UserSetting.setString(getApplicationContext(),
					Constants.SELECTEDPCIP, mSelectPCIp);
		}
		UserSetting.setInt(getApplicationContext(), Constants.SELECTTPYE,
				chooseItem.getType());
	}

	private void connect() {
		manager = new ConnectManager(this);
		manager.registerStateChange(new StateChanager() {
			@Override
			public void onChange(final int state) {
				// connect state change
				runOnUiThread(new Runnable() {
					public void run() {
						if (state == ConnectManager.ERROR) {
							KeepUserNetwork.getInstance(getContext())
									.restoreOldNetWork();
							castDes.setText(R.string.casterror);
							retry.setVisibility(View.VISIBLE);
							clean.setVisibility(View.VISIBLE);
							stopServer();
							progress = 200;
							pb.setProgress(progress);
							WifiApManager.getInstance(getApplication())
									.disableSoftAp();
							return;
						}
						castDes.setText(R.string.connect);

						/*
						 * int text = state == ConnectManager.CONNECT_LAN ?
						 * R.string.cast_lan : state ==
						 * ConnectManager.CONNECT_PC_NET_ID ?
						 * R.string.cast_pc_lan : R.string.cast_softap;
						 * castDes.setText(text);
						 */

					}
				});

			}
		});
		manager.connect(chooseItem);
	}

	private void registerSuccessReceiver() {
		if (mReceiver == null) {
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Constants.PC_START_DOWNLOAD);
			mReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, final Intent intent) {
					try {
						connectedToNext(intent);
					} catch (Exception e) {
						FLog.e(TAG, "onReceive throw error", e);
					}
				}

			};
			registerReceiver(mReceiver, intentFilter);
		}
	}

	BroadcastReceiver mReceiver = null;

	private TextView t1;

	private TextView t2;

	private TextView t3;

	private TextView t4;

	private void SetConnectedWifiName() {
		try {
			WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
			if (null == wifiManager) {
				return;
			}
			WifiInfo wifiInfo = wifiManager.getConnectionInfo();
			String ssid = wifiInfo.getSSID();
			currLinkNetInfo.setText(ssid);
			currLinkNetInfo1.setText(ssid);
		} catch (Exception e) {
			FLog.e(TAG, "getConnectWifiSsid throw error", e);
		}
	}

	@Override
	public void onBackPressed() {
		KeepUserNetwork.getInstance(this).restoreOldNetWork();
		resetCleanStatus();
		if (manager != null) {
			manager.destroy();
		}
		Intent service = new Intent(this, ServerManager.class);
		stopService(service);
		Intent intent = new Intent(getContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY);
		startActivity(intent);
		finish();
	}

	public void doExport() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			// if (pkg.getCleanStatus() != CleanFileStatusPkg.CLEAN_ING)
			{
				ExportByDateTime export = new ExportByDateTime();

				AllPhotoManager photoMgr = (AllPhotoManager) PhotoManagerFactory
						.getInstance(getContext(),
								PhotoManagerFactory.PHOTO_MGR_ALL);
				// List<FileItem> lstFile =
				// photoMgr.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
				// IPhotoManager.ORDER_BY_DESC, 0);
				// long wantClearSize = UserSetting.getWantCleanSize(this, 0);

				int position = UserSetting.getInt(getApplicationContext(),
						"position", 3);
				long wantClearSize = UserSetting.getLong(
						getApplicationContext(), "cleansize", 0);

				switch (position) {
				case 1:
					List<FileItem> lstFile = photoMgr.get1mounth(
							IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_DESC, 0);
					int num1 = lstFile.size();
					UserSetting.setInt(getApplicationContext(), "number", num1);
					export.exportToFile(lstFile, wantClearSize);
					break;
				case 2:
					List<FileItem> lstFile3 = photoMgr.get3mounth(
							IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_DESC, 0);
					int num3 = lstFile3.size();
					UserSetting.setInt(getApplicationContext(), "number", num3);
					export.exportToFile(lstFile3, wantClearSize);
					break;
				case 3:
					List<FileItem> lstFile6 = photoMgr.get6mounth(
							IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_DESC, 0);
					int num6 = lstFile6.size();
					UserSetting.setInt(getApplicationContext(), "number", num6);
					export.exportToFile(lstFile6, wantClearSize);
					break;
				case 4:
					List<FileItem> lstFile12 = photoMgr.get12mounth(
							IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_DESC, 0);
					int num12 = lstFile12.size();
					UserSetting
							.setInt(getApplicationContext(), "number", num12);
					export.exportToFile(lstFile12, wantClearSize);
					break;
				case 0:
					List<FileItem> lstFileAll = photoMgr.getPhotosSync(
							IPhotoManager.SORT_TYPE_TIME,
							IPhotoManager.ORDER_BY_DESC, 0);
					export.exportToFile(lstFileAll, wantClearSize);
					break;
				default:
					break;
				}

				pkg.setHandlePicTotal(String.valueOf(export.getFileNumber()));

				// 此处状态还不是正在进行中
				// pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_ING);
				UserSetting.setClearStatusInfo(this, pkg.toJson());

				// 清空导出文件的临时数据库
				DBMgr.getInstance(this).deleteAll(
						CurrentExportedImageItem.class);
			}
		} catch (Exception e) {
			FLog.e(TAG, "doExport throw error", e);
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void saveConnectedPCInfo() {
		selectedLast = UserSetting.getString(getApplicationContext(),
				Constants.SELECTEDPCNAME, "");

		PCClientItem item = new PCClientItem();
		item.setName(selectedLast);
		long currentTimeMillis = System.currentTimeMillis();
		Date date = new Date(currentTimeMillis);
		SimpleDateFormat s = new SimpleDateFormat("yyyy.MM.dd HH:mm");
		String formatData = s.format(date);

		UserSetting
				.setString(getApplicationContext(), selectedLast, formatData);

		String saveListStrItem = UserSetting.getString(getApplicationContext(),
				"saveListStr", "");
		if (!saveListStrItem.contains(selectedLast)) {
			if ("".equals(saveListStrItem)) {
				UserSetting.setString(getApplicationContext(), "saveListStr",
						selectedLast);
			} else {
				UserSetting.setString(getApplicationContext(), "saveListStr",
						saveListStrItem + "/" + selectedLast);
			}
		}
	}

	private void clickListener(int position) {
		PCClientItem tempItem = adapterDatas.get(position);
		if (tempItem.getItemType() == FIRSTTAG) {
			return;
		}

		boolean hide = tempItem.isHide();
		if (tempItem.getItemType() == THIRDTAG && !"".equals(selectedLast)) {
			tempItem.setHide(!hide);
			isHide = !isHide;
			refresh();
			return;
		}

		if (!tempItem.isOnline()) {
			return;// 离线不可点击
		}

		for (PCClientItem item : adapterDatas) {
			item.setStatus(NOCHOOSE);
		}
		cleanReal();// 所有数据置成不可选

		chooseItem = PCClientItem.parse(tempItem.toJson());// 只选中用户选择的PC
		adapterDatas.get(position).setStatus(CHOOSE);

		String name = chooseItem.getPcname();
		String name_ = UserSetting.getString(getApplicationContext(),
				Constants.SELECTEDPCNAME, "");
		String eventId = "";
		if (name.equals(name_)) {
			eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_LASTCONNPC;
		} else {
			eventId = Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_NEARPC;
		}
		StatisticsUtil.getDefaultInstance(getApplicationContext())
				.onEventCount(eventId);
		if (driveChoose.getVisibility() == View.VISIBLE) {
			driveChoose.setVisibility(View.GONE);
		}
		refresh();
	}

	private View getThridItemView(final int position) {
		View v = View.inflate(getContext(), R.layout.scan_hide_pclist_item,
				null);
		ImageView hide_pc_list_pic = (ImageView) v
				.findViewById(R.id.hide_pc_list_pic);
		if ("".equals(selectedLast)) {
			hide_pc_list_pic.setVisibility(View.GONE);
			v.setClickable(false);
			v.setEnabled(false);
		}

		if (isHide) {
			hide_pc_list_pic.setImageResource(R.drawable.btn_find_hide);
		} else {
			hide_pc_list_pic.setImageResource(R.drawable.btn_find_show);
		}
		v.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				clickListener(position);
			}
		});
		return v;
	}

	private ViewHolder getViewHolder(View convertView) {
		final ViewHolder mholder = new ViewHolder();

		mholder.PCName = (TextView) convertView.findViewById(R.id.PCName);
		mholder.PCPic = (ImageView) convertView.findViewById(R.id.PC_Pic);
		mholder.selectPCTag = (ImageView) convertView
				.findViewById(R.id.select_pc);
		mholder.pc_backgroud = (RelativeLayout) convertView
				.findViewById(R.id.pc_backgroud);
		mholder.finishTimeRoot = (LinearLayout) convertView
				.findViewById(R.id.finishTimeRoot);
		mholder.last_finishTime = (TextView) convertView
				.findViewById(R.id.last_finishTime);
		mholder.offline = (TextView) convertView.findViewById(R.id.offline);
		mholder.cancle = (TextView) convertView.findViewById(R.id.cancle);// 取消保存

		convertView.setTag(mholder);
		return mholder;
	}

	private void initChooseState(final int position, String lastTime,
			final ViewHolder mholder) {
		if (adapterDatas.get(position).getStatus() == CHOOSE) {
			mholder.selectPCTag.setVisibility(View.VISIBLE);
			mholder.pc_backgroud
					.setBackgroundColor(Color.parseColor("#4285dd"));
		} else {
			mholder.pc_backgroud.setBackgroundColor(Color
					.parseColor("#00000000"));
			mholder.selectPCTag.setVisibility(View.GONE);
		}

		if (TextUtils.isEmpty(lastTime)) {
			mholder.finishTimeRoot.setVisibility(View.GONE);
		} else {
			mholder.finishTimeRoot.setVisibility(View.VISIBLE);
			mholder.last_finishTime.setText(lastTime);
		}
	}

	private void initOnlineAndHideState(final int position, boolean online,
			boolean hide, String lastTime, final ViewHolder mholder) {
		if (!online) {
			mholder.PCName.setTextColor(Color.parseColor("#30ffffff"));
			mholder.offline.setTextColor(Color.parseColor("#30ffffff"));
			mholder.last_finishTime.setText(lastTime);
			mholder.PCName.setText(adapterDatas.get(position).getPcname());
			mholder.PCPic.setImageResource(R.drawable.icon_computer_gray);
			mholder.offline.setVisibility(View.VISIBLE);
		} else {
			mholder.offline.setVisibility(View.GONE);
			mholder.PCName.setText(adapterDatas.get(position).getPcname());
		}

		if (!hide) {
			mholder.cancle.setVisibility(View.GONE);
		} else {
			mholder.cancle.setVisibility(View.VISIBLE);
		}
	}

	private void initPcItemListener(final int position, View convertView,
			final int itemType, final ViewHolder mholder) {
		mholder.cancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				StatisticsUtil
						.getDefaultInstance(getApplicationContext())
						.onEventCount(
								Constants.UMENG.GUI_INFO_GATHER.UM_EVENT_ID_CANCLE_SELECTED);
				mholder.cancle.setVisibility(View.GONE);
				UserSetting.setString(getApplicationContext(),
						Constants.SELECTEDPCNAME, "");
				selectedLast = "";
				isHide = false;
				refresh();
				btnStart.setBackgroundResource(R.drawable.btn_white_line_disable);
				btnStart.setTextColor(Color.parseColor("#30FFFFFF"));
				btnStart.setClickable(false);
				btnStart.setEnabled(false);
			}
		});

		if (itemType == SELECTEDITEM) {
			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					adapterDatas.get(position).setHide(true);
					mholder.cancle.setVisibility(View.VISIBLE);
					refresh();
					return false;
				}
			});
		}
		convertView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (itemType == SELECTEDITEM) {
					adapterDatas.get(position).setHide(false);
					mholder.cancle.setVisibility(View.GONE);
				}
				clickListener(position);
			}
		});
	}

	private void connectedToNext(final Intent intent) {
		manager.destroy();
		String action = intent.getAction();
		if (action.equals(Constants.PC_START_DOWNLOAD)) {
			Intent export = new Intent(getContext(), ExportActivity.class);
			export.putExtra("pcip", chooseItem.getIp());
			if (mReceiver != null) {
				unregisterReceiver(mReceiver);
				mReceiver = null;
			}
			progress = 200;
			pb.setProgress(progress);
			startActivity(export);
			finish();
		}
	}

	private void stopServer() {
		Intent intent = new Intent(this, ServerManager.class);
		stopService(intent);
	}

	private void initDriveText() {
		String text = getString(R.string.drive_login);
		text = String.format(text, "");
		UserSetting.setString(getContext(), Constants.CLOUD_ONE_DRIVER, text);
		loginDesc.setText(text);
		new Thread() {
			public void run() {
				DriverManagerFactroy.getInstance().getDriveAsync(
						new OneDriveOperationListener() {

							@Override
							public void onSuccess(Object object) {
								FLog.i(TAG, "getDriveAsync onSuccess:" + object);
								final Drive driveCloud = (Drive) object;
								DriverManagerFactroy
										.getInstance()
										.setLoginUserName(
												driveCloud.Owner.User.DisplayName);
								runOnUiThread(new Runnable() {

									@Override
									public void run() {
										setDriveFuncVisible(null);
										String text = getString(R.string.drive_login);
										text = String
												.format(text,
														driveCloud.Owner.User.DisplayName);
										UserSetting.setString(getContext(),
												Constants.CLOUD_ONE_DRIVER,
												text);
										loginDesc.setText(text);
									}
								});
							}

							@Override
							public void onFailure(Exception exception) {
								FLog.i(TAG, "getDriveAsync onFailure:"
										+ exception);
							}
						});
			}
		}.start();
	}

}
