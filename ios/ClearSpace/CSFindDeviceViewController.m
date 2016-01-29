//
//  CSFindDeviceViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/9/12.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "CSFindDeviceViewController.h"
#import "DeviceTableViewCell.h"
#import "PublicData.h"
#import "ConnectedDeviceTableView.h"
#import "CSConfig.h"
#import "CSConst.h"
#import "YLGIFImage.h"
#import "YLImageView.h"
#import "AssetInfo.h"
#import "AssetsManager.h"
#import "statistics/CSStatisticsFactory.h"
#import "CSArrangePhotosViewController.h"

@interface CSFindDeviceViewController ()
@property (nonatomic) DeviceInfo* lastConnectDeivce;
@property (nonatomic) DeviceInfo* currentSelectedDeivce;
@property (nonatomic) NSMutableArray *deviceArray;
@property (nonatomic) DevicePaired *devicePaired;
@property (nonatomic) BOOL showFindDeviceGuideView;
@property (nonatomic) BOOL showNearbyDevice;
@property (nonatomic) BOOL connectDeviceSuccess;
@property (nonatomic) BOOL connecttingDeivce;
@property (nonatomic) BOOL switchViewController;
@property (nonatomic) NSInteger findDeviceTipViewHeight;
@property (nonatomic) ConnectedDeviceTableView *lastConnectedDeviceTableViewDelegate;
@property (nonatomic) NSTimer* idleTimer;
@property (nonatomic) NSInteger idleTimes;
@end

@implementation CSFindDeviceViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    @try {
        // Do any additional setup after loading the view.
        [self initTableViewDelegate];
        
        self.showFindDeviceGuideView = NO;
        self.showNearbyDevice = NO;
        self.connectDeviceSuccess = NO;
        self.switchViewController = NO;

        UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showFindDeviceGuideView:)];
        [self.findDeviceTipView addGestureRecognizer:tapGesture];
        
        UITapGestureRecognizer *tapGesture1 = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showNearbyDeviceTableView)];
        [self.showNearbyDeviceButton addGestureRecognizer:tapGesture1];

        UIActivityIndicatorView *loading = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhite];
        self.navigationItem.rightBarButtonItem  = [[UIBarButtonItem alloc] initWithCustomView:loading];
        [loading startAnimating];
        
        self.navigationController.navigationBar.tintColor = [UIColor whiteColor];
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(startDownLoad:) name:NTF_ID_FILE_DOWNLAOD_START object:nil];
        
        self.findPCTipGifImageViewHeightConstraint.constant = [UIScreen mainScreen].bounds.size.width * 150 / 960;
        [[CSStatisticsFactory shareDefaultInstance] onEventCount:UM_GUI_EXPORT];
        
        NSString *tryArrangePhotos = @"连接似乎有点问题，尝试本地整理？  好，帮我整理";
        NSRange range = [tryArrangePhotos rangeOfString:@"好，帮我整理"];
        NSMutableAttributedString *attrString = [[NSMutableAttributedString alloc] initWithString:tryArrangePhotos];
        [attrString addAttribute:NSForegroundColorAttributeName value:[UIColor yellowColor] range:range];
        [self.tryArrangePhotsButton setAttributedTitle:attrString forState:UIControlStateNormal];
    }
    @catch (NSException *exception) {
        ;
    }
}

-(void) viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    [self.searchingDeviceLable setHidden:YES];
    [self.findPCGifImageView setHidden:YES];
    [self.lastConnectDeviceView setHidden:YES];
    [self.nearbyDeviceView setHidden:YES];
    [self.tryArrangePhotsButton setHidden:YES];
    [self.beginButton setEnabled:NO];
    
    _findDeviceTipViewHeight = self.findDeviceTipView.frame.size.height;
    [self showGuideViewAnimation:_showFindDeviceGuideView showAnimate:NO time:0];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSFindDeviceViewController"];
}

