package com.clean.space;

import android.annotation.SuppressLint;

@SuppressLint("SdCardPath")
public class Constants {
	public static final boolean IS_TEST_BUILD = false;

	public static final int UDP_SERVER_BROADCAST_PORT = 7082;
	public static final int UDP_CMD_PORT = 7083;
	public static final int HTTP_SERVER_PORT = 7084;

	// 发送开始下载UDP的间隔时间
	public static final long INTERVAL_SEND_START_DOWNLOAD_UDP = 500;

	// 进入到主界面, 到点"导出并清理"或"导出"按钮的时间. 即停留在第一个界面的时间
	public static final String UM_EVENT_HOME_PAGE_TIME = "home_page_time";

	// 搜索到第一个设备花的时间.
	public static final String UM_EVENT_SEARCH_FRIST_PC = "search_pc_time";

	// 在选择电脑界面停留的时间
	public static final String UM_EVENT_CHOOSE_PC_TIME = "choose_pc_time";

	// 设置
	public static final String SETTING_KEY_EXIT = "exit";
	public static final String SETTING_VALUE_EXIT = "stop";
	public static final String SETTING_VALUE_RUNNING = "running";
	public static final String LENOVOID = "lenovoID";
	public static final boolean SOFTAP_SSID_IS_FIXED = false;//每一台设备softap的ssid是固定的
	// 命令
	public static final String CMD_SEND_PC_INFO = "kSendPCInfo";
	public static final String CMD_DOWNLOAD_COMPLETED = "kDownloadCompleted";
	public static final String CMD_START_DOWNLOAD = "kStartDownload";
	public static final String CMD_STOP_DOWNLOAD = "kStopDownload";
	public static final String MAIN_SERVICE_NAME = "MainService";
	public static final String SERVICE_RESTART_SERVICE_ACTION = "arui.restartservice.action";
	public static final String APP_ROOT_PATH = "/sdcard/clearspace/";
	public static final String APP_DB_NAME = "cleanspace.db";
	
	public static final String PAGE_EXPORTPHOTOFRAGMENT = "ExportPhotoFragment";
	public static final String PAGE_CLEANPHOTOFRAGMENT = "CleanPhotoFragment";

	public static final String FOLDER_SHARE_PATH = "/" + "";
	public static final String HTTP_EXPORT_FILE_NAME = "filelist.json";

	// http command
	public static final String DOWNLOAD_PATTERN = "/download*";
	public static final String DOWNLOAD_COMPLETE_PATTERN = "/downloadcomplete*";
	public static final String DOWNLOAD_STOP_BY_PC = "/downloadstopbypc";
	public static final long EXPIRED_RECV_TIME = 3 * 60 * 1000;

	// activity 和service之间的交互
	public static final String DOWNLOAD_PROGRESS_INTENT_BROADCAST = "downloadcompletebroadcast";
	public static final String PC_START_DOWNLOAD = "pcstartdownload";
	public static final String PROTOCOL_VERSION = "1.0";
	
	public static final String CLOUD_ONE_DRIVER = "oneDriver";
	public static final String CLOUD_KEY = "cloud" ;
	public static final String CLOUD_CMD = "cloud_cmd" ;
	public static final String CLOUD_CMD_START = "cloud_cmd_start" ;
	public static final String CLOUD_CMD_STOP = "cloud_cmd_stop" ;

	//
	public static final int CHECK_PC_LIVE_TIME = 5 * 1000;

	/**
	 * finish界面,每隔200ms发消息,判断修改button的状态
	 */
	public static final int CHECK_BUTTON_STATE = 200;

	// 系统设置
	public static final String CLEAR_FILE_STATUS = "clearfilestatus";
	public static final String WANT_CLEAN_SIZE = "wantcleansize";
	public static final String CHECK_EXPORT_AND_CLEAN = "exportandclean";

	public static final String LEVEL_VERBOSE = "VERBOSE";
	public static final String LEVEL_DEBUG = "DEBUG";
	public static final String LEVEL_INFO = "INFO";
	public static final String LEVEL_WARN = "WARN";
	public static final String LEVEL_ERROR = "ERROR";
	public static final String LEVEL_SUCCESS = "SUCCESS";

