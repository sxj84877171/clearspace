//
//  CSScanAlbumsViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/9/12.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "CSScanAlbumsViewController.h"
#import "PublicData.h"
#import "CircularProgressView.h"
#import "CSMainMenuTableViewController.h"
#import "AssetsManager.h"
#import "statistics/CSStatisticsFactory.h"
#import "CSFindDeviceViewController.h"

@interface CSScanAlbumsViewController () <AssetsManagerDelegate, CSMainMenuTableViewControllerDelegate>
@property (weak, nonatomic) IBOutlet UILabel *photoTotalSize;
@property (weak, nonatomic) IBOutlet UILabel *devicefreeSpace;
@property (weak, nonatomic) IBOutlet UITextField *spaceTextField;
@property (weak, nonatomic) IBOutlet UITableView *freedBeforeDateTableView;
@property (weak, nonatomic) IBOutlet UILabel *needFreeSize;
@property (weak, nonatomic) IBOutlet UILabel *needFreeSizeUnit;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *tableHeightContraint;
@property (weak, nonatomic) IBOutlet UIButton *exportButton;
@property (weak, nonatomic) IBOutlet UIButton *arrangeButton;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *showMainMenuButton;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *tableTopConstraint;
@property (weak, nonatomic) IBOutlet CircularProgressView *circularView;
- (IBAction)selectFreeTimePoint:(id)sender;
- (IBAction)startExportPhotos:(id)sender;
- (IBAction)MainMenuSelected:(id)sender;

@end

@implementation CSScanAlbumsViewController{
    AssetsManager *_assetsManager;
    NSArray *_freePhotosBeforeDateArray;
    NSInteger _selectDateIndex;
    CSMainMenuTableViewController *_mainMenu;
    UIPopoverController * popoverShareController;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    _assetsManager = [AssetsManager sharedInstance];
    _mainMenu = [[CSMainMenuTableViewController alloc] init];
    
    _freePhotosBeforeDateArray = @[@"一年前", @"半年前", @"三个月前", @"一个月前", @"所有照片"];
    self.freedBeforeDateTableView.dataSource = self;
    self.freedBeforeDateTableView.delegate = self;
    self.freedBeforeDateTableView.layer.borderColor = [[UIColor colorWithRed:51/255.0 green:51/255.0 blue:51/255.0 alpha:1.0] CGColor];
    self.freedBeforeDateTableView.layer.borderWidth = 1.0;
    [self.freedBeforeDateTableView setHidden:YES];
    
    _selectDateIndex = 0;
    
    UITapGestureRecognizer *tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(viewClick:)];
    tapGesture.delegate = self;
    [self.view addGestureRecognizer:tapGesture];
    
    self.spaceTextField.delegate = self;
    
    [self initCircleView];
    
    UIPanGestureRecognizer * panToShowMainMenu = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(panToShowMainMenu:)];
    [self.view addGestureRecognizer:panToShowMainMenu];
}

-(void) viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    _assetsManager.delegate = self;
    
    
    self.arrangeButton.enabled = YES;
    [self initPhotoTotalSizeLable:0];
    
    [self showCircleView];
    
    [self setSpaceTexFieldValue];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSScanAlbumsViewController"];
}

-(void) viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    [self.navigationController.navigationBar setBackgroundImage:[[UIImage alloc] init]
                                                  forBarMetrics:UIBarMetricsDefault];
    self.navigationController.navigationBar.shadowImage = [[UIImage alloc] init];
    
    self.devicefreeSpace.text = [NSString stringWithFormat:@"%.1fGB", [CommonFunctions freeDiskSpaceInGB]];
    
    
    if(_assetsManager){
        [_assetsManager fetchAllAssets];
    }
}
-(void) viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSScanAlbumsViewController"];
}

