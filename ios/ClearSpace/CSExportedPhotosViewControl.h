//
//  CSExportedPhotosViewControl.h
//  ClearSpace
//
//  Created by SW2 on 15/11/26.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface CSExportedPhotosViewControl : UIViewController<UICollectionViewDelegate, UICollectionViewDataSource>
@property (weak, nonatomic) IBOutlet UILabel *descriptionLable;
@property (weak, nonatomic) IBOutlet UILabel *operateTipLable;
@property (weak, nonatomic) IBOutlet UIProgressView *progressView;
@property (weak, nonatomic) IBOutlet UICollectionView *collectionView;
@property (weak, nonatomic) IBOutlet UIButton *doButton;
- (IBAction)Do:(id)sender;

@end