	public static final int LOG_LEVEL_VERBOSE = 0;
	public static final int LOG_LEVEL_DEBUG = 1;
	public static final int LOG_LEVEL_INFO = 2;
	public static final int LOG_LEVEL_WARN = 3;
	public static final int LOG_LEVEL_ERROR = 4;
	public static final int LOG_LEVEL_SUCCESS = 5;

	public static final int LOG_FILE_ACTIVITY = 0;
	public static final int LOG_FILE_SERVICE = 1;
	public static final int LOG_FILE_NO_SERVICE = 2;

	public static final String CONFIGFILE = "cachevalue";// sp的文件名

	public static final String SELECTEDPCNAME = "selectedpcname";
	public static final String SELECTEDPCIP = "selectedpcip";
	public static final String SELECTTPYE = "selecttype" ;
	public static String FILESIZE = "filesize";
	public static String FILENUMBER = "filenumber";

	public static String SOFTAP_HEADER = "zpdny_";
	public static String SOFTAP_IP = "192.168.173.1";
	public static String SOFTAP_PASSWORD = "Aa123456";
	public static String SOFTAP_OLD_NETWORK = "networkid";
	public static String SOFTAP_GETPC_SSID_URL = "http://114.215.236.240:8080/relayserver/searchsoftap?";

	public static final long one_month = 1000 * 60 * 60 * 24 * 30;
	public static final long three_month = 1000 * 60 * 60 * 24 * 30 * 3;
	public static final long six_month = 1000 * 60 * 60 * 24 * 182;
	public static final long one_year = 1000 * 60 * 60 * 24 * 365;
	// 处理图片
	public static final long LEAST_NEAR_TIME = 35 * 60 * 1000;
	public static double SIMILARITY = 0.985;

	public static final int SORT_TYPE_DATE_ASC = 1;
	public static final int SORT_TYPE_DATE_DESC = 2;
	public static final int SORT_TYPE_SIZE_ASC = 3;
	public static final int SORT_TYPE_SIZE_DESC = 4;

	/**
	 * 删除了所有照片,finish界面用到
	 */
	public static final String DELETEALL = "deleteall";

	/**
	 * finish界面点击"查看后删除"按钮,进入选择删除界面,判断是否删除了照片,false进去后没删除,true删除了照片
	 */
	public static final String DELETETAG = "deletetag";

	/**
	 * 删除后剩余导出照片的个数,finish界面用到
	 */
	public static final String REMAIN = "remain";

	/**
	 * export界面正在导出照片的路径
	 */
	public static final String PATH = "path";
	
	/**
	 * scan界面点击帮我清理的标记
	 */
	public static final String COMEFROMSCAN = "comefromscan";

	/**
	 * 延时两秒进入主界面
	 */
	public static final int SPLISH2MAIN_USETIME = 2000;
	
	/**
	 * 发送显示对话框广播的action
	 */
	public static final String ACTION_SHOW_DIALOG = "show_dialog";
	
	/**
	 * 更新对话框,更新内容
	 */
	public static final String UPDATE_INFO = "update_info";
	
	/**
	 * 正在下载最新版本
	 */
	public static final String DOWNLOAD_START = "down";
	
	/**
	 * 后台导出状态,1前台,2后台
	 */
	public static final String EXPORT_STATIC = "export_state";
	
	/**
	 * 分享图片的路径
	 */
	public static final String SHAREPATH = "/sdcard/cleanspace/new_share.jpg";
	
	public static class UMENG {

		// 界面信息收集
		public static class GUI_INFO_GATHER {

			/**
			 * 选择导出1个月前的次数
			 */
			public static final String UM_EVENT_ID_SELECT_BEFORE_ONE_MON_EXPORT = "gui_select_export_before_onemonth";

			/**
			 * 选择导出3个月前的次数
			 */
			public static final String UM_EVENT_ID_SELECT_BEFORE_THREE_MON_EXPORT = "gui_select_export_before_threemonth";

			/**
			 * 选择导出6个月前的次数
			 */
			public static final String UM_EVENT_ID_SELECT_BEFORE_SIX_MON_EXPORT = "gui_select_export_before_sixmonth";

