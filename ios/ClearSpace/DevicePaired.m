//
//  DevicePaired.m
//  ClearSpace
//
//  Created by SW2 on 15/9/14.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

/*
 设备发现采用两种方式：
 1. pc端广播方式，移动端接收到广播包，获取pc信息
 2. 云发现方式，因为广播可能会被禁止，所以pc,移动端都把自己的信息上传到云端，由云端比对再告诉移动端pc的信息，移动端再给该pc发个确认包，pc端如果回复了，则配对成功
 */
#import "DevicePaired.h"
#import "PublicData.h"
#import "CSCloudPaired.h"
#import <UIKit/UIKit.h>

@interface DevicePaired()
@end

@implementation DevicePaired{
    CSUdp *_udpServer;
    CSCloudPaired *_cloudPaired;
    
    NSMutableDictionary *_deviceDic;    //key:ip value:device name
    dispatch_queue_t _updateDeviceDicQueue;
    
    NSTimer *_checkAliveTimer;
    NSTimer *_cloudPairedTimer;
}

-(id) init{
    if(self = [super init]){
        _deviceDic = [[NSMutableDictionary alloc] init];
        _updateDeviceDicQueue = dispatch_queue_create("my_update_device_queue", nil);
    }
    
    return self;
}

-(void)dealloc{
    if(_checkAliveTimer){
        [_checkAliveTimer invalidate];
    }
}

+(DevicePaired*) sharedInstance{
    static DevicePaired *_devicePaired = nil;
    
    static dispatch_once_t once;
    dispatch_once(&once, ^{
        _devicePaired = [[DevicePaired alloc] init];
    });
    
    return _devicePaired;
}


-(void)startFindDevices{
    @try {
        _udpServer = [[CSUdp alloc] init];
        _udpServer.delegate = self;
        [_udpServer start:7082];
        
        if(_checkAliveTimer == nil){
            [self checkDeviceValidate];
        }
        
        _cloudPaired = [[CSCloudPaired alloc] init];
        if(_cloudPairedTimer == nil){
            [self startCloudPaired];
        }
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        
    }
}

-(void)pauseFindDevices{
    @try {
        if(_udpServer){
            [_udpServer pause];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)resumeFindDevices{
    @try {
        if(_udpServer){
            [_udpServer resume];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(NSMutableArray*) foundDevices{
    NSMutableArray *devicesArr = nil;
    @try {
        devicesArr = [[NSMutableArray alloc] init];
        [_deviceDic enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            [devicesArr addObject:obj];
        }];
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        return devicesArr;
    }
}

-(void)sendMessageToConnectDeivce:(NSData *)data device:(DeviceInfo *)device{
    @try {
        if(device && _udpServer){
            [_udpServer sendMessage:device.deviceIP port:device.devicePort withMessage:data];
        }
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        
    }
}

#pragma mark - CSUdpDelegate
-(void) didReceiveData:(NSDictionary *)jsonData clientIP:(NSString *)ip clientPort:(uint16_t)port{
    @try {
        if(jsonData){
            NSString *cmd = [jsonData objectForKey:@"cmd"];
            NSString *name = [jsonData objectForKey:@"name"];
            NSDate *date = [NSDate date];
            if([cmd isEqualToString:@"KSendPCInfo"] || [cmd isEqualToString:@"kAlive"]){
                DeviceInfo * deviceInfo = [_deviceDic objectForKey:ip];
                if(!deviceInfo){
                    DeviceInfo *info = [[DeviceInfo alloc] initWithInfo:name ip:ip port:7083];
                    info.receiveDate = date;
                    dispatch_sync(_updateDeviceDicQueue, ^{
                        [_deviceDic setObject:info forKey:ip];
                    });
                    
                    dispatch_async(dispatch_get_main_queue(), ^{
                        if(_delegate && [_delegate respondsToSelector:@selector(newDevice:)]){
                            [_delegate newDevice:info];
                        }
                    });
                }
                else{
                    deviceInfo.receiveDate = date;
                }
            }
        }
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        
    }
}

#pragma mark - private method
-(void) checkDeviceValidate{
    _checkAliveTimer = [NSTimer scheduledTimerWithTimeInterval:2.5 target:self selector:@selector(timerFired) userInfo:nil repeats:YES];
}

-(void)timerFired{
    dispatch_sync(_updateDeviceDicQueue, ^{
        NSDate *date = [NSDate date];
        [_deviceDic enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
            DeviceInfo *info = (DeviceInfo*)obj;
            if(info && [date timeIntervalSinceDate:info.receiveDate] > 5){
                dispatch_async(dispatch_get_main_queue(), ^{
                    if(_delegate && [_delegate respondsToSelector:@selector(deviceDisconnect:)]){
                        [_delegate deviceDisconnect:info];
                    }
                });
                [_deviceDic removeObjectForKey:key];

            }
        }];
    });
}

-(void) startCloudPaired{
    _cloudPairedTimer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(cloudPairedTimerFired) userInfo:nil repeats:YES];
}

-(void) cloudPairedTimerFired{
    [_cloudPaired paired:^(NSArray *deviceArray) {
        dispatch_async(dispatch_get_main_queue(), ^{
            if(deviceArray){
                for(id obj in deviceArray){
                    DeviceInfo *info = (DeviceInfo*)obj;
                    if(info){
                        NSDictionary *jsonDic = @{@"v":@"1.0", @"cmd":@"kTestAlive"};
                        NSData *data = [NSJSONSerialization dataWithJSONObject:jsonDic options:NSJSONWritingPrettyPrinted error:nil];
                        [self sendMessageToConnectDeivce:data device:info];
                    }
                }
                
            }
        });
    }];
}
@end
