package com.clean.space;

/**
 * Created by dongjl1 on 10/26/2015.
 */
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.clean.space.protocol.FileItem;
import com.clean.space.util.FileUtil;
import com.clean.space.util.FileUtils;
import com.clean.space.util.ScreenUtil;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

public class CleanMainFragImageAdapter extends BaseAdapter {
	private static final String TAG = CleanMainFragImageAdapter.class.getSimpleName();
    private Context mContext;
    public List<FileItem> mList = null;

    // Constructor
    public CleanMainFragImageAdapter(Context c ) {
        mContext = c;
        mList = new ArrayList<FileItem>();
    }
    
    public void addImages(List<FileItem> list) {
    	mList.addAll(list);
    	/*for (FileItem item : list) {
    		if (FileUtils.isFileExist(item.getPath())) {
    			mList.add(item);
    		} else {
    			Log.e(TAG, "file not exist:" + item.getPath());
    		}
    	}*/
    	notifyDataSetChanged();
    }

    public int getCount() {
        return mList.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        DisplayMetrics metrics = new DisplayMetrics();
        ((Activity)  mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        if (convertView == null) {
            imageView = new ImageView(mContext);
            int width = (metrics.widthPixels - ScreenUtil.dp2px(mContext, 10)) / 4 - ScreenUtil.dp2px(mContext, 7);
            int height = width;
            GridView.LayoutParams params = new GridView.LayoutParams(width, height);
            imageView.setLayoutParams(params);
            //imageView.setLayoutParams(new GridView.LayoutParams((int) (80 * metrics.density), (int) (80 * metrics.density)));
            //imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            //imageView.setBackgroundColor(Color.parseColor("#aaaaaa"));
            //imageView.setPadding((int)(0.5* metrics.density), (int)(0.5* metrics.density),(int)(0.5* metrics.density),(int)(0.5* metrics.density));
        }
        else
        {
            imageView = (ImageView) convertView;
        }
        
        FileItem fi = mList.get(position);
        setImageView(imageView, fi.getPath());
        //Bitmap thumBitmap = getImageThumbnail(this.mContext,this.mContext.getContentResolver(), fi.getPath());
        //imageView.setImageBitmap(thumBitmap);
        return imageView;
    }
    
    private void setImageView(ImageView image, String path) {
    	try {
			String fullPath = "file://" + path;
			if (FileUtils.isFileExist(path)) {
				image.setScaleType(ScaleType.CENTER_CROP);
				try {
					ImageLoaderManager.getImageLoader(mContext)
							.displayImage(
									fullPath,
									image,
									ImageLoaderManager
											.getImageOptions(), mImageLoadingListener);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				image.setVisibility(View.GONE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    private ImageLoadingListener mImageLoadingListener = new ImageLoadingListener() {
		
		@Override
		public void onLoadingStarted(String imageUri, View view) {
		}
		
		@Override
		public void onLoadingFailed(String imageUri, View view,
				FailReason failReason) {
			String fileName = FileUtil.getFileName(imageUri);
			if (FileUtil.isVideo(mContext, fileName)) {
				//((ImageView) view).setImageResource(R.drawable.video_loading);
				String loacation = imageUri.substring(7); // start with file://
				Bitmap thumBitmap = FileUtil.getImageThumbnail(mContext, loacation);
				if (thumBitmap != null) {
					((ImageView) view).setImageBitmap(thumBitmap);
				} else {
					((ImageView) view).setImageResource(R.drawable.video_loading);
				}
			}
		}
		
		@Override
		public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
		}
		
		@Override
		public void onLoadingCancelled(String imageUri, View view) {
		}
	};

}