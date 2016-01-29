package com.clean.space.network.discover;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.network.http.CloudDiscover;
import com.clean.space.network.udp.UDPDiscover;
import com.clean.space.network.wifi.SoftApDiscover;
import com.clean.space.protocol.PCClientItem;

/**
 * 发现管理类<br>
 * 1.局域网广播发现<br>
 * 2.云端发现，UDP验证发现<br>
 * 3.SOFTAP发现<br>
 * 为单例模式，可以任何时候调用<br>
 * <br>
 * <br>
 * 调用{@link #start()}方法 开始去发现设备。<br>
 * 有2中办法可以获得结果：<br>
 * 1.一种注册{@link #registerReceiver(Receiver)}，通过回调实时获得发现结果。<br>
 * 2.一种通过{@link #getOnlinePcItem()}，获得当前已有哪些结果。<br>
 * 通过{@link #stop()}方法，停止发现。<br>
 * <br>
 * <br>
 * 另外附加两个方法来判断当前是否已经开启发现功能和停止发现<br>
 * 1.{@link #isStart()}<br>
 * 2.{@link #isStop()}<br>
 * <br>
 * <br>
 * 三种发现的优先级：<br>
 * 局域网发现 > UDP验证发现 > SoftAp发现 <br>
 * 当有一个发现时，进行如下检测： <br>
 * 1.检查是否存在在已发现列表中<br>
 * 2.不存在，则直接添加到列表中。<br>
 * 3.存在，则检查已发现检测的类型优先级是否低于发现的类型<br>
 * 4.低于或者等于，则更新当前信息，如优先级，ip，softap信息，发现时间。<br>
 * 5.高于，则检测已发现列表中的类型最后检测到的时间，是否大于了给定的检测时间。<br>
 * 6.大于，则更新当前发现类型到已存在列表中<br>
 * 7.否则，不处理。<br>
 * 
 * @author Elvis<br>
 * <br>
 * <br>
 * <br>
 * 
 */
public class DiscoverManager {

	public final static String TAG = DiscoverManager.class.getSimpleName();

	public static interface Receiver {
		/**
		 * 返回当前这一刻找到的设备信息
		 * 
		 * @param servers
		 *            list of discovered servers, null on error <br>
		 *            设备信息
		 */
		void addAnnouncedServers(PCClientItem servers);
	}

	private static DiscoverManager instance = null;

	/**
	 * 发现管理类<br>
	 * 1.局域网广播发现<br>
	 * 2.云端发现，UDP验证发现<br>
	 * 3.SOFTAP发现<br>
	 * 为单例模式，可以任何时候调用<br>
	 * <br>
	 * <br>
	 * 调用{@link #start()}方法 开始去发现设备。<br>
	 * 有2中办法可以获得结果：<br>
	 * 1.一种注册{@link #registerReceiver(Receiver)}，通过回调实时获得发现结果。<br>
	 * 2.一种通过{@link #getOnlinePcItem()}，获得当前已有哪些结果。<br>
	 * 通过{@link #stop()}方法，停止发现。<br>
	 * <br>
	 * <br>
	 * 另外附加两个方法来判断当前是否已经开启发现功能和停止发现<br>
	 * 1.{@link #isStart()}<br>
	 * 2.{@link #isStop()}<br>
	 * <br>
	 * <br>
	 * * <br>
	 * 三种发现的优先级：<br>
	 * 局域网发现 > UDP验证发现 > SoftAp发现 <br>
	 * 当有一个发现时，进行如下检测： <br>
	 * 1.检查是否存在在已发现列表中<br>
	 * 2.不存在，则直接添加到列表中。<br>
	 * 3.存在，则检查已发现检测的类型优先级是否低于发现的类型<br>
	 * 4.低于或者等于，则更新当前信息，如优先级，ip，softap信息，发现时间。<br>
	 * 5.高于，则检测已发现列表中的类型最后检测到的时间，是否大于了给定的检测时间。<br>
	 * 6.大于，则更新当前发现类型到已存在列表中<br>
	 * 7.否则，不处理。<br>
	 * 
	 * @author Elvis
	 * 
	 */
	public static DiscoverManager getInstance(Context context) {
		if (instance == null) {
			instance = new DiscoverManager(context);
		}
		return instance;
	}

	private List<PCClientItem> listItem = new ArrayList<PCClientItem>();

	private boolean isRunning = false;
	private Context context;

