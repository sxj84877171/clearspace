package com.clean.space.util;

import java.io.File;

import android.annotation.SuppressLint;
import com.clean.space.log.FLog;

@SuppressLint("DefaultLocale")
public class SpaceUtil {
	private static final String TAG = "SpaceUtil";

	/** 方法重载,传入任意long类型字节,返回kb,mb,gb类型数据,code中需要对size进行判断属于kb,mb还是gb */
	public static String convertToGb(long size) {
		String strGb = "";

		if (size < 1024 * 1024 && size >= 0) {
			int dG = (int) (size / 1024.0);
			strGb = dG + "";
		} else if (size >= 1024 * 1024 && size < 1024 * 1024 * 1024) {
			int dG = (int) (size / 1024.0 / 1024.0);
			strGb = dG + "";
		} else if (size >= 1024 * 1024 * 1024) {
			double dG = size / 1024.0 / 1024 / 1024.0;
			strGb = String.format("%.2f", dG);// gb
		} else {
			strGb = "0";
		}

		return strGb;
	}

	/**
	 * @param size
	 * @return 返回两位有效数字的数据
	 */
	public static String convertSize(long size) {
		String strGb = "";
		if (size < 1024 * 1024 && size >= 0) {

			double dG = size / 1024.0;
			strGb = String.format("%.2f", dG);// kb
			strGb = exchangeChar(strGb);
			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 3);

		} else if (size >= 1024 * 1024 && size < 1024 * 1024 * 1024) {

			double dG = size / 1024.0 / 1024;
			strGb = String.format("%.2f", dG);// mb
			strGb = exchangeChar(strGb);
			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 3);

		} else if (size >= 1024 * 1024 * 1024) {

			double dG = size / 1024.0 / 1024 / 1024;
			strGb = String.format("%.2f", dG);// gb
			strGb = exchangeChar(strGb);
			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 3);

		} else {
			strGb = "0.00";
		}

		return strGb;
	}

	public static String convertSize(long size, boolean bIgnore) {
		String strGb = "";
		String strb = "";
		if (size < 1024 * 1024 && size >= 0) {

			double dG = size / 1024.0;
			strGb = String.format("%.2f", dG);// kb
			strGb = exchangeChar(strGb);
			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 3);
			strb = "KB";

		} else if (size >= 1024 * 1024 && size < 1024 * 1024 * 1024) {

			double dG = size / 1024.0 / 1024;
			strGb = String.format("%.2f", dG);// mb
			strGb = exchangeChar(strGb);
			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 3);
			strb = "MB";

		} else if (size >= 1024 * 1024 * 1024) {

			double dG = size / 1024.0 / 1024 / 1024;
			strGb = String.format("%.2f", dG);// gb
			strGb = exchangeChar(strGb);
			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 3);
			strb = "GB";

		} else {
			strGb = "0.00";
		}

		return strGb + strb;
	}

	/** i为任意int类型数值,返回带有一位小数的数据 */
	public static String convertSize(long size, int i) {
		String strGb = "";

		if (size < 1024 * 1024 && size >= 0) {

			double dG = size / 1024.0;
			strGb = String.format("%.1f", dG);// kb

			strGb = exchangeChar(strGb);

			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 2);

		} else if (size >= 1024 * 1024 && size < 1024 * 1024 * 1024) {

			double dG = size / 1024.0 / 1024;
			strGb = String.format("%.1f", dG);// mb
			strGb = exchangeChar(strGb);

			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 2);

		} else if (size >= 1024 * 1024 * 1024) {

			double dG = size / 1024.0 / 1024 / 1024;
			strGb = String.format("%.1f", dG);// gb
			
			strGb = exchangeChar(strGb);

			int indexOf = strGb.indexOf(".");
			strGb = strGb.substring(0, indexOf + 2);

		} else {
			strGb = "0.0";
		}

		return strGb;
	}

	/**
	 * @param size
	 *            long类型
	 * @param num
	 *            任意int类型数字
	 * @return gb类型大小
	 */
	public static String convertToGb(long size, int num) {
		String strGb = "";

		double dG = size / 1024.0 / 1024 / 1024.0;
		strGb = String.format("%.2f", dG);
		
		strGb = exchangeChar(strGb);

		return strGb;
	}

	/**
	 * 多语言格式错误"," 和 "."
	 * @param strGb
	 * @return
	 */
	private static String exchangeChar(String strGb) {
		int of = strGb.indexOf(",");
		if (of != -1) {
			strGb = strGb.replace(",", ".");
		}
		return strGb;
	}

	public static class SDCardInfo {
		public long total;

		public long free;
	}

	public static String getFreeSpace() {
		String freeSize = "";
		SDCardInfo info = getSDCardInfo();
		if (null != info) {
			freeSize = convertToGb(info.free, 1);
		}
		return freeSize;
	}

	/** 返回字符串 包含: 剩余空间大小(字节)和转换成kb/mb/gb的数据,格式: freeSize+"/"+size */
	public static String getFreeSpace2() {
		String freeSize = "";
		SDCardInfo info = getSDCardInfo();
		long free = info.free;
		if (null != info) {
			freeSize = convertSize(free);
		}
		return freeSize + "/" + free;
	}

	@SuppressWarnings("deprecation")
	public static SDCardInfo getSDCardInfo() {
		String sDcString = android.os.Environment.getExternalStorageState();

		if (sDcString.equals(android.os.Environment.MEDIA_MOUNTED)) {
			File pathFile = android.os.Environment
					.getExternalStorageDirectory();

			try {
				android.os.StatFs statfs = new android.os.StatFs(
						pathFile.getPath());

				// 获取SDCard上BLOCK总数
				long nTotalBlocks = statfs.getBlockCount();

				// 获取SDCard上每个block的SIZE
				long nBlocSize = statfs.getBlockSize();

				// 获取可供程序使用的Block的数量
				long nAvailaBlock = statfs.getAvailableBlocks();

				// 获取剩下的所有Block的数量(包括预留的一般程序无法使用的块)
				long nFreeBlock = statfs.getFreeBlocks();

				SDCardInfo info = new SDCardInfo();
				// 计算SDCard 总容量大小MB
				info.total = nTotalBlocks * nBlocSize;

				// 计算 SDCard 剩余大小MB
				info.free = nAvailaBlock * nBlocSize;

				return info;
			} catch (IllegalArgumentException e) {
				FLog.e(TAG, e.toString());
			}
		}

		return null;
	}
}
