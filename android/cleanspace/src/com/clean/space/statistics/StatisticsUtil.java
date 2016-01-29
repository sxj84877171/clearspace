package com.clean.space.statistics;

import android.content.Context;

// 负责统计,调用者无需关心具体的统计模块的实现逻辑
public class StatisticsUtil {
	public final static int TYPE_UMENG = 101;
	public final static int TYPE_FLURRY = 102;
	private static StatisticsUmeng mStatisticsUmeng = null;
	private static StatisticsFlurry mStatisticsFlurry = null;

	// 需要传递统计类型参数
	public static IStatistics getInstance(Context context, int statticsType) {
		switch (statticsType) {
		// 所有相片
		case TYPE_UMENG: {
			if (null == mStatisticsUmeng) {
				mStatisticsUmeng = new StatisticsUmeng(context);
			}
			return mStatisticsUmeng;
		}

		// 获取已经导出的相片
		case TYPE_FLURRY: {
			if (null == mStatisticsFlurry) {
				mStatisticsFlurry = new StatisticsFlurry(context);
			}
			return mStatisticsFlurry;
		}

		default:
			break;

		}
		return null;
	}

	// 默认统计是Umeng
	public static IStatistics getDefaultInstance(Context context) {
		if (null == mStatisticsUmeng) {
			mStatisticsUmeng = new StatisticsUmeng(context);
		}
		return mStatisticsUmeng;
	}
}