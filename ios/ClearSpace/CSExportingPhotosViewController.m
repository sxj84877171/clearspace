//
//  CSExportPhotosViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "CSExportingPhotosViewController.h"
#import "CSConst.h"
#import "PublicData.h"
#import "DevicePaired.h"
#import "CircularProgressView.h"
#import "CommonFunctions.h"
#import "AssetsManager.h"
#import "statistics/CSStatisticsFactory.h"

@interface CSExportPhotosViewController ()

@end

@implementation CSExportPhotosViewController{
    BOOL _haveFinished; //NSNotificationCenter 通知是并发的，可能所有图片都已传完，才通知主线程，所以的控制，防止多次页面跳转
    
    CADisplayLink *_displayLink;
    NSTimer *_timeoutMonitor;
    NSDate *_newRequestDate;    //如果三分钟内没有收到新请求，结束本次传输
    
    NSInteger _rate;
    CAGradientLayer *_gradientLayer;
}

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fileDownloadSuccessed:) name:NTF_ID_FILE_DOWNLAOD_SUCCESSED object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fileDownloadNextFile:) name:NTF_ID_FILE_DOWNLAOD_NEXT object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(fileDownloadPartial:) name:NTF_ID_FILE_DOWNLOAD_PARTIAL object:nil];
    
    _currentProcessImagePath.text = @"";
    _freeSpaceLable.text = @"0.00";
    _freeSpaceUnitLable.text = @"GB";
    
    _rate = 1;
    [_progressView updateProgress:0];
    
    _gradientLayer = [CAGradientLayer layer];
    _gradientLayer.bounds = _progressBackgroudView.bounds;
    
    _gradientLayer.frame = _progressBackgroudView.bounds;
    _gradientLayer.colors = @[(id)[UIColor ColorFromHex:0x314a6c alpha:1.0].CGColor, (id)[UIColor ColorFromHex:0x2C77D9 alpha:1.0].CGColor];
    _gradientLayer.startPoint = CGPointMake(0.5, 0.0);
    _gradientLayer.endPoint = CGPointMake(0.5, 1.0);
    [_progressBackgroudView.layer insertSublayer:_gradientLayer atIndex:0];
    
    [[CSStatisticsFactory shareDefaultInstance] onEventCount:UM_GUI_START];
}

- (void) viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    _haveFinished = NO;
    [self.navigationItem setHidesBackButton:YES];
    
    [self.navigationController.navigationBar setBarTintColor:[UIColor ColorFromHex:0x314a6c alpha:1.0]];
    [self initCircleProgressView];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"ExportPhotosViewController"];
}

- (void) viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    PublicData *publicData = [PublicData sharedInstance];
    AssetsManager *assetsManager = [AssetsManager sharedInstance];
    if(publicData && assetsManager){
        _exportImageCount.text = [NSString stringWithFormat:@"%ld/%ld", (unsigned long)assetsManager.currentExportedAssets.count, publicData.downloadFileTotalCount];
        if(publicData.connectingDevice){
            _pcNameLable.text = publicData.connectingDevice.deviceName;
        }
    }
    
    _displayLink = [CADisplayLink displayLinkWithTarget:self selector:@selector(progressRevolveTimer:)];
    [_displayLink addToRunLoop:[NSRunLoop currentRunLoop] forMode:NSRunLoopCommonModes];
    
    _timeoutMonitor = [NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(timeoutMonitorTimer) userInfo:nil repeats:YES];
}

-(void) viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [self.navigationController.navigationBar setBarTintColor:[UIColor ColorFromHex:0x2C77D9 alpha:1.0]];
    
    [_displayLink invalidate];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"ExportPhotosViewController"];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning]; 
    // Dispose of any resources that can be recreated.
}