			/**
			 * 选择导出12个月前的次数
			 */
			public static final String UM_EVENT_ID_SELECT_BEFORE_ONE_YEAR_EXPORT = "gui_select_export_before_twfmonth";

			/**
			 * 选择导出All的次数
			 */
			public static final String UM_EVENT_ID_SELECT_BEFORE_ALL_EXPORT = "gui_select_export_before_all";

			/**
			 * 用户点击上次连接的电脑次数.
			 */
			public static final String UM_EVENT_ID_LASTCONNPC = "scan_last_conn_pc";

			/**
			 * 用户点击附近的电脑次数.
			 */
			public static final String UM_EVENT_ID_NEARPC = "scan_near_pc";

			/**
			 * 用户点击取消的次数.
			 */
			public static final String UM_EVENT_ID_CANCLE = "scan_cancle";

			/**
			 * 选择电脑,点击取消保存的次数
			 */
			public static final String UM_EVENT_ID_CANCLE_SELECTED = "scan_cancle_save";

			/**
			 * scan界面点击"开始"的按钮
			 */
			public static final String UM_EVENT_ID_START = "scan_start_button";

			/**
			 * scan界面点击"重试"的按钮
			 */
			public static final String UM_EVENT_ID_RETRY = "scan_retry_button";

			/**
			 * 用户点击"返回"到主页面按钮
			 */
			public static final String UM_EVENT_ID_BACK = "scan_back_button";

			/**
			 * 选择电脑,连接失败后,点击取消的次数.
			 */
			public static final String UM_EVENT_ID_CONN_FAILE_CANCLE = "scan_cancle_conn_fail";

			/**
			 * 点击推荐好友的次数.
			 */
			public static final String UM_EVENT_ID_PULL2FRIENADS = "finish_share_button";

			/**
			 * 导出文件后,点击删除所有已导出文件的次数.
			 */
			public static final String UM_EVENT_ID_CLEANALLPIC = "finish_clean_all";

			/**
			 * 导出文件后,点击查看后删除按钮
			 */
			public static final String UM_EVENT_ID_CLEAN_AFTER_LOOK = "finish_clean_after_look";

			/**
			 * 弹出新版本提示框后,点击确定的次数.
			 */
			public static final String UM_EVENT_ID_UPDATA_P = "updata_p";

			/**
			 * 弹出新版本提示框后,点击取消的次数.
			 */
			public static final String UM_EVENT_ID_UPDATA_N = "updata_n";

			/**
			 * 用户点击"导出"次数.
			 */
			public final static String UM_EVENT_ID_EXPORT = "gui_export_btn";

			/**
			 * 用户点击"照片清理"次数.
			 */
			public static final String UM_EVENT_ID_MANAGE_PHOTO = "gui_main_manage_photo";

			/**
			 * 用户点击退出程序按钮
			 */
			public static final String UM_EVENT_SETTING_EXIT_SERVICE = "dialog_exit_service";

			/**
			 * 用户点击关闭界面按钮
			 */
			public static final String UM_EVENT_SETTING_EXIT_GUI = "dialog_exit_gui";

			/**
			 * 左侧菜单按钮点击的次数
			 */
			public static final String UM_EVENT_ID_MENU = "menu_button";

			/**
			 * 用户点击左侧菜单"分享"按钮次数
			 */
			public static final String UM_EVENT_ID_SHARE = "menu_share_button";

			/**
			 * 用户点击"反馈"的次数
			 */
			public static final String UM_EVENT_ID_FEEDBACK = "menu_feedback_button";

			/**
			 * 用户点击"退出"程序按钮
			 */
			public static final String UM_EVENT_ID_EXIT = "menu_exit_button";

			/**
			 * 用户点击"关于"程序按钮
			 */
			public static final String UM_EVENT_ID_MENU_ABOUT = "menu_about";

			/**
			 * 用户点击"开发者模式"程序按钮
			 */
			public static final String UM_EVENT_ID_MENU_DEVELOP = "menu_develop_button";

			/**
			 * 用户点击反馈"提交"按钮
			 */
			public static final String UM_EVENT_ID_FEEDBACK_SUBMIT = "menu_feedback_submit_button";

