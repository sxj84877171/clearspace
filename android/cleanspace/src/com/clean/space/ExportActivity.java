package com.clean.space;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clean.space.log.FLog;
import com.clean.space.network.http.ServerManager;
import com.clean.space.network.udp.UdpClient;
import com.clean.space.onedriver.OneDriveUploadManage;
import com.clean.space.phoneinfo.PhoneInfo;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.protocol.PCClientItem;
import com.clean.space.protocol.PhoneInfoPkt;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.SpaceUtil;

/**
 * @Des 正在处理照片的页面
 */
public class ExportActivity extends Activity {

	private static final String TAG = ExportActivity.class.getSimpleName();

	/**
	 * 后台导出完成
	 */
	public static final int EXPORT_STATIC_FINISH = 2;

	private TextView deallingPic;// 正在处理第(多少)张照片
	private TextView pic_path;
	private Animation operatingAnim;
	private Button btnClearCancle;// 处理取消
	private TextView allPicNumber;// 照片总数
	private RoundProgressBar mRoundProgressBar2;
	private int progress = 0;
	private TextView clearSize;// 已腾出空间

	private String mSelectPCIp = "";
	private boolean mNeedSendUdp = true;
	private TextView exportLocation;
	private Thread threadConnectPc = null;
	private TextView clearedSizeDanwei;
	private TextView smallPoint;
	private TextView export_size_des;
	private ImageView circle;
	private RelativeLayout expot_info_root;
	private ValueAnimator colorAnimation;
	private ProgressBar pb;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initView();// 初始化控件

		initGradualAnimation();

		initCircleAnimation();

		initEvent();// 初始化事件

		// 开始生成导出文件列表
		mSelectPCIp = getIP(getIntent());
		// resetExportStatus();

