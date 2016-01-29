package com.clean.space.network.http;

import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.content.Context;

import com.clean.space.UserSetting;
import com.clean.space.log.FLog;
import com.google.gson.Gson;

/**
 * 排名管理<br>
 * 根据当前用户的一些传输数据进行计算。 <br>
 * 传输数据有：此次传输的速速，传输文件的大小，个数。上传到云端，在云端进行排名，并下载到本地。 <br>
 * 首先显示本地传输本地数量和当前数据量大小，然后再把本次数据上传到云端。获取到排名后，保存到本地。<br>
 * 如果这次上传的数据失败，则本地简单计算累计传输数量和大小，保存上次排名。<br>
 * 直到下次进行传输时，再把传输的数据上传到网络上，再进行更新本地数据。
 * 
 * @author Elvis
 * 
 */
public class RankManager {

	public static class SortRet {
		/**
		 * 状态，大于等于0 表示成功
		 */
		private int status;
		/**
		 * 当前状态的详细信息
		 */
		private String message;
		/**
		 * 传输速度
		 */
		private long speed;

		public long getSpeed() {
			return speed;
		}

		public void setSpeed(long speed) {
			this.speed = speed;
		}

		private SortData sortdata;

		public int getStatus() {
			return status;
		}

		public void setStatus(int status) {
			this.status = status;
		}

		public String getMessage() {
			return message;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public SortData getSortdata() {
			return sortdata;
		}

		public void setSortdata(SortData sortdata) {
			this.sortdata = sortdata;
		}

		public static SortRet parser(String json) {
			Gson gson = new Gson();
			SortRet bean = gson.fromJson(json, SortRet.class);
			return bean;
		}

		public String toJson() {
			Gson gson = new Gson();
			return gson.toJson(this);
		}
	}

	public static class SortData {
		/**
		 * 总传输速度排名。为总排名数
		 */
		private long total_speed_rank = 1;
		/**
		 * 当前用户的排名
		 */
		private long your_speed_rank = 1;
		/**
		 * 总共传输文件数量排名
		 */
		private long total_transfer_count_rank = 1;
		/**
		 * 当前用户传输总文件数量排名
		 */
		private long your_transfer_count_rank = 1;
		/**
		 * 总共传输文件大小排名
		 */
		private long total_transfersize_rank = 1;
		/**
		 * 当前用户传输文件大小排名
		 */
		private long your_transfersize_rank = 1;
		/**
		 * 总共传输文件大小
		 */
		private long total_transfersize;
		/**
		 * 总传输文件数量
		 */
		private long total_transfercount;
		/**
		 * 总清理文件数量
		 */
		private long total_cleanphotocount;
		/**
		 * 总清除空间
		 */
		private long total_cleanspace;

		public long getTotal_speed_rank() {
			return total_speed_rank;
		}

		public void setTotal_speed_rank(long total_speed_rank) {
			this.total_speed_rank = total_speed_rank;
		}

		public long getYour_speed_rank() {
			return your_speed_rank;
		}

		public void setYour_speed_rank(long your_speed_rank) {
			this.your_speed_rank = your_speed_rank;
		}

		public long getTotal_transfer_count_rank() {
			return total_transfer_count_rank;
		}

		public void setTotal_transfer_count_rank(long total_transfer_count_rank) {
			this.total_transfer_count_rank = total_transfer_count_rank;
		}

		public long getYour_transfer_count_rank() {
			return your_transfer_count_rank;
		}

		public void setYour_transfer_count_rank(long your_transfer_count_rank) {
			this.your_transfer_count_rank = your_transfer_count_rank;
		}

		public long getTotal_transfersize_rank() {
			return total_transfersize_rank;
		}

		public void setTotal_transfersize_rank(long total_transfersize_rank) {
			this.total_transfersize_rank = total_transfersize_rank;
		}

		public long getYour_transfersize_rank() {
			return your_transfersize_rank;
		}

		public void setYour_transfersize_rank(long your_transfersize_rank) {
			this.your_transfersize_rank = your_transfersize_rank;
		}

		public long getTotal_transfersize() {
			return total_transfersize;
		}

		public void setTotal_transfersize(long total_transfersize) {
			this.total_transfersize = total_transfersize;
		}

		public long getTotal_transfercount() {
			return total_transfercount;
		}

		public void setTotal_transfercount(long total_transfercount) {
			this.total_transfercount = total_transfercount;
		}

		public long getTotal_cleanphotocount() {
			return total_cleanphotocount;
		}

		public void setTotal_cleanphotocount(long total_cleanphotocount) {
			this.total_cleanphotocount = total_cleanphotocount;
		}

