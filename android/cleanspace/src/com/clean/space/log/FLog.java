package com.clean.space.log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.clean.space.Constants;

@SuppressLint("SimpleDateFormat")
public class FLog {
	// format: 2014-10-01 12:00:01 DEBUG|TAG msg
	private static FileWriter fileWriter = null;
	private static FileReader fileReader;
	private static BufferedReader br;

	public static final String LOG_PATH = Constants.APP_ROOT_PATH + "log/";
	public static final String LOG_ALL_TAG = "All";

	public static final String ACTIVITY_LOG_FILE = "cleanspace_activity_$1.log";
	public static final String SERVICE_LOG_FILE = "cleanspace_service_$1.log";
	public static final String NOTIFICATION_LOG_FILE = "cleanspace_notification_$1.log";
	private static final String CORLOR_GRAY = "gray";
	private static final String CORLOR_BLUE = "blue";
	private static final String CORLOR_GREEN = "green";
	private static final String CORLOR_OLIVE = "olive";
	private static final String CORLOR_RED = "red";
	private static final String CORLOR_PURPLE = "Purple";
	private static final boolean DEBUG = true ;
	private static long FILE_SIZE = 1 * 1024 * 1024 ;
	private static long DELETE_LOG_TIME  = 0l;

	private static String logname;
	
	static{
		File file = new File(LOG_PATH);
		if(!file.exists()){
			file.mkdirs();
		}
	}

