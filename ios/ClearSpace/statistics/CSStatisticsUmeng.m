//
//  CSStatisticsUmeng.m
//  ClearSpace
//
//  Created by sw2 on 11/26/15.
//  Copyright © 2015 SW2. All rights reserved.
//

#import "CSStatisticsUmeng.h"
#import "MobClick.h"

@implementation CSStatisticsUmeng

- (void) initConfig
{
    @try {
        
        if ([self isRelease]) {
            NSString* appKey = @"5656b12267e58e4a4200273a";
            NSString* appChannel = @"App Store";
            
            [MobClick setEncryptEnabled:TRUE];
            [self getAppVersion];
            [MobClick setLogEnabled:YES];  // 打开友盟sdk调试，注意Release发布时需要注释掉此行,减少io消耗
            [MobClick startWithAppkey:appKey reportPolicy:BATCH   channelId:appChannel];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%s", "initConfig trhow error");
    }
}
// config parameter
- (void)   didFinishLanuchingWithOptions
{
    NSLog(@"%s", "didFinishLanuchingWithOptions");
}

// call when view will appear
- (void)   viewWillAppear:(NSString *)pageName;
{
    @try {
        if ([self isRelease]) {
            [MobClick beginLogPageView:pageName];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%s", "viewWillAppear trhow error");
    }
}

// call when view will disappear
- (void)    viewWillDisappear:(NSString *)pageName;
{
    @try {
        if ([self isRelease]) {
            [MobClick endLogPageView:pageName];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%s", "viewWillDisappear trhow error");
    }
    
}

// record count event
- (void)    onEventCount:(NSString*) eventId
{
    @try {
        
        if(eventId && [self isRelease]){
            [MobClick event:eventId];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%s", "onEventCount trhow error");
    }
    NSLog(@"%s", "onEventCount");
    
}


// example: [onEventValueCalcuate eventId:@"pay" attributes:@{@"book" : @"Swift Fundamentals"} counter:110];
// record value event
- (void)    onEventValueCalcuate:(NSString*)eventId attributes:(NSDictionary*)att counter:(int)number
{
    @try {
        if (eventId && [self isRelease]) {
            [MobClick event:eventId attributes:att counter:number];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"%s", "onEventValueCalcuate trhow error");
    }
    NSLog(@"%s", "onEventValueCalcuate");
}
- (void) getAppVersion
{    @try {
        NSString *version = [[[NSBundle mainBundle] infoDictionary]     objectForKey:@"CFBundleShortVersionString"];
        [MobClick setAppVersion:version];
    }
    @catch (NSException *exception) {
        NSLog(@"%s", "getAppVersion trhow error");
    }
}
- (Boolean) isRelease
{
    Boolean isRelease = TRUE;
#ifdef DEBUG
    isRelease = FALSE;
#endif
    return isRelease;
}
@end
