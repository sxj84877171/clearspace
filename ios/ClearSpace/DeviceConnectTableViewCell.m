//
//  DeviceConnectTableViewCell.m
//  ClearSpace
//
//  Created by SW2 on 15/11/19.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "DeviceConnectTableViewCell.h"
#import "UIButton+ImageWithColor.h"
#import "CommonFunctions.h"

@implementation DeviceConnectTableViewCell{
    NSTimer *_timer;
    NSInteger _currentProgress;
}


- (void)awakeFromNib {
    _timer = [NSTimer scheduledTimerWithTimeInterval:0.02 target:self selector:@selector(timerFired) userInfo:nil repeats:YES];
    _currentProgress = 0;
    
    [self.tryAgainButton setHidden:YES];
    [self.connectErrorLable setHidden:YES];
    
    
    [self.tryAgainButton setBackgroudColor:[UIColor ColorFromHex:0x195aaf alpha:1.0] forState:UIControlStateNormal];
    [self.tryAgainButton setBackgroudColor:[UIColor ColorFromHex:0x134991 alpha:1.0] forState:UIControlStateSelected];
}

-(void)timerFired{
    _currentProgress++;
    [self.connectProgressCtrl setProgress:_currentProgress * 0.001];
    
    if(_currentProgress == 1000){
        [self.connectErrorLable setHidden:NO];
        [self.tryAgainButton setHidden:NO];
        [_timer invalidate];
        if([self.delegate respondsToSelector:@selector(connectTimeout:)]){
            [self.delegate connectTimeout:self];
        }
    }
}

- (IBAction)tryAgain:(id)sender {
    [self.connectErrorLable setHidden:YES];
    [self.tryAgainButton setHidden:YES];
    _currentProgress = 0;
    _timer = [NSTimer scheduledTimerWithTimeInterval:0.02 target:self selector:@selector(timerFired) userInfo:nil repeats:YES];
    
    if([self.delegate respondsToSelector:@selector(connectTryAgain:)]){
        [self.delegate connectTryAgain:self];
    }
}
@end
