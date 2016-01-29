//
//  AssetsManager.h
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "PublicStruct.h"
#import "AssetInfo.h"

@class AssetsManager;

@protocol AssetsManagerDelegate <NSObject>

@optional
-(void)assetsManagerDelegate:(AssetsManager*)assetsManager photoTotalSizeChanged:(int64_t)newSize;
-(void)assetsManagerDelegate:(AssetsManager *)assetsManager newExportedAsset:(AssetInfo*)asset;
-(void)assetsManagerDelegate:(AssetsManager *)assetsManager newUnExportedAsset:(AssetInfo*)asset;
-(void)assetsManagerDelegateFetchCompleted:(AssetsManager*)assetsManager;

@end


@interface AssetsManager : NSObject

+(instancetype) sharedInstance;

@property (nonatomic, weak) id<AssetsManagerDelegate> delegate;

@property (nonatomic, readonly) float assetsTotalSize;
@property (nonatomic, readonly) NSArray *fetchedAssets;

@property (nonatomic, readonly) NSArray *selectedAssets;        //当前选择的照片
@property (nonatomic, readonly) NSArray *currentExportedAssets; //当前刚导出的照片

/*获取本地照片*/
- (void)fetchAllAssets;                          //开始扫描获取手机里的照片
- (NSArray*) exportedAssets:(SortBy)sortBy;      //已导出过的照片
- (NSArray*) unExportedAssets:(SortBy)sortBy;    //未导出过的照片
- (void)loadExportedAssets;                     //从数据库读取已导出过的照片
- (void)removeUnExportedAssets:(AssetInfo*)asset;

- (NSArray*)assetsLessThanSize:(double)size;
- (float) sizeBeforeDate:(TimePoint)timePoint;
- (AssetInfo*)assetWithPath:(NSString*)path;
- (int64_t) exportedAssetsSize;
- (int64_t) unExportedAssetsSize;
- (NSSortDescriptor*) assetSortDesc:(SortBy)sortBy;

/*管理选择的照片*/
- (void)selectAsset:(AssetInfo *)asset;
- (void)deselectAsset:(AssetInfo *)asset;
- (void)deselectAllAssets;
- (BOOL)isAssetSelected:(AssetInfo *)asset;

/*管理刚导出的照片*/
- (void)addCurrentExportedAsset:(AssetInfo *)asset;
- (void)removeCurrentExportedAssets:(AssetInfo *)asset;
- (void)removeAllCurrentExportedAssets;
- (void)saveCurrentExportedAssets;
- (void)initCurrentExportedAssetsFromExportedAssets;
@end
