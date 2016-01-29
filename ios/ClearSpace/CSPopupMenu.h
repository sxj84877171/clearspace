//
//  CSPopupMenu.h
//  ClearSpace
//
//  Created by SW2 on 15/12/6.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, PopupDirection){
    PopupMenuUp,
    PopupMenuDown
};

@protocol CSPopupMenuDelegate <NSObject>

-(void)menuItemClick:(NSIndexPath *)indexPath;

@end

@interface CSPopupMenu : UITableView<UITableViewDataSource,UITableViewDelegate>

@property (nonatomic, weak)id<CSPopupMenuDelegate> menuDelegate;
@property (nonatomic) NSArray* menuItems;

//Determines if the menu has been popped
@property (nonatomic, assign) BOOL isPopped;
//Determines the menu direction up, down
@property (nonatomic, assign) PopupDirection direction;

-(void) popUp:(CGPoint)origin parentView:(UIView*)view;
-(void) hide;

@end