-(void) dealloc{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

-(void)fileDownloadSuccessed:(NSNotification*)notification{
    PublicData *publicData = [PublicData sharedInstance];
    AssetsManager *assetsManager = [AssetsManager sharedInstance];
    if(publicData && assetsManager){
        dispatch_async(dispatch_get_main_queue() , ^{
            _newRequestDate = [NSDate date];
            _exportImageCount.text = [NSString stringWithFormat:@"%ld/%ld", (unsigned long)assetsManager.currentExportedAssets.count, publicData.downloadFileTotalCount];
            [self fileDownloadNextFile:notification];
            
            [self updateDownloadFileSize];
            if(assetsManager.currentExportedAssets.count == publicData.downloadFileTotalCount && !_haveFinished){
                [self performSegueWithIdentifier:@"export_finished" sender:self];
                _haveFinished = YES;
                [_displayLink invalidate];
                [[CSStatisticsFactory shareDefaultInstance] onEventCount:EXPORT_COMPLETE];
                
            }
        });
    }
}

-(void) fileDownloadNextFile:(NSNotification*)notification{
    if(notification != nil){
        NSDictionary *dic = [notification userInfo];
        NSString *filename = [dic objectForKey:@"file"];
        if(filename != nil){
            NSRange range = [filename rangeOfString:@"/" options:NSBackwardsSearch];
            if(range.location != NSNotFound){
                dispatch_async(dispatch_get_main_queue(), ^{
                    _currentProcessImagePath.text = [filename substringFromIndex:range.location + 1];
                });
            }
        }
    }
}

-(void) fileDownloadPartial:(NSNotification*)notification{
    if(notification != nil){
        NSDictionary *dic = [notification userInfo];
        NSNumber *partialFileSize = [dic objectForKey:@"filesize"];
        PublicData *publicData = [PublicData sharedInstance];
        if(publicData != nil &&  partialFileSize != nil){
            dispatch_async(dispatch_get_main_queue(), ^{
                publicData.havefreedSpaceSize += [partialFileSize unsignedIntegerValue];
                [self updateDownloadFileSize];
            });
        }
    }
}

-(void) updateDownloadFileSize{
    PublicData *publicData = [PublicData sharedInstance];
    if(publicData){
        NSString* size = nil;
        NSString* unit = nil;
        NSDictionary *dic = [CommonFunctions autoConvertFileSize:publicData.havefreedSpaceSize];
        if(dic){
            NSNumber *number = [dic objectForKey:@"size"];
            size = [NSString stringWithFormat:@"%.2f", [number floatValue]];
            unit = [dic objectForKey:@"unit"];
        }
        double dSize = [size doubleValue];
        _freeSpaceLable.text = [NSString stringWithFormat:@"%.d", (int)dSize];
        _freeSpaceDecimalLable.text = [NSString stringWithFormat:@".%02d", (int)((dSize - (int)dSize)*100)];
        _freeSpaceUnitLable.text = unit;
        
        [_progressView updateProgress:publicData.havefreedSpaceSize / publicData.willfreeSpaceSize];
    }

}
#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    self.cancelButton.enabled = NO;
}

- (IBAction)cancelExport:(id)sender {
    PublicData *publicData = [PublicData sharedInstance];
    DevicePaired *devciePaired = [DevicePaired sharedInstance];
    if(publicData && devciePaired){
        NSDictionary *dic = @{@"v":@"1.0", @"cmd":@"KStopDownload"};
        NSData *json = [NSJSONSerialization dataWithJSONObject:dic options:NSJSONWritingPrettyPrinted error:nil];
        [devciePaired sendMessageToConnectDeivce:json device:publicData.connectingDevice];
        [[CSStatisticsFactory shareDefaultInstance] onEventCount:EXPORT_CANCEL];
    }
}

-(void) initCircleProgressView{
    _progressView.lineWidth = 3;
    _progressView.progressColor = [UIColor whiteColor];
    _progressView.progressBackgroudColor = [UIColor ColorFromHex:0xffffff alpha:0.2];
    _progressView.progressFillColor = [UIColor clearColor];
    [_progressView drawProgress:0.0 end:0.0];
}

-(void) progressRevolveTimer:(CADisplayLink *)displayLink{
    CGAffineTransform rotate = CGAffineTransformMakeRotation( _rate * 3.14 / 180  );
    [_progressRevolveImageView setTransform:rotate];
    _rate+=1;
    if(_rate == 360){
        _rate = 1;
    }
}

-(void) timeoutMonitorTimer{
    NSTimeInterval timeInterval = [[NSDate date] timeIntervalSinceDate:_newRequestDate];
    
    CGFloat minutes = timeInterval / 60;
    if(minutes > 3.0){
        [_timeoutMonitor invalidate];
        [self performSegueWithIdentifier:@"export_finished" sender:self];
    }
}
@end
