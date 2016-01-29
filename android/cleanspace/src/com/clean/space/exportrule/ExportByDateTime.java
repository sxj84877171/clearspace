package com.clean.space.exportrule;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.protocol.FileItem;
import com.clean.space.util.FileUtils;
import com.google.gson.Gson;

public class ExportByDateTime {
	private static final String TAG = "ExportByDateTime";
	private HashMap<String, List<FileItem>> mGruopMap = new HashMap<String, List<FileItem>>();

	private long fileNumber = 0;
	public long getFileNumber() {
		return fileNumber;
	}

	public void setFileNumber(long fileNumber) {
		this.fileNumber = fileNumber;
	}

	// 把需要导出的文件列表保存在特定目录
	private String exportFileList(List<FileItem> lstFile,
			final long exportFileSize) {
		String strExport = "";
		if (null == lstFile || lstFile.isEmpty() || exportFileSize <= 0) {
			return strExport;
		}
		try {
			filterFile(lstFile, exportFileSize);
			
			// 为了减少文件大小,使用1路径-多文件的模式
			Gson gson = new Gson();
			strExport = gson.toJson(mGruopMap);
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return strExport;
	}

	// 导出文件在http服务器指定目录,供PC端下载
	public boolean exportToFile(List<FileItem> lstFile,
			final long exportFileSize) {
		String path = Constants.APP_ROOT_PATH + Constants.HTTP_EXPORT_FILE_NAME;
		List<FileItem> lstFileDes = lstFile;
		try {
			lstFileDes = sortByTime(lstFile);
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		try {
			String export = exportFileList(lstFileDes, exportFileSize);
			if (null == export || export.equals("")) {
				export = "{}";
				FLog.i(TAG, "exportToFile export is empty");
				//return false;
			}
			return FileUtils.writeFile(path, export);
		} catch (Exception e) {
			FLog.e(TAG, "exportToFile throw error", e);
		}
		return false;
	}

	// 获取满足条件的文件列表
	private long filterFile(List<FileItem> lstFile, final long exportFileSize) {
		try {
			long fileSize = 1;
			for (FileItem item : lstFile) {
				if (fileSize >= exportFileSize) {
					return fileNumber;
				}
				fileSize += item.getSize();
				++fileNumber;
				try {
					String parentName = item.getDir();
					item.setDir(null);
					// 根据父路径名将文件放入到mGruopMap中
					if (!mGruopMap.containsKey(parentName)) {
						List<FileItem> chileList = new ArrayList<FileItem>();
						chileList.add(item);
						mGruopMap.put(parentName, chileList);
					} else {
						mGruopMap.get(parentName).add(item);
					}
				} catch (Exception e) {
					FLog.e(TAG, e);
				}

			}
		} catch (Exception e) {
			FLog.e(TAG, e);
		}
		return fileNumber;
	}

	// 根据时间排序
	private List<FileItem> sortByTime(List<FileItem> lstFile) {
		if (null == lstFile || lstFile.isEmpty()) {
			return null;
		}
		Collections.sort(lstFile);
		return lstFile;
	}

}
