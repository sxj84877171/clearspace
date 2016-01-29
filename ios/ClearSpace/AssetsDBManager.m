//
//  AssetsDBManager.m
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "AssetsDBManager.h"
#import "FMDB.h"
#import "AssetInfo.h"

static NSString* TABLE_NAME = @"ExportedAssets";
static NSString* FIELD_ID = @"ID";
static NSString* FIELD_PATH = @"PATH";

@implementation AssetsDBManager{
    FMDatabase *_db;
    FMDatabaseQueue *_dbQueue;
    
    dispatch_queue_t _dataQueue;
}

+(instancetype) sharedInstance{
    static dispatch_once_t once;
    
    static AssetsDBManager* dbManager = nil;
    
    dispatch_once(&once, ^{
        dbManager = [AssetsDBManager new];
    });
    
    return dbManager;
}


-(id) init{
    if(self = [super init]){
        _dataQueue = dispatch_queue_create("com.cleanspace.database.queue", nil);
    }
    
    return self;
}

-(void) recordExportedAsset:(AssetInfo *)asset{
    dispatch_async(_dataQueue, ^{
        NSString *insertSql= [NSString stringWithFormat:
                              @"INSERT INTO '%@' ('%@') VALUES ('%@')",
                              TABLE_NAME, FIELD_PATH, asset.filePath];
        [self syncExcuteSQL:insertSql block:^(bool ret) {
            if (!ret) {
                NSLog(@"error when insert db table");
            } else {
                NSLog(@"success to insert db table");
            }
        }];
    });
}

-(void)removeExportedAsset:(AssetInfo *)asset{
    dispatch_async(_dataQueue, ^{
        NSString *deleteSql = [NSString stringWithFormat:@"DELETE FROM %@ WHERE %@ = '%@'", TABLE_NAME, FIELD_PATH, asset.filePath];
        [self syncExcuteSQL:deleteSql block:^(bool ret) {
            if (!ret) {
                NSLog(@"error when delete db table");
            } else {
                NSLog(@"success to delete db table");
            }
        }];
    });
    
}

-(NSArray*)loadExportedAssets{
    NSString *sql = [NSString stringWithFormat:@"SELECT * FROM %@", TABLE_NAME];
    
    NSMutableArray *array = [NSMutableArray array];
    [self query:sql block:^(FMResultSet *result) {
        while ([result next]) {
            NSString *path = [result stringForColumn:FIELD_PATH];
            [array addObject:path];
        }
    }];
    
    return array;
}

-(BOOL)isAssetExist:(AssetInfo *)asset{
    NSString * sql = [NSString stringWithFormat:
                      @"SELECT * FROM %@ WHERE %@ = '%@'",TABLE_NAME, FIELD_PATH, asset.filePath];
    
    __block BOOL isExist = NO;
    [self query:sql block:^(FMResultSet *result) {
        isExist = [result next];
    }];
    
    return isExist;
}

#pragma mark - operator db method

-(void) initDB{
    [self createDB:@"cleanspace_db"];
    
    NSString *sqlCreateTable =  [NSString stringWithFormat:@"CREATE TABLE IF NOT EXISTS '%@' ('%@' INTEGER PRIMARY KEY AUTOINCREMENT, '%@' TEXT)",TABLE_NAME,FIELD_ID,FIELD_PATH];
    [self createTable:sqlCreateTable];
    
}

-(void) createTable:(NSString*) sql{
    [self syncExcuteSQL:sql block:^(bool ret) {
        if (!ret) {
            NSLog(@"error when creating db table");
        } else {
            NSLog(@"success to creating db table");
        }
    }];
}
@end