-(void) viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    @try {
        
        self.devicePaired = [DevicePaired sharedInstance];
        if(self.devicePaired){
            self.devicePaired.delegate = self;
            
            CSConfig *config = [CSConfig shareInstance];
            if(config != nil){
                self.lastConnectDeivce = [config lastConnectDevice];
            }
            
            self.deviceArray = [self.devicePaired foundDevices];
            //从发现的设备列表中，去除上次连接的电脑
            for(id obj in self.deviceArray){
                if(self.lastConnectDeivce && [self.lastConnectDeivce isSameDevice:(DeviceInfo *)obj]){
                    self.lastConnectDeivce.online = YES;
                    [self.deviceArray removeObject:obj];
                    break;
                }
            }
            
            [self haveFoundDevice:(self.deviceArray.count > 0 || (self.lastConnectDeivce != nil &&
                                                                  self.lastConnectDeivce.online))];
        }
        
        [self showLastConnectedDeviceTableView];
        
        self.currentWifi.text = [CommonFunctions currentWifiSSID];
        
        _idleTimer = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(idleTimerFired) userInfo:nil repeats:YES];
        //[self initNotFoundPCTipGIf];

    }
    @catch (NSException *exception) {
        
    }
}

-(void) viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    
    [_idleTimer invalidate];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSFindDeviceViewController"];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

-(BOOL)shouldPerformSegueWithIdentifier:(NSString *)identifier sender:(id)sender{
    if([identifier isEqualToString:@"start_download_segue"]){
        return self.connectDeviceSuccess;
    }
    
    return YES;
}

- (IBAction)showFindDeviceGuideView:(id)sender {
    _showFindDeviceGuideView = !_showFindDeviceGuideView;
    
    [self showGuideViewAnimation:_showFindDeviceGuideView showAnimate:YES time:0.5];
}

- (IBAction)tryArrangePhotos:(id)sender {
    UIStoryboard *mainStory = [UIStoryboard storyboardWithName:@"Main" bundle:nil];
    
    CSArrangePhotosViewController *arrangePhotosVC = [mainStory instantiateViewControllerWithIdentifier:@"CSArrangePhotosViewController"];
    [self.navigationController pushViewController:arrangePhotosVC animated:YES];
}

- (void)showNearbyDeviceTableView {
    _showNearbyDevice = !_showNearbyDevice;
    
    NSString *imageName = self.showNearbyDevice ? @"btn_find_hide.png" : @"btn_find_show.png";
    UIImage *image = [UIImage imageNamed:imageName];
    [self.nearbyDeviceArrowImageView setImage:image];
    [self.nearbyDeviceTableView setHidden:!_showNearbyDevice];
}

- (IBAction)BeginExportPhotos:(id)sender {
    [_idleTimer invalidate];
    
    if(self.connecttingDeivce && !_switchViewController){ //取消导出
        _switchViewController = YES;
        [self.navigationController popToRootViewControllerAnimated:YES];
        return;
    }
    
    PublicData *publicData = [PublicData sharedInstance];
    if(publicData){
        [publicData startHttpServer];
        publicData.connectingDevice = self.currentSelectedDeivce;
        self.lastConnectDeivce = self.currentSelectedDeivce;
        self.lastConnectDeivce.connectTime = [NSDate date];
    }
    
    CSConfig *config = [CSConfig shareInstance];
    if(config != nil && self.lastConnectDeivce != nil){
        [config saveLastConnectDevice:self.lastConnectDeivce];
    }
    
    [self generateDownLoadFileList];
    
    //准备跟pc建立连接
    self.connecttingDeivce = YES;
    [self.beginButton setTitle:@"取消" forState:UIControlStateNormal];
    [self.nearbyDeviceView setHidden:YES];
    self.lastConnectedDeviceTableViewDelegate.cellType = DEVICE_CONNECTING;
    self.lastConnectedDeviceTableViewDelegate.lastConnectDeivce = self.lastConnectDeivce;
    [self.lastConnectedDeviceTableView reloadData];
}

#pragma mark - Device paired delegate
-(void)newDevice:(DeviceInfo *)device{
    if(self.connecttingDeivce){
        return;
    }
    if(self.lastConnectDeivce != nil && [self.lastConnectDeivce isSameDevice:device]){
        self.lastConnectDeivce.online = YES;
        [self.lastConnectedDeviceTableView reloadData];
    }
    else{
        [self.deviceArray addObject:device];
    }
    [self haveFoundDevice:YES];
}

