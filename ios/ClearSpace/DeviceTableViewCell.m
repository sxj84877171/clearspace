//
//  DeviceTableViewCell.m
//  ClearSpace
//
//  Created by SW2 on 15/9/14.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "DeviceTableViewCell.h"
#import "DeviceInfo.h"
#import "CSConfig.h"
#import "UIButton+ImageWithColor.h"
#import "CommonFunctions.h"

@implementation DeviceTableViewCell

- (void)awakeFromNib {
    // Initialization code
    [self selected:NO];
    [self.cancelSaveButton setHidden:YES];
    [self.disableCellView setHidden:YES];
    
    [self.cancelSaveButton setBackgroudColor:[UIColor ColorFromHex:0xa14261 alpha:1.0] forState:UIControlStateNormal];
    [self.cancelSaveButton setBackgroudColor:[UIColor ColorFromHex:0x803156 alpha:1.0] forState:UIControlStateSelected];
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
    [self selected:selected];
}

-(void)selected:(BOOL)selected{
    [self.choosedImageView setHidden:!selected];
    if(selected){
        //[self.computerNameLable setTextColor:[UIColor colorWithRed:0 green:142.0/255 blue:37.0/255 alpha:1]];
        
        //UIImage *image = [UIImage imageNamed:@"computer_green.png"];
        //[self.computerImageView setImage:image];
            }
    else {
        //UIImage *image = [UIImage imageNamed:@"computer_gray.png"];
        //[self.computerImageView setImage:image];
        //[self.computerNameLable setTextColor:[UIColor colorWithRed:102.0/255 green:102.0/255 blue:102.0/255 alpha:1]];
    }
}

- (IBAction)canceSave:(id)sender {
    CSConfig *config = [CSConfig shareInstance];
    if(config != nil){
        [config clearLastConnectDevice];
        if(self.delegate != nil && [self.delegate respondsToSelector:@selector(cancelSaveLastConnectedDevice:)]){
            [self.delegate cancelSaveLastConnectedDevice:self];
        }
    }
}
@end
