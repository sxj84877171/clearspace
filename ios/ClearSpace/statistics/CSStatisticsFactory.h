//
//  CSStatisticsFactory.h
//  ClearSpace
//
//  Created by sw2 on 11/26/15.
//  Copyright © 2015 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "CSStatisticsBase.h"
#import "../CSUmengEvent.h"

@interface CSStatisticsFactory : NSObject
{
    int statisticsType;
}
// get share instance

+ (CSStatisticsBase*) shareInstance:(int) statisticsType;
+ (CSStatisticsBase*) shareDefaultInstance;

@end