-(void)viewDidDisappear:(BOOL)animated
{
    [super viewDidDisappear:animated];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(void)panToShowMainMenu:(UIPanGestureRecognizer*)gesture
{
    static CGPoint startLocation;
    if (gesture.state == UIGestureRecognizerStateBegan )
    {
        startLocation = [gesture locationInView:self.view];
    }
    else if (gesture.state == UIGestureRecognizerStateEnded )
    {
        if (startLocation.x < 50 && [gesture velocityInView:self.view].x > 0) {
            [self showMainMenu];
        }
    }
}

- (IBAction)selectFreeTimePoint:(id)sender {
    self.tableTopConstraint.constant = self.spaceTextField.frame.origin.y + self.circularView.frame.origin.y + self.spaceTextField.frame.size.height;
    [self.freedBeforeDateTableView layoutIfNeeded];
    //[self.freedBeforeDateTableView setUserInteractionEnabled:YES];
    [self.freedBeforeDateTableView setHidden:NO];
    [self.freedBeforeDateTableView reloadData];
}

- (IBAction)startExportPhotos:(id)sender {
}

- (IBAction)MainMenuSelected:(id)sender {
    [self showMainMenu];
}

-(void)showMainMenu
{
    UIStoryboard *storyboard = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    
    CSMainMenuTableViewController * menuTableVC = [storyboard instantiateViewControllerWithIdentifier:@"CSMainMenuTableViewController"];
    menuTableVC.delegate = self;
    
    menuTableVC.modalPresentationStyle = UIModalPresentationOverCurrentContext;
    menuTableVC.view.backgroundColor = [UIColor colorWithRed:0 green:0 blue:0 alpha:0.8];
    [self presentViewController:menuTableVC animated:NO completion:nil];
    self.showMainMenuButton.enabled = NO;
    self.showMainMenuButton.tintColor = [UIColor clearColor];
}

#pragma mark delegate from CSMainMenuTableViewController
-(void)MainMenuTableViewController:(CSMainMenuTableViewController*)controller didDismissAfterSelectMenuItem:(NSInteger)itemIndex
{
    if (itemIndex == 0 ) {  //share
        [self share:[self shareContent]];
    }
}

-(void)MainMenuTableViewControllerDidDisappear:(CSMainMenuTableViewController*)controller
{
    self.showMainMenuButton.enabled = YES;
    self.showMainMenuButton.tintColor = nil;
}

-(NSArray*)shareContent
{
    NSString * sharedString = @"照片太多,空间不够,怎么办? \"照片大挪移\" 一键帮您轻松搞定!点击下载: http://114.215.236.240/update/cleanspace.apk ";
    NSMutableArray *arrayOfActivityItems = [[NSMutableArray alloc] init];
    
    [arrayOfActivityItems addObject:sharedString];
    
    return arrayOfActivityItems;
}

-(void)share:(NSArray*)arrayOfActivityItems
{
    // Display the view controller
    UIActivityViewController *activityVC = [[UIActivityViewController alloc]
                                            initWithActivityItems: arrayOfActivityItems applicationActivities:nil];
    activityVC.excludedActivityTypes = @[UIActivityTypeCopyToPasteboard];
    
    if (UI_USER_INTERFACE_IDIOM() == UIUserInterfaceIdiomPhone)
    {
        [self presentViewController:activityVC animated:YES completion:nil];
    }
    //if iPad
    else
    {
        // Change Rect to position Popover
        popoverShareController = [[UIPopoverController alloc] initWithContentViewController:activityVC];
        popoverShareController.contentViewController = activityVC;
        
        CGRect winBounds = [UIScreen mainScreen].bounds;
        CGRect popoverRect = CGRectMake(0, 0, winBounds.size.width, 50);
        [popoverShareController presentPopoverFromRect:popoverRect  inView:self.view permittedArrowDirections:UIPopoverArrowDirectionAny animated:YES];
        
    }
}

-(void) viewClick:(UIGestureRecognizer*)gestureRecognizer{
    [self.freedBeforeDateTableView setHidden:YES];
}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    
    //防止快速点击按钮，跳转多次
    self.exportButton.enabled = NO;
    self.arrangeButton.enabled = NO;
}



#pragma mark - AssetsManager Delegate

-(void)assetsManagerDelegate:(AssetsManager *)assetsManager photoTotalSizeChanged:(int64_t)newSize{
    //dispatch_async(dispatch_get_main_queue(), ^{
        [self initPhotoTotalSizeLable:newSize];
        [self setSpaceTexFieldValue];
    //});
}

-(void)assetsManagerDelegateFetchCompleted:(AssetsManager*)assetsManager{
    dispatch_async(dispatch_get_main_queue(), ^{
        float size = [_assetsManager sizeBeforeDate:_selectDateIndex];
        [self.exportButton setEnabled:size > 0];
    });
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return _freePhotosBeforeDateArray.count;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"SpaceSizeCell" forIndexPath:indexPath];
    
    UIView *bgColorView = [[UIView alloc] init];
    bgColorView.backgroundColor = [UIColor colorWithRed:208.0/255 green:228.0/255 blue:1.0 alpha:1];
    [cell setSelectedBackgroundView:bgColorView];
    
    cell.textLabel.text = _freePhotosBeforeDateArray[indexPath.row];
    // Configure the cell...
    return cell;
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    _selectDateIndex = indexPath.row;
    [self setSpaceTexFieldValue];
    [self.freedBeforeDateTableView setHidden:YES];
    [self collectData:_selectDateIndex];
}

