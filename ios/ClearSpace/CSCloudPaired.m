//
//  CSCloudPaired.m
//  ClearSpace
//
//  Created by SW2 on 15/9/22.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSCloudPaired.h"
#import "PublicData.h"

@implementation CSCloudPaired{
    dispatch_queue_t _httpQueue;
}

-(id)init{
    if(self = [super init]){
        _httpQueue = dispatch_queue_create("cloud_paired_http_queue", nil);
    }
    return self;
}

-(void)paired:(void(^)(NSArray* deviceArray))block{
    @try {
        dispatch_async(_httpQueue, ^{
            NSArray * devicesArray = [self httpRequest:@"http://114.215.236.240:8080/relayserver/register?device_id=%@&net_id=%@&ip=%@&os_type=ios" unregister:NO];
            block(devicesArray);
        });\
    }
    @catch (NSException *exception) {
        ;
    }
}

-(void)unpaired{
    dispatch_async(_httpQueue, ^{
        [self httpRequest:@"http://114.215.236.240:8080/relayserver/unregister?device_id=%@&net_id=%@&ip=%@&os_type=ios" unregister:YES];
    });
}

-(NSArray*)httpRequest:(NSString*)serverURL unregister:(BOOL)unregister{
    @try {
        NSMutableArray *devicesArray = [[NSMutableArray alloc] init];
        
        NSString *ssid = [CommonFunctions currentWifiSSID];
        NSString *ip = [CommonFunctions localIPAddress];
        NSString *deviceid = [CommonFunctions deviceID];
        NSString *urlFormat = [NSString stringWithFormat:serverURL, deviceid, ssid, ip];
        NSURL *url = [NSURL URLWithString:urlFormat];
        
        NSURLRequest *request = [NSURLRequest requestWithURL:url cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:0];
        NSData *received = [NSURLConnection sendSynchronousRequest:request returningResponse:nil error:nil];
        
        if(!unregister && received){
            
            NSDictionary *jsonData = [NSJSONSerialization JSONObjectWithData:received options:kNilOptions error:nil];
            if(jsonData == nil || jsonData[@"peers"] == nil){
                NSLog(@"Json parse failed\r\n");
                return nil;
            }
            
            NSDictionary *devicesDic = jsonData[@"peers"];
            for(id obj in devicesDic){
                NSDictionary *infoDic = (NSDictionary*)obj;
                NSString *ip = infoDic[@"ip"];
                NSArray *ipArray = [ip componentsSeparatedByString:@","];
                if(infoDic && infoDic[@"os_type"] && [infoDic[@"os_type"] isEqualToString:@"pc"]){
                    for(id obj in ipArray){
                        DeviceInfo *deviceInfo = [[DeviceInfo alloc] initWithInfo:@"" ip:obj port:7083];
                        [devicesArray addObject:deviceInfo];
                    }
                }
            }
            
        }
        return devicesArray;
    }
    @catch (NSException *exception) {
        
    }
    
    return nil;
}
@end
