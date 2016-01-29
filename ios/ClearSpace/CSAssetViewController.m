//
//  CSAssetViewController.m
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "CSAssetViewController.h"
#import "AssetInfo.h"
#import "statistics/CSStatisticsFactory.h"

@implementation CSAssetViewController{
    
    CGSize _lastImageViewSize;
}

#pragma mark - ViewController delegate
-(void)viewDidLoad{
    [super viewDidLoad];
}

-(void)viewWillAppear:(BOOL)animated{
    [super viewWillAppear:animated];
    
    if(_asset){
        [self.navigationItem setTitle:_asset.fileName];
    }
    [self.navigationItem.backBarButtonItem setTitle:@""];
    
    [self updateImage];
    [[CSStatisticsFactory shareDefaultInstance] viewWillAppear:@"CSAssetViewController"];
}

-(void)viewWillDisappear:(BOOL)animated{
    [super viewWillDisappear:animated];
    [[CSStatisticsFactory shareDefaultInstance] viewWillDisappear:@"CSAssetViewController"];
}
- (void)viewWillLayoutSubviews
{
    [super viewWillLayoutSubviews];
    
    if (!CGSizeEqualToSize(self.imageView.bounds.size, _lastImageViewSize)) {
        [self updateImage];
    }
}

- (void)updateImage
{
    _lastImageViewSize = self.imageView.bounds.size;
    
    CGFloat scale = [UIScreen mainScreen].scale;
    CGSize targetSize = CGSizeMake(CGRectGetWidth(self.imageView.bounds) * scale, CGRectGetHeight(self.imageView.bounds) * scale);
    
    PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
    
    // Download from cloud if necessary
    options.networkAccessAllowed = YES;
    options.progressHandler = ^(double progress, NSError *error, BOOL *stop, NSDictionary *info) {
        dispatch_async(dispatch_get_main_queue(), ^{
            //self.progressView.progress = progress;
            //self.progressView.hidden = (progress <= 0.0 || progress >= 1.0);
        });
    };
    
    [[PHImageManager defaultManager] requestImageForAsset:_asset.asset targetSize:targetSize contentMode:PHImageContentModeAspectFit options:options resultHandler:^(UIImage *result, NSDictionary *info) {
        if (result) {
            self.imageView.image = result;
        }
    }];
}

@end
