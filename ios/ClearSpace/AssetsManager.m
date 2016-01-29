//
//  AssetsManager.m
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "AssetsManager.h"
#import "AssetInfo.h"
#import "AssetsLoader.h"
#import "AssetsContainer.h"
#import "AssetsDBManager.h"

@interface AssetsManager() <AssetsLoaderDelegate>

@end

@implementation AssetsManager{
    AssetsLoader *_assetsLoader;
    
    NSMutableDictionary *_assetInfoDic; //key:asset path  value:AssetInfo
    NSMutableArray *_assetPathArray; //sort by create date;
    
    AssetsContainer *_exportedAssetsContainer;      //已导出过的照片
    AssetsContainer *_unExportedAssetsContainers;   //未导出过的照片
    
    AssetsContainer *_selectedAssetsContainer;
    AssetsContainer *_currentExportedAssetsContainer;
    
    AssetsDBManager *_assetsDBManager;
    
    NSMutableArray *_exportedAssetsPath;//已导出过的照片的路径，存储在数据库

}

-(id) init{
    if(self = [super init]){
        _assetsLoader = [AssetsLoader new];
        _assetsLoader.delegate = self;
        
        _exportedAssetsContainer = [AssetsContainer new];
        _unExportedAssetsContainers = [AssetsContainer new];
        _selectedAssetsContainer = [AssetsContainer new];
        _currentExportedAssetsContainer = [AssetsContainer new];
        
        _assetsDBManager = [AssetsDBManager sharedInstance];
        [_assetsDBManager initDB];
    }
    return self;
}

+(instancetype) sharedInstance{
    static dispatch_once_t once;
    
    static AssetsManager* assetsManager = nil;
    
    dispatch_once(&once, ^{
        assetsManager = [AssetsManager new];
    });
    
    return assetsManager;
}

