//
//  PublicStruct.h
//  ClearSpace
//
//  Created by SW2 on 15/11/20.
//  Copyright © 2015年 SW2. All rights reserved.
//

#ifndef PublicStruct_h
#define PublicStruct_h

//设备列表cell类型
typedef NS_ENUM(NSInteger, DeviceCellType){
    DEVICE_UNKNOW,
    DEVICE_CONNECTING,  //正在连接
    DEVICE_CONNECTED,   //上次已连接
    DEVICE_NEARBY       //附近发现的
};


typedef NS_ENUM(NSInteger, TimePoint) {
    BEFORE_ONEYEAR,
    BEFORE_HALFYEAR,
    BEFORE_THREEMONTH,
    BEFORE_ONEMONTH,
    ALLTIME
};

typedef NS_ENUM(NSInteger, SortBy) {
    SortBy_None,
    SortBy_TimeAsc,
    SortBy_TimeDesc,
    SortBy_SizeAsc,
    SortBy_SizeDesc
};
#endif /* PublicStruct_h */
