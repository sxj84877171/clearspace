package com.clean.space.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.GridView;

public class NoScrollGridView extends GridView {
    private boolean haveScrollbar = false;  
  
    public NoScrollGridView(Context context) {  
        super(context);  
    }  
  
    public NoScrollGridView(Context context, AttributeSet attrs) {  
        super(context, attrs);  
    }  
  
    public NoScrollGridView(Context context, AttributeSet attrs, int defStyle) {  
        super(context, attrs, defStyle);  
    }  
  
    /** 
     * �����Ƿ���ScrollBar����Ҫ��ScollView����ʾʱ��Ӧ������Ϊfalse�� Ĭ��Ϊ true 
     *  
     * @param haveScrollbars 
     */  
    public void setHaveScrollbar(boolean haveScrollbar) {  
        this.haveScrollbar = haveScrollbar;  
    }  
  
    @Override  
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {  
        if (haveScrollbar == false) {  
            int expandSpec = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE >> 2, MeasureSpec.AT_MOST);  
            super.onMeasure(widthMeasureSpec, expandSpec);  
        } else {  
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
        }  
    }  
}  
