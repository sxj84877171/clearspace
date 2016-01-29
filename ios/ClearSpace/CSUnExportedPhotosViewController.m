//
//  CSUnExportedPhotosViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/12/3.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSUnExportedPhotosViewController.h"
#import "UIButton+ImageWithColor.h"
#import "AssetsManager.h"
#import "CommonFunctions.h"
#import "AssetCell.h"
#import "AssetsCollectionViewLayout.h"
#import "UnExportedPhotosCollectionHeader.h"
#import "statistics/CSStatisticsFactory.h"
#import "CSAssetViewController.h"

@interface CSUnExportedPhotosViewController () <UICollectionViewDataSource, UICollectionViewDelegate, UnExportedPhotosCollectionHeaderDelegate, UIGestureRecognizerDelegate>

@end

@implementation CSUnExportedPhotosViewController
{
    AssetsManager *_assetsManager;
    
    NSMutableArray *_unExportedAssetArray;
    UnExportedPhotosCollectionHeader *_similarCollectionHeader;
    UnExportedPhotosCollectionHeader *_albumCollectionHeader;
    
    BOOL _showAlbumCollectionView;
    BOOL _showSimilarPhotoCollectionView;
    BOOL _haveSelectedCell;
    
    NSInteger _collectionViewHeaderHeight;
    NSInteger _collectionContainerViewHeight;
    CGSize _cellSize;
}

#pragma mark ViewControl delegate
- (void)viewDidLoad {
    [super viewDidLoad];
    
    _assetsManager = [AssetsManager sharedInstance];
    
    [self lableGradientLayer:_assetsTotalCountLabel];
    [self lableGradientLayer:_assetsTotalSizeLabel];
    [self lableGradientLayer:_assetsTotalSizeUnitLabel];
    
    _collectionViewHeaderHeight = 36;
    [self newCollectionView:_albumCollectionView];
    //[self newCollectionView:_similarPhotosCollectionView];
    
    _similarPhotosCollectionView.hidden = YES;
    _similarPhotosCVHeightConstraint.constant = 0;
    _albumCVHeightConstraint.constant = 0;
    
    [self assetsStat];
}

-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    [self.navigationItem.backBarButtonItem setTitle:@""];
    
    if(_assetsManager){
        [_assetsManager deselectAllAssets];
    }
    
    [self selectAseetsStat];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSUnExportedPhotosViewController"];
}

-(void)viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    _collectionContainerViewHeight = [_similarPhotosCollectionView superview].frame.size.height;
    _albumCVHeightConstraint.constant = _collectionViewHeaderHeight;
    if(_assetsManager){
        [_assetsManager deselectAllAssets];
        _unExportedAssetArray = [NSMutableArray arrayWithArray:[_assetsManager unExportedAssets:SortBy_TimeAsc]];
        [self assetsStat];
        [_albumCollectionView reloadData];
    }
}

-(void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSUnExportedPhotosViewController"];
}

-(void)viewDidDisappear:(BOOL)animated{
    [super viewDidDisappear:animated];
}

- (IBAction)back:(id)sender {
    if(!_haveSelectedCell){
        [self.navigationController popViewControllerAnimated:YES];
    }
    else{
        [self deleteAssets:_assetsManager.selectedAssets];
    }
}

static NSString *cellID = nil;
-(void) newCollectionView:(UICollectionView*)collectionView{
    @try {
        collectionView.delegate = self;
        collectionView.dataSource = self;
        collectionView.collectionViewLayout = [self assetsCollectionViewLayoutForOrientation:[[UIApplication sharedApplication] statusBarOrientation]];
        cellID = NSStringFromClass([AssetCell class]);
        [collectionView registerClass:[AssetCell class] forCellWithReuseIdentifier:cellID];
        
        UILongPressGestureRecognizer *longPress = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(handleLongPress:)];
        longPress.delegate = self;
        longPress.delaysTouchesBegan = YES;
        [collectionView addGestureRecognizer:longPress];
    }
    @catch (NSException *exception) {
        
    }
}

