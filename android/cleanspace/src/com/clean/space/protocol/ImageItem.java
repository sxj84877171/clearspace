package com.clean.space.protocol;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Transient;

import org.opencv.core.Mat;

import com.clean.space.log.FLog;

@SuppressWarnings("rawtypes")
public class ImageItem   extends FileItem implements Comparable {

	private int type;

	@Id(column="id")
	private int id;
	
	// 该相片的直方图,无需存入db
	@Transient
	private Mat hist;
	
	private boolean inOtherImageSimilarList;
	
	public ImageItem(){
		
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	
	public boolean isInOtherImageSimilarList() {
		return inOtherImageSimilarList;
	}
	public void setInOtherImageSimilarList(boolean inOtherImageSimilarList) {
		this.inOtherImageSimilarList = inOtherImageSimilarList;
	}
	public Mat getHist() {
		return hist;
	}
	public void setHist(Mat hist) {
		this.hist = hist;
	}
	private String filename;
	private String dir;
	private String date;
	private long size;
	private String path;
	private int state;

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

	
	public void copy(FileItem item){
		this.setDate(item.getDate());
		this.setPath(item.getPath());
		this.setFilename(item.getFilename());
		this.setSize(item.getSize());
	}
	public void copy(ImageItem item){
		this.setDate(item.getDate());
		this.setPath(item.getPath());
		this.setFilename(item.getFilename());
		this.setSize(item.getSize());
	}
}