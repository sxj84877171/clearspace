//
//  CSArrangePhotosViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CSArrangePhotosViewController : UIViewController
@property (weak, nonatomic) IBOutlet UICollectionView *exportedCollectionView;
@property (weak, nonatomic) IBOutlet UICollectionView *unExportedCollectionView;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *unExportedCVHeightConstraint;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *exportedCVHeightConstraint;
@property (weak, nonatomic) IBOutlet UILabel *assetsTotalSizeLabel;
@property (weak, nonatomic) IBOutlet UILabel *assetsTotalSizeUnitLabel;
@property (weak, nonatomic) IBOutlet UILabel *assetsTotalCountLabel;
@property (weak, nonatomic) IBOutlet UILabel *noAssetsLabel;
@property (weak, nonatomic) IBOutlet UIButton *backButton;
- (IBAction)back:(id)sender;

@end
