//
//  ConnectedDeviceTableView.m
//  ClearSpace
//
//  Created by SW2 on 15/11/17.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "ConnectedDeviceTableView.h"
#import "DeviceTableViewCell.h"
#import "DeviceConnectTableViewCell.h"
#import "DeviceInfo.h"
#import "CommonFunctions.h"

@interface ConnectedDeviceTableView() <DeviceTableViewCellDelegate, DeviceConnectTableViewCellDelegate>


@end

@implementation ConnectedDeviceTableView

#pragma mark - Table view data source

-(id) init{
    if(self = [super init]){
        
    }
    return self;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    if(_cellType == DEVICE_CONNECTED){
        return _lastConnectDeivce != nil ? 1 : 0;
    }
    else{
        return 1;
    }
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return 1;
}


- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    @try {
        if(_cellType == DEVICE_CONNECTED){
            DeviceTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"LastConnectDeviceCell" forIndexPath:indexPath];
            
            UIView *bgColorView = [[UIView alloc] init];
            bgColorView.backgroundColor = [UIColor colorWithRed:66.0/255 green:133/255.0 blue:221/255.0 alpha:0.3];
            [cell setSelectedBackgroundView:bgColorView];
            
            //防止cell选中后清除了button背景
            cell.cancelSaveButton.layer.backgroundColor = [UIColor ColorFromHex:0x803156 alpha:1.0].CGColor;
            
            if(_lastConnectDeivce != nil){
                cell.computerNameLable.text = _lastConnectDeivce.deviceName;
                
                NSDateFormatter *dateFormatter = [[NSDateFormatter alloc] init];
                [dateFormatter setDateFormat:@"yyyy.MM.dd HH:mm"];
                cell.lastConnectTime.text = [dateFormatter stringFromDate:_lastConnectDeivce.connectTime];
                cell.device = _lastConnectDeivce;
                
                cell.userInteractionEnabled = _lastConnectDeivce.online;
            }
            
            cell.cellType = _cellType;
            UILongPressGestureRecognizer *recongnizer = [[UILongPressGestureRecognizer alloc] initWithTarget:self action:@selector(cellLongPress:)];
            [cell addGestureRecognizer:recongnizer];
            cell.delegate = self;
            
            return cell;
        }
        else if(_cellType == DEVICE_CONNECTING){
            DeviceConnectTableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:@"ConnectDeviceCell" forIndexPath:indexPath];
            
            if(_lastConnectDeivce != nil){
                cell.computerNameLable.text = _lastConnectDeivce.deviceName;
            }
            cell.delegate = self;
            return cell;
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    @try {
        if(_cellType == DEVICE_CONNECTED){
            DeviceTableViewCell *cell = [tableView cellForRowAtIndexPath:indexPath];
            if(cell != nil){
                [cell.cancelSaveButton setHidden:YES];
            }
            
            if(self.selectedCallback != nil){
                self.selectedCallback();
            }
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath{
    if(_cellType == DEVICE_CONNECTED){
        DeviceTableViewCell *deviceCell = (DeviceTableViewCell*)cell;
        if(deviceCell != nil && _lastConnectDeivce != nil){
            [deviceCell.disableCellView setHidden:_lastConnectDeivce.online];
        }
    }
}

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

-(void) cellLongPress:(UIGestureRecognizer*)recongnizer{
    if(_cellType == DEVICE_CONNECTED){
        DeviceTableViewCell *cell = (DeviceTableViewCell*)recongnizer.view;
        if(cell){
            [cell.cancelSaveButton setHidden:NO];
            [cell.choosedImageView setHidden:YES];
        }
    }
}

#pragma mark DeviceTableViewCellDelegate
-(void) cancelSaveLastConnectedDevice:(DeviceTableViewCell *)cell{
    if(self.cancelSaveCallback != nil){
        self.cancelSaveCallback();
    }
}

#pragma mark -DeviceConnectTableViewCellDelegate
-(void) connectTryAgain:(DeviceConnectTableViewCell *)cell{
    if (self.tryAgainCallback != nil){
        self.tryAgainCallback();
    }
}

-(void) connectTimeout:(DeviceConnectTableViewCell *)cell{
    if(self.connectTimeout != nil){
        self.connectTimeout();
    }
}

@end