	public static void init(String filename) {
		logname = filename;
		try {
			if (logname != null) {
				checkLogFile();
				if (fileWriter == null) {
					File file = new File(LOG_PATH + logname);
					fileWriter = new FileWriter(file,
							true);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void uninit() {
		try {
			if (fileWriter != null) {
				fileWriter.close();
			}
			fileWriter = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static String readLogByLine() {
		String ret = null;
		try {
			if (br != null) {
				ret = br.readLine();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return ret;
	}

	private static String CorlorFilter(String msg) {
		int level = Constants.LOG_LEVEL_VERBOSE;

		if (msg.contains((Constants.LEVEL_VERBOSE + "|"))) {
			level = Constants.LOG_LEVEL_VERBOSE;
			msg = "<font color=\"" + CORLOR_GRAY + "\">" + msg + "</font>";
		} else if (msg.contains((Constants.LEVEL_DEBUG + "|"))) {
			level = Constants.LOG_LEVEL_DEBUG;
			msg = "<font color=\"" + CORLOR_BLUE + "\">" + msg + "</font>";
		} else if (msg.contains((Constants.LEVEL_INFO + "|"))) {
			level = Constants.LOG_LEVEL_INFO;
			msg = "<font color=\"" + CORLOR_GREEN + "\">" + msg + "</font>";
		} else if (msg.contains((Constants.LEVEL_WARN + "|"))) {
			level = Constants.LOG_LEVEL_WARN;
			msg = "<font color=\"" + CORLOR_OLIVE + "\">" + msg + "</font>";
		} else if (msg.contains((Constants.LEVEL_ERROR + "|"))) {
			level = Constants.LOG_LEVEL_ERROR;
			msg = "<font color=\"" + CORLOR_RED + "\">" + msg + "</font>";
		} else if (msg.contains((Constants.LEVEL_SUCCESS + "|"))) {
			level = Constants.LOG_LEVEL_SUCCESS;
			msg = "<font color=\"" + CORLOR_PURPLE + "\">" + msg + "</font>";
		}

		if (mLevel > level) {
			return "";
		}

		return msg + "<br>";
	}

	public static String filterByLevel(String msg, int level) {
		String[] levels = { Constants.LEVEL_VERBOSE, Constants.LEVEL_DEBUG,
				Constants.LEVEL_INFO, Constants.LEVEL_WARN,
				Constants.LEVEL_ERROR, Constants.LEVEL_SUCCESS };
		int startpos = 0;
		String logString = msg;
		while (true) {
			int start = logString.indexOf('<', startpos);
			if (start < 0) {
				break;
			}

			int end = logString.indexOf("<br>", start + 1);
			if (end < 0) {
				break;
			}

			startpos = end + 4;

			String subString = logString.substring(start, end + 4);
			for (int i = 0; i < level; i++) {
				if (subString.contains(levels[i] + "|")) {
					logString = logString.substring(startpos);
					startpos = 0;
					break;
				}
			}
		}

		return logString;
	}

	private static int mLevel = Constants.LOG_LEVEL_VERBOSE;

	public static String getHtmlLogByLevel(int level) {
		StringBuilder builder = new StringBuilder();
		try {
			fileReader = new FileReader(new File(LOG_PATH));
			br = new BufferedReader(fileReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		mLevel = level;
		while (true) {
			String aline = readLogByLine();
			if (aline == null) {
				break;
			}
			builder.append(CorlorFilter(aline));
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static String getLogByFilename(String filename) {
		StringBuilder builder = new StringBuilder();
		try {
			fileReader = new FileReader(new File(LOG_PATH + filename));
			br = new BufferedReader(fileReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (true) {
			String aline = readLogByLine();
			if (aline == null) {
				break;
			}
			if ("".equals(aline.trim())) {
				continue;
			}
			try {
				if (aline.length() > 120) {
					builder.append(CorlorFilter(aline.substring(
							aline.indexOf(":") + 1, 120)
							+ "..."));
				} else {
					builder.append(CorlorFilter(aline.substring(aline
							.indexOf(":") + 1)));
				}
			} catch (Exception e) {
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static String getLogByTAG(String filename, String tag) {
		StringBuilder builder = new StringBuilder();
		try {
			fileReader = new FileReader(new File(LOG_PATH + filename));
			br = new BufferedReader(fileReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (true) {
			String aline = readLogByLine();
			if (aline == null) {
				break;
			}
			if (aline.contains(tag)) {
				try {
					builder.append(CorlorFilter(aline.substring(aline
							.indexOf(":") + 1)));
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return builder.toString();
	}

	public static List<String> getLogTagAndPid(String filename) {
		try {
			fileReader = new FileReader(new File(LOG_PATH + filename));
			br = new BufferedReader(fileReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		List<String> list = new ArrayList<String>();
		list.add(LOG_ALL_TAG);
		while (true) {
			String aline = readLogByLine();
			if (aline == null) {
				break;
			}
			try {
				String tag = aline.substring(aline.indexOf("|") + 1,
						aline.indexOf(" ", aline.indexOf("|") + 1));
				boolean found = false;
				for (String str : list) {
					if (str.equals(tag)) {
						found = true;
						break;
					}
				}
				if (!found) {
					list.add(tag);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		try {
			if(br != null){
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			if(fileReader != null){
				fileReader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

	private synchronized static void writeLine(String msg) {
		try {
			if (DEBUG) {
				init(logname);
				if (fileWriter != null) {
					fileWriter.write(msg + "\r\n");
					fileWriter.flush();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void logMessageFormat(String level, String tag, String msg) {
		String logmsg = getTimeStamp() + " " + Thread.currentThread().getId();

		logmsg += " " + level + "|" + tag + " " + msg;
		writeLine(logmsg);
	}

	private static String getTimeStamp() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd HH:mm:ss:SSS");

		return sDateFormat.format(new java.util.Date());
	}

	private static String getFilenameStamp() {
		SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyyMMddHHmmssSSS");

		return sDateFormat.format(new java.util.Date());
	}

	public static void verbose(String tag, String msg) {
		Log.v(tag, msg);
		logMessageFormat(Constants.LEVEL_VERBOSE, tag, msg);
	}

	public static void v(String tag, String msg) {
		Log.v(tag, msg);
		logMessageFormat(Constants.LEVEL_VERBOSE, tag, msg);
	}

	public static void debug(String tag, String msg) {
		Log.d(tag, msg);
		logMessageFormat(Constants.LEVEL_DEBUG, tag, msg);
	}

	public static void d(String tag, String msg) {
		Log.d(tag, msg);
		logMessageFormat(Constants.LEVEL_DEBUG, tag, msg);
	}

	public static void info(String tag, String msg) {
		Log.i(tag, msg);
		logMessageFormat(Constants.LEVEL_INFO, tag, msg);
	}

	public static void i(String tag, String msg) {
		Log.i(tag, msg);
		logMessageFormat(Constants.LEVEL_INFO, tag, msg);
	}

	public static void warn(String tag, String msg) {
		Log.w(tag, msg);
		logMessageFormat(Constants.LEVEL_WARN, tag, msg);
	}

	public static void w(String tag, String msg) {
		Log.w(tag, msg);
		logMessageFormat(Constants.LEVEL_WARN, tag, msg);
	}

	public static void w(String tag, String msg, Throwable e) {
		if(e != null){
			msg = msg + "\r\n" + getStackTraceMessage(e);
		}
		Log.w(tag, msg);
		logMessageFormat(Constants.LEVEL_WARN, tag, msg);
	}

	public static void error(String tag, String msg) {
		Log.e(tag, msg);
		logMessageFormat(Constants.LEVEL_ERROR, tag, msg);
	}

	public static void e(String tag, String msg) {
		Log.e(tag, msg);
		logMessageFormat(Constants.LEVEL_ERROR, tag, msg);
	}

	public static void e(String tag, Throwable e) {
		e(tag, "ERROR", e);
	}

	public static void e(String tag, String msg, Throwable e) {
		if(e != null){
			msg = msg + "\r\n" + getStackTraceMessage(e);
		}
		Log.e(tag, msg);
		logMessageFormat(Constants.LEVEL_ERROR, tag, msg);
	}

	public static void success(String tag, String msg) {
		Log.i(tag + "-SUCCESS", msg);
		logMessageFormat(Constants.LEVEL_SUCCESS, tag, msg);
	}

	public static void s(String tag, String msg) {
		Log.i(tag + "-SUCCESS", msg);
		logMessageFormat(Constants.LEVEL_SUCCESS, tag, msg);
	}

	private static String getStackTraceMessage(Throwable e) {
		return Log.getStackTraceString(e);
	}

	public static void uploadLogToServer(Context context, String deviceId,
			String filename, String msg) {
//		String logmsg = getTimeStamp();
//		logmsg += " U|USERHELP " + msg;
//		writeLine(logmsg);
//		try {
//			File file = new File(LOG_PATH + "xphonelog.zip");
//			File noservie = new File(LOG_PATH + noServiceName);
//			if(!noservie.exists()){
//				noservie.createNewFile();
//			}
//			
//			zipFile(new File(LOG_PATH + activityName), new File(LOG_PATH
//					+ serviceNameFile),noservie,file);
//			InputStream input = new FileInputStream(LOG_PATH + "xphonelog.zip");
//			IconHelper.uploadByStream(UserSetting.getWebSocketAddress(context),
//					filename, getFilenameStamp(), "xphonelog.zip", input);
//			if (input != null) {
//				try {
//					input.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			file.delete();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
	}

	public static void zipFile(File activity, File zipFile)
			throws IOException {
		FileOutputStream fos = null;
		ZipOutputStream zos = null;
		InputStream is = null;
		byte[] buf = new byte[1024];
		int length = 0;
		try {
			fos = new FileOutputStream(zipFile);
			zos = new ZipOutputStream(fos);
			is = new FileInputStream(activity);
			BufferedInputStream bis = new BufferedInputStream(is);
			zos.putNextEntry(new ZipEntry(activity.getName()));
			while ((length = bis.read(buf)) > 0) {
				zos.write(buf, 0, length);
			}
			bis.close();

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (zos != null) {
				try {
					zos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (fos != null) {
				try {
					fos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

	}
	
	private static void checkLogFile(){
		//每隔�?个小时check�?下log文件大小，如果文件大�?1M，则清除文件
		if(System.currentTimeMillis() - DELETE_LOG_TIME > 1 * 60 * 60 * 1000 || DELETE_LOG_TIME == 0l){
			File file = new File(LOG_PATH + logname);
			DELETE_LOG_TIME = System.currentTimeMillis();
			if(file.length() > FILE_SIZE){
				file.delete();
				fileWriter = null;
			}
		}
	}
	
	
	public static void deleteOldVersionFile(String curVersion){
		File folder = new File(LOG_PATH);
		
		if(folder.exists()){
			for(File tmpFile : folder.listFiles()){
				if(tmpFile.getName().contains(".log")){
					if(!tmpFile.getName().contains(curVersion)){
						tmpFile.delete();
					}
				}
			}
		}
	}
}
