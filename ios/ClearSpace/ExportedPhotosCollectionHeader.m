//
//  ExportedPhotosCollectionHeader.m
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "ExportedPhotosCollectionHeader.h"
#import "CSPopupMenu.h"

@interface ExportedPhotosCollectionHeader() <CSPopupMenuDelegate>

@end

@implementation ExportedPhotosCollectionHeader{
    NSArray* _sortByArray ;
    
    CSPopupMenu *_sortMenu;
}

-(void) awakeFromNib{
    _sortByArray = @[@"按时间 从新到旧", @"按时间 从旧到新", @"按大小 从大到小", @"按大小 从小到大"];
    
    UITapGestureRecognizer *gestureRecognizer1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(selectAll:)];
    self.selectAllLable.userInteractionEnabled = YES;
    [self.selectAllLable addGestureRecognizer:gestureRecognizer1];
    
    UITapGestureRecognizer *gestureRecognizer2 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(sortBy:)];
    self.sortbyLable.userInteractionEnabled = YES;
    [self.sortbyLable addGestureRecognizer:gestureRecognizer2];
}

- (IBAction)selectAll:(id)sender {
    self.selectType = self.selectType == Select_All ? Select_None : Select_All;
    
    if([_delegate respondsToSelector:@selector(ExportedPhotosCollectionHeader:didSelectAll:)]){
        [_delegate ExportedPhotosCollectionHeader:self didSelectAll:_selectType == Select_All];
    }
}

- (IBAction)sortBy:(id)sender {
    if([_sortMenu isPopped]){
        [_sortMenu hide];
        _sortMenu = nil;
        [_sortbyButton setBackgroundImage:[UIImage imageNamed:@"icon_sort_up.png"] forState:UIControlStateNormal];
    }
    else{
        _sortMenu = [[CSPopupMenu alloc] initWithFrame:CGRectMake(0, 0, 160, 0)];
        _sortMenu.menuDelegate = self;
        _sortMenu.menuItems = [NSArray arrayWithArray:_sortByArray];
        CGPoint pt = CGPointMake(self.sortbyButton.frame.origin.x, self.frame.origin.y + self.frame.size.height);
        [_sortMenu popUp:pt parentView:self.collectionView];
        
        [_sortbyButton setBackgroundImage:[UIImage imageNamed:@"icon_sort_down.png"] forState:UIControlStateNormal];
    }
}

-(void) setSelectType:(SelectType)selectType{
    _selectType = selectType;
    NSString *imageName = @"btn_not_check.png";
    if(selectType == Select_All){
        imageName = @"btn_checked.png";
    }
    else if(selectType == Select_Part){
        imageName = @"btn_checked_part.png";
    }
    
    [self.selectAllButton setImage:[UIImage imageNamed:imageName] forState:UIControlStateNormal];
}

#pragma mark - CSPopupMenuDelegate
-(void)menuItemClick:(NSIndexPath *)indexPath{
    if(indexPath.row >=0 && indexPath.row < _sortByArray.count){
        self.sortbyLable.text = _sortByArray[indexPath.row];
        
        if(self.delegate && [self.delegate respondsToSelector:@selector(ExportedPhotosCollectionHeader:sortBy:)]){
            [self.delegate ExportedPhotosCollectionHeader:self sortBy:indexPath.row + 1];
        }
    }
}
@end
