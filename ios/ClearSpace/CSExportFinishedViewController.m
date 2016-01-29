//
//  CSExportFinishedViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/9/17.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "CSExportFinishedViewController.h"
#import "AssetInfo.h"
#import "PublicData.h"
#import "AssetsManager.h"
#import "statistics/CSStatisticsFactory.h"
#import "CommonFunctions.h"
@import Photos;

@interface CSExportFinishedViewController ()

@property (nonatomic) BOOL showGuideView;
@property (nonatomic) CGFloat showGuideLableHeight;
@property (nonatomic) CGFloat showGuideLableWidth;
@property (nonatomic) CGFloat exportedSpeed; //B/s
@property (nonatomic) AssetsManager* assetsManager;
@end

@implementation CSExportFinishedViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view.
    
    PublicData *publicData = [PublicData sharedInstance];
    _assetsManager = [AssetsManager sharedInstance];
    
    if(publicData && _assetsManager){
        self.haveExportCount.text = [NSString stringWithFormat:@"%ld张，", (unsigned long)_assetsManager.currentExportedAssets.count];
        self.leftSpaceSize.text = [NSString stringWithFormat:@"%.1fGB", [CommonFunctions freeDiskSpaceInGB]];
        self.freeSpaceSize.text = [CommonFunctions autoConvertFileSizeToString:publicData.havefreedSpaceSize];
        
        
        if(publicData.connectingDevice){
            self.pcNameLable.text = publicData.connectingDevice.deviceName;
        }
        publicData.startDownLoad = NO;
        NSTimeInterval exportTime = [[NSDate date] timeIntervalSinceDate:publicData.startDownloadDate];
        self.exportedSpeed = publicData.havefreedSpaceSize / exportTime;
    }
    
    [self showGuideViewAnimation:self.showGuideView time:0];
    
    UITapGestureRecognizer* tapGesture = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(showDeleteGuideView:)];
    [self.deleteGuideView addGestureRecognizer:tapGesture];
    
}

- (void) viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    self.showGuideLableHeight = self.deleteGuideLable.frame.size.height;
    self.showGuideLableWidth =self.deleteGuideLable.frame.size.width;
    [self showGuideViewAnimation:self.showGuideView time:0];
    [self.navigationItem setHidesBackButton:YES];
    [self switchController:NO];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"ExportFinishedViewController"];
    
    [self getRankingInfo];
}

-(void) viewDidAppear:(BOOL)animated{
    [super viewDidAppear:animated];
    
    PublicData* publicData = [PublicData sharedInstance];
    if(publicData){
        [publicData stopHttpServer];
    }
    [self saveAssets];
}
-(void) viewWillDisappear:(BOOL)animated{
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"ExportFinishedViewController"];
}
- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}


- (IBAction)ExportFinished:(id)sender {
    [self switchController:YES];
    [self.navigationController popToRootViewControllerAnimated:YES];
}

- (IBAction)showDeleteGuideView:(id)sender {
    self.showGuideView = !self.showGuideView;

    [self showGuideViewAnimation:self.showGuideView time:0.5];
}

- (IBAction)deleteExportedAssets:(id)sender {
    [self deleteAssets];
    [[CSStatisticsFactory shareDefaultInstance] onEventCount:UM_GUI_CLEANALLPIC];

}

#pragma mark - Navigation

// In a storyboard-based application, you will often want to do a little preparation before navigation
- (void)prepareForSegue:(UIStoryboardSegue *)segue sender:(id)sender {
    // Get the new view controller using [segue destinationViewController].
    // Pass the selected object to the new view controller.
    [self switchController:YES];
}


- (void)deleteAssets
{
    if(_assetsManager){
        NSMutableArray *photoAssets = [[NSMutableArray alloc] init];
        for(id obj in _assetsManager.currentExportedAssets){
            AssetInfo *info = (AssetInfo*)obj;
            [photoAssets addObject:info.asset];
        }
        
        self.deleteButton.enabled = NO;
        self.showButton.enabled = NO;
        void (^completionHandler)(BOOL, NSError *) = ^(BOOL success, NSError *error) {
            if (success) {
                dispatch_async(dispatch_get_main_queue(), ^{
                    [_assetsManager removeAllCurrentExportedAssets];
                });
            } else {
                NSLog(@"Error: %@", error);
                dispatch_async(dispatch_get_main_queue(), ^{
                    self.deleteButton.enabled = YES;
                    self.showButton.enabled = YES;
                });
            }
        };
        
        
        // Delete asset from library
        [[PHPhotoLibrary sharedPhotoLibrary] performChanges:^{
            [PHAssetChangeRequest deleteAssets:photoAssets];
        } completionHandler:completionHandler];
    }
}

