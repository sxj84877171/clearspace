package com.clean.space.photowall;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ViewItem {

	private String path;

	private boolean b;

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public boolean isB() {
		return b;
	}

	public void setB(boolean b) {
		this.b = b;
	}

	//////////////////////////////////////////////////////////////////
	public static ViewItem parse(String jsonStr) {
		ViewItem viewitem = null;
		try {
			Gson gson = new Gson();
			viewitem = gson.fromJson(jsonStr, ViewItem.class);
		} catch (JsonSyntaxException e) {
		}
		return viewitem;
	}

	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}

}
