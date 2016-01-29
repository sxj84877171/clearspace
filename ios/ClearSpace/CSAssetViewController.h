//
//  CSAssetViewController.h
//  ClearSpace
//
//  Created by SW2 on 15/11/27.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
@class AssetInfo;

@interface CSAssetViewController : UIViewController
@property (weak, nonatomic) IBOutlet UIImageView *imageView;

@property(nonatomic) AssetInfo* asset;

@end