-(void)deviceDisconnect:(DeviceInfo *)device{
    if(self.connecttingDeivce){
        return;
    }
    if(self.lastConnectDeivce != nil && [self.lastConnectDeivce isSameDevice:device]){
        self.lastConnectDeivce.online = NO;
        [self.lastConnectedDeviceTableView reloadData];
    }
    else{
        [self.deviceArray removeObject:device];
        [self.nearbyDeviceTableView reloadData];
    }
    
    if([self.currentSelectedDeivce isSameDevice:device]){
        self.beginButton.enabled = NO;
    }
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {    // Return the number of sections.
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    // Return the number of rows in the section.
    return self.deviceArray.count;
}


 - (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
     @try {
         DeviceTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"DeviceCell" forIndexPath:indexPath];
         
         // Configure the cell...
         UIView *bgColorView = [[UIView alloc] init];
         bgColorView.backgroundColor = [UIColor colorWithRed:66.0/255 green:133/255.0 blue:221/255.0 alpha:0.3];
         [cell setSelectedBackgroundView:bgColorView];
         
         DeviceInfo *info = self.deviceArray[indexPath.row];
         cell.computerNameLable.text = info.deviceName;
         cell.device = info;
         cell.lastConnectLable.hidden = YES;
         cell.lastConnectTime.hidden = YES;
         
         return cell;
     }
     @catch (NSException *exception) {
         ;
     }
     
 }

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    self.beginButton.enabled = YES;
    
    if(indexPath && indexPath.row < self.deviceArray.count){
        self.currentSelectedDeivce = self.deviceArray[indexPath.row];
    }
    
    if(self.lastConnectedDeviceTableView != nil){
        NSIndexPath *index = self.lastConnectedDeviceTableView.indexPathForSelectedRow;
        if(index){
            [self.lastConnectedDeviceTableView deselectRowAtIndexPath:index animated:NO];
        }
    }
}


 // Override to support conditional editing of the table view.
 - (BOOL)tableView:(UITableView *)tableView canEditRowAtIndexPath:(NSIndexPath *)indexPath {
     // Return NO if you do not want the specified item to be editable.
     
     return YES;
 }


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


#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
}


#pragma mark - private method
-(void)haveFoundDevice:(BOOL)have{
    [self.searchingDeviceLable setHidden:have];
    [self.nearbyDeviceView setHidden:!have];
    [self.nearbyDeviceTableView reloadData];
    
    BOOL showFindPCAnimation = !have && self.lastConnectDeivce == nil;
    if(showFindPCAnimation){
        [self initNotFoundPCGif];
    }
    
    showFindPCAnimation ? [self.findPCGifImageView startAnimating]  : [self.findPCGifImageView stopAnimating];
    [self.findPCGifImageView setHidden:!showFindPCAnimation];
}

