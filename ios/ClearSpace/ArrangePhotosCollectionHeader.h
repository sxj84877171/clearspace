//
//  ArrangePhotosCollectionHeader.h
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ArrangePhotosCollectionHeader;
@protocol ArrangePhotosCollectionHeaderDelegate <NSObject>

-(void)clickHeader:(ArrangePhotosCollectionHeader*)header;

@end

@interface ArrangePhotosCollectionHeader : UICollectionReusableView

@property (nonatomic, weak) id<ArrangePhotosCollectionHeaderDelegate> delegate;
@property (nonatomic, weak) UICollectionView *collectionView;
@property (nonatomic) NSString* assetsSize;

@property (weak, nonatomic) IBOutlet UILabel *cleanSizeLable;
@property (weak, nonatomic) IBOutlet UILabel *title;

@end
