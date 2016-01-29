package com.clean.space.statistics;

import java.util.Map;

public interface IStatistics {
	
	public void onCreate();
	public void onStart();
	public void onResume();
	public void onPause();
	public void onStop();
	public void onDestroy();
	
	/**
	 * 计数事件
	 * @param eventid
	 */
	public void onEventCount(String eventid);
	
	/**
	 * 计算事件
	 * @param eventid
	 * @param m
	 * @param du
	 */
	public void onEventValueCalculate(String eventid, Map<String,String> m, int du);
}