-(void) saveAssets{
    if(_assetsManager){
        [_assetsManager saveCurrentExportedAssets];
    }
}

-(void) showGuideViewAnimation:(BOOL)show time:(NSTimeInterval) time{
    if(self.showGuideView){
        NSString *text = @"iOS系统会在30天之后自动清除已删除的照片，如果您想立刻腾出空间，请手动打开照片应用，再“最近删除”中选择全部删除。";
        NSRange rang1 = [text rangeOfString:@"30天"];
        NSRange rang2 = [text rangeOfString:@"自动清除"];
        NSRange rang3 = [text rangeOfString:@"手动"];
        NSRange rang4 = [text rangeOfString:@"全部删除"];
        NSMutableAttributedString *str = [[NSMutableAttributedString alloc] initWithString:text];
        [str addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:0 green:142.0/255 blue:37.0/255 alpha:1] range:rang1];
        [str addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:0 green:142.0/255 blue:37.0/255 alpha:1] range:rang2];
        [str addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:1 green:0 blue:0 alpha:1] range:rang3];
        [str addAttribute:NSForegroundColorAttributeName value:[UIColor colorWithRed:1 green:0 blue:0 alpha:1] range:rang4];
        
        [str addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:12.0] range:rang1];
        [str addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:12.0] range:rang2];
        [str addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:12.0] range:rang3];
        [str addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:12.0] range:rang4];
        
        NSMutableParagraphStyle *ps1 = [[NSMutableParagraphStyle alloc] init];
        CGFloat lineSpace = 5;
        [ps1 setLineSpacing:lineSpace];
        [str addAttribute:NSParagraphStyleAttributeName value:ps1 range:NSMakeRange(0,str.length)];
        
        self.deleteGuideLable.attributedText = str;
        [self.deleteGuideLable sizeToFit];
        
        NSStringDrawingOptions options =  NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading;
        CGRect rect = [str boundingRectWithSize:CGSizeMake(self.showGuideLableWidth, CGFLOAT_MAX)
                                            options:options
                                            context:nil];
        
        [UIView animateWithDuration:time animations:^{
            self.deleteGuideViewHeightConstraint.constant = rect.size.height + lineSpace * 3;
            [self.deleteGuideView layoutIfNeeded];
        }];
        
        UIImage *showUpImage = [UIImage imageNamed:@"find_icon_down.png"];
        [self.showGuideButton setImage:showUpImage forState:UIControlStateNormal];
    }
    else{
        [UIView animateWithDuration:time animations:^{
            self.deleteGuideViewHeightConstraint.constant = self.showGuideLableHeight + 10;
            [self.deleteGuideView layoutIfNeeded];
        } completion:^(BOOL finished) {
            //self.deleteGuideLable.numberOfLines = 1;
            NSString *text = @"iOS系统会在30天之后自动彻底清除已删除的照片";
            NSRange rang1 = [text rangeOfString:@"30天"];
            NSMutableAttributedString *str = [[NSMutableAttributedString alloc] initWithString:text];
            [str addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:12.0] range:rang1];
            
            self.deleteGuideLable.attributedText = str;
            [self.deleteGuideLable sizeToFit];
        }];
        
        UIImage *showUpImage = [UIImage imageNamed:@"find_icon_top.png"];
        [self.showGuideButton setImage:showUpImage forState:UIControlStateNormal];
    }
    
}

-(void)switchController:(BOOL)bSwitch{
    self.completeButton.enabled = !bSwitch;
    self.deleteButton.enabled = !bSwitch;
    self.showButton.enabled = !bSwitch;
}

