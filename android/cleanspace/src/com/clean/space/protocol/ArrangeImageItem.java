package com.clean.space.protocol;

public class ArrangeImageItem extends ImageItem {

	private boolean checked = false;
	
	private int type ;

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public ArrangeImageItem(ImageItem item){
		if(null != item){
			this.copy(item);
		}
	}
	public ArrangeImageItem(){
	}
	
}
