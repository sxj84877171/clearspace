//
//  AssetsLoader.h
//  ClearSpace
//
//  Created by SW2 on 15/9/7.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AssetInfo.h"

@class AssetsLoader;
@protocol AssetsLoaderDelegate <NSObject>

-(void)assetsLoaderDelegate:(AssetsLoader *)loader assetsCount:(NSUInteger)count;
-(void)assetsLoaderDelegate:(AssetsLoader *)loader fetchAsset:(AssetInfo*)asset index:(NSInteger)index cacheAsset:(BOOL)isCache;
-(void)assetsLoaderDelegateFinished:(AssetsLoader*)loader;
@end


@interface AssetsLoader : NSObject<PHPhotoLibraryChangeObserver>

@property(nonatomic, weak) id<AssetsLoaderDelegate> delegate;
@property(nonatomic, readonly) BOOL fetching;

/**
 * Begin scan all assets(photo, video) in album.
 **/
-(void)fetchAllAssets;
@end
