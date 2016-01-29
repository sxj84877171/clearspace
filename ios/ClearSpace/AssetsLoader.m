//
//  AssetsLoader.m
//  ClearSpace
//
//  Created by SW2 on 15/9/7.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "AssetsLoader.h"
#import <UIKit/UIKit.h>

@interface AssetsLoader()

@end

@implementation AssetsLoader
{
    int             _currentAssetCount;
    BOOL            _firstFetchAssets;
    
    NSMutableDictionary *_cacheAssetsDetailInfo;
    
    dispatch_queue_t _assets_get_queue;
}

-(id) init
{
    if((self = [super init]))
    {
        _currentAssetCount = 0;
        _firstFetchAssets = YES;
        _assets_get_queue = dispatch_queue_create("com.clearspace.assets.get.queue", nil);
        _cacheAssetsDetailInfo = [NSMutableDictionary dictionary];
    }
    
    [[PHPhotoLibrary sharedPhotoLibrary] registerChangeObserver:self];

    return  self;
}

-(void) dealloc{
    [[PHPhotoLibrary sharedPhotoLibrary] unregisterChangeObserver:self];
}

-(void) fetchAssetCompletely:(NSUInteger)totalAsset{
    if(self.delegate && _currentAssetCount == totalAsset){
        if([self.delegate respondsToSelector:@selector(assetsLoaderDelegateFinished:)]){
            [self.delegate assetsLoaderDelegateFinished:self];
        }
    }
}

//Fetch all assets, sorted by date created
-(void) fetchAllAssets{
    PHAuthorizationStatus authorizationStatus = [PHPhotoLibrary authorizationStatus];
    if(authorizationStatus == PHAuthorizationStatusRestricted || authorizationStatus == PHAuthorizationStatusDenied){
        NSDictionary *mainInfoDictionary = [[NSBundle mainBundle] localizedInfoDictionary];
        NSString *appName = [mainInfoDictionary objectForKey:@"CFBundleDisplayName"];
        NSString * tipTextWhenNoPhotosAuthorization = [NSString stringWithFormat:@"%@想访问您的照片", appName];
        
        UIAlertView* alertView = [[UIAlertView alloc] initWithTitle:nil message:tipTextWhenNoPhotosAuthorization delegate:nil cancelButtonTitle:@"确定" otherButtonTitles:nil, nil];
        
        [alertView show];
        return;
    }
    else if(authorizationStatus == PHAuthorizationStatusNotDetermined){
        return;
    }
    
    [self fetchAllAssetsInfo];
}

