//
//  CommonFunctions.h
//  ClearSpace
//
//  Created by SW2 on 15/11/20.
//  Copyright © 2015年 SW2. All rights reserved.
//
#import <Foundation/Foundation.h>
#import <UIKit/UIColor.h>

typedef NS_ENUM(NSInteger, DeviceType) {
    IS_IPHONE,
    IS_IPAD
};

@interface CommonFunctions : NSObject
+(NSDictionary*)autoConvertFileSize:(int64_t)fileSizeInByte;
+(NSString*)autoConvertFileSizeToString:(int64_t)fileSizeInByte;
+(float) freeDiskSpaceInGB;
+(NSString*) currentWifiSSID;
+(NSString*) localIPAddress;
+(NSString*) deviceID;
@end


@interface UIColor(ColorFromHex)

+ (UIColor*)ColorFromHex:(int64_t)hexColor alpha:(CGFloat)alpha;
+ (UIColor *)gradientColor:(NSArray *)colors locations:(CGFloat *)locations frameHeight:(CGFloat)height;

@end