//
//  UnExportedPhotosCollectionHeader.h
//  ClearSpace
//
//  Created by SW2 on 15/12/4.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PublicStruct.h"
@class UnExportedPhotosCollectionHeader;
@protocol UnExportedPhotosCollectionHeaderDelegate <NSObject>

-(void)clickHeader:(UnExportedPhotosCollectionHeader*)header showCollection:(BOOL)show;
-(void)UnExportedPhotosCollectionHeader:(UnExportedPhotosCollectionHeader *)header sortBy:(SortBy)sortType;

@end
@interface UnExportedPhotosCollectionHeader : UICollectionReusableView

@property (weak, nonatomic) IBOutlet UILabel *cleanSizeLable;
@property (weak, nonatomic) IBOutlet UIButton *arrowButton;
@property (weak, nonatomic) IBOutlet UIButton *sortByButton;
@property (weak, nonatomic) IBOutlet UILabel *sortByLable;
@property (weak, nonatomic) IBOutlet UILabel *totalSize;
- (IBAction)sortBy:(id)sender;
@property (nonatomic, weak) id<UnExportedPhotosCollectionHeaderDelegate> delegate;

@property(nonatomic, weak) UICollectionView *collectionView;
@property (nonatomic) NSString* assetsSize;


@end
