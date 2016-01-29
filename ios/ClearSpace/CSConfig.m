//
//  CSConfig.m
//  ClearSpace
//
//  Created by SW2 on 15/11/20.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSConfig.h"
#import "DeviceInfo.h"

@implementation CSConfig

+(instancetype) shareInstance{
    static CSConfig *config = nil;
    
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        config = [[CSConfig alloc] init];
    });
    
    return config;
}

-(DeviceInfo*)lastConnectDevice{
    DeviceInfo *info = nil;
    @try {
        NSUserDefaults *setting = [NSUserDefaults standardUserDefaults];
        if(setting){
            NSString* name = [setting objectForKey:@"devicename"];
            NSString* ip = [setting objectForKey:@"deviceip"];
            uint16_t port = [((NSNumber*)[setting objectForKey:@"deviceport"]) unsignedIntValue];
            NSDate* time = [setting objectForKey:@"deviceconnecttime"];
            
            if(name != nil && ip != nil){
                info = [[DeviceInfo alloc] initWithInfo:name ip:ip port:port];
                info.connectTime = time;
                info.online = NO;
            }
        }
    }
    @catch (NSException *exception) {
        ;
    }
    @finally {
        return info;
    }
    
}

-(void)saveLastConnectDevice:(DeviceInfo *)device{
    @try {
        NSUserDefaults *setting = [NSUserDefaults standardUserDefaults];
        if(setting){
            [setting setObject:device.deviceName forKey:@"devicename"];
            [setting setObject:device.deviceIP forKey:@"deviceip"];
            [setting setObject:[NSNumber numberWithUnsignedInt:device.devicePort] forKey:@"deviceport"];
            [setting setObject:device.connectTime forKey:@"deviceconnecttime"];
        }
    }
    @catch (NSException *exception) {
        ;
    }
    @finally {
        ;
    }
}

-(void)clearLastConnectDevice{
    @try {
        NSUserDefaults *setting = [NSUserDefaults standardUserDefaults];
        if(setting){
            [setting removeObjectForKey:@"devicename"];
            [setting removeObjectForKey:@"deviceip"];
            [setting removeObjectForKey:@"deviceport"];
            [setting removeObjectForKey:@"deviceconnecttime"];
        }
    }
    @catch (NSException *exception) {
        ;
    }
    @finally {
        ;
    }
}
@end