-(void)generateDownLoadFileList{
    @try {
        PublicData *publicData = [PublicData sharedInstance];
        AssetsManager *assetManger = [AssetsManager sharedInstance];
        
        if(publicData && assetManger){
            NSArray *photoArray = [assetManger assetsLessThanSize:[publicData willfreeSpaceSize]];
            if(photoArray){
                publicData.downloadFileTotalCount = photoArray.count;
                publicData.havefreedSpaceSize = 0.0;
                [assetManger removeAllCurrentExportedAssets];
            }
            
            NSMutableDictionary *photoDic = [[NSMutableDictionary alloc] init];
            for(id obj in photoArray){
                AssetInfo *info = (AssetInfo*)obj;
                if(info){
                    NSString *path = info.filePath;
                    NSRange range = [path rangeOfString:@"/" options:NSBackwardsSearch];
                    if(range.location != NSNotFound){
                        NSString* key = [path substringToIndex:range.location];
                        NSString* fileName = [path substringFromIndex:range.location + 1];
                        
                        NSMutableDictionary *infoDic = [NSMutableDictionary dictionary];
                        
                        NSString *date = [NSString stringWithFormat:@"%.0f", [info.asset.creationDate timeIntervalSince1970] * 1000];
                        [infoDic setObject:date forKey:@"date"];
                        [infoDic setObject:fileName forKey:@"filename"];
                        [infoDic setObject:[NSNumber numberWithDouble:info.fileSize] forKey:@"size"];
                        
                        NSMutableArray* photoAarray = [photoDic objectForKey:key];
                        if(photoAarray){
                            [photoAarray addObject:infoDic];
                        }
                        else{
                            photoAarray = [[NSMutableArray alloc] init];
                            [photoAarray addObject:infoDic];
                            [photoDic setObject:photoAarray forKey:key];
                        }
                    }
                }
            }
            
            NSData *jsonData = [NSJSONSerialization dataWithJSONObject:photoDic options:NSJSONWritingPrettyPrinted error:nil];
            NSString *jsonString = [[NSString alloc] initWithData:jsonData
                                                         encoding:NSUTF8StringEncoding];
            NSLog(@"%@", jsonString);
            
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_BACKGROUND, 0), ^{
                NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
                NSString *docDirectory = [paths objectAtIndex:0];
                NSString *filePath = [docDirectory stringByAppendingPathComponent:@"filelist.json"];
                publicData.downloadFileListPath = filePath;
                if([jsonData writeToFile:filePath atomically:YES]){
                    //send udp data to pc
                    [self sendStartCmdToPC:filePath];
                }
            });
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) sendStartCmdToPC:(NSString*)filePath{
    @try {
        //send udp data to pc
        NSString *deviceName = [NSString stringWithUTF8String:[[UIDevice currentDevice].name UTF8String]];
        NSString *groupTime = [NSDate date].description;
        NSDictionary *fileListInfoDic = @{@"v" : @"1.0",
                                          @"cmd" : @"kStartDownload",
                                          @"path" : filePath,
                                          @"devicename" : deviceName,
                                          @"groupTime" : groupTime,
                                          @"deviceid" : [[[UIDevice currentDevice] identifierForVendor] UUIDString]};
        
        NSData *fileListjsonData = [NSJSONSerialization dataWithJSONObject:fileListInfoDic options:NSJSONWritingPrettyPrinted error:nil];
        PublicData *publicData = [PublicData sharedInstance];
        if(self.devicePaired && publicData){
            [self.devicePaired sendMessageToConnectDeivce:fileListjsonData device:publicData.connectingDevice];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) showGuideViewAnimation:(BOOL)show showAnimate:(BOOL)showAnimate time:(NSTimeInterval) time{
    if(show){
        [UIView animateWithDuration:time animations:^{
            self.findDeviceTipViewTopConstraint.constant = 0;
            [self.findDeviceTipView layoutIfNeeded];
        }];
    }
    else{
        [UIView animateWithDuration:time animations:^{
            if(showAnimate){
                self.findDeviceTipViewTopConstraint.constant = _findDeviceTipViewHeight - self.findPCTipGifImageView.frame.size.height;
            }
            else {
                self.findDeviceTipViewTopConstraint.constant = _findDeviceTipViewHeight;
            }
            [self.findDeviceTipView layoutIfNeeded];
        } completion:^(BOOL finished) {
        }];
    }

}

-(void) initTableViewDelegate{
    @try {
        self.nearbyDeviceTableView.dataSource = self;
        self.nearbyDeviceTableView.delegate = self;
        
        self.lastConnectedDeviceTableViewDelegate = [[ConnectedDeviceTableView alloc] init];
        self.lastConnectedDeviceTableViewDelegate.lastConnectDeivce = self.lastConnectDeivce;
        self.lastConnectedDeviceTableViewDelegate.cellType = DEVICE_CONNECTED;
        
        __weak typeof (self) weakSelf = self;
        self.lastConnectedDeviceTableViewDelegate.selectedCallback = ^{
            if(weakSelf != nil && weakSelf.nearbyDeviceTableView != nil){
                weakSelf.beginButton.enabled = YES;
                NSIndexPath *index = [weakSelf.nearbyDeviceTableView indexPathForSelectedRow];
                if(index != nil){
                    [weakSelf.nearbyDeviceTableView deselectRowAtIndexPath:index animated:NO];
                    weakSelf.currentSelectedDeivce = weakSelf.lastConnectDeivce;
                }
            }
        };
        
        self.lastConnectedDeviceTableViewDelegate.cancelSaveCallback = ^{
            if(weakSelf != nil){
                [weakSelf.deviceArray addObject:weakSelf.lastConnectDeivce];
                weakSelf.lastConnectDeivce = nil;
                [weakSelf showLastConnectedDeviceTableView];
                weakSelf.showNearbyDevice = NO;
                [weakSelf showNearbyDeviceTableView];
                [weakSelf.nearbyDeviceTableView reloadData];
            }
        };
        
        self.lastConnectedDeviceTableViewDelegate.tryAgainCallback = ^{
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
            NSString *docDirectory = [paths objectAtIndex:0];
            NSString *filePath = [docDirectory stringByAppendingPathComponent:@"filelist.json"];
            if(weakSelf != nil){
                [weakSelf.tryArrangePhotsButton setHidden:YES];
                //send udp data to pc
                [weakSelf sendStartCmdToPC:filePath];
            }
        };
        
        self.lastConnectedDeviceTableViewDelegate.connectTimeout = ^{
            if(weakSelf != nil){
                [weakSelf.tryArrangePhotsButton setHidden:NO];
            }
        };
        
        self.lastConnectedDeviceTableView.dataSource = self.lastConnectedDeviceTableViewDelegate;
        self.lastConnectedDeviceTableView.delegate = self.lastConnectedDeviceTableViewDelegate;
    }
    @catch (NSException *exception) {
        
    }
}

-(void) initNotFoundPCGif{
    @try {
        self.findPCGifImageView.image = [YLGIFImage imageNamed:@"find_computer.gif"];
    }
    @catch (NSException *exception) {
        
    }
}

-(void) initNotFoundPCTipGIf{
    @try {
        self.findPCTipGifImageView.loopCountdown = 1;
        self.findPCTipGifImageView.image = [YLGIFImage imageNamed:@"not_find_one.gif"];
        self.findPCTipGifImageView.gifStopCallback = ^{
            self.findPCTipGifImageView.loopCountdown = 0;
            self.findPCTipGifImageView.image = [YLGIFImage imageNamed:@"not_find_two.gif"];
        };
    }
    @catch (NSException *exception) {
        
    }
}

-(void) showLastConnectedDeviceTableView{
    @try {
        
        self.lastConnectedDeviceTableViewDelegate.lastConnectDeivce = self.lastConnectDeivce;
        if(self.lastConnectDeivce == nil){
            self.lastConnectDeviceViewHeightConstraint.constant = 0;
            [self.lastConnectDeviceView layoutIfNeeded];
            [self.lastConnectDeviceView setHidden:YES];
            [self.lastConnectedDeviceTableView reloadData];
        }
        else{
            [self.lastConnectDeviceView setHidden:NO];
            [self.lastConnectedDeviceTableView reloadData];
            
            self.currentSelectedDeivce = self.lastConnectDeivce;
            if(self.lastConnectDeivce.online){
                self.showNearbyDevice = YES;
                [self showNearbyDeviceTableView];
                
                [self.lastConnectedDeviceTableView selectRowAtIndexPath:[NSIndexPath indexPathForRow:0 inSection:0] animated:YES scrollPosition:UITableViewScrollPositionBottom];
                [self.beginButton setEnabled:YES];
            }
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) startDownLoad:(NSNotification*)notification{
    dispatch_async(dispatch_get_main_queue(), ^{
        _connectDeviceSuccess = YES;
        if(!_switchViewController){
            _switchViewController = YES;
            [self performSegueWithIdentifier:@"start_download_segue" sender:self];
        }
    });
}

-(void)idleTimerFired{
    _idleTimes++;
    if(_idleTimes == 10){
        _showFindDeviceGuideView = NO;
        [self showGuideViewAnimation:_showFindDeviceGuideView showAnimate:YES time:1.0];
        [self initNotFoundPCTipGIf];
        [_idleTimer invalidate];
    }
}
@end
