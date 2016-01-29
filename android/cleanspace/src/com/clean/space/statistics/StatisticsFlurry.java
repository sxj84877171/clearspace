package com.clean.space.statistics;

import java.util.Map;

import android.content.Context;

public class StatisticsFlurry implements IStatistics{
	
	private Context mContext = null;
	public StatisticsFlurry(Context context) {
		mContext = context;
	}
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onResume() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEventCount(String eventid) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onEventValueCalculate(String eventid, Map<String, String> m,
			int du) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onPause() {
		// TODO Auto-generated method stub
		
	}

}
