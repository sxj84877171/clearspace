//
//  BaseDBManager.h
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import <Foundation/Foundation.h>

@class FMDatabase;
@class FMDatabaseQueue;
@class FMResultSet;

@interface BaseDBManager : NSObject

@property (nonatomic) FMDatabase *db;
@property (nonatomic) FMDatabaseQueue *dbQueue;

-(void) createDB:(NSString*)dbName;
-(void) syncExcuteSQL:(NSString*)sql block:(void(^)(bool ret))block;
-(void) asyncExcuteSQL:(NSString*)sql block:(void(^)(bool ret))block;
-(void) query:(NSString*)sql block:(void(^)(FMResultSet* result))block;

@end
