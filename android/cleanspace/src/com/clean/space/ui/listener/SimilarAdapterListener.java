package com.clean.space.ui.listener;

public interface SimilarAdapterListener {
	public void onItemClick(String id, boolean selected);
	public void onItemLongClick(String path,int position,int type);
	public void onItemAllDeleted();
}
