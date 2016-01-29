//
//  AssetCell.h
//  ClearSpace
//
//  Created by SW2 on 15/11/26.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@class AssetInfo;

@interface AssetCell : UICollectionViewCell

@property (nonatomic) UIImageView *thumbnailImageView;
@property (nonatomic) UIImageView *movieMarkImageView;
@property (nonatomic) UIImageView *selectFlagImageView;
@property (nonatomic, readonly, getter=isCellSelected) BOOL cellSelected;
@property (nonatomic) AssetInfo* asset;

-(void)markAsSelected:(BOOL)selected;


#pragma mark - Customization
+(CGSize) preferedCellSize;
+(CGRect) preferedThumbnailRect;
+(CGRect) preferedMovieMarkRect;
+(UIImage *) preferedMovieMarkImage;
+(UIColor *) preferedBackgroundColorForStateNormal;
+(UIColor *) preferedBackgroundColorForStateSelected;
@end