- (UICollectionViewLayout *)assetsCollectionViewLayoutForOrientation:(UIInterfaceOrientation)orientation {
    AssetsCollectionViewLayout *layout = [AssetsCollectionViewLayout new];
    if (UIInterfaceOrientationIsPortrait(orientation)) {
        NSInteger columns = 4;
        UIEdgeInsets inset = UIEdgeInsetsMake(5.0f, 5.0f, 5.0f, 5.0f);;
        CGFloat internItemSpacingX = 5.0;
        CGFloat internItemSpacingY = 5.0;
        if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
        }
        else {
            inset = UIEdgeInsetsMake(10.0f, 10.0f, 10.0f, 10.0f);
            internItemSpacingY = 10.0;
            columns = 6;
        }
        [layout setItemInsets:inset];
        [layout setNumberOfColumns:columns];
        [layout setSupplementaryViewHeight:_collectionViewHeaderHeight];
        [layout setInternItemSpacingX:internItemSpacingX];
        [layout setInternItemSpacingY:internItemSpacingY];
        
        CGFloat viewWidth = [UIScreen mainScreen].bounds.size.width - inset.left - inset.right - (internItemSpacingX - 1) * columns;
        _cellSize.height = _cellSize.width = viewWidth / columns;
    }
    return layout;
}

#pragma mark - UICollectionViewDataSource
-(NSInteger) numberOfSectionsInCollectionView:(UICollectionView *)collectionView{
    return 1;
}

-(NSInteger) collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    NSInteger items = 0;
    if(collectionView == _similarPhotosCollectionView){
        ;
    }
    else if(collectionView == _albumCollectionView){
        items = _showAlbumCollectionView ? _unExportedAssetArray.count : 0;
    }
    
    return items;
}

-(UICollectionViewCell*)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    AssetCell * cell = [collectionView dequeueReusableCellWithReuseIdentifier:cellID forIndexPath:indexPath];
    
    @try {
        NSInteger currentTag = cell.tag;
        
        AssetInfo* asset = nil;
        CGSize cellSize;
        if(collectionView == _similarPhotosCollectionView){
        }
        else if(collectionView == _albumCollectionView){
            asset = (AssetInfo*)_unExportedAssetArray[indexPath.row];
            cellSize = ((UICollectionViewFlowLayout *)_albumCollectionView.collectionViewLayout).itemSize;
        }
        
        CGFloat scale = [UIScreen mainScreen].scale;
        CGSize assetGridThumbnailSize = CGSizeMake((cellSize.width - 1) * scale, (cellSize.height - 1) * scale);
        
        [[PHCachingImageManager defaultManager] requestImageForAsset:asset.asset targetSize:assetGridThumbnailSize contentMode:PHImageContentModeAspectFill options:nil resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
            if (cell.tag == currentTag) {
                cell.thumbnailImageView.image = result;
            }
        }];
        
        cell.asset = asset;
        if(_assetsManager){
            [cell markAsSelected:[_assetsManager isAssetSelected:asset]];
        }
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        return cell;
    }
}

