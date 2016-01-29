//
//  MainMenuTableViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/11/17.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>

@class CSMainMenuTableViewController;

@protocol CSMainMenuTableViewControllerDelegate <NSObject>

-(void)MainMenuTableViewController:(CSMainMenuTableViewController*)controller didDismissAfterSelectMenuItem:(NSInteger)itemIndex;
-(void)MainMenuTableViewControllerDidDisappear:(CSMainMenuTableViewController*)controller;
-(void)MainMenuTableViewControllerDidDisappear:(CSMainMenuTableViewController*)controller;
@end

@interface CSMainMenuTableViewController : UITableViewController

@property id<CSMainMenuTableViewControllerDelegate> delegate;

@end
