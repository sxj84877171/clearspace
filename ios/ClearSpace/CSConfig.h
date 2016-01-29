//
//  CSConfig.h
//  ClearSpace
//
//  Created by SW2 on 15/11/20.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
@class DeviceInfo;

@interface CSConfig : NSObject

+(instancetype) shareInstance;

/**
 * get/save last connected deivce
 **/
-(DeviceInfo*) lastConnectDevice;
-(void)saveLastConnectDevice:(DeviceInfo*)deivce;
-(void)clearLastConnectDevice;

@end
