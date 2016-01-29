//
//  CSUdp.h
//  ClearSpace
//
//  Created by SW2 on 15/9/14.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "GCDAsyncUdpSocket.h"

@protocol CSUdpDelegate <NSObject>

/**
 * receive udp data
 **/
-(void)didReceiveData:(NSDictionary*)jsonData clientIP:(NSString*)ip clientPort:(uint16_t)port;


@end

@interface CSUdp : NSObject<GCDAsyncUdpSocketDelegate>

@property(weak) id<CSUdpDelegate> delegate;

-(void)start:(uint16_t)port;
-(void)pause;
-(void)resume;
/**
 * send data to target device
 **/
-(void)sendMessage:(NSString *)ip port:(uint16_t)port withMessage:(NSData *)message;

@end
