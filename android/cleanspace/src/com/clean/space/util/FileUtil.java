package com.clean.space.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.clean.space.R;
import com.clean.space.log.FLog;

import android.R.integer;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;

public class FileUtil {
	private static long SIZE_KB = 1024;
	private static long SIZE_MB = 1048576;
	private static long SIZE_GB = 1073741824;
	private static long SIZE_TB = 1099511627776L;

	public static String getNakedFileName(final String fileName) {
		String path = fileName;
		if (path == null) {
			return null;
		}

		int pos = path.lastIndexOf('.');
		if (pos < 0) {
			return path;
		}

		return path.substring(0, pos);
	}

	public static String getFileTypeString(final String fileName) {
		String path = fileName;
		if (path == null) {
			return null;
		}

		/*
		 * if (path.contains(File.separator)) { path =
		 * path.substring(path.lastIndexOf(File.separator) + 1); if (path ==
		 * null) { return null; } }
		 */

		int pos = path.lastIndexOf('.');
		if (pos < 0) {
			return null;
		}

		return path.substring(pos + 1, path.length());
	}

	public static String getFileName(String filePath) {
		if (filePath == null) {
			return null;
		}

		int pos = filePath.lastIndexOf(File.separator);
		if (pos >= 0) {
			return filePath.substring(pos + 1);
		}

		return null;
	}

	public static String convertSearchPath(String filePath) {
		if (filePath == null) {
			return null;
		}

		if (filePath.contains("..." + File.separator)) {
			return filePath.substring(0, filePath.length() - 4);
		}

		if (filePath.contains("...")) {
			return filePath.substring(0, filePath.length() - 3);
		}

		return filePath;
	}

	public static String formatFileSize(long size) {
		if (size < SIZE_KB) {
			return String.format("%dB", size);
		}

		if (size < SIZE_MB) {
			double value = ((double) size) / SIZE_KB;
			return String.format("%.2fKB", value);
		}

		if (size < SIZE_GB) {
			double value = ((double) size) / SIZE_MB;
			return String.format("%.2fMB", value);
		}

		if (size < SIZE_TB) {
			double value = ((double) size) / SIZE_GB;
			return String.format("%.2fGB", value);
		}

		double value = ((double) size) / SIZE_TB;
		return String.format("%.2fTB", value);
	}

	public static String getPathFormat(String path) {
		if (path == null) {
			return "";
		}

		String newPath = path.replace(File.separatorChar, '>');
		return newPath;
	}

	public static boolean isVideo(Context context, String fileName) {
		String type = getFileTypeString(fileName);
		if (type == null) {
			return false;
		}

		String[] videos = context.getResources().getStringArray(
				R.array.video_files);
		for (String video : videos) {
			if (type.toLowerCase().equals(video)) {
				return true;
			}
		}

		return false;
	}

	public static Bitmap getImageThumbnail(Context context, String Imagepath) {
		ContentResolver cr = context.getContentResolver();
		String[] projection = { MediaStore.Images.Media.DATA,
				MediaStore.Images.Media._ID, };
		String whereClause = MediaStore.Images.Media.DATA + " = '" + Imagepath
				+ "'";
		Cursor cursor = cr.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
				whereClause, null, null);
		int _id = 0;
		Bitmap bitmap = null;
		if (cursor == null || cursor.getCount() == 0) {
			bitmap = getVideoThumbnail(context, Imagepath);
			if (null != bitmap)
				return bitmap;
			return null;
		}
		if (cursor.moveToFirst()) {
			int _idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
			do {
				_id = cursor.getInt(_idColumn);
			} while (cursor.moveToNext());
		}
		cursor.close();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		bitmap = MediaStore.Images.Thumbnails.getThumbnail(cr, _id,
				MediaStore.Images.Thumbnails.MINI_KIND, options);
		return bitmap;
	}

	public static Bitmap getVideoThumbnail(Context context, String videoPath) {
		ContentResolver cr = context.getContentResolver();
		String[] projection = { MediaStore.Video.Media.DATA,
				MediaStore.Video.Media._ID, };
		String whereClause = MediaStore.Video.Media.DATA + " = '" + videoPath
				+ "'";
		Cursor cursor = cr.query(
				MediaStore.Video.Media.EXTERNAL_CONTENT_URI, projection,
				whereClause, null, null);
		int _id = 0;
		if (cursor == null || cursor.getCount() == 0) {
			return null;
		}
		if (cursor.moveToFirst()) {
			int _idColumn = cursor.getColumnIndex(MediaStore.Video.Media._ID);
			do {
				_id = cursor.getInt(_idColumn);
			} while (cursor.moveToNext());
		}
		cursor.close();
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inDither = false;
		options.inPreferredConfig = Bitmap.Config.RGB_565;
		Bitmap bitmap = MediaStore.Video.Thumbnails.getThumbnail(cr, _id,
				MediaStore.Video.Thumbnails.MINI_KIND, options);
		return bitmap;
	}

	/**
	 * 保存一张图片到本地
	 * @param context
	 * @param _file
	 */
	public static void saveNeedSharedPhoto2File(Context context ,String _file) {

		File lastfile = new File(_file);
		if (lastfile.exists()) {
			// FileUtils.deleteFile(path);
			// FLog.d(TAG, "分享的图片已经存在,不需要再次下载.");
			return;
		}
		// FLog.d(TAG, "分享的图片被删除了,需要重新下载.");
		BufferedOutputStream os = null;
		try {
			Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
					R.drawable.share_friends);
			File file = new File(_file);
			// String _filePath_file.replace(File.separatorChar +
			// file.getName(), "");
			int end = _file.lastIndexOf(File.separator);
			String _filePath = _file.substring(0, end);
			File filePath = new File(_filePath);
			if (!filePath.exists()) {
				filePath.mkdirs();
			}
			file.createNewFile();
			os = new BufferedOutputStream(new FileOutputStream(file));
			bitmap.compress(Bitmap.CompressFormat.PNG, 50, os);

			if (!bitmap.isRecycled()) {
				FLog.d(FileUtil.class.getSimpleName(), "执行了: bitmap.recycle()");
				bitmap.recycle();
			}

		} catch (IOException e) {
			FLog.e(FileUtil.class.getSimpleName(), "saveBitmapToFile throw error1", e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
					FLog.e(FileUtil.class.getSimpleName(), "saveBitmapToFile throw error2", e);
				}
			}
		}
	}
}
