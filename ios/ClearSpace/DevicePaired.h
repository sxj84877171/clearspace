//
//  DevicePaired.h
//  ClearSpace
//
//  Created by SW2 on 15/9/14.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CSUdp.h"
#import "DeviceInfo.h"


@protocol DevicePairedDelegate <NSObject>

/**
 * Found new Deivce
 **/
-(void)newDevice:(DeviceInfo*)device;

/**
 * Deivce disconnect
 **/
-(void)deviceDisconnect:(DeviceInfo*)device;

@end

@interface DevicePaired : NSObject<CSUdpDelegate>

@property (weak) id<DevicePairedDelegate> delegate;
/**
 * Returns singleton instance of the DevicePaired
 **/
+(DevicePaired*)sharedInstance;

/**
 * start to search devices in the same LAN
 **/
-(void) startFindDevices;
-(void) pauseFindDevices;
-(void) resumeFindDevices;

/**
 * return current found device list
 **/
-(NSMutableArray*)foundDevices;

/**
 * send Message To ConnectDeivce
 **/
-(void)sendMessageToConnectDeivce:(NSData*)data device:(DeviceInfo*)device;

@end
