//
//  CSUdp.m
//  ClearSpace
//
//  Created by SW2 on 15/9/14.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "CSUdp.h"

@implementation CSUdp{
    GCDAsyncUdpSocket *_udpServerSocket;
    GCDAsyncUdpSocket *_udpClientSocket;
    uint16_t _listenPort;
    
    long _packageTag;
    NSMutableDictionary *_packages; //key:packtagTag  value:package info
}

-(id) init{
    if(self = [super init]){
        _packageTag = 0;
        _packages = [[NSMutableDictionary alloc] init];
    }
    
    return self;
}

- (GCDAsyncUdpSocket*) createServerUdpSocket:(const char *)name port:(uint16_t)port{
    //create a background queue to recive data;
    dispatch_queue_t queue = dispatch_queue_create(name, nil);
    GCDAsyncUdpSocket *socket = [[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:queue socketQueue:nil];
    
    [socket enableBroadcast:YES error:nil];
    
    [socket bindToPort:port error:nil];
    
    //接收一次消息(启动一个等待接收,且只接收一次)
    //[socket beginReceiving:nil];
    
    return socket;
}

-(void)start:(uint16_t)port{
    _listenPort = port;
    _udpServerSocket = [self createServerUdpSocket:"com.clearspace.udp.server.queue" port:port];
}

-(void)pause{
    @try {
        if(_udpServerSocket){
            [_udpServerSocket pauseReceiving];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)resume{
    @try {
        NSError *error = nil;
        if(_udpServerSocket && ![_udpServerSocket beginReceiving:&error]){
            [_udpServerSocket close];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)sendMessage:(NSString *)ip port:(uint16_t)port withMessage:(NSData *)message{
    @try {
        if(_udpClientSocket == nil){
            dispatch_queue_t queue = dispatch_queue_create("com.clearspace.udp.client.queue", nil);
            _udpClientSocket = [[GCDAsyncUdpSocket alloc] initWithDelegate:self delegateQueue:queue socketQueue:nil];
        }
        NSMutableDictionary *package = [[NSMutableDictionary alloc] init];
        [package setValue:[NSNumber numberWithUnsignedInt:port] forKey:@"port"];
        [package setValue:message forKey:@"message"];
        [_packages setObject:package forKey:[NSNumber numberWithLongLong:_packageTag]];
        
        [_udpClientSocket sendData:message toHost:ip port:port withTimeout:60 tag:_packageTag++];
    }
    @catch (NSException *exception) {
        NSLog(@"%@", exception.description);
    }
    @finally {
        
    }
}

#pragma mark - GCDAsyncUdpSocketDelegate

-(void)udpSocket:(GCDAsyncUdpSocket *)sock didReceiveData:(NSData *)data fromAddress:(NSData *)address withFilterContext:(id)filterContex{
    @try {
        //解析json数据包
        NSError *error;
        NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:&error];
        if(json == nil){
            NSLog(@"Json parse failed\r\n");
            return;
        }
        
        //取得发送发的ip和端口
        NSString *ip = [GCDAsyncUdpSocket hostFromAddress:address];
        uint16_t port = [GCDAsyncUdpSocket portFromAddress:address];
        
        if(ip && [ip containsString:@"::ffff:"]){
            return;
        }
        
        if(self.delegate && [self.delegate respondsToSelector:@selector(didReceiveData:clientIP:clientPort:)]){
            [self.delegate didReceiveData:json clientIP:ip clientPort:port];
        }
        
        //data就是接收的数据
        //NSString *s = [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding];
    }
    @catch (NSException *exception) {
        NSLog(@"%@", exception.description);
    }
    @finally {
        
    }
}

-(void)udpSocket:(GCDAsyncUdpSocket *)sock didSendDataWithTag:(long)tag{
    //data send successfull
    @try {
        [_packages removeObjectForKey:[NSNumber numberWithLong:tag]];
    }
    @catch (NSException *exception) {
        NSLog(@"%@", exception.description);
    }
    @finally {
        
    }
}
-(void)udpSocket:(GCDAsyncUdpSocket *)sock didNotSendDataWithTag:(long)tag dueToError:(NSError *)error{
    @try {
        NSLog(@"tag %ld send failed, reason: %@", tag, error);
        NSDictionary *info = [_packages objectForKey:[NSNumber numberWithLong:tag]];
        if(info){
            NSData *message = [info objectForKey:@"message"];
            NSString *ip = [info objectForKey:@"ip"];
            NSNumber *port = [info objectForKey:@"port"];
            [_udpServerSocket sendData:message toHost:ip port:[port longLongValue] withTimeout:60 tag:tag];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%@", exception.description);
    }
    @finally {
        
    }
}

- (void)udpSocketDidClose:(GCDAsyncUdpSocket *)sock withError:(NSError *)error{
    
    NSLog(@"socket close, reason: %@", error);
    if(sock == _udpClientSocket){
        [_udpClientSocket close];
        _udpClientSocket = nil;
    }
    else{
        [self start:_listenPort];
    }
}

@end
