//
//  MainMenuTableViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/11/17.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSMainMenuTableViewController.h"
#import "MainMenuTableViewCell.h"
#import "CSFeedbackViewController.h"
#import "statistics/CSStatisticsFactory.h"

@interface CSMainMenuTableViewController ()
@property IBOutlet UITableViewCell * cell_share;
@property IBOutlet UITableViewCell * cell_feedback;
@property IBOutlet UITableViewCell * cell_about;
@property IBOutlet UITableViewCell * cell_exit;
@end

@implementation CSMainMenuTableViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    // Uncomment the following line to preserve selection between presentations.
    // self.clearsSelectionOnViewWillAppear = NO;
    
    // Uncomment the following line to display an Edit button in the navigation bar for this view controller.
    // self.navigationItem.rightBarButtonItem = self.editButtonItem;
    [self updateBackgroundColorForcell:self.cell_share];
    [self updateBackgroundColorForcell:self.cell_feedback];
    [self updateBackgroundColorForcell:self.cell_about];
    [self updateBackgroundColorForcell:self.cell_exit];
    
    UIPanGestureRecognizer * panToExitMainMenu = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panToExitMainMenu:)];
    [self.view addGestureRecognizer:panToExitMainMenu];
}

-(void)viewWillAppear:(BOOL)animated
{
        CGRect windowRect = [UIScreen mainScreen].bounds;
        self.view.frame = CGRectMake(-windowRect.size.width, 0, windowRect.size.width, windowRect.size.height);
        //menuTableVC.view.frame = CGRectMake(self.view.frame.origin.x - self.view.frame.size.width, self.view.frame.origin.y, self.view.frame.size.width, self.view.frame.size.height);
    
        [UIView animateWithDuration:0.5 animations:^{
            self.view.frame = windowRect;
        } completion:^(BOOL finished) {
        }];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSMainMenuTableViewController"];
}

-(void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSMainMenuTableViewController"];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
    
    [self.delegate MainMenuTableViewControllerDidDisappear:self];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 4;
}

-(void)updateBackgroundColorForcell:(UITableViewCell*)cell
{
    UIView *bgColorView = [[UIView alloc] init];
    bgColorView.backgroundColor = [UIColor colorWithRed:51.0/255 green:51.0/255 blue:51.0/255 alpha:1];
    [cell setSelectedBackgroundView:bgColorView];
    
    if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPad) {
        cell.backgroundColor = [UIColor clearColor];
    }
}

-(void)panToExitMainMenu:(UIPanGestureRecognizer*)gesture
{
    if (gesture.state == UIGestureRecognizerStateEnded )
    {
        if ([gesture velocityInView:self.view].x < 0) {
            [self dismissFromRightToLeftAndFadeOut:nil];
        }
    }
}

//- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
//    MainMenuTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"Menu_Cell" forIndexPath:indexPath];
//    
//    switch (indexPath.row) {
//        case 0:
//            cell.leftImageView.image = [UIImage imageNamed:@"icon_share.png"];
//            cell.leftTitle.text = @"分享";
//            cell.rightImageView.image = [UIImage imageNamed:@"icon_share_togeter.png"];
//            cell.rightTitle.text = @"好用大家一起用";
//            break;
//        case 1:
//            cell.leftImageView.image = [UIImage imageNamed:@"icon_feedback.png"];
//            cell.leftTitle.text = @"反馈";
//            cell.rightImageView.image = [UIImage imageNamed:@"icon_feedback_foot.png"];
//            cell.rightTitle.text = @"好用大家一起用";
//            break;
//            
//        case 2:
//            cell.leftImageView.image = [UIImage imageNamed:@"icon_about.png"];
//            cell.leftTitle.text = @"关于";
//            [cell.rightImageView setHidden:YES];;
//            [cell.rightTitle setHidden:YES];
//            break;
//            
//        case 3:
//            cell.leftImageView.image = [UIImage imageNamed:@"icon_exit.png"];
//            cell.leftTitle.text = @"退出";
//            [cell.rightImageView setHidden:YES];;
//            [cell.rightTitle setHidden:YES];
//            break;
//
//            
//        default:
//            break;
//    }
//    
//    UIView *bgColorView = [[UIView alloc] init];
//    bgColorView.backgroundColor = [UIColor colorWithRed:51.0/255 green:51.0/255 blue:51.0/255 alpha:1];
//    [cell setSelectedBackgroundView:bgColorView];
//    
//    return cell;
//}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    NSString* eventID = nil;
    switch (indexPath.row)
    {
    case 0:
        {
            UINavigationController * presentingVC = self.presentingViewController;
            [self dismissFromRightToLeftAndFadeOut:^{
                [self.delegate MainMenuTableViewController:self didDismissAfterSelectMenuItem:0];
            }];
            eventID = UM_GUI_SHARE;
        }
        break;
    case 1:
//        UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
        
        {
            UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
            CSFeedbackViewController * feedbackVC = [storyboard instantiateViewControllerWithIdentifier:@"CSFeedbackViewController"];
        
            feedbackVC.modalPresentationStyle = UIModalPresentationOverCurrentContext;
            feedbackVC.view.backgroundColor = [UIColor colorWithRed:255 green:255 blue:255 alpha:1];
//            [self presentViewController:feedbackVC animated:NO completion:nil];
            
            UINavigationController * presentingVC = self.presentingViewController;
            
            [self dismissViewControllerAnimated:NO completion:^{
                [presentingVC pushViewController:feedbackVC animated:YES];
            }];
            eventID = UM_GUI_FEEDBACK;
            
        }
        break;

    case 2:
        {
            UIAlertController * alert = [UIAlertController alertControllerWithTitle:@"关于" message:@"照片大挪移\nVersion:1.4.66" preferredStyle:UIAlertControllerStyleAlert];
            [alert addAction:[UIAlertAction actionWithTitle:@"完成" style:UIAlertActionStyleCancel handler:nil]];
            [self.tableView reloadData];
            [self presentViewController:alert animated:YES completion:nil];
        }
            eventID = UM_GUI_MENU_ABOUT;
        break;

    case 3:
            
            eventID = UM_EVENT_SETTING_EXIT_SERVICE;
        break;

        
    default:
        break;
    }
    [[CSStatisticsFactory shareDefaultInstance] onEventCount:eventID];}




/*
// Override to support conditional editing of the table view.
- (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the specified item to be editable.
    return YES;
}
*/

/*
// Override to support editing the table view.
- (void)tableView:(UITableView *)tableView commitEditingStyle:(UITableViewCellEditingStyle)editingStyle forRowAtIndexPath:(NSIndexPath *)indexPath {
    if (editingStyle == UITableViewCellEditingStyleDelete) {
        // Delete the row from the data source
        [tableView deleteRowsAtIndexPaths:@[indexPath] withRowAnimation:UITableViewRowAnimationFade];
    } else if (editingStyle == UITableViewCellEditingStyleInsert) {
        // Create a new instance of the appropriate class, insert it into the array, and add a new row to the table view
    }   
}
*/

/*
// Override to support rearranging the table view.
- (void)tableView:(UITableView *)tableView moveRowAtIndexPath:(NSIndexPath *)fromIndexPath toIndexPath:(NSIndexPath *)toIndexPath {
}
*/

/*
// Override to support conditional rearranging of the table view.
- (BOOL)tableView:(UITableView *)tableView canMoveRowAtIndexPath:(NSIndexPath *)indexPath {
    // Return NO if you do not want the item to be re-orderable.
    return YES;
}
*/

/*
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}
*/

- (IBAction)exitButtonTouchUpInside:(id)sender {
    [self dismissFromRightToLeftAndFadeOut:nil];
}

-(void)dismissFromRightToLeftAndFadeOut:(void (^ __nullable)(void))completion
{
    CGRect windowRect = [UIScreen mainScreen].bounds;
    //menuTableVC.view.frame = CGRectMake(self.view.frame.origin.x - self.view.frame.size.width, self.view.frame.origin.y, self.view.frame.size.width, self.view.frame.size.height);
    
    [UIView animateWithDuration:0.2 animations:^{
        self.view.frame = CGRectMake(-windowRect.size.width, 0, windowRect.size.width, windowRect.size.height);
        self.view.alpha = 0;
    } completion:^(BOOL finished) {
        [self dismissViewControllerAnimated:NO completion:completion];
    }];
}

@end
