//
//  ArrangePhotosCollectionHeader.m
//  ClearSpace
//
//  Created by SW2 on 15/12/1.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "ArrangePhotosCollectionHeader.h"

@implementation ArrangePhotosCollectionHeader

- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        //[self setup];
    }
    return self;
}

- (id)init {
    self = [super init];
    if (self) {
        //[self setup];
    }
    return self;
}
-(void)awakeFromNib{
    [self setup];
}

-(void) setup{
    UITapGestureRecognizer *gestureRecognizer = [[UITapGestureRecognizer alloc] initWithTarget:self action:@selector(clickHeader)];
    
    [self addGestureRecognizer:gestureRecognizer];
}

-(void) clickHeader{
    if([self.delegate respondsToSelector:@selector(clickHeader:)]){
        [self.delegate clickHeader:self];
    }
}

-(void)setAssetsSize:(NSString*)assetsSize{
    if(assetsSize != nil){
        _cleanSizeLable.text = [NSString stringWithFormat:@"清理(%@)", assetsSize];
    }
    else{
        _cleanSizeLable.text = @"未找到";
        _cleanSizeLable.textColor = [UIColor grayColor];
    }
}

@end