#pragma mark - fetch assets
-(void) fetchAllAssets{
    @try {
        if(!_assetsLoader.fetching){
            _assetInfoDic = [NSMutableDictionary dictionary];
            _assetPathArray = [NSMutableArray array];
            [_exportedAssetsContainer removeAllAssets];
            [_unExportedAssetsContainers removeAllAssets];
            _assetsTotalSize = 0;
            [self loadExportedAssets];
            
            [_assetsLoader fetchAllAssets];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(NSArray*)exportedAssets:(SortBy)sortBy{
    @try {
        NSArray *array = _exportedAssetsContainer.assets;
        
        if(sortBy == SortBy_None){
            return [NSArray arrayWithArray:array];
        }
        
        NSSortDescriptor *sortRule = [self assetSortDesc:sortBy];
        return [array sortedArrayUsingDescriptors:@[sortRule]];
    }
    @catch (NSException *exception) {
        
    }
}

-(NSArray*)unExportedAssets:(SortBy)sortBy{
    @try {
        NSArray *array = _unExportedAssetsContainers.assets;
        if(sortBy == SortBy_None){
            return [NSArray arrayWithArray:array];
        }
        
        NSSortDescriptor *sortRule = [self assetSortDesc:sortBy];
        return [array sortedArrayUsingDescriptors:@[sortRule]];
    }
    @catch (NSException *exception) {
        
    }
}

-(void)loadExportedAssets{
    @try {
        static BOOL haveLoad = NO;
        if(!haveLoad){
            _exportedAssetsPath = [NSMutableArray arrayWithArray:[_assetsDBManager loadExportedAssets]];
        }
        haveLoad = YES;
    }
    @catch (NSException *exception) {
        
    }
}

-(void)removeUnExportedAssets:(AssetInfo *)asset{
    [_unExportedAssetsContainers removeAsset:asset];
}

-(NSArray*) assetsLessThanSize:(double)size{
    NSMutableArray *array = [[NSMutableArray alloc] init];
    
    double totleSize = 0.0;
    for(id obj in _assetPathArray){
        AssetInfo *info = [_assetInfoDic objectForKey:obj];
        if(info){
            [array addObject:info];
            if((totleSize + info.fileSize) >= size){
                break;
            }
        }
    }
    
    return array;
}

-(float)sizeBeforeDate:(TimePoint)timePoint{
    NSDate *now = [NSDate date];
    NSTimeInterval before = 0;
    float size = 0.0;
    switch (timePoint) {
        case BEFORE_ONEYEAR:
            before = 365 * 24 * 60 * 60;
            break;
        case BEFORE_HALFYEAR:
            before = 365 * 24 * 60 * 60 / 2;
            break;
        case BEFORE_THREEMONTH:
            before = 90 * 24 * 60 * 60;
            break;
        case BEFORE_ONEMONTH:
            before = 30 * 24 * 60 * 60;
            break;
        case ALLTIME:
            size = _assetsTotalSize;
            return size;
        default:
            break;
    }
    
    NSDate *date = [now dateByAddingTimeInterval:-before];
    
    for(id obj in _assetPathArray){
        AssetInfo *info = [_assetInfoDic objectForKey:obj];
        if(info != nil && info.asset != nil && [info.asset.creationDate timeIntervalSinceDate:date] <= 0){
            size += info.fileSize;
        }
        else{
            break;
        }
    }
    
    return size;
}

-(AssetInfo*)assetWithPath:(NSString *)path{
    return [_assetInfoDic objectForKey:path];
}

-(int64_t)exportedAssetsSize{
    int64_t size = 0;
    
    for(id obj in _exportedAssetsContainer.assets){
        AssetInfo *asset = (AssetInfo*)obj;
        if(asset){
            size += asset.fileSize;
        }
    }
    
    return size;
}

-(int64_t)unExportedAssetsSize{
    int64_t size = 0;
    for(id obj in _unExportedAssetsContainers.assets){
        AssetInfo *asset = (AssetInfo*)obj;
        if(asset){
            size += asset.fileSize;
        }
    }
    
    return size;
}

#pragma mark - manager select assets
- (void)selectAsset:(AssetInfo *)asset {
    [_selectedAssetsContainer addAsset:asset];
}

- (void)deselectAsset:(AssetInfo *)asset {
    [_selectedAssetsContainer removeAsset:asset];
}

- (void)deselectAllAssets {
    [_selectedAssetsContainer removeAllAssets];
}

- (BOOL)isAssetSelected:(AssetInfo *)asset {
    return ([_selectedAssetsContainer assetSimilarTo:asset] != nil);
}

#pragma mark - manager export assets
-(void)addCurrentExportedAsset:(AssetInfo *)asset{
    [_currentExportedAssetsContainer addAsset:asset];
}

-(void)removeCurrentExportedAssets:(AssetInfo *)asset{
    [_currentExportedAssetsContainer removeAsset:asset];
    [_exportedAssetsContainer removeAsset:asset];
}

-(void)removeAllCurrentExportedAssets{
    [_currentExportedAssetsContainer removeAllAssets];
}

-(void)saveCurrentExportedAssets{
    for(id obj in self.currentExportedAssets){
        AssetInfo *info = (AssetInfo*)obj;
        if(![self isAssetExported:info.filePath]){
            [_assetsDBManager recordExportedAsset:info];
            [_exportedAssetsPath addObject:info.filePath];
        }
    }
}

-(void)initCurrentExportedAssetsFromExportedAssets{
    _currentExportedAssetsContainer.assets = _exportedAssetsContainer.assets;
}

#pragma mark - Accessors
-(NSArray*)selectedAssets{
    return _selectedAssetsContainer.assets;
}

-(NSArray*)currentExportedAssets{
    return _currentExportedAssetsContainer.assets;
}

#pragma mark - assetsLoader delegate
-(void)assetsLoaderDelegate:(AssetsLoader *)loader assetsCount:(NSUInteger)count{
    dispatch_async(dispatch_get_main_queue(), ^{
        _assetPathArray = [NSMutableArray new];
        for(int i=0; i < count; i++){
            [_assetPathArray addObject:@""];
        }
    });
}

-(void)assetsLoaderDelegate:(AssetsLoader *)loader fetchAsset:(AssetInfo *)asset index:(NSInteger)index cacheAsset:(BOOL)isCache{
    dispatch_async(dispatch_get_main_queue(), ^{
        if(index >=0 && index <_assetPathArray.count){
            [_assetInfoDic setObject:asset forKey:asset.filePath];
            [_assetPathArray replaceObjectAtIndex:index withObject:asset.filePath];
            _assetsTotalSize += asset.fileSize;
            
            if([self isAssetExported:asset.filePath]){
                [_exportedAssetsContainer addAsset:asset];
                if([self.delegate respondsToSelector:@selector(assetsManagerDelegate:newExportedAsset:)]){
                    [self.delegate assetsManagerDelegate:self newExportedAsset:asset];
                }
            }
            else{
                [_unExportedAssetsContainers addAsset:asset];
                if([self.delegate respondsToSelector:@selector(assetsManagerDelegate:newUnExportedAsset:)]){
                    [self.delegate assetsManagerDelegate:self newUnExportedAsset:asset];
                }
            }
            
            if(!isCache){
                //减少刷新UI的次数，提高性能，再loader finished时校正total size
                if([self.delegate respondsToSelector:@selector(assetsManagerDelegate:photoTotalSizeChanged:)]){
                    [self.delegate assetsManagerDelegate:self photoTotalSizeChanged:_assetsTotalSize];
                }
            }
        }
    });
}

-(void)assetsLoaderDelegateFinished:(AssetsLoader*)loader{
    dispatch_async(dispatch_get_main_queue(), ^{
        if([self.delegate respondsToSelector:@selector(assetsManagerDelegate:photoTotalSizeChanged:)]){
            [self.delegate assetsManagerDelegate:self photoTotalSizeChanged:_assetsTotalSize];
        }
    });
}

#pragma mark - private method
-(BOOL) isAssetExported:(NSString*)assetPath{
    for(id obj in _exportedAssetsPath){
        NSString *path = (NSString*)obj;
        if([path isEqualToString:assetPath]){
            return YES;
        }
    }
    
    return NO;
}

-(NSSortDescriptor*) assetSortDesc:(SortBy)sortBy{
    NSSortDescriptor *sortDes = nil;
    if(sortBy == SortBy_TimeAsc || sortBy == SortBy_TimeDesc){
        sortDes = [NSSortDescriptor sortDescriptorWithKey:@"asset.creationDate" ascending:sortBy == SortBy_TimeAsc];
    }
    else if(sortBy == SortBy_SizeAsc || sortBy == SortBy_SizeDesc){
        sortDes = [NSSortDescriptor sortDescriptorWithKey:@"fileSize" ascending:sortBy == SortBy_SizeAsc];

    }
    
    return sortDes;
}
@end
