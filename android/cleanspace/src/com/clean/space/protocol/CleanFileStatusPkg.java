package com.clean.space.protocol;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class CleanFileStatusPkg extends Packet {

	/**
	 * 正在导出
	 */
	public static final int CLEAN_ING = 100;
	
	/**
	 * 停止导出
	 */
	public static final int CLEAN_STOP = 101;
	
	/**
	 * 导出完成
	 */
	public static final int CLEAN_FINISH = 102;
	
	/**
	 * 导出中断
	 */
	public static final int CLEAN_INTERRUPT = 102;
	
	private String handlePicNumber;
	private String handlePicTotal;
	private String clearedSpace;
	private String currentFileSize ;
	private String currentTransferSize ;
	public String getCurrentFileSize() {
		return currentFileSize;
	}

	public void setCurrentFileSize(String currentFileSize) {
		this.currentFileSize = currentFileSize;
	}

	public String getCurrentTransferSize() {
		return currentTransferSize;
	}

	public void setCurrentTransferSize(String currentTransferSize) {
		this.currentTransferSize = currentTransferSize;
	}

	private String pcip;
	private int cleanStatus;
	private long start;
	private long end;

	public int getCleanStatus() {
		return cleanStatus;
	}

	public void setCleanStatus(int cleanStatus) {
		this.cleanStatus = cleanStatus;
	}

	public String getHandlePicNumber() {
		if(handlePicNumber == null || "".equals(handlePicNumber)){
			handlePicNumber = "0" ;
		}
		return handlePicNumber;
	}

	public String getPcip() {
		return pcip;
	}

	public void setPcip(String pcip) {
		this.pcip = pcip;
	}

	public void setHandlePicNumber(String handlePicNumber) {
		this.handlePicNumber = handlePicNumber;
	}

	public String getHandlePicTotal() {
		if(handlePicTotal == null || "".equals(handlePicTotal)){
			handlePicTotal = "0" ;
		}
		return handlePicTotal;
	}

	public void setHandlePicTotal(String handlePicTotal) {
		this.handlePicTotal = handlePicTotal;
	}

	public String getCleanedSpace() {
		if(clearedSpace == null || "".equals(clearedSpace)){
			clearedSpace = "0" ;
		}
		return clearedSpace;
	}

	public void setClearedSpace(String clearedSpace) {
		this.clearedSpace = clearedSpace;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		if (end <= start) {
			end = start + 1;
		}
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

	// //////////////////////////////////////////////////////////////////////////////////////
	public static CleanFileStatusPkg parse(String jsonStr) {
		CleanFileStatusPkg packet = null;
		try {
			Gson gson = new Gson();
			packet = gson.fromJson(jsonStr, CleanFileStatusPkg.class);
		} catch (JsonSyntaxException e) {
		}
		if (null == packet) {
			packet = new CleanFileStatusPkg();
		}
		return packet;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
