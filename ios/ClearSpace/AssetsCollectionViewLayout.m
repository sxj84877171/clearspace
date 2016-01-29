//
//  AssetsCollectionViewLayout.m
//  TSAssetsPickerController
//
//  Created by Tomasz Szulc on 01.03.2014.
//  Copyright (c) 2014 Tomasz Szulc. All rights reserved.
//

#import "AssetsCollectionViewLayout.h"

@implementation AssetsCollectionViewLayout {
    NSMutableDictionary *_layoutInfo;
}

static NSString *const kCell = @"AssetCell";
static NSString *const kSupplementary = @"AssetSupplementary";

#pragma mark - Initialization & Setup
- (void)setup {
    _itemInsets = UIEdgeInsetsMake(5.0f, 5.0f, 5.0f, 5.0f);
    _itemSize = CGSizeMake(74, 74);
    _internItemSpacingX = 5.0;
    _internItemSpacingY = 5.0f;
    _numberOfColumns = 4;
    _supplementaryViewHeight = 35;
}

- (id)init {
    self = [super init];
    if (self) {
        [self setup];
    }
    return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder {
    self = [super initWithCoder:aDecoder];
    if (self) {
        [self setup];
    }
    return self;
}


#pragma mark - Layout
- (void)prepareLayout {
    [self calculateItemSize];
    
    NSMutableDictionary *newLayoutInfo = [NSMutableDictionary dictionary];
    NSMutableDictionary *cellLayoutInfo = [NSMutableDictionary dictionary];
    
    NSUInteger itemCount = [self.collectionView numberOfItemsInSection:0];
    
    for (NSUInteger item = 0; item < itemCount; item++) {
        NSIndexPath *indexPath = [NSIndexPath indexPathForItem:item inSection:0];
        
        UICollectionViewLayoutAttributes *itemAttributes =
        [UICollectionViewLayoutAttributes layoutAttributesForCellWithIndexPath:indexPath];
        itemAttributes.frame = [self frameForCellAtIndexPath:indexPath];
        cellLayoutInfo[indexPath] = itemAttributes;
    }
    
    NSMutableDictionary *supplementaryInfo = [NSMutableDictionary dictionary];
    NSIndexPath *indexPath = [NSIndexPath indexPathForItem:0 inSection:0];
    UICollectionViewLayoutAttributes *supplementaryAttributes = [UICollectionViewLayoutAttributes layoutAttributesForSupplementaryViewOfKind:UICollectionElementKindSectionHeader withIndexPath:indexPath];
    supplementaryAttributes.frame = CGRectMake(0, 0, self.collectionView.frame.size.width, _supplementaryViewHeight);
    supplementaryInfo[indexPath] = supplementaryAttributes;
    
    newLayoutInfo[kCell] = cellLayoutInfo;
    newLayoutInfo[kSupplementary] = supplementaryInfo;
    _layoutInfo = newLayoutInfo;
}

- (CGRect)frameForCellAtIndexPath:(NSIndexPath *)indexPath {
    NSUInteger row = (indexPath.row / _numberOfColumns);
    NSUInteger column = indexPath.row % _numberOfColumns;
    
    CGFloat spacingX = self.collectionView.bounds.size.width - _itemInsets.left - _itemInsets.right - (_numberOfColumns * _itemSize.width);
    if (_numberOfColumns > 1)
        spacingX = spacingX / (_numberOfColumns - 1);
    
    CGFloat originX = floorf(_itemInsets.left + (_itemSize.width + spacingX) * column);
    CGFloat originY = floor(_itemInsets.top + (_itemSize.height + _internItemSpacingY) * row + _supplementaryViewHeight);
    
    return CGRectMake(originX, originY, _itemSize.width, _itemSize.height);
}

- (NSArray *)layoutAttributesForElementsInRect:(CGRect)rect {
    NSMutableArray *allAttributes = [NSMutableArray array];
    [_layoutInfo enumerateKeysAndObjectsUsingBlock:^(NSString *identifier,
                                                     NSDictionary *info,
                                                     BOOL *stop) {
        [info enumerateKeysAndObjectsUsingBlock:^(NSIndexPath *indexPath,
                                                  UICollectionViewLayoutAttributes *attributes,
                                                  BOOL *stop) {
            if (CGRectIntersectsRect(rect, attributes.frame)) {
                [allAttributes addObject:attributes];
            }
        }];
    }];
    
    return allAttributes;
}

- (UICollectionViewLayoutAttributes *)layoutAttributesForItemAtIndexPath:(NSIndexPath *)indexPath {
    return _layoutInfo[kCell][indexPath];
}

- (UICollectionViewLayoutAttributes *)layoutAttributesForSupplementaryViewOfKind:(NSString *)elementKind atIndexPath:(NSIndexPath *)indexPath{
    
    return _layoutInfo[kSupplementary][indexPath];
}

- (CGSize)collectionViewContentSize {
    NSUInteger numberOfItems = [self.collectionView numberOfItemsInSection:0];
    
    return [self calculateCollectionViewContentSize:numberOfItems];
}

-(CGSize) calculateCollectionViewContentSize:(NSInteger)numberOfItems{
    NSInteger rowCount = numberOfItems / _numberOfColumns;
    if (numberOfItems % _numberOfColumns)
        rowCount++;
    
    CGFloat height = _itemInsets.top + (rowCount * _itemSize.height) + ((rowCount - 1) * _internItemSpacingY) + _itemInsets.bottom;
    
    return CGSizeMake(self.collectionView.bounds.size.width, height);
}
#pragma mark - Properties
- (void)setItemInsets:(UIEdgeInsets)itemInsets {
    if (!UIEdgeInsetsEqualToEdgeInsets(_itemInsets, itemInsets)) {
        _itemInsets = itemInsets;
        [self invalidateLayout];
    }
}

- (void)setItemSize:(CGSize)itemSize {
    if (!CGSizeEqualToSize(_itemSize, itemSize)) {
        _itemSize = itemSize;
        [self invalidateLayout];
    }
}

- (void)setInternItemSpacingX:(CGFloat)internItemSpacingX {
    if (_internItemSpacingX != internItemSpacingX) {
        _internItemSpacingX = internItemSpacingX;
        [self invalidateLayout];
    }
}

- (void)setInternItemSpacingY:(CGFloat)internItemSpacingY {
    if (_internItemSpacingY != internItemSpacingY) {
        _internItemSpacingY = internItemSpacingY;
        [self invalidateLayout];
    }
}

- (void)setNumberOfColumns:(NSUInteger)numberOfColumns {
    if (_numberOfColumns != numberOfColumns) {
        _numberOfColumns = numberOfColumns;
        [self invalidateLayout];
    }
}

-(void)setSupplementaryViewHeight:(NSInteger)supplementaryViewHeight{
    if(_supplementaryViewHeight != supplementaryViewHeight){
        _supplementaryViewHeight = supplementaryViewHeight;
        [self invalidateLayout];
    }
}

-(void) calculateItemSize{
    CGFloat viewWidth = self.collectionView.bounds.size.width - _itemInsets.left - _itemInsets.right - (_internItemSpacingX - 1) * _numberOfColumns;
    _itemSize.height = _itemSize.width = viewWidth / _numberOfColumns;
}

@end
