//
//  PublicData.m
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "PublicData.h"
@import Darwin.sys.mount;
#import <SystemConfiguration/CaptiveNetwork.h>
#include <ifaddrs.h>
#include <arpa/inet.h>
#import <UIKit/UIKit.h>
#import "HTTPServer.h"
#import "MyHTTPConnection.h"
#import "DDLog.h"
#import "DDTTYLogger.h"
static const int ddLogLevel = LOG_LEVEL_VERBOSE;

@implementation PublicData{
    HTTPServer* _httpServer;
}

+(PublicData*) sharedInstance{
    static PublicData* _publicData = nil;
    
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _publicData = [[PublicData alloc] init];
    });
    
    return _publicData;
}

-(id) init{
    if(self = [super init]){
    }
    
    return self;
}

-(void) startHttpServer{
    [DDLog addLogger:[DDTTYLogger sharedInstance]];
    
    _httpServer = [[HTTPServer alloc] init];
    
    if(_httpServer){
        [_httpServer setType:@"_http._tcp."];
        
        //NSString *docRot = [[[NSBundle mainBundle] pathForResource:@"index" ofType:@"html" inDirectory:@"web"] stringByDeletingLastPathComponent];
        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
        NSString *docRot = [paths objectAtIndex:0];
        
        
        DDLogInfo(@"Setting document root:%@", docRot);
        
        [_httpServer setPort:7084];
        [_httpServer setDocumentRoot:docRot];
        [_httpServer setConnectionClass:[MyHTTPConnection class]];
        
        NSError *error = nil;
        if(![_httpServer start:&error]){
            DDLogError(@"Error starting HTTP Server: %@", error);
        }
    }
}

-(void) stopHttpServer{
    if(_httpServer){
        [_httpServer stop:NO];
    }
}
@end
