//
//  DeviceTableViewCell.h
//  ClearSpace
//
//  Created by SW2 on 15/9/14.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PublicStruct.h"

@class DeviceInfo;
@class DeviceTableViewCell;


@protocol DeviceTableViewCellDelegate <NSObject>

-(void) cancelSaveLastConnectedDevice:(DeviceTableViewCell*)cell;

@end

@interface DeviceTableViewCell : UITableViewCell
@property (weak, nonatomic) IBOutlet UIImageView *computerImageView;
@property (weak, nonatomic) IBOutlet UILabel *computerNameLable;
@property (weak, nonatomic) IBOutlet UIImageView *choosedImageView;
@property (weak, nonatomic) IBOutlet UILabel *lastConnectLable;
@property (weak, nonatomic) IBOutlet UILabel *lastConnectTime;
@property (weak, nonatomic) IBOutlet UIButton *cancelSaveButton;
@property (weak, nonatomic) IBOutlet UIView *disableCellView;
- (IBAction)canceSave:(id)sender;
@property (weak, nonatomic) DeviceInfo* device;
@property (assign, nonatomic) DeviceCellType cellType;

@property (weak, nonatomic) id<DeviceTableViewCellDelegate> delegate;
@end
