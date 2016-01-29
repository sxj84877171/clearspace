package com.clean.space.statistics;

import java.util.Map;

import android.content.Context;

import com.clean.space.log.FLog;
import com.clean.space.notification.CleanSpaceNotificationManager;
import com.clean.space.util.ReadMataDataUtil;
import com.umeng.analytics.AnalyticsConfig;
import com.umeng.analytics.MobclickAgent;

public class StatisticsUmeng implements IStatistics {

	private final String TAG = "StatisticsUmeng";
	private Context mContext = null;
	private boolean bReleaseChannel = true;
	private static String UMENG_TEST_KEY = "CDLSW2";
	private static String UMENG_KEY = "5608a4f667e58e1f9800000f";
	public StatisticsUmeng(Context context) {
		mContext = context;
	}

	@Override
	public void onCreate() {
		// AnalyticsConfig.setChannel(getChannel());
		if (isReleaseChannel()) {
			MobclickAgent.setDebugMode(true);
			MobclickAgent.updateOnlineConfig(mContext);
			AnalyticsConfig.enableEncrypt(true);
		}
	}

	private boolean isReleaseChannel(){
		boolean releaseChannel = true;
		try {
			String channel = ReadMataDataUtil.getChannel(mContext);
			
			// 如果channel是CDLSW2,则不统计到友盟
			releaseChannel = !channel.equalsIgnoreCase("CDLSW2");
		} catch (Exception e) {
			FLog.e(TAG, "isReleaseChannel throw error", e);
		}

		return releaseChannel;

	}
	@Override
	public void onStart() {

	}

	@Override
	public void onResume() {
		try {
			CleanSpaceNotificationManager.getInstance().closeNotificationFunction(mContext);
			if (isReleaseChannel()) {
				MobclickAgent.onResume(mContext);
			}
		} catch (Exception e) {
			FLog.e(TAG, "throw error onResume", e);
		}
	}

	@Override
	public void onPause() {
		try {
			CleanSpaceNotificationManager.getInstance().openNotificationFunction(mContext);
			if (isReleaseChannel()) {
				MobclickAgent.onPause(mContext);
			}
		} catch (Exception e) {
			FLog.e(TAG, "throw error onResume", e);
		}
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onDestroy() {

	}

	@Override
	public void onEventCount(String eventid) {
		try {
			if (isReleaseChannel()) {
				if (null != eventid && !eventid.isEmpty() && null != mContext) {
					MobclickAgent.onEvent(mContext, eventid);
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "throw error onEventCount" + eventid);
		}
	}

	@Override
	public void onEventValueCalculate(String eventid, Map<String, String> m,
			int du) {
		try {
			if (isReleaseChannel()) {
				if (null != eventid && !eventid.isEmpty() && null != mContext) {
					MobclickAgent.onEventValue(mContext, eventid, m, du);
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "throw error onEventValueCalculate" + eventid);
		}
	}
}
