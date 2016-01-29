//
//  CSUmengEvent.h
//  ClearSpace
//
//  Created by sw2 on 12/3/15.
//  Copyright © 2015 SW2. All rights reserved.
//

#ifndef CSUmengEvent_h
#define CSUmengEvent_h

    // 界面信息收集

    /**
     * 选择导出1个月前的次数
     */
    static NSString* const  UM_GUI_SELECT_BEFORE_ONE_MON_EXPORT = @"gui_select_export_before_onemonth";
    
    /**
     * 选择导出3个月前的次数
     */
    static NSString* const  UM_GUI_SELECT_BEFORE_THREE_MON_EXPORT = @"gui_select_export_before_threemonth";
    
    /**
     * 选择导出6个月前的次数
     */
    static NSString* const  UM_GUI_SELECT_BEFORE_SIX_MON_EXPORT = @"gui_select_export_before_sixmonth";
    
    /**
     * 选择导出12个月前的次数
     */
    static NSString* const  UM_GUI_SELECT_BEFORE_ONE_YEAR_EXPORT = @"gui_select_export_before_twfmonth";
    
    /**
     * 选择导出All的次数
     */
    static NSString* const  UM_GUI_SELECT_BEFORE_ALL_EXPORT = @"gui_select_export_before_all";
    
    /**
     * 用户点击上次连接的电脑次数.
     */
    static NSString* const  UM_GUI_LASTCONNPC = @"scan_last_conn_pc";
    
    /**
     * 用户点击附近的电脑次数.
     */
    static NSString* const  UM_GUI_NEARPC = @"scan_near_pc";
    
    /**
     * 用户点击取消的次数.
     */
    static NSString* const  UM_GUI_CANCLE = @"scan_cancle";
    
    /**
     * 选择电脑,点击取消保存的次数
     */
    static NSString* const  UM_GUI_CANCLE_SELECTED = @"scan_cancle_save";
    
    /**
     * scan界面点击"开始"的按钮
     */
    static NSString* const  UM_GUI_START = @"scan_start_button";
    
    /**
     * scan界面点击"重试"的按钮
     */
    static NSString* const  UM_GUI_RETRY = @"scan_retry_button";
    
    /**
     * 用户点击"返回"到主页面按钮
     */
    static NSString* const  UM_GUI_BACK = @"scan_back_button";
    
    /**
     * 选择电脑,连接失败后,点击取消的次数.
     */
    static NSString* const  UM_GUI_CONN_FAILE_CANCLE = @"scan_cancle_conn_fail";
    
    /**
     * 点击推荐好友的次数.
     */
    static NSString* const  UM_GUI_PULL2FRIENADS = @"finish_share_button";
    
    /**
     * 导出文件后,点击删除所有已导出文件的次数.
     */
    static NSString* const  UM_GUI_CLEANALLPIC = @"finish_clean_all";
    
    /**
     * 导出文件后,点击查看后删除按钮
     */
    static NSString* const  UM_GUI_CLEAN_AFTER_LOOK = @"finish_clean_after_look";
    
    /**
     * 弹出新版本提示框后,点击确定的次数.
     */
    static NSString* const  UM_GUI_UPDATA_P = @"updata_p";
    
    /**
     * 弹出新版本提示框后,点击取消的次数.
     */
    static NSString* const  UM_GUI_UPDATA_N = @"updata_n";
    
    /**
     * 用户点击"导出"次数.
     */
    static NSString* const  UM_GUI_EXPORT = @"gui_export_btn";
    
    /**
     * 用户点击"照片清理"次数.
     */
    static NSString* const  UM_GUI_MANAGE_PHOTO = @"gui_main_manage_photo";
    
    /**
     * 用户点击退出程序按钮
     */
    static NSString* const  UM_EVENT_SETTING_EXIT_SERVICE = @"dialog_exit_service";
    
    /**
     * 用户点击关闭界面按钮
     */
    static NSString* const  UM_EVENT_SETTING_EXIT_GUI = @"dialog_exit_gui";
    
    /**
     * 左侧菜单按钮点击的次数
     */
    static NSString* const  UM_GUI_MENU = @"menu_button";
    
    /**
     * 用户点击左侧菜单"分享"按钮次数
     */
    static NSString* const  UM_GUI_SHARE = @"menu_share_button";
    
    /**
     * 用户点击"反馈"的次数
     */
    static NSString* const  UM_GUI_FEEDBACK = @"menu_feedback_button";
    
    /**
     * 用户点击"退出"程序按钮
     */
    static NSString* const  UM_GUI_EXIT = @"menu_exit_button";
    
    /**
     * 用户点击"关于"程序按钮
     */
    static NSString* const  UM_GUI_MENU_ABOUT = @"menu_about";
    
    /**
     * 用户点击"开发者模式"程序按钮
     */
    static NSString* const  UM_GUI_MENU_DEVELOP = @"menu_develop_button";
    
    /**
     * 用户点击反馈"提交"按钮
     */
    static NSString* const  UM_GUI_FEEDBACK_SUBMIT = @"menu_feedback_submit_button";
    
    /**
     * finish界面,删除文件过程中,点击"取消"删除文件的次数.
     */
    static NSString* const  UM_GUI_DELETING_CANCLE = @"finish_dialog_deleting_cancle_button";
    
    /**
     * 点击照片清理->已导出未清理的照片次数
     */
    static NSString* const  UM_GUI_EXPORTED_UNCLEAN = @"clean_exported_unclean";
    
    /**
     * 点击照片清理->未导出的照片次数
     */
    static NSString* const  UM_GUI_UNEXPORT = @"clean_unexport";
    
    /**
     * 点击照片清理->未导出->"相似"照片次数
     */
    static NSString* const  UM_GUI_UNEXPORT_SIMILAR = @"clean_unexport_similar";
    
    /**
     * 点击照片清理->未导出->"相册"照片次数
     */
    static NSString* const  UM_GUI_UNEXPORT_ALBUM = @"clean_unexport_album";



    // 2.连接网络信息收集
 //   public static class NETWORK_CONNECT
        /**
         * 选择电脑,点击开始,连接成功的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_SUCCESS = @"connect_success";
        /**
         * 选择电脑,点击开始,连接成功所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_SUCCESS_TIME = @"connect_success_time";
        /**
         * 选择电脑,点击开始,连接失败的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_FAIL = @"connect_fail";
        /**
         * 选择电脑,点击开始,连接失败所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_FAIL_TIME = @"connect_fail_time";
    
    
        /**
         * 用softap发现,连接成功的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_SOFATAP_SUCCESS = @"connect_softap_success";
        /**
         * 用softap发现,连接成功所花的时间
         */
        static NSString* const  UM_EVENT_CONNECT_SOFATAP_SUCCESS_TIME = @"connect_softap_success_time";
        
        /**
         * 用softap发现,连接失败的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_SOFATAP_FAIL = @"connect_softap_fail";
        /**
         * 用softap发现,连接失败所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_SOFATAP_FAIL_TIME = @"connect_softap_fail_time";
        /**
         * 用lan发现,连接成功的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_LAN_SUCCESS = @"connect_lan_success";
        /**
         * 用lan发现,连接成功所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_LAN_SUCCESS_TIME = @"connect_lan_success_time";
        /**
         * 用lan发现,连接失败的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_LAN_FAIL = @"connect_lan_fail";
        /**
         * 用lan发现,连接失败所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_LAN_FAIL_TIME = @"connect_lan_fail_time";
        
        /**
         * 用cloud发现,连接成功的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_CLOUD_SUCCESS = @"connect_cloud_success";
        /**
         * 用cloud发现,连接成功所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_CLOUD_SUCCESS_TIME = @"connect_cloud_success_time";
        /**
         * 用cloud发现,连接失败的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_CLOUD_FAIL = @"connect_cloud_fail";
        /**
         * 用cloud发现,连接失败所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_CLOUD_FAIL_TIME = @"connect_cloud_fail_time";
        /**
         * 用softap发现,hotspot连接成功的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_HOTSPOT_SUCCESS = @"connect_hotspot_success";
        /**
         * 用softap发现,hotspot连接成功所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_HOTSPOT_SUCCESS_TIME = @"connect_hotspot_success_time";
        /**
         * 用softap发现,hotspot连接失败的次数.
         */
        static NSString* const  UM_EVENT_CONNECT_HOTSPOT_FAIL = @"connect_hotspot_fail";
        
        /**
         * 用softap发现,hotspot连接失败所花的时间.
         */
        static NSString* const  UM_EVENT_CONNECT_HOTSPOT_FAIL_TIME = @"connect_hotspot_fail_time";
    
    // 3.导出功能信息收集
 //   public static class EXPORT_PHOTO
        // 3.1.实际导出的文件大小与期望导出的文件大小不一致的次数.
        static NSString* const  DIFF_NEEDEXPORT_EXPORTED = @"export_diff_needexport_between_exported";
        
        // 导出成功
        static NSString* const  EXPORT_COMPLETE = @"export_complete";

        static NSString* const  EXPORT_CANCEL = @"export_cancel";
        // 3.2.导出文件超时的次数.(在界面上接收到超时通知的次数,与http超时次数不一样)
        static NSString* const  EXPORT_GUI_TIMEOUT_TIMES = @"export_gui_timeout";

    
    // 4.网络传输信息收集
 //   public static class NETWORK_TRANSFER

        //
        // 4.1.调用http server首次启动的次数.
        static NSString* const  HTTP_SERVER_ONCREATE = @"network_http_server_oncreate";
        
        // 4.2.http server启动后,还收到重新启动命令的次数.
        static NSString* const  HTTP_SERVER_ONSTART = @"network_http_server_onstart";
        
        // 4.3.超时的次数
        static NSString* const  EXPORT_HTTPSERVER_TIMEOUT_TIMES = @"network_http_server_timeout";
        
        // 4.4.从0开始下载的文件个数
        static NSString* const  TRANSFER_FILE_FROM_ZERO = @"network_transfer_from_zero";
        
        // 4.5.PC下载完成的个数
        static NSString* const  TRANSFER_FILE_FINISHED = @"network_transfer_finished";
        
        // 4.6.使用断点续传的次数.
        static NSString* const  USED_TRANSFER_CONTINUE_DOWNLOAD = @"network_transfer_continue_download";
        
        // 4.7.http server启动失败的次数.
        static NSString* const  HTTP_SERVER_START_FAILED = @"network_http_server_start_failed";
        
        static NSString* const  HTTP_SERVER_CREATE_DOWNLOAD_THREAD_FAILED = @"network_http_server_create_thread_failed";
        
        // 4.8.停止http server的次数
        static NSString* const  HTTP_SERVER_STOP = @"network_http_server_stop";

    
    // 5.照片整理信息收集
