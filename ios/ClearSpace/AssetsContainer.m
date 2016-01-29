//
//  AssetsContainer.m
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "AssetsContainer.h"
#import "AssetInfo.h"

@implementation AssetsContainer{
    NSMutableArray *_assets;
}

-(id) init{
    if(self = [super init]){
        _assets = [NSMutableArray new];
    }
    return self;
}

- (void)setAssets:(NSArray *)assets {
    @try {
        _assets = [NSMutableArray arrayWithArray:assets];
    }
    @catch (NSException *exception) {
        
    }
}

- (void)addAsset:(AssetInfo *)asset {
    @try {
        
        if (asset && ![self assetExists:asset]) {
            [_assets addObject:asset];
        }
    }
    @catch (NSException *exception) {
        
    }
}

- (void)removeAsset:(AssetInfo *)asset {
    @try {
        AssetInfo *assetToRemove = [self assetSimilarTo:asset];
        if (assetToRemove)
            [_assets removeObject:assetToRemove];
    }
    @catch (NSException *exception) {
        
    }
}

- (void)removeAllAssets {
    @try {
        [_assets removeAllObjects];
    }
    @catch (NSException *exception) {
        
    }
}

- (BOOL)assetExists:(AssetInfo *)asset {
    @try {
        return ([self assetSimilarTo:asset] != nil);
    }
    @catch (NSException *exception) {
        
    }
    return NO;
}

- (AssetInfo *)assetSimilarTo:(AssetInfo *)asset {
    AssetInfo *similarAsset = nil;
    
    @try {
        if(asset != nil){
            NSString *assetURL = asset.filePath;
            for (AssetInfo *a in _assets) {
                NSString *aURL = a.filePath;
                
                if ([assetURL isEqualToString:aURL]) {
                    similarAsset = a;
                    break;
                }
            }
        }
    }
    @catch (NSException *exception) {
        
    }
    return similarAsset;
}

-(NSUInteger)count{
    @try {
        return _assets.count;
    }
    @catch (NSException *exception) {
        
    }
    return 0;
}
@end
