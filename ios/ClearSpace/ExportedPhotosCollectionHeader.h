//
//  ExportedPhotosCollectionHeader.h
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "PublicStruct.h"

typedef NS_ENUM(NSInteger, SelectType){
    Select_None,
    Select_Part,
    Select_All
};

@class ExportedPhotosCollectionHeader;
@protocol ExportedPhotosCollectionHeaderDelegate <NSObject>

-(void)ExportedPhotosCollectionHeader:(ExportedPhotosCollectionHeader*)header didSelectAll:(BOOL)selectAll;
-(void)ExportedPhotosCollectionHeader:(ExportedPhotosCollectionHeader *)header sortBy:(SortBy)sortType;

@end

@interface ExportedPhotosCollectionHeader : UICollectionReusableView

@property (weak, nonatomic) IBOutlet UIButton *selectAllButton;
@property (weak, nonatomic) IBOutlet UILabel *selectAllLable;
@property (weak, nonatomic) IBOutlet UIButton *sortbyButton;
@property (weak, nonatomic) IBOutlet UILabel *sortbyLable;
- (IBAction)selectAll:(id)sender;
- (IBAction)sortBy:(id)sender;

@property(nonatomic, weak) UICollectionView *collectionView;
@property(nonatomic, setter = setSelectType:) SelectType selectType;

@property (nonatomic, weak) id<ExportedPhotosCollectionHeaderDelegate> delegate;
@end
