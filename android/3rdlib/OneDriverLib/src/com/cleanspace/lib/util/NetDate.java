package com.cleanspace.lib.util;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.SystemClock;

public class NetDate {
	
	private final static List<String> NTP_HOSTS = new ArrayList<String>();
	
	private final static int TIMEOUT = 3000;
	
	private static long recordNetDate;
	private static long recordRunDate;
	
	static {
		NTP_HOSTS.add("ntp1.jst.mfeed.ad.jp");
		NTP_HOSTS.add("clock.tl.fukuoka-u.ac.jp");

		NTP_HOSTS.add("clock.cuhk.edu.hk");
		NTP_HOSTS.add("ntp.nict.jp");

		NTP_HOSTS.add("ntp.ring.gr.jp");
	}
	
	private long computeDate() {
		long stamp = (recordRunDate == 0) ? 0 : SystemClock.elapsedRealtime()
				- recordRunDate;
		return recordNetDate + stamp;
	}
	
	public long getUTCNetworkDate() {
		if (recordNetDate != 0) {
			return computeDate();
		} else {
			ExecutorService es = Executors.newFixedThreadPool(5);
			Object syncLock = new Object();
			List<FutureTask<Long>> tasks = new ArrayList<FutureTask<Long>>();
			AtomicInteger ai = new AtomicInteger();
			for (int index = 0; index < NTP_HOSTS.size(); index++) {
				FutureTask<Long> task = new FutureTask<Long>(new SntpTask(NTP_HOSTS.get(index), ai, syncLock));
				tasks.add(task);
				ai.addAndGet(1);
				es.submit(task);
			}
			es.shutdown();
			
			synchronized (syncLock) {
				try {
					syncLock.wait(TIMEOUT*3);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			long now = System.currentTimeMillis();
			// cancel request after get ntp time
			for(FutureTask<Long> task : tasks) {
				if(task.isDone()) {
					try {
						long result = task.get();
						if(result != -1) {
							now = result;
							recordNetDate = now;
							recordRunDate = SystemClock.elapsedRealtime();
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				} else {
					task.cancel(false);
				}
			}
			return now;
		}
	}
	
	// return -1 if fail
	public class SntpTask implements Callable<Long> {
		
		String host = "";
		Object syncLock = null;
		AtomicInteger ai;
		
		public SntpTask(String host, AtomicInteger ai, Object syncLock) {
			this.host = host;
			this.syncLock = syncLock;
			this.ai = ai;
		}

		@Override
		public Long call() {
			long result = -1;
			SntpClient client = new SntpClient();
			if (client.requestTime(host, TIMEOUT)) {
				result = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
				synchronized (syncLock) {
					syncLock.notify();
				}
			}
			if(ai.decrementAndGet() == 0) {
				synchronized (syncLock) {
					syncLock.notify();
				}
			}
			return result;
		}
		
	}
}
