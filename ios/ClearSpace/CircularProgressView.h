//
//  CircularProgressView.h
//  CircularProgressView
//
//  Created by nijino saki on 13-3-2.
//  Copyright (c) 2013年 nijino. All rights reserved.
//  QQ:20118368
//  http://nijino.cn

#import <UIKit/UIKit.h>

@interface CircularProgressView : UIView

@property (nonatomic) UIColor *progressColor;
@property (nonatomic) UIColor *progressBackgroudColor;
@property (nonatomic) UIColor *progressFillColor;
@property (assign, nonatomic) CGFloat lineWidth;

-(void)drawProgress:(CGFloat)strokeStart end:(CGFloat)strokeEnd;
- (void)updateProgress:(float)progress;

@end