//
//  CSExportedPhotosViewControl.m
//  ClearSpace
//
//  Created by SW2 on 15/11/26.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSExportedPhotosViewControl.h"
#import "CommonFunctions.h"
#import "PublicData.h"
#import "AssetInfo.h"
#import "AssetCell.h"
#import "AssetsManager.h"
#import "ExportedPhotosCollectionHeader.h"
#import "CSAssetViewController.h"
#import "UIButton+ImageWithColor.h"
#import "AssetsCollectionViewLayout.h"
#import "statistics/CSStatisticsFactory.h"
#import "CSConfig.h"

@interface CSExportedPhotosViewControl() <ExportedPhotosCollectionHeaderDelegate, UIGestureRecognizerDelegate>{
}
@end

@implementation CSExportedPhotosViewControl{
    NSMutableArray* _assetsArray;
    
    AssetsManager* _assetsManager;
    
    BOOL _haveSelectedCell;
    
    ExportedPhotosCollectionHeader *_header;
}


#pragma mark ViewControl delegate
- (void)viewDidLoad {
    [super viewDidLoad];
    
    _assetsManager = [AssetsManager sharedInstance];
    _assetsArray = [NSMutableArray arrayWithArray:_assetsManager.currentExportedAssets];
    
    [self newCollectionView];
}

-(void)viewWillAppear:(BOOL)animated{
    @try {
        [super viewWillAppear:animated];
        [self.navigationItem.backBarButtonItem setTitle:@""];
        [self.navigationController.navigationBar setBarTintColor:[UIColor ColorFromHex:0x33943c alpha:1.0]];
        _progressView.hidden = YES;
        
        CSConfig *config = [CSConfig shareInstance];
        if(config != nil){
            DeviceInfo* device = [config lastConnectDevice];
            self.descriptionLable.text = [NSString stringWithFormat:@"这些照片曾经导出到：%@ 的 桌面\\照片大挪移\\", device.deviceName];
        }
        
        if(_assetsManager){
            [_assetsManager deselectAllAssets];
        }
        [self selectAseetsStat];
        [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSExportedPhotosViewControl"];
    }
    @catch (NSException *exception) {
        
    }
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
}

-(void)viewWillDisappear:(BOOL)animated{
    @try {
        [super viewWillDisappear:animated];
        [self.navigationController.navigationBar setBarTintColor:[UIColor ColorFromHex:0x2C77D9 alpha:1.0]];
        [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSExportedPhotosViewControl"];
    }
    @catch (NSException *exception) {
        
    }
}

-(void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
}

#pragma mark - UI Event Action
- (IBAction)Do:(id)sender {
    if(!_haveSelectedCell){
        [self.navigationController popViewControllerAnimated:YES];
    }
    else{
        [self deleteAssets:_assetsManager.selectedAssets];
    }
}

#pragma mark - UICollectionViewDataSource

static NSString *cellID = nil;
-(void) newCollectionView{
    @try {
        self.collectionView.delegate = self;
        self.collectionView.dataSource = self;
        self.collectionView.collectionViewLayout = [self assetsCollectionViewLayoutForOrientation:[[UIApplication sharedApplication] statusBarOrientation]];
        cellID = NSStringFromClass([AssetCell class]);
        [self.collectionView registerClass:[AssetCell class] forCellWithReuseIdentifier:cellID];
        
        UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPress:)];
        longPress.delegate = self;
        longPress.delaysTouchesBegan = YES;
        [self.collectionView addGestureRecognizer:longPress];
    }
    @catch (NSException *exception) {
        
    }
}

-(NSInteger) collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    return _assetsArray.count;
}

