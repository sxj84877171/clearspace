//
//  DeviceInfo.h
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DeviceInfo : NSObject

@property (nonatomic, copy, readonly) NSString* deviceName;
@property (nonatomic, copy, readonly) NSString* deviceIP;
@property (nonatomic, readonly) uint16_t devicePort;
@property (nonatomic) NSDate* connectTime;  //record the time of device connected
@property (nonatomic) NSDate* receiveDate;  //record the time when receive data from the device
@property (nonatomic) BOOL online;

-(id) initWithInfo:(NSString*)deviceName ip:(NSString*)ip port:(uint16_t)port;
-(BOOL) isSameDevice:(DeviceInfo*)device;
@end
