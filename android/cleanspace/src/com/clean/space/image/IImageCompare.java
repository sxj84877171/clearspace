package com.clean.space.image;

import com.clean.space.protocol.SimilarImageItem;

public interface IImageCompare {
	public boolean compareImage(SimilarImageItem item, SimilarImageItem itemRight) ;

	public void calcHist(SimilarImageItem imageItem);
}