	private Receiver pcclientsReceiver = new Receiver() {

		@Override
		public synchronized void addAnnouncedServers(PCClientItem server) {
			if (isRunning) {
//				FLog.i(TAG, server.getPcname() + " online. and type =" + server.getType());
				int index = listItem.indexOf(server);
				if (index >= 0) {
					// 在列表中，已存在对应该机器。
					PCClientItem pcClientItem = listItem.get(index);
//					FLog.i(TAG, pcClientItem.getPcname() + " has online. and type =" + pcClientItem.getType());
					// 已存在发现的机器类型发现等级低于当前等级，更新当前等级类型及ip
					if (server.getType() <= pcClientItem.getType()) {
//						FLog.i(TAG, pcClientItem.getPcname() + " type is same or lower .and update time");
						if(server.getType() < pcClientItem.getType()){
							FLog.i(TAG, "update " + pcClientItem.getPcname()
									+ " type from" + PCClientItem.getTypeName(pcClientItem.getType())
									+ " to " + PCClientItem.getTypeName(server.getType()));
						}
						pcClientItem.setIp(server.getIp());
						pcClientItem.setType(server.getType());
						pcClientItem.setRectime(server.getRectime());
						if (server.getSoftAp() != null) {
							pcClientItem.setSoftAp(server.getSoftAp());
						}
						if (server.getClient() != null) {
							pcClientItem.setClient(server.getClient());
						}
					} else {
						// 低于当前等级类型，查询上次该类型是否超时，则切换到低等级类型
						long liveTime = System.currentTimeMillis()
								- pcClientItem.getRectime();
//						FLog.i(TAG, "cur:" + System.currentTimeMillis()+ ",last time:" + pcClientItem.getRectime() + ",live time is :" + liveTime);
						if (liveTime > Constants.CHECK_PC_LIVE_TIME) {
							FLog.i(TAG, "update " + pcClientItem.getPcname()
									+ " type from" + PCClientItem.getTypeName(pcClientItem.getType())
									+ " to " + PCClientItem.getTypeName(server.getType()));
							pcClientItem.setIp(server.getIp());
							pcClientItem.setType(server.getType());
							pcClientItem.setRectime(server.getRectime());
							if (server.getSoftAp() != null) {
								pcClientItem.setSoftAp(server.getSoftAp());
							}
							if (server.getClient() != null) {
								pcClientItem.setClient(server.getClient());
							}
						}
					}
				} else {
					// 添加到列表中
					FLog.i(TAG, "add device:" + server.getPcname() +" "+  PCClientItem.getTypeName(server.getType()));
					listItem.add(server);
				}
				if (receiver != null) {
					receiver.addAnnouncedServers(server);
				}
			}
		}
	};

	/**
	 * 获得当前已有哪些结果<br>
	 * 参考{@link #start()} 参考{@link #addAnnouncedServers()}
	 * 
	 * @return 当前搜素发现的结果列表
	 */
	public List<PCClientItem> getOnlinePcItem() {
		List<PCClientItem> result = new ArrayList<PCClientItem>();
		for (PCClientItem item : listItem) {
			if (System.currentTimeMillis() - item.getRectime() < Constants.CHECK_PC_LIVE_TIME) {
				item.setOnline(true);
				item.setStatus(0);
				result.add(item);
			}
		}
		return result;
	}

	private CloudDiscover clound;
	private SoftApDiscover softap;
	private UDPDiscover udp;
	private Receiver receiver;

	/**
	 * 通过回调实时获得发现结果。<br>
	 * 参考{@link #start()}
	 * 
	 * @param receiver
	 */
	public void registerReceiver(Receiver receiver) {
		this.receiver = receiver;
	}

	/**
	 * 发现管理类<br>
	 * 1.局域网广播发现<br>
	 * 2.云端发现，UDP验证发现<br>
	 * 3.SOFTAP发现<br>
	 * 为单例模式，可以任何时候调用<br>
	 * <br>
	 * <br>
	 * 调用{@link #start()}方法 开始去发现设备。<br>
	 * 有2中办法可以获得结果：<br>
	 * 1.一种注册{@link #registerReceiver(Receiver)}，通过回调实时获得发现结果。<br>
	 * 2.一种通过{@link #getOnlinePcItem()}，获得当前已有哪些结果。<br>
	 * 通过{@link #stop()}方法，停止发现。<br>
	 * <br>
	 * <br>
	 * 另外附加两个方法来判断当前是否已经开启发现功能和停止发现<br>
	 * 1.{@link #isStart()}<br>
	 * 2.{@link #isStop()}<br>
	 * <br>
	 * <br>
	 * 
	 * @author Elvis
	 * 
	 */
	private DiscoverManager(Context context) {
		isRunning = false;
		this.context = context;
	}

	/**
	 * 开启功能，搜素附近的机器。可以通过调用{@link #stop()}方法进行停止 <Br>
	 * 想获得结果，通过下面两个方法获得结果<br>
	 * 1.一种注册{@link #registerReceiver(Receiver)}，通过回调实时获得发现结果。<br>
	 * 2.一种通过{@link #getOnlinePcItem()}，获得当前已有哪些结果。<br>
	 */
	public void start() {
		if (!isRunning) {
			isRunning = true;
			softap = new SoftApDiscover(context);
			clound = new CloudDiscover(context);
			udp = UDPDiscover.getInstance();
			clound.setReceiver(pcclientsReceiver);
			softap.setReceiver(pcclientsReceiver);
			udp.registerReceiver(pcclientsReceiver);
			clound.start();
			udp.start();
			softap.start();
			FLog.i(TAG, "DiscoverManager start.");
		}

	}

	/**
	 * 关闭功能。可以通过调用{@link #start()}方法进行停止 <Br>
	 * 想获得结果，通过下面两个方法获得结果<br>
	 * 1.一种注册{@link #registerReceiver(Receiver)}，通过回调实时获得发现结果。<br>
	 * 2.一种通过{@link #getOnlinePcItem()}，获得当前已有哪些结果。<br>
	 */
	public void stop() {
		if (isRunning) {
			isRunning = false;
			clound.stop();
			softap.stop();
			udp.stop();
			udp.unRegisterReceiver(pcclientsReceiver);
			listItem.clear();
			FLog.i(TAG, "DiscoverManager stop.");
		}
	}

	/**
	 * 当前是否在发现设备，参考{@link #start()}方法
	 * 
	 * @return 是否工作
	 */
	public boolean isStart() {
		return isRunning;
	}

	/**
	 * 当前是否在发现设备，参考{@link #stop()}方法
	 * 
	 * @return 是否工作
	 */
	public boolean isStop() {
		return !isRunning;
	}

	@Override
	protected void finalize() throws Throwable {
		stop();
		super.finalize();
	}

}
