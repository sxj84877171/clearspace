//
//  CSArrangePhotosViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSArrangePhotosViewController.h"
#import "AssetCell.h"
#import "AssetsCollectionViewLayout.h"
#import "ArrangePhotosCollectionHeader.h"
#import "AssetsManager.h"
#import "CommonFunctions.h"
#import "UIButton+ImageWithColor.h"
#import "statistics/CSStatisticsFactory.h"

@interface CSArrangePhotosViewController() <UICollectionViewDataSource, UICollectionViewDelegate, AssetsManagerDelegate, ArrangePhotosCollectionHeaderDelegate>{
    
}
@end

@implementation CSArrangePhotosViewController{
    AssetsManager *_assetsManager;
    
    NSArray *_exportedAssetArray;
    NSArray *_unExportedAssetArray;
    
    ArrangePhotosCollectionHeader *_exportedHeader;
    ArrangePhotosCollectionHeader *_unExportedHeader;
    
    NSInteger _collectionViewHeaderHeight;
    CGSize _cellSize;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    
    _collectionViewHeaderHeight = 36;
    [self newCollectionView:_exportedCollectionView];
    [self newCollectionView:_unExportedCollectionView];
    
    
    _assetsManager = [AssetsManager sharedInstance];
    
    [self changeCollectionViewHeight];
    
    [[CSStatisticsFactory shareDefaultInstance] onEventCount:UM_GUI_EXPORTED_UNCLEAN];
}

- (void) viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    _assetsManager.delegate = self;
    
    _assetsTotalCountLabel.hidden = YES;
    _assetsTotalSizeUnitLabel.hidden = YES;
    _assetsTotalSizeLabel.hidden = YES;
    
    
    [self lableGradientLayer:_assetsTotalCountLabel];
    [self lableGradientLayer:_assetsTotalSizeLabel];
    [self lableGradientLayer:_assetsTotalSizeUnitLabel];
    
    [self assetsStat:_assetsManager.assetsTotalSize];
    
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSArrangePhotosViewController"];
}

-(void) viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    if(_assetsManager){
        _exportedAssetArray = [_assetsManager exportedAssets:SortBy_None];
        [_exportedCollectionView reloadData];
        _unExportedAssetArray = [_assetsManager unExportedAssets:SortBy_None];
        [_unExportedCollectionView reloadData];
        [self changeCollectionViewHeight];
        
        [self.backButton setBackgroudColor:[UIColor ColorFromHex:0x2c77d9 alpha:1.0] forState:UIControlStateNormal];
        [self.backButton setBackgroudColor:[UIColor ColorFromHex:0x1e5cad alpha:1.0] forState:UIControlStateSelected];
        [[CSStatisticsFactory shareDefaultInstance] onEventCount:UM_GUI_EXPORTED_UNCLEAN];
    }
    
}
-(void) viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSArrangePhotosViewController"];
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

