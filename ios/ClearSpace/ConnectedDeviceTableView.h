//
//  ConnectedDeviceTableView.h
//  ClearSpace
//
//  Created by SW2 on 15/11/17.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import "PublicStruct.h"

@class DeviceInfo;

typedef void  (^ConnectedDeviceTableView_RowSelected) ();
typedef void  (^ConnectedDeviceTableView_CancelSave) ();
typedef void  (^ConnectedDeviceTableView_connectTimeout) ();
typedef void  (^ConnectedDeviceTableView_tryAgain) ();


@interface ConnectedDeviceTableView : NSObject<UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) ConnectedDeviceTableView_RowSelected selectedCallback;
@property (nonatomic, strong) ConnectedDeviceTableView_CancelSave cancelSaveCallback;
@property (nonatomic, strong) ConnectedDeviceTableView_tryAgain tryAgainCallback;
@property (nonatomic, strong) ConnectedDeviceTableView_connectTimeout connectTimeout;
@property (nonatomic) DeviceInfo* lastConnectDeivce;
@property (assign, nonatomic) DeviceCellType cellType;
@end
