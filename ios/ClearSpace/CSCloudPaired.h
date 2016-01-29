//
//  CSCloudPaired.h
//  ClearSpace
//
//  Created by SW2 on 15/9/22.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DeviceInfo.h"

@interface CSCloudPaired : NSObject

-(void)paired:(void(^)(NSArray* deviceArray))block;
-(void)unpaired;

@end
