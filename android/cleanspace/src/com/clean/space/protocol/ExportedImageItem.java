package com.clean.space.protocol;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Transient;

import org.opencv.core.Mat;

import com.clean.space.log.FLog;
//因为使用FinalDB数据库,所以每个数据库表必须对应一个数据结构类,而且FinalDb只能反射获取到子类的元素,而无法获取父类,所以这样会导致比较多的冗余数据,后期可以修改FinalDB机制
public class ExportedImageItem extends FileItem implements Comparable {

	private int type;

	@Id(column="id")
	private int id;
	
	// 该相片的直方图,无需存入db
	@Transient
	private Mat hist;
	
	private boolean inOtherImageSimilarList;
	
	public ExportedImageItem(){
		
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
	
	private int storage_location;

	

	public int getStorage_location() {
		return storage_location;
//		return 0;
	}
	public void setStorage_location(int storage_location) {
		this.storage_location = storage_location;
	}
	public void setSavePcClient() {
		storage_location = storage_location | PC_CLIEN;
	}

	public void setSaveOneDrive() {
		storage_location = storage_location | ONEDRIVE;
	}

	public boolean isSavePcClient() {
		return (storage_location & PC_CLIEN) > 0;
//		return true;
	}
	
	public boolean isSaveOneDrive(){
		return (storage_location & ONEDRIVE) > 0;
//		return true;
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