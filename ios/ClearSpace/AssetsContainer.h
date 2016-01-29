//
//  AssetsContainer.h
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
@class AssetInfo;

@interface AssetsContainer : NSObject

@property (nonatomic, setter=setAssets:) NSArray *assets;
@property (nonatomic, readonly) NSUInteger count;

-(void)setAssets:(NSArray *)assets;
-(void)addAsset:(AssetInfo*)asset;
-(void)removeAsset:(AssetInfo*)asset;
-(void)removeAllAssets;

-(AssetInfo*)assetSimilarTo:(AssetInfo*)asset;
@end
