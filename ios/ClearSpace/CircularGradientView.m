//
//  CircularGradientView.m
//  ClearSpace
//
//  Created by SW2 on 15/11/23.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CircularGradientView.h"

@implementation CircularGradientView

- (void) awakeFromNib{
    self.backgroundColor = [UIColor clearColor];
}

-(void) drawRect:(CGRect)rect{
    /*CALayer *gradientLayer = [CALayer layer];
    CAGradientLayer *gradientLayer1 =  [CAGradientLayer layer];
    gradientLayer1.frame = CGRectMake(0, 0, self.bounds/2, self.bounds/2);
    [gradientLayer1 setColors:[NSArray arrayWithObjects:(id)[[UIColor redColor] CGColor],(id)[UIColorFromRGB(0xfde802) CGColor], nil]];
    [gradientLayer1 setLocations:@[@0.5,@0.9,@1 ]];
    [gradientLayer1 setStartPoint:CGPointMake(0.5, 1)];
    [gradientLayer1 setEndPoint:CGPointMake(0.5, 0)];
    [gradientLayer addSublayer:gradientLayer1];
    
    CAGradientLayer *gradientLayer2 =  [CAGradientLayer layer];
    [gradientLayer2 setLocations:@[@0.1,@0.5,@1]];
    gradientLayer2.frame = CGRectMake(self.width/2, 0, self.width/2, self.height);
    [gradientLayer2 setColors:[NSArray arrayWithObjects:(id)[UIColorFromRGB(0xfde802) CGColor],(id)[MAIN_BLUE CGColor], nil]];
    [gradientLayer2 setStartPoint:CGPointMake(0.5, 0)];
    [gradientLayer2 setEndPoint:CGPointMake(0.5, 1)];
    [gradientLayer addSublayer:gradientLayer2];*/
}
@end
