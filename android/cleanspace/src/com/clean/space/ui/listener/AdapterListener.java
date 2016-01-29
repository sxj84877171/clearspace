package com.clean.space.ui.listener;

public interface AdapterListener {
	/*
	 * position, 选中对象的index
	 * selected, true表示选中，false表示取消选中
	 **/
	public void onItemClick(int position, boolean selected);
	
	public void onItemLongClick(String path,int position,int type);
}
