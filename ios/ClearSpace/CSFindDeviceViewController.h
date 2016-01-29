//
//  CSFindDeviceViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/9/12.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "DevicePaired.h"
@class YLImageView;
@interface CSFindDeviceViewController : UIViewController<UITableViewDataSource, UITableViewDelegate, DevicePairedDelegate>
@property (weak, nonatomic) IBOutlet UILabel *searchingDeviceLable;
@property (weak, nonatomic) IBOutlet UITableView *lastConnectedDeviceTableView;
@property (weak, nonatomic) IBOutlet UITableView *nearbyDeviceTableView;
@property (weak, nonatomic) IBOutlet UIButton *beginButton;
@property (weak, nonatomic) IBOutlet UIView *findDeviceTipView;
@property (weak, nonatomic) IBOutlet UILabel *currentWifi;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *findDeviceTipViewTopConstraint;
@property (weak, nonatomic) IBOutlet YLImageView *findPCGifImageView;
@property (weak, nonatomic) IBOutlet YLImageView *findPCTipGifImageView;
@property (weak, nonatomic) IBOutlet UIView *lastConnectDeviceView;
@property (weak, nonatomic) IBOutlet UIView *nearbyDeviceView;
@property (weak, nonatomic) IBOutlet UIView *showNearbyDeviceButton;
@property (weak, nonatomic) IBOutlet UIImageView *nearbyDeviceArrowImageView;
@property (weak, nonatomic) IBOutlet UIButton *tryArrangePhotsButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *lastConnectDeviceViewHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *findPCTipGifImageViewHeightConstraint;
- (IBAction)BeginExportPhotos:(id)sender;
- (IBAction)showFindDeviceGuideView:(id)sender;
- (IBAction)tryArrangePhotos:(id)sender;

@end