		public long getTotal_cleanspace() {
			return total_cleanspace;
		}

		public void setTotal_cleanspace(long total_cleanspace) {
			this.total_cleanspace = total_cleanspace;
		}
	}

	public static final String URL = "http://114.215.236.240:8080/metrics/sort?app_id=photomaster&";

	public static final String RANK_KEY = "rank_key";
	public static final String SAVE_RANK_KEY = "save_rank_key";

	private RankManager() {
		// &device_id=1&transfer_size=1&transfer_count=1&clean_photo_count=1&clean_space=1
	}

	private static RankManager instance;

	public static RankManager getInstance() {
		if (instance == null) {
			synchronized (RankManager.class) {
				if (instance == null) {
					instance = new RankManager();
				}
			}
		}
		return instance;
	}

	public SortRet uploadData(String deviceID, String speed) {
		return uploadData(deviceID, "0", "0", speed);
	}

	public SortRet uploadData(String deviceID, String transferSize,
			String transferCount, String speed) {
		return uploadData(deviceID, transferSize, transferCount, "0", "0",
				speed);
	}

	/**
	 * 上传排名数据
	 * 
	 * @param deviceID
	 *            设备ID
	 * @param transferSize
	 *            传输文件大小
	 * @param transferCount
	 *            传输文件数量
	 * @param cleanPhoneCount
	 *            清理文件数量
	 * @param cleanSpace
	 *            清理文件大小
	 * @param speed
	 *            速度
	 * @return
	 */
	public SortRet uploadData(String deviceID, String transferSize,
			String transferCount, String cleanPhoneCount, String cleanSpace,
			String speed) {
		SortRet sr = new SortRet();
		StringBuilder sb = new StringBuilder();
		sb.append("device_id=").append(deviceID).append("&");
		sb.append("transfer_size=").append(transferSize).append("&");
		sb.append("transfer_count=").append(transferCount).append("&");
		sb.append("clean_photo_count=").append(cleanPhoneCount).append("&");
		sb.append("clean_space=").append(cleanSpace).append("&");
		sb.append("speed=").append(speed);
		try {
			sr = upload(sb.toString());
			if (sr != null) {
				return sr;
			}
		} catch (Exception e) {
			FLog.e(RankManager.class.getSimpleName(), e);
		}
		sr = new SortRet();
		sr.status = -1;
		sr.message = sb.toString();
		return sr;
	}

	private SortRet upload(String sb) throws IOException,
			ClientProtocolException {
		FLog.i(RankManager.class.getSimpleName(), URL + sb);
		HttpGet httpGet = new HttpGet(URL + sb.toString());
		HttpResponse response = new DefaultHttpClient().execute(httpGet);
		if (response.getStatusLine().getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			String result = EntityUtils.toString(entity, HTTP.UTF_8);
			FLog.i(RankManager.class.getSimpleName(), "result:" + result);
			return SortRet.parser(result);
		}
		return null;
	}

	/**
	 * 获取本地排名数据
	 * 
	 * @param context
	 * @return
	 */
	public SortRet getLocalData(Context context) {
		SortRet ret = new SortRet();
		ret.setSortdata(new SortData());
		String value = UserSetting.getString(context, RANK_KEY, "");
		if (value != null && !"".equals(value.trim())) {
			ret = SortRet.parser(value);
		}
		return ret;
	}

	/**
	 * 保存排名数据
	 * 
	 * @param context
	 * @param ret
	 */
	public void saveSortRetData(Context context, SortRet ret) {
		String json = ret.toJson();
		UserSetting.setString(context, RANK_KEY, json);
	}

	/**
	 * 保存未上传成功数据
	 * 
	 * @param contex
	 * @param data
	 */
	public void saveunUploadData(Context contex, String data) {
		String value = UserSetting.getString(contex, SAVE_RANK_KEY, "");
		if (value != null && !"".equals(value)) {
			value = value + ";" + data;
		} else {
			value = data;
		}
		UserSetting.setString(contex, SAVE_RANK_KEY, value);
	}

	/**
	 * 上传未成功数据
	 * 
	 * @param contex
	 */
	public void updateOldData(Context contex) {
		String value = UserSetting.getString(contex, SAVE_RANK_KEY, "");
		if (value != null && !"".equals(value)) {
			String[] vas = value.split(";");
			for (String tmp : vas) {
				try {
					SortRet sr = upload(tmp);
					if (sr != null) {
						UserSetting.setString(contex, SAVE_RANK_KEY, "");
					} else {
						break;
					}
				} catch (Exception e) {
				}
			}
		}
	}
}
