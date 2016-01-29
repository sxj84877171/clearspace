//
//  CSUnExportedPhotosViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/12/3.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CSUnExportedPhotosViewController : UIViewController

@property (weak, nonatomic) IBOutlet UICollectionView *albumCollectionView;
@property (weak, nonatomic) IBOutlet UICollectionView *similarPhotosCollectionView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *albumCVHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *similarPhotosCVHeightConstraint;
@property (weak, nonatomic) IBOutlet UILabel *assetsTotalSizeLabel;
@property (weak, nonatomic) IBOutlet UILabel *assetsTotalSizeUnitLabel;
@property (weak, nonatomic) IBOutlet UILabel *assetsTotalCountLabel;
@property (weak, nonatomic) IBOutlet UILabel *operateTipLable;
@property (weak, nonatomic) IBOutlet UIButton *backButton;
- (IBAction)back:(id)sender;

@end
