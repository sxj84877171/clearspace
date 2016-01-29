//
//  CSExportPhotosViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "CircularProgressView.h"

@interface CSExportPhotosViewController : UIViewController
@property (weak, nonatomic) IBOutlet UILabel *freeSpaceLable;
@property (weak, nonatomic) IBOutlet UILabel *freeSpaceDecimalLable;
@property (weak, nonatomic) IBOutlet UILabel *freeSpaceUnitLable;
@property (weak, nonatomic) IBOutlet CircularProgressView *progressView;
@property (weak, nonatomic) IBOutlet UIImageView *progressRevolveImageView;
@property (weak, nonatomic) IBOutlet UILabel *currentProcessImagePath;
@property (weak, nonatomic) IBOutlet UILabel *exportImageCount;
@property (weak, nonatomic) IBOutlet UILabel *pcNameLable;
@property (weak, nonatomic) IBOutlet UIView *progressBackgroudView;
@property (weak, nonatomic) IBOutlet UIButton *cancelButton;
- (IBAction)cancelExport:(id)sender;

@end