-(UICollectionViewCell*) collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    AssetCell *cell = [collectionView dequeueReusableCellWithReuseIdentifier:cellID forIndexPath:indexPath];
    
    @try {
        NSInteger currentTag = cell.tag;
        
        AssetInfo *asset = _assetsArray[indexPath.row];
        cell.asset = asset;
        
        if(_assetsManager){
            [cell markAsSelected:[_assetsManager isAssetSelected:asset]];
        }
        
        CGFloat scale = [UIScreen mainScreen].scale;
        CGSize cellSize = ((UICollectionViewFlowLayout *)self.collectionView.collectionViewLayout).itemSize;
        CGSize assetGridThumbnailSize = CGSizeMake((cellSize.width - 1) * scale, (cellSize.height - 1) * scale);
        
        [[PHCachingImageManager defaultManager] requestImageForAsset:asset.asset targetSize:assetGridThumbnailSize contentMode:PHImageContentModeAspectFill options:nil resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
            if (cell.tag == currentTag) {
                cell.thumbnailImageView.image = result;
            }
        }];
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        return cell;
    }
    
}

-(UICollectionReusableView*) collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath{
    UICollectionReusableView *reusableView = nil;
    
    @try {
        if([kind isEqualToString: UICollectionElementKindSectionHeader]){
            
            _header = [collectionView dequeueReusableSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:@"clean_exported_header" forIndexPath:indexPath];
            _header.collectionView = _collectionView;
            _header.delegate = self;
            reusableView = _header;
        }
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        return reusableView;
    }
}

#pragma mark - UICollectionViewDelegate
-(BOOL)collectionView:(UICollectionView *)collectionView shouldSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    @try {
        
        AssetCell *cell = (AssetCell*)[collectionView cellForItemAtIndexPath:indexPath];
        
        BOOL shouldSelect = YES;
        if(cell.isCellSelected){
            shouldSelect = NO;
        }
        
        if(shouldSelect){
            [_assetsManager selectAsset:cell.asset];
        }
        else{
            [_assetsManager deselectAsset:cell.asset];
        }
        _haveSelectedCell = _assetsManager.selectedAssets.count > 0;
        if(_assetsManager.selectedAssets.count == 0){
            _header.selectType = Select_None;
        }
        else if(_assetsManager.selectedAssets.count == _assetsArray.count){
            _header.selectType = Select_All;
        }
        else{
            _header.selectType = Select_Part;
        }
        
        [cell markAsSelected:shouldSelect];
        [self selectAseetsStat];
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        return YES;
    }
    
}

#pragma mark - ExportedPhotosCollectionHeader Delegate
-(void) ExportedPhotosCollectionHeader:(ExportedPhotosCollectionHeader *)header didSelectAll:(BOOL)selectAll{
    @try {
        for(id obj in _assetsArray){
            if(selectAll){
                [_assetsManager selectAsset:(AssetInfo*)obj];
            }
            else{
                [_assetsManager deselectAsset:(AssetInfo*)obj];
            }
        }
        
        [self selectAseetsStat];
        [self.collectionView reloadData];
    }
    @catch (NSException *exception) {
        
    }
}

-(void) ExportedPhotosCollectionHeader:(ExportedPhotosCollectionHeader *)header sortBy:(SortBy)sortType{
    @try {
        NSSortDescriptor *sortRule = [_assetsManager assetSortDesc:sortType];
        
        if (sortRule) {
            [_assetsArray sortUsingDescriptors:@[sortRule]];
            [_collectionView reloadData];
        }
    }
    @catch (NSException *exception) {
        
    }
}

#pragma mark - UIGestureRecognizerDelegate
-(void) handleLongPress:(UILongPressGestureRecognizer*)gestureRecognizer{
    if(gestureRecognizer.state == UIGestureRecognizerStateBegan){
        CGPoint p = [gestureRecognizer locationInView:_collectionView];
        
        NSIndexPath *indexPath = [_collectionView indexPathForItemAtPoint:p];
        if(indexPath){
            AssetCell *cell = (AssetCell*)[_collectionView cellForItemAtIndexPath:indexPath];
            
            UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
            
            CSAssetViewController * assetVC = [storyboard instantiateViewControllerWithIdentifier:@"CSAssetViewController"];
            assetVC.asset = cell.asset;
            [self.navigationController pushViewController:assetVC animated:NO];
        }
    }
}