static NSString *cellID = nil;
static NSString *headerID = nil;
-(void) newCollectionView:(UICollectionView*)collectionView{
    collectionView.delegate = self;
    collectionView.dataSource = self;
    collectionView.collectionViewLayout = [self assetsCollectionViewLayoutForOrientation:[[UIApplication sharedApplication] statusBarOrientation]];
    cellID = NSStringFromClass([AssetCell class]);
    [collectionView registerClass:[AssetCell class] forCellWithReuseIdentifier:cellID];
    headerID = NSStringFromClass([ArrangePhotosCollectionHeader class]);
    //[collectionView registerClass:[ArrangePhotosCollectionHeader class] forSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:headerID];
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
-(NSInteger)collectionView:(UICollectionView *)collectionView numberOfItemsInSection:(NSInteger)section{
    NSInteger items = 4;
    if(collectionView == _exportedCollectionView){
        items =  _exportedAssetArray.count> items ? items : _exportedAssetArray.count;
    }
    else if(collectionView == _unExportedCollectionView){
        items =  _unExportedAssetArray.count > items ? items : _unExportedAssetArray.count;
    }
    return items;
}

-(NSInteger)numberOfSectionsInCollectionView:(UICollectionView *)collectionView{
    return 1;
}

-(UICollectionViewCell*)collectionView:(UICollectionView *)collectionView cellForItemAtIndexPath:(NSIndexPath *)indexPath{
    AssetCell* cell = [collectionView dequeueReusableCellWithReuseIdentifier:cellID forIndexPath:indexPath];
    NSInteger currentTag = cell.tag;
    
    AssetInfo* asset = nil;
    CGSize cellSize;
    if(collectionView == _exportedCollectionView){
        asset = (AssetInfo*)_exportedAssetArray[indexPath.row];
        cellSize = ((UICollectionViewFlowLayout *)_exportedCollectionView.collectionViewLayout).itemSize;
    }
    else if(collectionView == _unExportedCollectionView){
        asset = (AssetInfo*)_unExportedAssetArray[indexPath.row];
        cellSize = ((UICollectionViewFlowLayout *)_unExportedCollectionView.collectionViewLayout).itemSize;
    }
    
    CGFloat scale = [UIScreen mainScreen].scale;
    CGSize assetGridThumbnailSize = CGSizeMake((cellSize.width - 1) * scale, (cellSize.height - 1) * scale);
    
    [[PHCachingImageManager defaultManager] requestImageForAsset:asset.asset targetSize:assetGridThumbnailSize contentMode:PHImageContentModeAspectFill options:nil resultHandler:^(UIImage * _Nullable result, NSDictionary * _Nullable info) {
        if (cell.tag == currentTag) {
            cell.thumbnailImageView.image = result;
        }
    }];
    
    return cell;
}

-(UICollectionReusableView*)collectionView:(UICollectionView *)collectionView viewForSupplementaryElementOfKind:(NSString *)kind atIndexPath:(NSIndexPath *)indexPath{
    UICollectionReusableView *reusableView = nil;
    if([kind isEqualToString:UICollectionElementKindSectionHeader]){
        ArrangePhotosCollectionHeader *header = [collectionView dequeueReusableSupplementaryViewOfKind:UICollectionElementKindSectionHeader withReuseIdentifier:@"ArrangePhotosCollectionHeader" forIndexPath:indexPath];
        
        header.collectionView = collectionView;
        header.delegate = self;
        reusableView = header;
        
        int64_t assetSize = 0;
        if(collectionView == _exportedCollectionView){
            _exportedHeader = header;
            assetSize = [_assetsManager exportedAssetsSize];
        }
        else if(collectionView == _unExportedCollectionView){
            _unExportedHeader = header;
            assetSize = [_assetsManager unExportedAssetsSize];
        }
        if(assetSize > 0){
            header.assetsSize = [CommonFunctions autoConvertFileSizeToString:assetSize];
        }
        else{
            header.assetsSize = nil;
        }
    }
    
    return reusableView;
}

#pragma mark - UICollectionViewDelegate
-(BOOL)collectionView:(UICollectionView *)collectionView shouldSelectItemAtIndexPath:(NSIndexPath *)indexPath{
    if(collectionView == _exportedCollectionView){
        [self switchToExportedViewController];
    }
    else if(collectionView == _unExportedCollectionView){
        [self switchToUnExportedViewController];
    }
    
    return YES;
}

- (IBAction)back:(id)sender {
    [self.navigationController popToRootViewControllerAnimated:YES];
}

#pragma mark - AssetsManager delegate
-(void)assetsManagerDelegate:(AssetsManager *)assetsManager photoTotalSizeChanged:(int64_t)newSize{
    dispatch_async(dispatch_get_main_queue(), ^{
        [self assetsStat:newSize];
    });
}

-(void)assetsManagerDelegate:(AssetsManager *)assetsManager newExportedAsset:(AssetInfo *)asset{
    BOOL needReload = _exportedAssetArray.count < 4;
    _exportedAssetArray = [_assetsManager exportedAssets:SortBy_None];
    
    if(needReload){
        [self changeCollectionViewHeight];
        [_exportedCollectionView reloadData];
    }
    else{
        _exportedHeader.assetsSize = [CommonFunctions autoConvertFileSizeToString:[_assetsManager exportedAssetsSize]];
    }
}

-(void)assetsManagerDelegate:(AssetsManager *)assetsManager newUnExportedAsset:(AssetInfo *)asset{
    BOOL needReload = _unExportedAssetArray.count < 4;
    
    _unExportedAssetArray = [_assetsManager unExportedAssets:SortBy_None];
    
    if(needReload){
        [self changeCollectionViewHeight];
        [_unExportedCollectionView reloadData];
    }
    else{
        _unExportedHeader.assetsSize = [CommonFunctions autoConvertFileSizeToString:[_assetsManager unExportedAssetsSize]];
    }
}

#pragma mark - ArrangePhotosCollectionHeaderDelegate
-(void) clickHeader:(ArrangePhotosCollectionHeader *)header{
    if(header.collectionView == _exportedCollectionView){
        [self switchToExportedViewController];
    }
    else if(header.collectionView == _unExportedCollectionView){
        [self switchToUnExportedViewController];
    }
}

#pragma mark - private method
-(void) changeCollectionViewHeight{
    _unExportedCVHeightConstraint.constant = _unExportedAssetArray.count == 0 ? _collectionViewHeaderHeight : _collectionViewHeaderHeight + _cellSize.height + 10;
    _exportedCVHeightConstraint.constant = _exportedAssetArray.count == 0 ? _collectionViewHeaderHeight : _collectionViewHeaderHeight + _cellSize.height + 10;
}

-(void) switchToExportedViewController{
    if(_exportedAssetArray.count > 0){
        [_assetsManager initCurrentExportedAssetsFromExportedAssets];
        [self performSegueWithIdentifier:@"show_exported_viewcontroller" sender:self];
    }
}

-(void) switchToUnExportedViewController{
    if(_unExportedAssetArray.count > 0){
        [self performSegueWithIdentifier:@"show_unexported_viewcontroller" sender:self];
    }
    
}

-(void) lableGradientLayer:(UILabel*)lable{
    /*CAGradientLayer *layer = [CAGradientLayer layer];
    layer.colors = @[(id)[UIColor ColorFromHex:0xffffff alpha:1].CGColor, (id)[UIColor ColorFromHex:0xadd1ff alpha:1].CGColor];
    layer.frame = lable.frame;
    
    [self.view.layer addSublayer:layer];
    layer.mask = lable.layer;
    lable.frame = layer.bounds;*/
    
    lable.textColor = [UIColor gradientColor:@[[UIColor ColorFromHex:0xffffff alpha:1], [UIColor ColorFromHex:0xadd1ff alpha:1]] locations:nil frameHeight:lable.bounds.size.height];
    
}

-(void)assetsStat:(CGFloat)totalSize{
    _noAssetsLabel.hidden = totalSize > 0;
    _assetsTotalSizeLabel.hidden = totalSize == 0;
    _assetsTotalSizeUnitLabel.hidden = totalSize == 0;
    _assetsTotalCountLabel.hidden = totalSize == 0;
    
    if(totalSize > 0){
        NSDictionary *dic = [CommonFunctions autoConvertFileSize:totalSize];
        if(dic){
            NSNumber *number = [dic objectForKey:@"size"];
            NSString *unit = [dic objectForKey:@"unit"];
            if(number){
                _assetsTotalSizeLabel.text = [NSString stringWithFormat:@"%.1f", [number floatValue]];
                _assetsTotalSizeUnitLabel.text = unit;
            }
        }
        if(_exportedAssetArray.count > 0 || _unExportedAssetArray.count > 0){
            NSUInteger count = _exportedAssetArray.count + _unExportedAssetArray.count;
            _assetsTotalCountLabel.text = [NSString stringWithFormat:@"共%lu张", (unsigned long)count];
        }
    }
}
@end
