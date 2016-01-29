//
//  PublicData.h
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#include "DeviceInfo.h"
#import "CommonFunctions.h"

@interface PublicData : NSObject

-(void) startHttpServer;
-(void) stopHttpServer;

/**
 * the space size will be freed
 **/
@property(nonatomic) double willfreeSpaceSize;

/**
 * the space size have been freed
 **/
@property(nonatomic) int64_t havefreedSpaceSize;

/**
 * the currenttly exporting file.
 **/
@property(nonatomic) NSString* currentExportingFile;


/**
 * the device which we are connecting
 **/
@property(nonatomic, strong) DeviceInfo* connectingDevice;

/**
 * the file path of download list
 **/
@property(nonatomic, strong) NSString* downloadFileListPath;

/**
 * the total count of files which need to be downlaod.
 **/
@property(nonatomic) long downloadFileTotalCount;


@property(nonatomic) BOOL startDownLoad;
@property(nonatomic) NSDate* startDownloadDate;
/**
 * Returns singleton instance of the PublicData
 **/
+(PublicData*)sharedInstance;


@end
