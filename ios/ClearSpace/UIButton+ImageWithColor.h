//
//  UIButton+ImageWithColor.h
//  ClearSpace
//
//  Created by SW2 on 15/11/20.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface UIButton (ImageWithColor)

-(void)setBackgroudColor:(UIColor*)backgroudColor forState:(UIControlState)state;

+(UIImage*) ImageWithColor:(UIColor*)color;

@end
