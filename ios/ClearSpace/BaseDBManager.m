//
//  BaseDBManager.m
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "BaseDBManager.h"
#import "FMDB.h"

@implementation BaseDBManager

-(id) init{
    if(self = [super init]){
        ;
    }
    
    return self;
}


-(void) createDB:(NSString*)dbName{
    @try {
        if(_db == nil){
            NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
            NSString *docDirectory = [paths objectAtIndex:0];
            NSString *database_path = [docDirectory stringByAppendingPathComponent:dbName];
            _db = [FMDatabase databaseWithPath:database_path];
            _dbQueue = [FMDatabaseQueue databaseQueueWithPath:database_path];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) syncExcuteSQL:(NSString*)sql block:(void(^)(bool ret))block{
    @try {
        if([_db open]){
            BOOL ret = [_db executeUpdate:sql];
            [_db close];
            block(ret);
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void) asyncExcuteSQL:(NSString*)sql block:(void(^)(bool ret))block{
    @try {
        if(_dbQueue){
            [_dbQueue inDatabase:^(FMDatabase *db) {
                BOOL ret = [db executeUpdate:sql];
                block(ret);
            }];
        }
    }
    @catch (NSException *exception) {
        
    }
}

-(void)query:(NSString *)sql block:(void(^)(FMResultSet* result))block{
    @try {
        if([_db open]){
            
            block([_db executeQuery:sql]);
            [_db close];
        }
    }
    @catch (NSException *exception) {
        
    }
    
}

@end
