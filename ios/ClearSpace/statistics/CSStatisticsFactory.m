//
//  CSStatisticsFactory.m
//  ClearSpace
//
//  Created by sw2 on 11/26/15.
//  Copyright © 2015 SW2. All rights reserved.
//

#import "CSStatisticsFactory.h"
#import "../CSConst.h"
#import "CSStatisticsUmeng.h"
#import "CSStatisticsFlurry.h"


@implementation CSStatisticsFactory



// get share instance
+ (CSStatisticsBase*) shareInstance:(int) statisticsType
{
    static CSStatisticsBase* pInstance = nil;
    switch (statisticsType) {
        case STATISTICS_TYPE_UMENG:
        {
            static dispatch_once_t onceUmeng;
            dispatch_once(&onceUmeng, ^{
                pInstance = [[CSStatisticsUmeng alloc] init];
            });
        }
        break;
        case STATISTICS_TYPE_FLURRY:
        {
            static dispatch_once_t onceFlurry;
            dispatch_once(&onceFlurry, ^{
                pInstance = [[CSStatisticsFlurry alloc] init];
            });
        }
        break;
            
        default:
            break;
    }
    return pInstance;
}

+ (CSStatisticsBase*) shareDefaultInstance
{
    int type  = STATISTICS_TYPE_UMENG;//STATISTICS_TYPE_FLURRY
    CSStatisticsBase* pInstance = [self shareInstance:type];
    return pInstance;
}

@end
