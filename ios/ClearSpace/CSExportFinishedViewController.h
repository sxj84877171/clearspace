//
//  CSExportFinishedViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/9/17.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CSExportFinishedViewController : UIViewController
@property (weak, nonatomic) IBOutlet UILabel *haveExportCount;
@property (weak, nonatomic) IBOutlet UILabel *freeSpaceSize;
@property (weak, nonatomic) IBOutlet UILabel *leftSpaceSize;
@property (weak, nonatomic) IBOutlet UIView *deleteGuideView;
@property (weak, nonatomic) IBOutlet UILabel *deleteGuideLable;
@property (weak, nonatomic) IBOutlet UIButton *showGuideButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *deleteGuideViewHeightConstraint;
@property (weak, nonatomic) IBOutlet UIButton *completeButton;
@property (weak, nonatomic) IBOutlet UIButton *deleteButton;
@property (weak, nonatomic) IBOutlet UIButton *showButton;
@property (weak, nonatomic) IBOutlet UILabel *pcNameLable;
@property (weak, nonatomic) IBOutlet UILabel *historyExportedCountLabel;
@property (weak, nonatomic) IBOutlet UILabel *exportedCountRanking;
@property (weak, nonatomic) IBOutlet UILabel *historyExportedSizeLabel;
@property (weak, nonatomic) IBOutlet UILabel *exportedSizeRanking;
@property (weak, nonatomic) IBOutlet UILabel *exportAverageSpeedLable;
@property (weak, nonatomic) IBOutlet UILabel *exportAverageSpeedUnitLable;
@property (weak, nonatomic) IBOutlet UILabel *exportTitleLevel;
@property (weak, nonatomic) IBOutlet UILabel *exportSpeedCompared;
@property (weak, nonatomic) IBOutlet UIImageView *titleImage;
- (IBAction)ExportFinished:(id)sender;
- (IBAction)showDeleteGuideView:(id)sender;
- (IBAction)deleteExportedAssets:(id)sender;

@end
