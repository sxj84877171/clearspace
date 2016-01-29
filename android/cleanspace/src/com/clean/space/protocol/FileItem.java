package com.clean.space.protocol;

import java.lang.ref.WeakReference;

import net.tsz.afinal.annotation.sqlite.Id;
import android.graphics.Bitmap;

import com.clean.space.log.FLog;
import com.google.gson.annotations.Expose;

@SuppressWarnings("rawtypes")
public class FileItem implements Comparable {
	
	public static final int PC_CLIEN = 1;
	public static final int ONEDRIVE = PC_CLIEN << 1;
	
	private String filename;
	@Id(column = "id")
	@Expose
	private int id;

	@Expose
	private String dir;

	@Expose
	private String date;

	@Expose
	private long size;

	@Expose
	private String path;

	@Expose
	private int state;

	private transient WeakReference<Bitmap> ref = null;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public String getDir() {
		return dir;
	}

	public void setDir(String dir) {
		this.dir = dir;
	}

	public long getDate() {
		if (null == date || date.equals("")) {
			return 0;
		}
		return Long.parseLong(date);
	}

	public void setDate(long date) {
		this.date = String.valueOf(date);
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public int compareTo(Object another) {
		long ret = 0;
		try {
			if (null == another) {
				return (int) ret;
			}
			FileItem right = (FileItem) another;
			ret = this.getSize() - right.getSize();
		} catch (Exception e) {
			FLog.e("FileItem", "equals", e);
		}
		return (int) ret;
	}

	// 基于目前业务,判断两个元素是否一致,根据文件名和文件大小(如果子类业务需要,可以重写该函数)
	@Override
	public boolean equals(Object another) {
		boolean ret = false;
		try {
			if (null == another) {
				return ret;
			}
			FileItem right = (FileItem) another;
			ret = (this.getSize() - right.getSize()) == 0
					&& (this.getPath().equalsIgnoreCase(right.getPath())) ? true
					: false;
		} catch (Exception e) {
			FLog.e("FileItem", "equals", e);
		}
		return ret;
	}

	public WeakReference<Bitmap> getRef() {
		return ref;
	}

	public void setRef(WeakReference<Bitmap> ref) {
		this.ref = ref;
	}

	@Override
	public FileItem clone() {
		FileItem fileItem = new FileItem();
		fileItem.date = date;
		fileItem.dir = dir;
		fileItem.filename = filename;
		fileItem.id = id;
		fileItem.path = path;
		fileItem.ref = ref;
		fileItem.size = size;
		fileItem.state = state;
		return fileItem;
	}
}