-(void) fetchAllAssetsInfo{
    @try {
        _firstFetchAssets = NO;
        
        if(_fetching){
            //上次扫描照片还没有结束，不重新开始，因为扫描是异步的，重新开始会导致数据重复
            return;
        }
        
        _currentAssetCount = 0;
        
        PHFetchOptions *options = [[PHFetchOptions alloc] init];
        options.sortDescriptors = @[[NSSortDescriptor sortDescriptorWithKey:@"creationDate" ascending:YES]];
        PHFetchResult* allPhotos = [PHAsset fetchAssetsWithOptions:options];
        
        if([self.delegate respondsToSelector:@selector(assetsLoaderDelegate:assetsCount:)]){
            [self.delegate assetsLoaderDelegate:self assetsCount:allPhotos.count];
        }
        
        if(allPhotos.count == 0){
            [self fetchAssetCompletely:allPhotos.count];
        }
        for(id obj in allPhotos){
            if([obj isKindOfClass:[PHAsset class]]){
                __block PHAsset *asset = (PHAsset*)obj;
                
                dispatch_async(_assets_get_queue, ^{
                    if(asset.mediaType == PHAssetMediaTypeImage){
                        [self fetchImageFromAsset:asset assetIndex:[allPhotos indexOfObject:asset] assetCount:allPhotos.count];
                    }
                    else if(asset.mediaType == PHAssetMediaTypeVideo){
                        [self fetchVedioFromAsset:asset assetIndex:[allPhotos indexOfObject:asset] assetCount:allPhotos.count];
                    }
                });
                
            }
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) fetchImageFromAsset:(PHAsset*)asset assetIndex:(NSInteger)index assetCount:(NSInteger)count{
    @autoreleasepool {
        AssetInfo *assetInfo = [[AssetInfo alloc] init];
        assetInfo.mediaType = PHAssetMediaTypeImage;
        assetInfo.asset = asset;
        
        __block BOOL bIsCache = YES;
        if(![self getCacheAssetInfo:asset assetInfo:assetInfo]){
            PHImageRequestOptions *imageRequestOptions = [[PHImageRequestOptions alloc] init];
            imageRequestOptions.synchronous = YES;
            [[PHImageManager defaultManager] requestImageDataForAsset:asset options:imageRequestOptions resultHandler:^(NSData *imageData, NSString *dataUTI, UIImageOrientation orientation, NSDictionary *info) {
                assetInfo.fileSize = imageData.length;
                if([info objectForKey:@"PHImageFileURLKey"]){
                    NSURL *path = [info objectForKey:@"PHImageFileURLKey"];
                    assetInfo.filePath = path.path;
                    // NSLog(@"path:%@", path.path);
                }
                bIsCache = NO;
                [self cacheAssetInfo:asset assetSize:assetInfo.fileSize assetPath:assetInfo.filePath];
            }];
        }
        
        _currentAssetCount++;
        
        if([self.delegate respondsToSelector:@selector(assetsLoaderDelegate:fetchAsset:index:cacheAsset:)]){
            [self.delegate assetsLoaderDelegate:self fetchAsset:assetInfo index:index cacheAsset:bIsCache];
        }
        
        [self fetchAssetCompletely:count];
        _fetching = _currentAssetCount != count;
    }
}

-(void) fetchVedioFromAsset:(PHAsset*)asset assetIndex:(NSInteger)index assetCount:(NSInteger)count{
    @autoreleasepool {
        [[PHImageManager defaultManager] requestAVAssetForVideo:asset options:nil resultHandler:^(AVAsset *avAsset, AVAudioMix *audioMix, NSDictionary *info) {
            
            if([avAsset isKindOfClass:[AVURLAsset class]]){
                AVURLAsset *urlAsset = (AVURLAsset*)avAsset;
                NSNumber *size;
                [urlAsset.URL getResourceValue:&size forKey:NSURLFileSizeKey error:nil];
                
                AssetInfo *assetInfo = [[AssetInfo alloc] init];
                assetInfo.mediaType = PHAssetMediaTypeVideo;
                assetInfo.fileSize = [size longLongValue];
                assetInfo.filePath = urlAsset.URL.path;
                assetInfo.data = nil;
                assetInfo.asset = asset;
                
                NSFileHandle *fileHandle = [NSFileHandle fileHandleForReadingAtPath:assetInfo.filePath];
                assetInfo.fileHandle = fileHandle;
                _currentAssetCount++;
                
                //NSLog(@"path:%@", urlAsset.URL);
                //NSData *data = [NSData dataWithContentsOfURL:urlAsset.URL];
                
                if([self.delegate respondsToSelector:@selector(assetsLoaderDelegate:fetchAsset:index:cacheAsset:)]){
                    [self.delegate assetsLoaderDelegate:self fetchAsset:assetInfo index:index cacheAsset:NO];
                }
                
                [self fetchAssetCompletely:count];
                _fetching = _currentAssetCount != count;
            }
        }];
    }
}

-(void) cacheAssetInfo:(PHAsset*)asset assetSize:(int64_t)size assetPath:(NSString*)path{
    @try {
        NSDictionary *infoDic = @{@"filesize":[NSNumber numberWithLongLong:size], @"filepath":path};
        [_cacheAssetsDetailInfo setObject:infoDic forKey:asset.localIdentifier];
    }
    @catch (NSException *exception) {
        
    }
}

-(BOOL)getCacheAssetInfo:(PHAsset*)asset assetInfo:(AssetInfo*)assetInfo{
    @try {
        NSDictionary *assetInfoDic = [_cacheAssetsDetailInfo objectForKey:asset.localIdentifier];
        if(assetInfoDic){
            NSNumber *fielsize = (NSNumber*)assetInfoDic[@"filesize"];
            assetInfo.fileSize = [fielsize longLongValue];
            assetInfo.filePath = assetInfoDic[@"filepath"];
            return YES;
        }
    }
    @catch (NSException *exception) {
        
    }
    return NO;
}

#pragma mark - PHPhotoLibraryChangeObserver

- (void)photoLibraryDidChange:(PHChange *)changeInstance
{
    // Call might come on any background queue. Re-dispatch to the main queue to handle it.
    dispatch_async(dispatch_get_main_queue(), ^{
        if(_firstFetchAssets){
            [self fetchAllAssetsInfo];
        }
    });
}
@end
