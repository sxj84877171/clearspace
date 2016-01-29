package com.clean.space.db;

import java.util.List;

import net.tsz.afinal.FinalDb;
import net.tsz.afinal.FinalDb.DbUpdateListener;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Environment;

import com.clean.space.Constants;
import com.clean.space.log.FLog;
import com.clean.space.protocol.ExportedImageItem;
import com.clean.space.protocol.FileItem;

public class DBMgr {
	private final String TAG = "DBMgr";
	private Context mContext = null;
	private FinalDb mDBMgr = null;
	private static DBMgr instance;

	public DBMgr(Context context) {
		mContext = context;

		// 数据库路径放在apk的内部路径,在系统卸载的时候,可以自动删除该数据库
		String dbPath = mContext
				.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
				+ Constants.APP_DB_NAME;
//		mDBMgr = FinalDb.create(mContext, dbPath, false);
		mDBMgr = FinalDb.create(mContext, mContext
				.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getPath(), Constants.APP_DB_NAME, true, 2, new DbUpdateListener(){

					@Override
					public void onUpgrade(SQLiteDatabase arg0, int arg1,
							int arg2) {
						if(arg2 == 2 && arg2 > arg1){
							FLog.e(TAG, "arg1:" + arg1 + ",arg2:" + arg2);
//							arg0.execSQL("alter com_clean_space_protocol_SimilarImageItem add column xxx type int default 0 ;");
//							arg0.execSQL("select * from com_clean_space_protocol_SimilarImageItem" );
							
							
						}
						
						
					}
			
		});
	}

	public static DBMgr getInstance(Context context) {
		if (instance == null) {
			instance = new DBMgr(context);
		}
		return instance;
	}

	public void addTable(Object t) {
		try {
			mDBMgr.save(t);
		} catch (Exception e) {
			FLog.e(TAG, "addTable throw error", e);
		}
	}

	public void update(Object t) {
		try {
			mDBMgr.update(t);
		} catch (Exception e) {
			FLog.e(TAG, "update throw error", e);
		}
	}

	public int getMaxID(Class<?> clazz) {
		return 0;
	}

	public Object getMaxObject(String key, Class<?> clazz) {
		List<Class<?>> list = null;
		try {
			String strWhere = key + " > 0 order by " + key + " desc limit 1 ";
			list = (List<Class<?>>) mDBMgr.findAllByWhere(clazz, strWhere);
			if (null != list && !list.isEmpty()) {
				return list.get(0);
			}
		} catch (Exception e) {
			FLog.e(TAG, "getLaterData throw error", e);
		}
		return null;
	}

	public Object getMinObject(String key, Class<?> clazz) {
		List<Class<?>> list = null;
		try {
			String strWhere = key + " > 0 order by " + key + " asc limit 1 ";
			list = (List<Class<?>>) mDBMgr.findAllByWhere(clazz, strWhere);
			if (null != list && !list.isEmpty()) {
				return list.get(0);
			}
		} catch (Exception e) {
			FLog.e(TAG, "getLaterData throw error", e);
		}
		return null;
	}

	public void addTables(List<?> data) {
		try {
			for (Object obj : data) {
				mDBMgr.save(obj);
			}
		} catch (Exception e) {
			FLog.e(TAG, "addTable throw error", e);
		}
	}

	public boolean deleteRecords(List<?> data) {
		try {
			mDBMgr.delete(data);
		} catch (Exception e) {
			FLog.e(TAG, "deleteRecords throw error", e);
		}
		return false;
	}

	// 同一个数据表删除记录
	public boolean deleteRecord(Object data) {
		try {
			mDBMgr.delete(data);
		} catch (Exception e) {
			FLog.e(TAG, "deleteRecord throw error", e);
		}
		return true;
	}

