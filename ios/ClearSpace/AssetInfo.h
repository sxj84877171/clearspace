//
//  AssetInfo.h
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>
@import Photos;

@interface AssetInfo : NSObject

@property (nonatomic, assign) PHAssetMediaType mediaType;
@property (nonatomic, assign) int64_t fileSize;
@property (nonatomic, strong) NSString* filePath;
@property (nonatomic, strong) NSData* data;
@property (nonatomic, strong) PHAsset* asset;
@property (nonatomic, strong) NSFileHandle *fileHandle;

@property (nonatomic, readonly, getter=fileName) NSString* fileName;
@end
