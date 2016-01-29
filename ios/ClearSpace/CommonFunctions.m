//
//  CommonFunctions.m
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//
#import "CommonFunctions.h"
#import <Foundation/Foundation.h>
@import Darwin.sys.mount;
#import <SystemConfiguration/CaptiveNetwork.h>
#include <ifaddrs.h>
#include <arpa/inet.h>
#import <UIKit/UIKit.h>

@implementation CommonFunctions

//根据byte值自动转为相应的单位值
+(NSDictionary*)autoConvertFileSize:(int64_t)fileSizeInByte{
    NSString* strUnit = @"KB";
    float filesize = fileSizeInByte * 1.0 / 1024;
    
    if(fileSizeInByte / (1024 * 1024 * 1024)){
        filesize = fileSizeInByte * 1.0/ (1024 * 1024 * 1024);
        strUnit = @"GB";
    }
    else if(fileSizeInByte / (1024 * 1024) >= 1){
        filesize = fileSizeInByte * 1.0/ (1024 * 1024);
        strUnit = @"MB";
    }
    
    NSNumber *size = [NSNumber numberWithFloat:filesize];
    NSDictionary *dic = [[NSDictionary alloc] initWithObjectsAndKeys:strUnit, @"unit", size, @"size", nil];
    return dic;
}

+(NSString*)autoConvertFileSizeToString:(int64_t)fileSizeInByte{
    NSString* strUnit = @"KB";
    float filesize = fileSizeInByte * 1.0 / 1024;
    
    if(fileSizeInByte / (1024 * 1024 * 1024)){
        filesize = fileSizeInByte * 1.0/ (1024 * 1024 * 1024);
        strUnit = @"GB";
    }
    else if(fileSizeInByte / (1024 * 1024) >= 1){
        filesize = fileSizeInByte * 1.0/ (1024 * 1024);
        strUnit = @"MB";
    }
    
    NSString *size = [NSString stringWithFormat:@"%.1f", filesize];
    size = [size stringByAppendingString:strUnit];
    
    return size;
}

//获取手机剩余空间
+(float)freeDiskSpaceInGB{
    uint64_t totalSpace = 0;
    uint64_t totalFreeSpace = 0;
    NSError *error = nil;
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
    NSDictionary *dictionary = [[NSFileManager defaultManager] attributesOfFileSystemForPath:[paths lastObject] error: &error];
    
    if (dictionary) {
        NSNumber *fileSystemSizeInBytes = [dictionary objectForKey: NSFileSystemSize];
        NSNumber *freeFileSystemSizeInBytes = [dictionary objectForKey:NSFileSystemFreeSize];
        totalSpace = [fileSystemSizeInBytes unsignedLongLongValue];
        totalFreeSpace = [freeFileSystemSizeInBytes unsignedLongLongValue];
        NSLog(@"Memory Capacity of %llu MiB with %llu MiB Free memory available.", ((totalSpace/1024ll)/1024ll), ((totalFreeSpace/1024ll)/1024ll));
    } else {
        NSLog(@"Error Obtaining System Memory Info: Domain = %@, Code = %ld", [error domain], (long)[error code]);
    }
    
    return totalFreeSpace * 1.0 / (1024 * 1024 * 1024);
    
    /*struct statfs buf;
     long long freespace = -1;
     if(statfs("/var", &buf) >= 0){
     freespace = (long long)(buf.f_bsize * buf.f_bfree);
     }
     
     return freespace * 1.0 / (1024 * 1024 * 1024);*/
}

//获取手机当前wifi
+(NSString*)currentWifiSSID{
    // Does not work on the simulator.
    NSString *ssid = nil;
    NSArray *ifs = (__bridge_transfer id)CNCopySupportedInterfaces();
    
    for (NSString *ifnam in ifs) {
        NSDictionary *info = (__bridge_transfer id)CNCopyCurrentNetworkInfo((CFStringRef)ifnam);
        if (info[@"SSID"]) {
            ssid = info[@"SSID"];
        }
    }
    return ssid;
}

//获取当前Ip
+(NSString*)localIPAddress{
    NSString *address = @"error";
    struct ifaddrs *interfaces = NULL;
    struct ifaddrs *temp_addr = NULL;
    int success = 0;
    // retrieve the current interfaces - returns 0 on success
    success = getifaddrs(&interfaces);
    if (success == 0) {
        // Loop through linked list of interfaces
        temp_addr = interfaces;
        while(temp_addr != NULL) {
            if(temp_addr->ifa_addr->sa_family == AF_INET) {
                // Check if interface is en0 which is the wifi connection on the iPhone
                if([[NSString stringWithUTF8String:temp_addr->ifa_name] isEqualToString:@"en0"]) {
                    // Get NSString from C String
                    address = [NSString stringWithUTF8String:inet_ntoa(((struct sockaddr_in *)temp_addr->ifa_addr)->sin_addr)];
                    
                }
                
            }
            
            temp_addr = temp_addr->ifa_next;
        }
    }
    // Free memory
    freeifaddrs(interfaces);
    return address;
}

+(NSString*)deviceID{
    return [[[UIDevice currentDevice] identifierForVendor] UUIDString];
}

@end


#pragma mark - UIColor


@implementation UIColor(ColorFromHex)

+ (UIColor*)ColorFromHex:(int64_t)hexColor alpha:(CGFloat)alpha{
    return [UIColor colorWithRed: ((hexColor >> 16) & 0xFF) / 255.0
                           green: ((hexColor >> 8) & 0xFF) / 255.0
                            blue: ((hexColor >> 0) & 0xFF) / 255.0
                           alpha: alpha];
}

+ (UIColor *)gradientColor:(NSArray *)colors locations:(CGFloat *)locations frameHeight:(CGFloat)height
{
    CGSize size = CGSizeMake(1, height);
    UIGraphicsBeginImageContextWithOptions(size, NO, 0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    NSMutableArray *array = [NSMutableArray arrayWithCapacity:colors.count];
    for (UIColor *color in colors) {
        [array addObject:(__bridge id)color.CGColor];
    }
    CGGradientRef gradient = CGGradientCreateWithColors(colorSpace, (CFArrayRef)array, locations);
    CGContextDrawLinearGradient(context, gradient, CGPointMake(0, 0), CGPointMake(0, size.height), 0);
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    CGGradientRelease(gradient);
    CGColorSpaceRelease(colorSpace);
    UIGraphicsEndImageContext();
    
    return [UIColor colorWithPatternImage:image];
}

@end