-(void)getRankingInfo{
    @try {
        
        PublicData* publicData = [PublicData sharedInstance];
        if(publicData && _assetsManager){
            NSString *fileSize = [NSString stringWithFormat:@"%lld", publicData.havefreedSpaceSize];
            NSString *filecount = [NSString stringWithFormat:@"%ld", (unsigned long)_assetsManager.currentExportedAssets.count];
            
            NSString *url = [NSString stringWithFormat:@"http://114.215.236.240:8080/metrics/sort?app_id=photomaster&device_id=%@&transfer_size=%@&transfer_count=%@&speed=%.0f&clean_photo_count=0&clean_space=0", [CommonFunctions deviceID], fileSize, filecount, self.exportedSpeed / 1024];
            NSURLRequest *request = [NSURLRequest requestWithURL:[NSURL URLWithString:url] cachePolicy:NSURLRequestUseProtocolCachePolicy timeoutInterval:0];
            [NSURLConnection sendAsynchronousRequest:request queue:[[NSOperationQueue alloc] init] completionHandler:^(NSURLResponse * _Nullable response, NSData * _Nullable data, NSError * _Nullable connectionError) {
                NSDictionary *json = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
                
                if(json && json[@"status"] && json[@"status"] >=0 && json[@"sortdata"]){
                    NSDictionary *info = json[@"sortdata"];
                    
                    dispatch_async(dispatch_get_main_queue(), ^{
                        self.historyExportedCountLabel.text = [[info objectForKey:@"total_transfercount"] stringValue];
                        self.exportedCountRanking.text = [[info objectForKey:@"your_transfer_count_rank"] stringValue];
                        self.historyExportedSizeLabel.text = [CommonFunctions autoConvertFileSizeToString:[[info objectForKey:@"total_transfersize"] unsignedLongLongValue]];
                        self.exportedSizeRanking.text = [[info objectForKey:@"your_transfersize_rank"] stringValue];
                        
                        NSDictionary* dic = [CommonFunctions autoConvertFileSize:self.exportedSpeed];
                        if(dic != nil){
                            NSString *number = [dic objectForKey:@"size"];
                            NSString *unit = [dic objectForKey:@"unit"];
                            self.exportAverageSpeedLable.text = [NSString stringWithFormat:@"%.1f", [number floatValue]];
                            self.exportAverageSpeedUnitLable.text = [unit stringByAppendingString:@"/s"];
                        }
                        
                        NSString *title = [self getTitle];
                        NSString *titleString = [NSString stringWithFormat:@"特此授予您%@称号", title];
                        NSMutableAttributedString* attriTitleString = [[NSMutableAttributedString alloc]initWithString:titleString];
                        NSRange range = [titleString rangeOfString:title];
                        [attriTitleString addAttribute:NSForegroundColorAttributeName value:[UIColor ColorFromHex:0xffc258 alpha:1.0] range:range];
                        [attriTitleString addAttribute:NSFontAttributeName value:[UIFont boldSystemFontOfSize:12] range:range];
                        self.exportTitleLevel.attributedText = attriTitleString;
                        
                        int mySpeedRank = [[info objectForKey:@"your_speed_rank"] intValue];
                        int totalSpeedRank = [[info objectForKey:@"total_speed_rank"] intValue];
                        NSString *ranking = [NSString stringWithFormat:@"%d%%", mySpeedRank * 100 / totalSpeedRank];
                        NSString *speedCompare = [NSString stringWithFormat:@"太快了！您的速度击败了全球%@的用户", ranking];
                        NSMutableAttributedString* attriTitleString1 = [[NSMutableAttributedString alloc]initWithString:speedCompare];
                        NSRange range1 = [speedCompare rangeOfString:ranking];
                        [attriTitleString1 addAttribute:NSForegroundColorAttributeName value:[UIColor ColorFromHex:0xffc258 alpha:1.0] range:range1];
                        self.exportSpeedCompared.attributedText = attriTitleString1;
                    });
                }
            }];
        }
        
    }
    @catch (NSException *exception) {
        
    }
}

-(NSString*)getTitle{
    NSInteger M = 1024 * 1024;
    if(_exportedSpeed < 200 * 1024){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_1.png"]];
        return @"赤脚大仙";
    }
    else if(_exportedSpeed < M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_2.png"]];
        return @"摩擦摩擦滑板鞋";
    }
    else if(_exportedSpeed < 2 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_3.png"]];
        return @"永久28自行车";
    }
    else if(_exportedSpeed < 3 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_4.png"]];
        return @"雅迪电动车";
    }
    else if(_exportedSpeed < 4 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_5.png"]];
        return @"夏利旗舰版";
    }
    else if(_exportedSpeed < 5 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_6.png"]];
        return @"桑塔纳2000";
    }
    else if(_exportedSpeed < 6 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_7.png"]];
        return @"法拉利";
    }
    else if(_exportedSpeed < 7 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_8.png"]];
        return @"波音飞机";
    }
    else if(_exportedSpeed < 8 * M){
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_9.png"]];
        return @"长征号火箭";
    }
    else{
        [self.titleImage setImage:[UIImage imageNamed:@"img_title_10.png"]];
        return @"宇宙飞船";
    }
}
@end