	// 数据表之间删除记录,根据文件大小和文件路径确定唯一记录
	public boolean deleteRecord(Class<?> clazz, FileItem t) {

		try {
			List<FileItem> list = (List<FileItem>) queryRecord(clazz, "size",
					t.getSize());
			if (null != list) {
				for (FileItem item : list) {
					// 都为空
					if (null == item.getPath() && null == t.getPath()) {
						continue;
					}
					// 有一个不为空
					if (null == item.getPath() || null == t.getPath()) {
						continue;
					}
					if (item.getPath().equalsIgnoreCase(t.getPath())) {
						mDBMgr.deleteById(clazz, item.getId());
					}
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "deleteRecord(Class<?> clazz, FileItem t) throw error",
					e);
		}
		return true;
	}

	public boolean deleteAll(Class<?> clazz) {
		try {
			mDBMgr.deleteAll(clazz);
		} catch (Exception e) {
			FLog.e(TAG, "deleteRecord throw error", e);
		}
		return false;
	}

	public List<?> getLaterData(Class<?> clazz, int maxid, int op) {
		List<?> list = null;
		try {
			String strWhere = "id > " + maxid + " and op=" + op;
			list = mDBMgr.findAllByWhere(clazz, strWhere);
		} catch (Exception e) {
			FLog.e(TAG, "getLaterData throw error", e);
		}
		return list;
	}

	// 需要处理特殊字符串
	public List<?> queryRecord(Class<?> clazz, String key, Object value) {
		List<?> list = null;
		try {
			String strWhere = key + " = " + value;
			list = mDBMgr.findAllByWhere(clazz, strWhere);
		} catch (Exception e) {
			FLog.e(TAG, "queryRecord throw error", e);
		}
		return list;
	}

	public List<?> getLaterData(Class<?> clazz, int maxid) {
		List<?> list = null;
		try {
			String strWhere = "id > " + maxid;
			list = mDBMgr.findAllByWhere(clazz, strWhere);
		} catch (Exception e) {
			FLog.e(TAG, "getLaterData throw error", e);
		}
		return list;
	}

	public List<?> getData(Class<?> clazz, String sortType, String orderType,
			int photoCount) {
		List<?> list = null;
		try {
			String strWhere = "id>=0 order by " + sortType + " " + orderType;
			if (photoCount > 0) {
				strWhere += " limit " + photoCount;
			}
			list = mDBMgr.findAllByWhere(clazz, strWhere);
		} catch (Exception e) {
			FLog.e(TAG, "getData throw error", e);
		}
		return list;
	}

	public List<?> getGroup(Class<?> clazz, int maxid, String groupby) {
		List<?> list = null;
		try {
			String strWhere = "id > " + maxid + " group by " + groupby;
			list = mDBMgr.findAllByWhere(clazz, strWhere);
		} catch (Exception e) {
			FLog.e(TAG, "getLaterData throw error", e);
		}
		return list;
	}

	// 根据特别的元数据自定义,需要处理特殊字符串
	public boolean queryRecord(Class<?> clazz, FileItem t) {
		boolean exist = false;
		if (null == t) {
			return exist;
		}
		try {
			List<FileItem> list = (List<FileItem>) queryRecord(clazz, "size",
					t.getSize());
			if (null != list) {
				for (FileItem item : list) {
					// 都为空
					if (null == item.getPath() && null == t.getPath()) {
						exist = true;
						break;
					}
					// 有一个不为空
					if (null == item.getPath() || null == t.getPath()) {
						continue;
					}
					if (item.getPath().equalsIgnoreCase(t.getPath())) {
						exist = true;
					}
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "queryRecord throw error", e);
		}
		return exist;
	}

	// 根据特别的元数据自定义,需要处理特殊字符串
	public ExportedImageItem queryRecordBean(Class<?> clazz, ExportedImageItem t) {
		ExportedImageItem ret = null;
		if (null == t) {
			return null;
		}
		try {
			List<ExportedImageItem> list = (List<ExportedImageItem>) queryRecord(
					clazz, "size", t.getSize());
			if (null != list) {
				for (ExportedImageItem item : list) {
					// 都为空
					if (null == item.getPath() && null == t.getPath()) {
						ret = item;
						break;
					}
					// 有一个不为空
					if (null == item.getPath() || null == t.getPath()) {
						continue;
					}
					if (item.getPath().equalsIgnoreCase(t.getPath())) {
						ret = item;
					}
				}
			}
		} catch (Exception e) {
			FLog.e(TAG, "queryRecord throw error", e);
		}
		return ret;
	}

	// 加入数据库,记录唯一性
	public void addTableUnique(Class<?> clazz, FileItem t) {
		try {
			boolean exist = queryRecord(clazz, t);
			if (!exist) {
				// 　判断元素是否存在
				mDBMgr.save(t);
			}

		} catch (Exception e) {
			FLog.e(TAG, "addTable throw error", e);
		}
	}

	// 加入数据库,记录唯一性
	public void addTableUniqueSaveOrUpdate(Class<?> clazz, ExportedImageItem t) {
		try {
			ExportedImageItem result = queryRecordBean(clazz, t);
			if (result != null) {
				// 　判断元素是否存在
				result.setStorage_location(result.getStorage_location() | t.getStorage_location());
				mDBMgr.update(result);
			} else {
				mDBMgr.save(t);
			}

		} catch (Exception e) {
			FLog.e(TAG, "addTable throw error", e);
		}
	}

}