#pragma mark - update ui method
-(void) selectAseetsStat{
    @try {
        if(_assetsManager){
            NSString *selectCountText = @"选取需要删除的照片， 长按查看大图";
            if(_assetsManager.selectedAssets.count > 0){
                selectCountText = [NSString stringWithFormat:@"已选择%ld照片，长按查看大图", (unsigned long)_assetsManager.selectedAssets.count];
            }
            self.operateTipLable.text = selectCountText;
            
            int64_t fileSize = 0;
            for(id obj in _assetsManager.selectedAssets){
                AssetInfo* info = (AssetInfo*)obj;
                if(info){
                    fileSize += info.fileSize;
                }
            }
            if(fileSize == 0){
                _haveSelectedCell = NO;
                [self.doButton setTitle:@"返回" forState:UIControlStateNormal];
                [self.doButton setBackgroudColor:[UIColor ColorFromHex:0x2c77d9 alpha:1.0] forState:UIControlStateNormal];
                [self.doButton setBackgroudColor:[UIColor ColorFromHex:0x1e5cad alpha:1.0] forState:UIControlStateSelected];
            }
            else{
                NSString* title = [NSString stringWithFormat:@"删除(%@)", [CommonFunctions autoConvertFileSizeToString:fileSize]];
                [self.doButton setTitle:title forState:UIControlStateNormal];
                [self.doButton setBackgroudColor:[UIColor ColorFromHex:0xa00202 alpha:1.0] forState:UIControlStateNormal];
                [self.doButton setBackgroudColor:[UIColor ColorFromHex:0x6c0000 alpha:1.0] forState:UIControlStateSelected];
                [self.doButton setBackgroudColor:[UIColor ColorFromHex:0xa00202 alpha:0.7] forState:UIControlStateDisabled];
            }
        }
    }
    @catch (NSException *exception) {
        
    }
}

- (UICollectionViewLayout *)assetsCollectionViewLayoutForOrientation:(UIInterfaceOrientation)orientation {
    AssetsCollectionViewLayout *layout = [AssetsCollectionViewLayout new];
    if (UIInterfaceOrientationIsPortrait(orientation)) {
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
            [layout setItemInsets:UIEdgeInsetsMake(5.0f, 5.0f, 5.0f, 5.0f)];
            [layout setInternItemSpacingY:5.0f];
            [layout setNumberOfColumns:4];
        } else {
            [layout setItemInsets:UIEdgeInsetsMake(10.0f, 10.0f, 10.0f, 10.0f)];
            [layout setInternItemSpacingY:10.0f];
            [layout setNumberOfColumns:6];
        }
    }
    return layout;
}

#pragma mark - private method
- (void)deleteAssets:(NSArray*)assets
{
    @try {
        
        NSMutableArray *photoAssets = [[NSMutableArray alloc] init];
        for(id obj in assets){
            AssetInfo *info = (AssetInfo*)obj;
            [photoAssets addObject:info.asset];
        }
        
        void (^completionHandler)(BOOL, NSError *) = ^(BOOL success, NSError *error) {
            if (success) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    for(id obj in assets){
                        AssetInfo *info = (AssetInfo*)obj;
                        [_assetsArray removeObject:info];
                        [_assetsManager removeCurrentExportedAssets:info];
                    }
                    [_assetsManager deselectAllAssets];
                    [self selectAseetsStat];
                    [self.collectionView reloadData];
                    self.doButton.enabled = YES;
                });
            } else {
                NSLog(@"Error: %@", error);
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.doButton.enabled = YES;
                });
            }
        };
        
        
        // Delete asset from library
        self.doButton.enabled = NO;
        [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
            [PHAssetChangeRequest deleteAssets:photoAssets];
        } completionHandler:completionHandler];
    }
    @catch (NSException *exception) {
        
    }
}
@end