			/**
			 * finish界面,删除文件过程中,点击"取消"删除文件的次数.
			 */
			public static final String UM_EVENT_ID_DELETING_CANCLE = "finish_dialog_deleting_cancle_button";

			/**
			 * 点击照片清理->已导出未清理的照片次数
			 */
			public static final String UM_EVENT_ID_EXPORTED_UNCLEAN = "clean_exported_unclean";

			/**
			 * 点击照片清理->未导出的照片次数
			 */
			public static final String UM_EVENT_ID_UNEXPORT = "clean_unexport";

			/**
			 * 点击照片清理->未导出->"相似"照片次数
			 */
			public static final String UM_EVENT_ID_UNEXPORT_SIMILAR = "clean_unexport_similar";

			/**
			 * 点击照片清理->未导出->"相册"照片次数
			 */
			public static final String UM_EVENT_ID_UNEXPORT_ALBUM = "clean_unexport_album";
			
		}

		// 2.连接网络信息收集
		public static class NETWORK_CONNECT {
			/**
			 * 选择电脑,点击开始,连接成功的次数.
			 */
			public static final String UM_EVENT_CONNECT_SUCCESS = "connect_success";
			/**
			 * 选择电脑,点击开始,连接成功所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_SUCCESS_TIME = "connect_success_time";
			/**
			 * 选择电脑,点击开始,连接失败的次数.
			 */
			public static final String UM_EVENT_CONNECT_FAIL = "connect_fail";
			/**
			 * 选择电脑,点击开始,连接失败所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_FAIL_TIME = "connect_fail_time";
			
			
			/**
			 * 用softap发现,连接成功的次数.
			 */
			public static final String UM_EVENT_CONNECT_SOFATAP_SUCCESS = "connect_softap_success";
			/**
			 * 用softap发现,连接成功所花的时间
			 */
			public static final String UM_EVENT_CONNECT_SOFATAP_SUCCESS_TIME = "connect_softap_success_time";
			
			/**
			 * 用softap发现,连接失败的次数.
			 */
			public static final String UM_EVENT_CONNECT_SOFATAP_FAIL = "connect_softap_fail";
			/**
			 * 用softap发现,连接失败所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_SOFATAP_FAIL_TIME = "connect_softap_fail_time";
			/**
			 * 用lan发现,连接成功的次数.
			 */
			public static final String UM_EVENT_CONNECT_LAN_SUCCESS = "connect_lan_success";
			/**
			 * 用lan发现,连接成功所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_LAN_SUCCESS_TIME = "connect_lan_success_time";
			/**
			 * 用lan发现,连接失败的次数.
			 */
			public static final String UM_EVENT_CONNECT_LAN_FAIL = "connect_lan_fail";
			/**
			 * 用lan发现,连接失败所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_LAN_FAIL_TIME = "connect_lan_fail_time";

			/**
			 * 用cloud发现,连接成功的次数.
			 */
			public static final String UM_EVENT_CONNECT_CLOUD_SUCCESS = "connect_cloud_success";
			/**
			 * 用cloud发现,连接成功所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_CLOUD_SUCCESS_TIME = "connect_cloud_success_time";
			/**
			 * 用cloud发现,连接失败的次数.
			 */
			public static final String UM_EVENT_CONNECT_CLOUD_FAIL = "connect_cloud_fail";
			/**
			 * 用cloud发现,连接失败所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_CLOUD_FAIL_TIME = "connect_cloud_fail_time";
			/**
			 * 用softap发现,hotspot连接成功的次数.
			 */
			public static final String UM_EVENT_CONNECT_HOTSPOT_SUCCESS = "connect_hotspot_success";
			/**
			 * 用softap发现,hotspot连接成功所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_HOTSPOT_SUCCESS_TIME = "connect_hotspot_success_time";
			/**
			 * 用softap发现,hotspot连接失败的次数.
			 */
			public static final String UM_EVENT_CONNECT_HOTSPOT_FAIL = "connect_hotspot_fail";
			
			/**
			 * 用softap发现,hotspot连接失败所花的时间.
			 */
			public static final String UM_EVENT_CONNECT_HOTSPOT_FAIL_TIME = "connect_hotspot_fail_time";
		}