-(UICollectionReusableView*)collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath{
    UICollectionReusableView *reusableView = nil;
    @try {
        
        if([kind isEqualToString:UICollectionElementKindSectionHeader]){
            UnExportedPhotosCollectionHeader *header = [collectionView dequeueReusableSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:@"UnExportedPhotosCollectionHeader" forIndexPath:indexPath];
            
            header.delegate = self;
            reusableView = header;
            
            int64_t assetSize = 0;
            if(collectionView == _similarPhotosCollectionView){
                _similarCollectionHeader = header;
                assetSize = [_assetsManager exportedAssetsSize];
                header.collectionView = _similarPhotosCollectionView;
            }
            else if(collectionView == _albumCollectionView){
                _albumCollectionHeader = header;
                assetSize = [_assetsManager unExportedAssetsSize];
                header.collectionView = _albumCollectionView;
            }
            header.assetsSize = [CommonFunctions autoConvertFileSizeToString:assetSize];
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
        
        [cell markAsSelected:shouldSelect];
        [self selectAseetsStat];
    }
    @catch (NSException *exception) {
        
    }
    @finally {
        return YES;
    }
    
}

#pragma mark - UnExportedPhotosCollectionHeaderDelegate
-(void)clickHeader:(UnExportedPhotosCollectionHeader *)header showCollection:(BOOL)show{
    @try {
        
        if(header == _similarCollectionHeader){
            _showSimilarPhotoCollectionView = show;
            [_similarPhotosCollectionView reloadData];
        }
        else if(header == _albumCollectionHeader){
            _showAlbumCollectionView = show;
            [self resizeCollectionViewSize:show];
            [_albumCollectionView reloadData];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)UnExportedPhotosCollectionHeader:(UnExportedPhotosCollectionHeader *)header sortBy:(SortBy)sortType{
    NSSortDescriptor *sortRule = [_assetsManager assetSortDesc:sortType];
    
    if (sortRule) {
        [_unExportedAssetArray sortUsingDescriptors:@[sortRule]];
        [_albumCollectionView reloadData];
    }
}

#pragma mark - UIGestureRecognizerDelegate
-(void) handleLongPress:(UILongPressGestureRecognizer*)gestureRecognizer{
    if(gestureRecognizer.state == UIGestureRecognizerStateBegan){
        CGPoint p = [gestureRecognizer locationInView:_albumCollectionView];
        
        NSIndexPath *indexPath = [_albumCollectionView indexPathForItemAtPoint:p];
        if(indexPath){
            AssetCell *cell = (AssetCell*)[_albumCollectionView cellForItemAtIndexPath:indexPath];
            
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
                [self.backButton setTitle:@"返回" forState:UIControlStateNormal];
                [self.backButton setBackgroudColor:[UIColor ColorFromHex:0x2c77d9 alpha:1.0] forState:UIControlStateNormal];
                [self.backButton setBackgroudColor:[UIColor ColorFromHex:0x1e5cad alpha:1.0] forState:UIControlStateSelected];
                [self.backButton setBackgroudColor:[UIColor ColorFromHex:0x2c77d9 alpha:0.7] forState:UIControlStateDisabled];
            }
            else{
                NSString* size = nil;
                NSString* unit = nil;
                NSString* title = [NSString stringWithFormat:@"删除(%@)", [CommonFunctions autoConvertFileSizeToString:fileSize]];
                [self.backButton setTitle:title forState:UIControlStateNormal];
                [self.backButton setBackgroudColor:[UIColor ColorFromHex:0xa00202 alpha:1.0] forState:UIControlStateNormal];
                [self.backButton setBackgroudColor:[UIColor ColorFromHex:0x6c0000 alpha:1.0] forState:UIControlStateSelected];
                [self.backButton setBackgroudColor:[UIColor ColorFromHex:0xA00202 alpha:0.7] forState:UIControlStateDisabled];
            }
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) lableGradientLayer:(UILabel*)lable{
    lable.textColor = [UIColor gradientColor:@[[UIColor ColorFromHex:0xffffff alpha:1], [UIColor ColorFromHex:0xadd1ff alpha:1]] locations:nil frameHeight:lable.bounds.size.height];
    
}

-(CGFloat) unExportedAssetsTotolSize{
    CGFloat size = 0.0;
    for(id obj in _unExportedAssetArray){
        AssetInfo *asset = (AssetInfo*)obj;
        if(asset){
            size += asset.fileSize;
        }
    }
    return size;
}

-(void)assetsStat{
    CGFloat totalSize = [self unExportedAssetsTotolSize];
    NSDictionary *dic = [CommonFunctions autoConvertFileSize:totalSize];
    if(dic){
        NSNumber *number = [dic objectForKey:@"size"];
        NSString *unit = [dic objectForKey:@"unit"];
        if(number){
            _assetsTotalSizeLabel.text = [NSString stringWithFormat:@"%.1f", [number floatValue]];
            _assetsTotalSizeUnitLabel.text = unit;
        }
    }
    _assetsTotalCountLabel.text = [NSString stringWithFormat:@"共%lu张", (unsigned long)_unExportedAssetArray.count];
}

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
                        [_unExportedAssetArray removeObject:info];
                        [_assetsManager removeUnExportedAssets:info];
                    }
                    [_assetsManager deselectAllAssets];
                    [self assetsStat];
                    [self selectAseetsStat];
                    [self resizeCollectionViewSize:YES];
                    [self.albumCollectionView reloadData];
                    self.backButton.enabled = YES;
                });
            } else {
                NSLog(@"Error: %@", error);
                dispatch_async(dispatch_get_main_queue(), ^{
                    [self.backButton setEnabled:YES];
                });
            }
        };
        
        
        self.backButton.enabled = NO;
        // Delete asset from library
        [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
            [PHAssetChangeRequest deleteAssets:photoAssets];
        } completionHandler:completionHandler];
    }
    @catch (NSException *exception) {
        
    }
}

-(void)resizeCollectionViewSize:(BOOL)show{
    AssetsCollectionViewLayout *layout = (AssetsCollectionViewLayout*)_albumCollectionView.collectionViewLayout;
    CGFloat collectionViewHeight = [layout calculateCollectionViewContentSize:_unExportedAssetArray.count].height + _collectionViewHeaderHeight;
    if(collectionViewHeight > _collectionContainerViewHeight){
        collectionViewHeight = _collectionContainerViewHeight;
    }
    _showAlbumCollectionView = show;
    _albumCVHeightConstraint.constant = show ? collectionViewHeight : _collectionViewHeaderHeight;
}
@end
