package com.clean.space.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.imgproc.Imgproc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.protocol.SimilarImageItem;

public class ImageHistCompare implements IImageCompare {

	private final String TAG = "SimilarImageEngine";
	private static ImageHistCompare mInstance = null;
	private static Context mContext = null;

	public static ImageHistCompare getCompareImageEngine(Context context) {
		if (mInstance == null) {
			mInstance = new ImageHistCompare(context);
		}
		return mInstance;
	}
	public ImageHistCompare(Context context){
		mContext  = context;
	}

	// 比较图片,根据特定的相似度判断是否一致
	public boolean compareImage(SimilarImageItem item, SimilarImageItem itemRight) {
		boolean bSimilar = false;
		try {
			if (null == item || item.getHist() == null) {
				return bSimilar;
			}
			if (null == itemRight || itemRight.getHist() == null) {
				return bSimilar;
			}
			double compare = Imgproc.compareHist(item.getHist(),
					itemRight.getHist(), Imgproc.CV_COMP_CORREL);
			bSimilar = compare > Constants.SIMILARITY;
		} catch (Exception e) {
			FLog.e(TAG, "compareImage throw error", e);
		}
		return bSimilar;
	}
	// 计算直方图
	public void calcHist(SimilarImageItem item) {
		try {
			Bitmap bmpimg1 = getBitmap(item.getPath());
			if (null == bmpimg1) {
				return;
			}
			//bmpimg1 = Bitmap.createScaledBitmap(bmpimg1, 100, 100, true);
			Mat img1 = new Mat();
			Utils.bitmapToMat(bmpimg1, img1);
			img1.convertTo(img1, CvType.CV_32F);
			Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGBA2GRAY);
			Mat hist = new Mat();

			MatOfInt histSize = new MatOfInt(180);
			MatOfInt channels = new MatOfInt(0);
			ArrayList<Mat> bgr_planes1 = new ArrayList<Mat>();
			Core.split(img1, bgr_planes1);
			MatOfFloat histRanges = new MatOfFloat(0f, 180f);
			boolean accumulate = false;
			Imgproc.calcHist(bgr_planes1, channels, new Mat(), hist, histSize,
					histRanges, accumulate);
		//	Core.normalize(hist, hist, 0, hist.rows(), Core.NORM_MINMAX, -1,
		//			new Mat());
			//img1.convertTo(img1, CvType.CV_32F);
			hist.convertTo(hist, CvType.CV_32F);

			bmpimg1.recycle();
			item.setHist(hist);
		} catch (Exception e) {
			FLog.e(TAG, "calcHist throw error", e);
		}
	}
	


	// 获取bitmap
	private Bitmap getBitmap(String path) {
		try {
		//	Uri uri = Uri.fromFile(new File(path));
//			BitmapFactory.Options options = new BitmapFactory.Options();
//			options.outWidth = 200;
//			options.outHeight = 200;
//			options.inSampleSize = 2;
			Bitmap bitmap = decodeBitmapFromUri(Uri.fromFile(new File(path)), 500, 500);//BitmapFactory.decodeFile(path, options);
			//Bitmap bitmap = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), uri);

//			InputStream imageStream = mContext.getContentResolver()
//					.openInputStream(uri);
//			Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
			return bitmap;
		}  catch(Exception e){
			FLog.e(TAG, "getBitmap throw error", e);
		}
		return null;
	}
	
	public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;

	    if (height > reqHeight || width > reqWidth) {
	        if (width > height) {
	            inSampleSize = Math.round((float)height / (float)reqHeight);
	        } else {
	            inSampleSize = Math.round((float)width / (float)reqWidth);
	        }
	    }
	    return inSampleSize;
	}

	public Bitmap decodeBitmapFromUri(Uri uri,
	        int reqWidth, int reqHeight) {
	    // First decode with inJustDecodeBounds=true to check dimensions
	    try {
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri), null, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeStream(mContext.getContentResolver().openInputStream(uri), null, options);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return null;
	}
}