		// 3.导出功能信息收集
		public static class EXPORT_PHOTO {
			// 3.1.实际导出的文件大小与期望导出的文件大小不一致的次数.
			public final static String DIFF_NEEDEXPORT_EXPORTED = "export_diff_needexport_between_exported";
			
			// 导出成功
			public final static String EXPORT_COMPLETE = "export_complete";

			// 3.2.导出文件超时的次数.(在界面上接收到超时通知的次数,与http超时次数不一样)
			public final static String EXPORT_GUI_TIMEOUT_TIMES = "export_gui_timeout";
		}

		// 4.网络传输信息收集
		public static class NETWORK_TRANSFER {
			//
			// 4.1.调用http server首次启动的次数.
			public final static String HTTP_SERVER_ONCREATE = "network_http_server_oncreate";

			// 4.2.http server启动后,还收到重新启动命令的次数.
			public final static String HTTP_SERVER_ONSTART = "network_http_server_onstart";

			// 4.3.超时的次数
			public final static String EXPORT_HTTPSERVER_TIMEOUT_TIMES = "network_http_server_timeout";

			// 4.4.从0开始下载的文件个数
			public final static String TRANSFER_FILE_FROM_ZERO = "network_transfer_from_zero";

			// 4.5.PC下载完成的个数
			public final static String TRANSFER_FILE_FINISHED = "network_transfer_finished";

			// 4.6.使用断点续传的次数.
			public final static String USED_TRANSFER_CONTINUE_DOWNLOAD = "network_transfer_continue_download";

			// 4.7.http server启动失败的次数.
			public final static String HTTP_SERVER_START_FAILED = "network_http_server_start_failed";

			public final static String HTTP_SERVER_CREATE_DOWNLOAD_THREAD_FAILED = "network_http_server_create_thread_failed";
			
			// 4.8.停止http server的次数
			public final static String HTTP_SERVER_STOP = "network_http_server_stop";
		}

		// 5.照片整理信息收集
		public static class ARRANGE_PHOTO {
			//
			// 5.1.查找相似照片时间.
			public final static String FIND_SIMILAR_PHOTO_TIMES = "arrange_find_similar_photo_times";

			// 5.2.照片管理->导出未清理文件文件->删除文件的个数.
			public final static String DELETE_EXPORTED_FILE = "arrange_exported_delete_file";

			// 5.3.照片管理->未导出文件文件->长按查看大图的次数.
			public final static String SHOW_BIG_PICTURE = "arrange_show_big_picture";

			// 5.4.照片管理->相似照片->删除文件的个数.
			// 5.5.照片管理->相册照片->删除文件的个数.
			// 5.6.获取相似照片个数占相册照片的比例.
			public final static String FIND_SIMILAR_PHOTO_PERCENT = "arrange_find_similar_percent";

			// 5.7.点击相册照片->选择排序的次数(按时间新旧,按大小).
			public final static String SORT_BY_TIME_ASC = "arrange_sort_time_asc";
			public final static String SORT_BY_TIME_DESC = "arrange_sort_time_desc";
			public final static String SORT_BY_SIZE_ASC = "arrange_sort_size_asc";
			public final static String SORT_BY_SIZE_DESC = "arrange_sort_size_desc";

			// 5.8.导出文件个数
			// public final static String EXPORTED_FILE =
			// "arrange_exported_file";

			// 5.9.查找10张相似照片耗费的时间(因为umeng的计算事件参数必须是int型)
			public final static String FIND_SIMILAR_EVERY_TEN_PHOTO_TIMES = "arrange_find_similar_every_ten_pic_times";
		}

		// 6.用户行为信息收集
		public static class USER_BEHAVE {
			// 6.1.进入到主界面, 到点"导出"或"照片管理"按钮的时间. 即停留在第一个界面的时间.
			public final static String STAY_MAINACTIVITY_TIMES = "arrange_stay_mainactivity_times";

			// 6.2.用户在选择电脑界面停留的时间.
			public final static String STAY_SCANACTIVITY_TIMES = "arrange_stay_scantactivity_times";
			
            // 记录使用export photo功能的新用户
			public final static String USED_EXPORT_FUNCTION_NEW_USER = "user_behave_used_export_new_user";

		}

	}

}
