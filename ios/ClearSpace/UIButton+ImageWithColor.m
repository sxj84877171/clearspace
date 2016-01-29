//
//  UIButton+ImageWithColor.m
//  ClearSpace
//
//  Created by SW2 on 15/11/20.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "UIButton+ImageWithColor.h"

@implementation UIButton(ImageWithColor)

-(void)setBackgroudColor:(UIColor *)backgroudColor forState:(UIControlState)state{
    [self setBackgroundImage:[UIButton ImageWithColor:backgroudColor] forState:state];
}

+(UIImage*) ImageWithColor:(UIColor *)color{
    CGRect rect = CGRectMake(0.0f, 0.0f, 1.0f, 1.0f);
    
    UIGraphicsBeginImageContext(rect.size);
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    
    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);
    
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    
    UIGraphicsEndImageContext();
    
    return image;
}

@end