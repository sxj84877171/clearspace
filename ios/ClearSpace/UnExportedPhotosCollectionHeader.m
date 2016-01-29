//
//  UnExportedPhotosCollectionHeader.m
//  ClearSpace
//
//  Created by SW2 on 15/12/4.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "UnExportedPhotosCollectionHeader.h"
#import "CSPopupMenu.h"

@interface UnExportedPhotosCollectionHeader() <CSPopupMenuDelegate>


@end

@implementation UnExportedPhotosCollectionHeader{
    BOOL _showCollectionView;
    
    NSArray* _sortByArray;
    CSPopupMenu *_sortMenu;
}

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        //[self setup];
    }
    return self;
}

- (id)init {
    self = [super init];
    if (self) {
        //[self setup];
    }
    return self;
}
-(void)awakeFromNib{
    [self setup];
}

-(void) setup{
    _showCollectionView = NO;
    UITapGestureRecognizer *gestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickHeader)];
    
    [self addGestureRecognizer:gestureRecognizer];
    
    UITapGestureRecognizer *gestureRecognizer2 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(sortBy:)];
    self.sortByLable.userInteractionEnabled = YES;
    [self.sortByLable addGestureRecognizer:gestureRecognizer2];
    
    _sortByArray = @[@"按时间 从新到旧", @"按时间 从旧到新", @"按大小 从大到小", @"按大小 从小到大"];
}

-(void) clickHeader{
    _showCollectionView = !_showCollectionView;
    if([self.delegate respondsToSelector:@selector(clickHeader:showCollection:)]){
        [self.delegate clickHeader:self showCollection:_showCollectionView];
    }
    if([_sortMenu isPopped]){
        [_sortMenu hide];
        _sortMenu = nil;
        [_sortByButton setBackgroundImage:[UIImage imageNamed:@"icon_sort_up.png"] forState:UIControlStateNormal];
    }
}

- (IBAction)sortBy:(id)sender {
    if([_sortMenu isPopped]){
        [_sortMenu hide];
        _sortMenu = nil;
        [_sortByButton setBackgroundImage:[UIImage imageNamed:@"icon_sort_up.png"] forState:UIControlStateNormal];
    }
    else{
        _sortMenu = [[CSPopupMenu alloc] initWithFrame:CGRectMake(0, 0, 160, 0)];
        _sortMenu.menuDelegate = self;
        _sortMenu.menuItems = [NSArray arrayWithArray:_sortByArray];
        CGPoint pt = CGPointMake(self.sortByLable.frame.origin.x, self.frame.origin.y + self.frame.size.height);
        [_sortMenu popUp:pt parentView:[self.collectionView superview]];
        
        [_sortByButton setBackgroundImage:[UIImage imageNamed:@"icon_sort_down.png"] forState:UIControlStateNormal];
    }
}

- (void) changeArrowImage:(BOOL)show{
    if(show){
        [self.arrowButton setBackgroundImage:[UIImage imageNamed:@"btn_up_close_normal.png"] forState:UIControlStateNormal];
        [self.arrowButton setBackgroundImage:[UIImage imageNamed:@"btn_up_close_pressed.png"] forState:UIControlStateSelected];
    }
    else{
        [self.arrowButton setBackgroundImage:[UIImage imageNamed:@"btn_down_open_normal.png"] forState:UIControlStateNormal];
        [self.arrowButton setBackgroundImage:[UIImage imageNamed:@"btn_down_open_pressed.png"] forState:UIControlStateSelected];
        [self.arrowButton setBackgroundImage:[UIImage imageNamed:@"btn_down_disable.png"] forState:UIControlStateDisabled];
    }
}



#pragma mark - CSPopupMenuDelegate
-(void)menuItemClick:(NSIndexPath *)indexPath{
    if(indexPath.row >=0 && indexPath.row < _sortByArray.count){
        self.sortByLable.text = _sortByArray[indexPath.row];
        
        if(self.delegate && [self.delegate respondsToSelector:@selector(UnExportedPhotosCollectionHeader:sortBy:)]){
            [self.delegate UnExportedPhotosCollectionHeader:self sortBy:indexPath.row + 1];
        }
    }
}

#pragma mark - assesser
-(void)setAssetsSize:(NSString *)assetsSize{
    self.totalSize.text = assetsSize;
}
@end
