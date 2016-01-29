//
//  DeviceInfo.m
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "DeviceInfo.h"

@implementation DeviceInfo

-(id) initWithInfo:(NSString*)deviceName ip:(NSString*)ip port:(uint16_t)port{
    if(self = [super init]){
        _deviceName = deviceName;
        _deviceIP = ip;
        _devicePort = port;
        
        
    }
    return self;
}

-(BOOL) isSameDevice:(DeviceInfo *)device{
    if(device){
        return [device.deviceName isEqualToString:_deviceName] &&
        [device.deviceIP isEqualToString:_deviceIP];
    }
    
    return NO;
}
@end