#pragma mark - text field delegate
-(BOOL)textFieldShouldBeginEditing:(UITextField *)textField{
    [self selectFreeTimePoint:nil];
    return NO;
}

#pragma mark - UIGestureRecognizer Delegate
-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldReceiveTouch:(UITouch *)touch{
    //NSLog(@"%@", [touch.view class]);
    
    if([touch.view isKindOfClass:[UILabel class]]){
        return NO;
    }
    return YES;
}

#pragma mark - private method
-(void) setSpaceTexFieldValue{
    if(_selectDateIndex >= _freePhotosBeforeDateArray.count){
        _selectDateIndex = 0;
    }
    
    if(_assetsManager){
        float size = [_assetsManager sizeBeforeDate:_selectDateIndex];
        
        PublicData *publicData = [PublicData sharedInstance];
        if(publicData){
            publicData.willfreeSpaceSize = size;
        }
        
        if(_selectDateIndex == _freePhotosBeforeDateArray.count - 1){
            self.spaceTextField.text = _freePhotosBeforeDateArray[_selectDateIndex];
        }
        else
        {
            self.spaceTextField.text = [NSString stringWithFormat:@"%@的照片", _freePhotosBeforeDateArray[_selectDateIndex]];
        }
        NSDictionary *dic = [CommonFunctions autoConvertFileSize:size];
        if(dic){
            NSNumber *number = [dic objectForKey:@"size"];
            _needFreeSize.text = [NSString stringWithFormat:@"%.1f", [number floatValue]];
            _needFreeSizeUnit.text = [dic objectForKey:@"unit"];
            self.exportButton.enabled = [number intValue] != 0;
        }
    }
}

-(void) initFreeSpaceDate{
    if(_assetsManager){
        /*[_freePhotosBeforeDateArray removeAllObjects];
        float freeDiskSpace = [_photoAccess assetTotalSize] / (1024 * 1024 * 1024);
        _spaceSizeArray = [[NSMutableArray alloc] init];
        int sizeCout = freeDiskSpace / 0.5;
        for(int i=1; i<=sizeCout && i<=5; i++){
            [_spaceSizeArray addObject:[NSNumber numberWithDouble:0.5 * i]];
         }*/
    }
}

-(void) initPhotoTotalSizeLable:(int64_t)size{
    self.photoTotalSize.text = [CommonFunctions autoConvertFileSizeToString:size];
}

-(void) initCircleView{
    self.circularView.lineWidth = 7;
    self.circularView.progressColor = [UIColor colorWithRed:1.0 green:179.0/255 blue:89.0/255 alpha:1.0];
    self.circularView.progressFillColor = [UIColor whiteColor];
    [self.circularView updateProgress:1.0];
}

-(void) showCircleView{
    [UIView animateWithDuration:0.0 animations:^{
        self.circularView.transform = CGAffineTransformMakeScale(0.0, 0.0);
    } completion:^(BOOL finished) {
        [self.circularView setHidden:NO];
        [self.circularView drawProgress:0.0 end:1.0];
        [UIView animateWithDuration:1.0 animations:^{
            self.circularView.transform = CGAffineTransformMakeScale(1.0, 1.0);
        } completion:^(BOOL finished) {
            ;
        }];
    }];
}

#pragma Collect data
-(void) collectData:(int) row
{
    NSString* eventId = nil;
    switch (row) {
        case 0:
            eventId = UM_GUI_SELECT_BEFORE_ONE_YEAR_EXPORT;
            break;
            
        case 1:
            eventId = UM_GUI_SELECT_BEFORE_SIX_MON_EXPORT;
            break;
        case 2:
            eventId = UM_GUI_SELECT_BEFORE_THREE_MON_EXPORT;
            break;
        case 3:
            eventId = UM_GUI_SELECT_BEFORE_ONE_MON_EXPORT;
            break;
        case 4:
            eventId = UM_GUI_SELECT_BEFORE_ALL_EXPORT;
            break;
        default:
            break;
    }
    [[CSStatisticsFactory shareDefaultInstance] onEventCount:eventId];
}
@end
