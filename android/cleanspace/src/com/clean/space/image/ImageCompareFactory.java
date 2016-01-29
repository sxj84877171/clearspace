package com.clean.space.image;

import android.content.Context;

public class ImageCompareFactory {
	public final static int IMAGE_COMPARE_TYPE_HIST = 101;
	
	public static IImageCompare getCompareEngine(Context context, int histtype) {  
		
		switch(histtype){
		case IMAGE_COMPARE_TYPE_HIST:
            return new ImageHistCompare(context); 
		default:
			break;
		
		}
        return null;
	}
}
