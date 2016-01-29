//
//  CSPopupMenu.m
//  ClearSpace
//
//  Created by SW2 on 15/12/6.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSPopupMenu.h"
#import "CommonFunctions.h"

@implementation CSPopupMenu

#pragma mark - Initialization
- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

- (id)init {
    self = [super init];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)awakeFromNib{
    [self setup];
}
/*
// Only override drawRect: if you perform custom drawing.
// An empty implementation adversely affects performance during animation.
- (void)drawRect:(CGRect)rect {
    // Drawing code
}
*/

static NSString *cellID = @"Cell";
-(void) setup{
    self.rowHeight = 44;
    
    self.delegate = self;
    self.dataSource = self;
    self.direction = PopupMenuDown;
    [self registerClass:[UITableViewCell class] forCellReuseIdentifier:cellID];
    self.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.layer.borderColor = [UIColor ColorFromHex:0x333333 alpha:1.0].CGColor;
    self.layer.borderWidth = 1.0;
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
// Return the number of rows in the section.
    return _menuItems.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:cellID forIndexPath:indexPath];
    
    UIView *bgColorView = [[UIView alloc] init];
    bgColorView.backgroundColor = [UIColor ColorFromHex:0xeeeeee alpha:1.0];
    [cell setSelectedBackgroundView:bgColorView];
    
    [cell.textLabel setFont:[UIFont systemFontOfSize:16.0]];
    [cell.textLabel setTextColor:[UIColor ColorFromHex:0x333333 alpha:1.0]];
    
    cell.textLabel.text = _menuItems[indexPath.row];
    [cell.textLabel setTextAlignment:NSTextAlignmentCenter];
    // Configure the cell...
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    if([self.menuDelegate respondsToSelector:@selector(menuItemClick:)]){
        [self.menuDelegate menuItemClick:indexPath];
    }
    
    [self hide];
}

-(void) setMenuItems:(NSArray *)menuItems{
    _menuItems = menuItems;
    
    [self reloadData];
}

-(void)popUp:(CGPoint)origin parentView:(UIView*)view{
    CGFloat cellSize = [self rowHeight];
    //Get number of rows
    NSInteger numberOfRows = [self numberOfRowsInSection:0];
    
    //Set frame for menu view
    CGRect frame = self.frame;
    NSInteger menuHeight = cellSize * numberOfRows;
    NSInteger menuWidth = frame.size.width;
    NSInteger menuX = origin.x;
    NSInteger menuY = origin.y;
    
    if(menuX + menuWidth > view.frame.origin.x + view.frame.size.width){
        menuX = view.frame.origin.x + view.frame.size.width - menuWidth;
    }
    
    if (self.direction == PopupMenuUp) {
        self.frame = CGRectMake(menuX,
                                menuY,
                                menuWidth,
                                menuHeight);
    }else{
        self.frame = CGRectMake(menuX,
                                menuY,// + frame.size.height - menuHeight,
                                menuWidth,
                                menuHeight);
    }
    
    //[self setAlpha:0.0];
    [view addSubview:self];
    
    /*[UIView beginAnimations:nil context:nil];
    [self setAlpha:1.0];
    [UIView commitAnimations];*/
    //Animate popup
    /*[UIView animateWithDuration:0.3
                     animations:^{
                         self.frame = CGRectApplyAffineTransform(self.frame, CGAffineTransformMakeTranslation(0, (self.direction == PopupMenuUp ? -cellSize : cellSize) * numberOfRows));
                     }
                     completion:^(BOOL finished){
                         if(finished){
                         }
                     }];*/

}

-(void)hide{
    /*[UIView beginAnimations:nil context:nil];
    [self setAlpha:0.0];
    [UIView commitAnimations];*/
    
    [self removeFromSuperview];
    
    //Animate popup
    
    /*
     
     //Get row height
     CGFloat cellSize = [self rowHeight];
     //Get number of rows
     NSInteger numberOfRows = [self numberOfRowsInSection:0];
     [UIView animateWithDuration:0.3
                     animations:^{
                         self.frame = CGRectApplyAffineTransform(self.frame, CGAffineTransformMakeTranslation(0, (self.direction == PopupMenuUp ?cellSize : -cellSize) * numberOfRows));
                         
                     }
                     completion:^(BOOL finished){
                         if(finished){
                             [self removeFromSuperview];
                         }
                     }];*/
}
- (BOOL)isPopped
{
    return self.superview != nil;
}
@end
