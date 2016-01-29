//
//  AssetsDBManager.h
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseDBManager.h"

@class AssetInfo;
@interface AssetsDBManager : BaseDBManager

+(instancetype) sharedInstance;

-(void) initDB;
-(void) recordExportedAsset:(AssetInfo*)asset;
-(void) removeExportedAsset:(AssetInfo*)asset;
-(NSArray*) loadExportedAssets;
-(BOOL) isAssetExist:(AssetInfo*)asset;

@end