		registerReceiver();

	}

	@SuppressLint("NewApi")
	private void initGradualAnimation() {
		Integer colorFrom = getResources().getColor(R.color.from);
		Integer colorTo = getResources().getColor(R.color.to);

		colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom,
				colorTo);

		colorAnimation.setDuration(15000);
		colorAnimation.addUpdateListener(new AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animator) {

				expot_info_root.setBackgroundColor((Integer) animator
						.getAnimatedValue());
			}
		});

		colorAnimation.start();
	}

	private void initCircleAnimation() {
		operatingAnim = AnimationUtils.loadAnimation(this, R.anim.circle);
		operatingAnim.setInterpolator(new Interpolator() {

			@Override
			public float getInterpolation(float x) {
				return x;
			}
		});
		circle.startAnimation(operatingAnim);
	}

	private String getIP(Intent intent) {

		try {
			Bundle bundle = intent.getExtras();
			if (null != bundle) {
				return bundle.getString("pcip");
			}
		} catch (Exception e) {
			FLog.e(TAG, "getIP throw error", e);
		}
		return "";
	}

	// 进入该界面后,立即设置导出状态为正在导出
	public void resetExportStatus() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			if (pkg.getCleanStatus() != CleanFileStatusPkg.CLEAN_ING) {
				pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_ING);
				UserSetting.setClearStatusInfo(this, pkg.toJson());
			}
		} catch (Exception e) {
			FLog.e(TAG, "doExport throw error", e);
		}
	}

	private void initEvent() {

		btnClearCancle.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				stopServer(CleanFileStatusPkg.CLEAN_STOP);
				startCoreServer();
			}
		});
	}

	private void initView() {
		setContentView(R.layout.export);
		pic_path = (TextView) findViewById(R.id.pic_path);
		deallingPic = (TextView) findViewById(R.id.dealling_);
		btnClearCancle = (Button) findViewById(R.id.Btn_clear_cancle);
		allPicNumber = (TextView) findViewById(R.id.all_pic);
		mRoundProgressBar2 = (RoundProgressBar) findViewById(R.id.roundProgressBar2);// 中环
		clearSize = (TextView) findViewById(R.id.clear_size);// 时时清理数据
		smallPoint = (TextView) findViewById(R.id.xiaoshudian);// 小数点部分
		export_size_des = (TextView) findViewById(R.id.export_size_des);// 导出描述
		circle = (ImageView) findViewById(R.id.circle);

		expot_info_root = (RelativeLayout) findViewById(R.id.expot_info_root);

		clearedSizeDanwei = (TextView) findViewById(R.id.export_danwei_);

		exportLocation = (TextView) findViewById(R.id.exportPCNameLocation);

		pb = (ProgressBar) findViewById(R.id.export_pb);

		int type = UserSetting.getInt(getApplicationContext(),
				Constants.SELECTTPYE, 0);

		if (type != PCClientItem.DISCOVER_BY_ONEDRIVE) {
			String PCName = UserSetting.getString(getApplicationContext(),
					Constants.SELECTEDPCNAME, "");
			exportLocation.setText(String.format(
					getString(R.string.alert_export_location), PCName));
		} else {
			exportLocation.setText(getString(R.string.one_drive_string)
					+ File.separator + OneDriveUploadManage.FOLDER_NAME
					+ File.separator + android.os.Build.MODEL + File.separator);
		}
		boolean outAndCleanInfo = UserSetting.getBoolean(
				getApplicationContext(), Constants.CHECK_EXPORT_AND_CLEAN,
				false);
		if (!outAndCleanInfo) {
			export_size_des.setText(R.string.has_export);
		}
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
			case 0:
				updateUi();
				break;
			case 1:
				break;
			case 2:

				break;

			default:
				break;
			}
		}
	};

	int currFinishNumber = 0;
	int PicTotal = 0;

	public void updateUi() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			String currentFileSize = pkg.getCurrentFileSize();
			String currentTransferSize = pkg.getCurrentTransferSize();

			if (currentTransferSize == null) {
				currentTransferSize = "0";
			}

			if (currentFileSize == null) {
				currentFileSize = "0";
			}

			long oneTotal = Long.parseLong(currentFileSize);
			long oneTransfer = Long.parseLong(currentTransferSize);

			if (oneTransfer >= oneTotal) {
				oneTransfer = oneTotal;
			}

			if (oneTotal != 0) {
				int pressed = Math.round(100 * oneTransfer / oneTotal);
				// System.out.println("单个的进度: " + pressed + ",oneTransfer:"
				// + oneTransfer + ",oneTotal:" + oneTotal);
				int last = pb.getProgress();
				if (last != pressed) {
					pb.setProgress(pressed);
				}
			} else {
				pb.setProgress(0);
			}

			String handlerNumber = pkg.getHandlePicNumber();
			String string = UserSetting.getString(getApplicationContext(),
					Constants.PATH, "");

			String pathLast = (String) pic_path.getText();
			if (!pathLast.equals(string)) {
				pic_path.setText(string);
			}

			if (null == handlerNumber) {
				handlerNumber = "0";
			}

			String cleanSpace = pkg.getCleanedSpace();
			if (null == cleanSpace) {
				cleanSpace = "0";
			}
			long lCleanSpace = Long.parseLong(cleanSpace);

			if (lCleanSpace < 1024 * 1024) {
				boolean equs = equs(clearedSizeDanwei, "KB");
				if (!equs) {
					clearedSizeDanwei.setText(R.string.Dwsize_kb);
				}
			} else if (lCleanSpace >= 1024 * 1024
					&& lCleanSpace < 1024 * 1024 * 1024) {
				boolean equs = equs(clearedSizeDanwei, "MB");
				if (!equs) {
					clearedSizeDanwei.setText(R.string.Dwsize_mb);
				}
			} else {
				boolean equs = equs(clearedSizeDanwei, "GB");
				if (!equs) {
					clearedSizeDanwei.setText(R.string.Dwsize);
				}
			}

			String currClearSize = SpaceUtil.convertSize(lCleanSpace);

			String[] split = currClearSize.split("\\.");

			String currClearSizeSmallPoint = "." + split[1];
			String currClearSizeInt = split[0];

			String lastIntStr = (String) clearSize.getText();
			if (!currClearSizeInt.equals(lastIntStr)) {
				clearSize.setText(currClearSizeInt);
			}

			String lastPointStr = (String) smallPoint.getText();
			if (!currClearSizeSmallPoint.equals(lastPointStr)) {
				smallPoint.setText(currClearSizeSmallPoint);
			}

			String handlePicTotal = pkg.getHandlePicTotal();
			if (null == handlePicTotal) {
				handlePicTotal = "0";
			}

			String picLast = (String) allPicNumber.getText();
			if ("0".equals(picLast)) {
				allPicNumber.setText(handlePicTotal);
			}

			currFinishNumber = Integer.parseInt(handlerNumber);
			PicTotal = Integer.parseInt(handlePicTotal);

			if (currClearSizeSmallPoint.equals(".00")
					&& currClearSizeInt.equals("0") || PicTotal == 0) {
				mRoundProgressBar2.setProgress(0);
			} else {
				progress = Math
						.round((100 * currFinishNumber / PicTotal) + 0.5f);
				int progress2 = mRoundProgressBar2.getProgress();
				if (progress != progress2) {
					if (progress >= 99) {
						mRoundProgressBar2.setProgress(99);
					} else {
						mRoundProgressBar2.setProgress(progress);
					}
				}
			}

			int status = pkg.getCleanStatus();
			FLog.i(TAG, "PicTotal:" + PicTotal + ",currFinishNumber="
					+ currFinishNumber + ",status=" + status);
			if (PicTotal <= currFinishNumber
					|| CleanFileStatusPkg.CLEAN_INTERRUPT == status) {
				FLog.i(TAG, "updateUi clean fished");
				mRoundProgressBar2.setProgress(100);
				stopServer(status);// 处理完成

				if (PicTotal != currFinishNumber) {
					String eventid = Constants.UMENG.EXPORT_PHOTO.DIFF_NEEDEXPORT_EXPORTED;
					StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG)
							.onEventCount(eventid);
				}

				currFinishNumber = currFinishNumber - 1;
			}
			if (CleanFileStatusPkg.CLEAN_INTERRUPT == status) {
				String eventid = Constants.UMENG.EXPORT_PHOTO.EXPORT_GUI_TIMEOUT_TIMES;
				StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG)
						.onEventCount(eventid);
			}
			if (currFinishNumber >= PicTotal) {
				String eventid = Constants.UMENG.EXPORT_PHOTO.EXPORT_COMPLETE;
				StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG)
						.onEventCount(eventid);
			}
			String picNumberLast = (String) deallingPic.getText();
			if (!picNumberLast.equals(currFinishNumber + 1 + "")) {
				deallingPic.setText(currFinishNumber + 1 + "");
			}

		} catch (Exception e) {
			FLog.e(TAG, "updateUi throw error", e);
		}
	}

	private boolean equs(TextView tv, String s) {
		boolean tag = false;
		String str = (String) tv.getText();
		if (str.equals(s)) {
			return true;
		}
		return tag;
	}

	public void startServer() {
		Intent intent = new Intent(this, ServerManager.class);
		this.startService(intent);
		// startCircleProgressBar();
	}

	@Override
	protected void onResume() {
		super.onResume();
		UserSetting.setInt(getApplicationContext(), Constants.EXPORT_STATIC, 1);// 前台
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onResume();
		updateUi();
	}

	@Override
	protected void onPause() {
		super.onPause();
		UserSetting.setInt(getApplicationContext(), Constants.EXPORT_STATIC, 2);// 后台导出
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onPause();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		try {
			if (null != mReceiver) {
				this.unregisterReceiver(mReceiver);
			}
			mNeedSendUdp = false;
			if (null != threadConnectPc) {
				threadConnectPc.interrupt();
			}
		} catch (Exception e) {
			FLog.e(TAG, "onDestroy throw error", e);
		}
		StatisticsUtil.getInstance(this, StatisticsUtil.TYPE_UMENG).onDestroy();
	}

	private void stopServer(int status) {
		try {
			mNeedSendUdp = false;
			Intent intent = new Intent(this, ServerManager.class);
			this.stopService(intent);

			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			pkg.setCleanStatus(status);
			UserSetting.setClearStatusInfo(this, pkg.toJson());

			disConnectPC(mSelectPCIp);

			// 当接收到PC的确认包后
			moveFinish();
		} catch (Exception e) {
			FLog.e(TAG, "stopServer throw error", e);
		}

	}

	public void startCoreServer() {
		FLog.i(TAG, "startCoreServer");
		Bundle extras = new Bundle();
		extras.putString(Constants.CLOUD_KEY, Constants.CLOUD_ONE_DRIVER);
		extras.putString(Constants.CLOUD_CMD, Constants.CLOUD_CMD_STOP);
		Intent intent = new Intent(this, CoreService.class);
		intent.putExtras(extras);
		startService(intent);
	}

	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, final Intent intent) {
			try {
				String action = intent.getAction();
				if (action.equals(Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST)) {
					Bundle bundle = intent.getExtras();
					// long fileSize = bundle.getLong("filesize");
					mNeedSendUdp = false;
					updateUi();
				}
			} catch (Exception e) {
				FLog.e(TAG, "onReceive throw error", e);
			}
		}

	};

	public void updateUi(long fileSize) {
		runOnUiThread(new Runnable() {
			public void run() {

			}
		});

	}

	private void registerReceiver() {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Constants.DOWNLOAD_PROGRESS_INTENT_BROADCAST);
		this.registerReceiver(mReceiver, intentFilter);
	}

	private void disConnectPC(final String ip) {
		try {

			if (null == ip || ip.equals("")) {
				return;
			}
			new Thread(new Runnable() {
				public void run() {
					for (int i = 0; i < 2; ++i) {
						try {
							PhoneInfo info = new PhoneInfo();
							info.initMoreInfo(ExportActivity.this);
							PhoneInfoPkt pkg = new PhoneInfoPkt();
							pkg.setCmd(Constants.CMD_STOP_DOWNLOAD);

							pkg.setDevicename(info.getDevicename());
							pkg.setDeviceid(info.getDeviceid());
							String path = Constants.APP_ROOT_PATH
									+ Constants.HTTP_EXPORT_FILE_NAME;
							pkg.setPath(path);
							UdpClient.send(ip, pkg.toJson());
							Thread.sleep(2000);
						} catch (InterruptedException e) {
							FLog.e(TAG, "disConnectPC throw error", e);
						}
					}
				}
			}).start();
		} catch (Exception e) {
			FLog.e(TAG, "disConnectPC throw error", e);
		}
	}

	@SuppressLint("SimpleDateFormat")
	private void moveFinish() {
		int state = UserSetting.getInt(getApplicationContext(),
				Constants.EXPORT_STATIC, 0);

		String clearStatus = UserSetting.getClearStausInfo(this);
		CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
		pkg.setCleanStatus(CleanFileStatusPkg.CLEAN_FINISH);
		UserSetting.setClearStatusInfo(this, pkg.toJson());

		try {
			String PCName = UserSetting.getString(getApplicationContext(),
					Constants.SELECTEDPCNAME, "");
			long currentTimeMillis = System.currentTimeMillis();
			Date date = new Date(currentTimeMillis);
			SimpleDateFormat s = new SimpleDateFormat("yyyy.MM.dd HH:mm");
			String formatData = s.format(date);
			UserSetting.setString(getApplicationContext(), PCName, formatData);

			if (state != EXPORT_STATIC_FINISH) {
				Intent finish = new Intent(ExportActivity.this,
						FinishActivity.class);
				this.startActivity(finish);
				finish();
			}

		} catch (Exception e) {
			FLog.e(TAG, "moveFinish, throw error", e);
		}
	}

	private void connectPC(final String ip) {
		try {
			if (null == ip || ip.equals("")) {
				return;
			}
			final String groupTime = String.valueOf(System.currentTimeMillis());
			threadConnectPc = new Thread(new Runnable() {
				public void run() {
					while (mNeedSendUdp) {
						try {
							PhoneInfo info = new PhoneInfo();
							info.initMoreInfo(ExportActivity.this);
							PhoneInfoPkt pkg = new PhoneInfoPkt();
							pkg.setGroupTime(groupTime);
							pkg.setCmd(Constants.CMD_START_DOWNLOAD);

							pkg.setDevicename(info.getDevicename());
							pkg.setDeviceid(info.getDeviceid());
							String path = Constants.APP_ROOT_PATH
									+ Constants.HTTP_EXPORT_FILE_NAME;
							pkg.setPath(path);
							UdpClient.send(ip, pkg.toJson());

							Thread.sleep(Constants.INTERVAL_SEND_START_DOWNLOAD_UDP);
						} catch (InterruptedException e) {
							FLog.e(TAG, "send udp package to pc, throw error",
									e);
						}
					}
				}
			});
			threadConnectPc.start();
		} catch (Exception e) {
			FLog.e(TAG, "selectPC throw error", e);
		}
	}
}
