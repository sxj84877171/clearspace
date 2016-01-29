package com.clean.space;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;

import com.clean.space.log.FLog;
import com.clean.space.protocol.CleanFileStatusPkg;
import com.clean.space.statistics.StatisticsUtil;
import com.clean.space.util.FileUtil;

/**
 * @Des Loading_
 */
@SuppressLint("SdCardPath")
public class SplishActivity extends Activity {

	private String TAG = SplishActivity.class.getSimpleName();

	private Handler handler = new Handler();

	private Runnable main = new Runnable() {

		@Override
		public void run() {
			// jump2MainActivity();
			selectAct();
			if (main != null) {
				handler.removeCallbacks(main);
				main = null;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		FLog.init(FLog.ACTIVITY_LOG_FILE.replace("$1", getVersionCode()));

		initView();

		Intent service = new Intent();
		service.setAction("com.clean.space.CoreService");
		service.setPackage(getPackageName());
		startService(service);

		new Thread() {
			public void run() {
				FileUtil.saveNeedSharedPhoto2File(SplishActivity.this,
						Constants.SHAREPATH);
			}
		}.start();

		// initData();

		// initAnimation();

		// initEvent();

		// scanFile();

		// testPhotoMgr();

	}

	/** 初始化控件 */
	private void initView() {
		setContentView(R.layout.splish);

		handler.postDelayed(main, Constants.SPLISH2MAIN_USETIME);
		UserSetting.setBoolean(SplishActivity.this, Constants.DOWNLOAD_START,
				false);
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

	private void selectAct() {
		try {
			String clearStatus = UserSetting.getClearStausInfo(this);
			CleanFileStatusPkg pkg = CleanFileStatusPkg.parse(clearStatus);
			Intent intent = null;
			// 程序退出后,不在直接进入正在导出界面
			if (pkg.getCleanStatus() == CleanFileStatusPkg.CLEAN_ING) {
				intent = new Intent(SplishActivity.this, ExportActivity.class);// export界面
			} else if (pkg.getCleanStatus() == CleanFileStatusPkg.CLEAN_FINISH) {
				intent = new Intent(SplishActivity.this, FinishActivity.class);// finish界面
			} else {
				intent = new Intent(SplishActivity.this, MainActivity.class);// main界面
			}
			// jump2MainActivity();
			startActivity(intent);
			finish();
		} catch (Exception e) {
			FLog.e(TAG, "selectAct select throw error", e);
		}
	}

	@Override
	public void onBackPressed() {
		return;
	}

	@Override
	protected void onDestroy() {
		if (main != null) {
			handler.removeCallbacks(main);
			main = null;
		}
		StatisticsUtil.getDefaultInstance(getApplicationContext()).onDestroy();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		StatisticsUtil.getDefaultInstance(getApplicationContext()).onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		StatisticsUtil.getDefaultInstance(getApplicationContext()).onResume();
		super.onResume();
	}

	// private void jump2MainActivity() {
	// Intent intent = new Intent(SplishActivity.this, MainActivity.class);
	// startActivity(intent);// 启动主界面
	// finish();
	// }

	// private void startPhotoMgrEngine(){
	// {
	// AllPhotoManager photoMgr4 =
	// (AllPhotoManager)PhotoManagerFactory.getInstance(SplishActivity.this,
	// PhotoManagerFactory.PHOTO_MGR_ALL);
	// photoMgr4.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	// fileSize = photoMgr4.getFileSize();
	// fileNumber = photoMgr4.getFileNumber();
	//
	// //
	// //IPhotoManager photoMgr = PhotoManagerFactory.getPhontoMgrInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_SIMILAR);
	// //photoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	// //IPhotoManager photoMgr2 =
	// PhotoManagerFactory.getPhontoMgrInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
	// //photoMgr2.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	//
	// IPhotoManager photoMgr3 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_EXPORTED);
	// photoMgr3.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	//
	// }
	// }
	// /** 初始化数据 */
	// private void initData() {
	// PackageManager pm = getPackageManager();
	// try {
	// PackageInfo packageInfo = pm.getPackageInfo(getPackageName(), 0);
	//
	// versionName = packageInfo.versionName;
	//
	// version.setText(versionName);
	// } catch (NameNotFoundException e) {
	// }
	// }

	// /** 初始化事件 */
	// private void initEvent() {
	// aa.setAnimationListener(new AnimationListener() {
	//
	// @Override
	// public void onAnimationStart(Animation animation) {
	//
	// }
	//
	// @Override
	// public void onAnimationRepeat(Animation animation) {
	//
	// }
	//
	// @Override
	// public void onAnimationEnd(Animation animation) {
	//
	// selectAct();
	// }
	// });
	// }

	// /** 初始化动画 */
	// private void initAnimation() {
	//
	// aa = new AlphaAnimation(1, 1);
	//
	// aa.setDuration(1500);
	// aa.setFillAfter(true);

	// loading.startAnimation(aa);

	// }

	// private void scanFile() {
	// new Thread(new Runnable() {
	// public void run() {
	// try {
	//
	// AllPhotoManager photoMgr =
	// (AllPhotoManager)PhotoManagerFactory.getInstance(SplishActivity.this,
	// PhotoManagerFactory.PHOTO_MGR_ALL);
	// photoMgr.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	// fileSize = photoMgr.getFileSize();
	// fileNumber = photoMgr.getFileNumber();
	//
	// Thread.sleep(2000);
	// Message msg = new Message();
	// msg.what = SCAN_SUCCEED;
	// mHandler.sendMessage(msg);
	//
	// // 先扫描文件
	// //startPhotoMgrEngine();
	// } catch (InterruptedException e) {
	// FLog.e(TAG, "scanFile select throw error", e);
	// }
	// }
	// }).start();
	// }

	// private Handler mHandler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// super.handleMessage(msg);
	// switch (msg.what) {
	// case SCAN_SUCCEED:
	// selectAct();
	// break;
	//
	// default:
	// break;
	// }
	// }
	// };

	// private void testPhotoMgr(){
	// {
	// //
	// IPhotoManager photoMgr = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_SIMILAR);
	// photoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_ASC, 0);
	//
	// IPhotoManager photoMgr2 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
	// photoMgr2.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_ASC, 0);
	//
	//
	// IPhotoManager photoMgr3 =PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_EXPORTED);
	// photoMgr3.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_ASC, 0);
	//
	// AllPhotoManager photoMgr4 =
	// (AllPhotoManager)PhotoManagerFactory.getInstance(SplishActivity.this,
	// PhotoManagerFactory.PHOTO_MGR_ALL);
	// photoMgr4.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_ASC, 0);
	// fileSize = photoMgr4.getFileSize();
	// fileNumber = photoMgr4.getFileNumber();
	//
	// }
	// {
	// //
	// IPhotoManager photoMgr = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_SIMILAR);
	// photoMgr.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	// IPhotoManager photoMgr2 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
	// photoMgr2.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	//
	// IPhotoManager photoMgr3 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_EXPORTED);
	// photoMgr3.startScan(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	// AllPhotoManager photoMgr4 =
	// (AllPhotoManager)PhotoManagerFactory.getInstance(SplishActivity.this,
	// PhotoManagerFactory.PHOTO_MGR_ALL);
	// photoMgr4.getPhotosSync(IPhotoManager.SORT_TYPE_TIME,
	// IPhotoManager.ORDER_BY_DESC, 0);
	// fileSize = photoMgr4.getFileSize();
	// fileNumber = photoMgr4.getFileNumber();
	//
	// }
	// {
	// //
	// IPhotoManager photoMgr = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_SIMILAR);
	// photoMgr.startScan(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_ASC, 0);
	//
	// IPhotoManager photoMgr2 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
	// photoMgr2.startScan(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_ASC, 0);
	//
	//
	// IPhotoManager photoMgr3 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_EXPORTED);
	// photoMgr3.startScan(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_ASC, 0);
	//
	// AllPhotoManager photoMgr4 =
	// (AllPhotoManager)PhotoManagerFactory.getInstance(SplishActivity.this,
	// PhotoManagerFactory.PHOTO_MGR_ALL);
	// photoMgr4.getPhotosSync(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_ASC, 0);
	// fileSize = photoMgr4.getFileSize();
	// fileNumber = photoMgr4.getFileNumber();
	//
	// }
	//
	// {
	// //
	// IPhotoManager photoMgr = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_SIMILAR);
	// photoMgr.startScan(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	// IPhotoManager photoMgr2 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_UNEXPORTED);
	// photoMgr2.startScan(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	//
	// IPhotoManager photoMgr3 = PhotoManagerFactory.getInstance(this,
	// PhotoManagerFactory.PHOTO_MGR_EXPORTED);
	// photoMgr3.startScan(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_DESC, 0);
	//
	// AllPhotoManager photoMgr4 =
	// (AllPhotoManager)PhotoManagerFactory.getInstance(SplishActivity.this,
	// PhotoManagerFactory.PHOTO_MGR_ALL);
	// photoMgr4.getPhotosSync(IPhotoManager.SORT_TYPE_SIZE,
	// IPhotoManager.ORDER_BY_DESC, 0);
	// fileSize = photoMgr4.getFileSize();
	// fileNumber = photoMgr4.getFileNumber();
	//
	// }
	// }
}
