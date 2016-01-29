//
//  DeviceConnectTableViewCell.h
//  ClearSpace
//
//  Created by SW2 on 15/11/19.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@class DeviceConnectTableViewCell;

@protocol DeviceConnectTableViewCellDelegate <NSObject>

-(void)connectTimeout:(DeviceConnectTableViewCell*)cell;
-(void)connectTryAgain:(DeviceConnectTableViewCell*)cell;

@end

@interface DeviceConnectTableViewCell : UITableViewCell

@property (weak, nonatomic) id<DeviceConnectTableViewCellDelegate> delegate;

@property (weak, nonatomic) IBOutlet UIImageView *computerImageView;
@property (weak, nonatomic) IBOutlet UILabel *computerNameLable;
@property (weak, nonatomic) IBOutlet UILabel *connectErrorLable;
@property (weak, nonatomic) IBOutlet UIProgressView *connectProgressCtrl;
@property (weak, nonatomic) IBOutlet UIButton *tryAgainButton;
- (IBAction)tryAgain:(id)sender;
@end