//    public static class ARRANGE_PHOTO

        //
        // 5.1.查找相似照片时间.
        static NSString* const  FIND_SIMILAR_PHOTO_TIMES = @"arrange_find_similar_photo_times";
        
        // 5.2.照片管理->导出未清理文件文件->删除文件的个数.
        static NSString* const  DELETE_EXPORTED_FILE = @"arrange_exported_delete_file";
        
        // 5.3.照片管理->未导出文件文件->长按查看大图的次数.
        static NSString* const  SHOW_BIG_PICTURE = @"arrange_show_big_picture";
        
        // 5.4.照片管理->相似照片->删除文件的个数.
        // 5.5.照片管理->相册照片->删除文件的个数.
        // 5.6.获取相似照片个数占相册照片的比例.
        static NSString* const  FIND_SIMILAR_PHOTO_PERCENT = @"arrange_find_similar_percent";
        
        // 5.7.点击相册照片->选择排序的次数(按时间新旧,按大小).
        static NSString* const  SORT_BY_TIME_ASC = @"arrange_sort_time_asc";
        static NSString* const  SORT_BY_TIME_DESC = @"arrange_sort_time_desc";
        static NSString* const  SORT_BY_SIZE_ASC = @"arrange_sort_size_asc";
        static NSString* const  SORT_BY_SIZE_DESC = @"arrange_sort_size_desc";
        
        // 5.8.导出文件个数
        // static NSString* const  EXPORTED_FILE =
        // "arrange_exported_file";
        
        // 5.9.查找10张相似照片耗费的时间(因为umeng的计算事件参数必须是int型)
        static NSString* const  FIND_SIMILAR_EVERY_TEN_PHOTO_TIMES = @"arrange_find_similar_every_ten_pic_times";

    
    // 6.用户行为信息收集
//    public static class USER_BEHAVE

        // 6.1.进入到主界面, 到点"导出"或"照片管理"按钮的时间. 即停留在第一个界面的时间.
        static NSString* const  STAY_MAINACTIVITY_TIMES = @"arrange_stay_mainactivity_times";
        
        // 6.2.用户在选择电脑界面停留的时间.
        static NSString* const  STAY_SCANACTIVITY_TIMES = @"arrange_stay_scantactivity_times";
        
        // 记录使用export photo功能的新用户
        static NSString* const  USED_EXPORT_FUNCTION_NEW_USER = @"user_behave_used_export_new_user";
        
    

#endif /* CSUmengEvent_h */
