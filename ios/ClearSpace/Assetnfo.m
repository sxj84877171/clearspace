//
//  AssetInfo.m
//  ClearSpace
//
//  Created by SW2 on 15/9/15.
//  Copyright (c) 2015年 SW2. All rights reserved.
//

#import "AssetInfo.h"

@implementation AssetInfo

-(id) init{
    if(self = [super init]){
        ;
    }
    
    return self;
}

-(NSString*) fileName{
    NSString *fileName = nil;
    if(_filePath){
        NSRange range = [_filePath rangeOfString:@"/" options:NSBackwardsSearch];
        if(range.location != NSNotFound){
            fileName = [_filePath substringFromIndex:range.location + 1];
        }
    }
    
    return fileName;
}

@end
