//
//  AssetCell.m
//  ClearSpace
//
//  Created by SW2 on 15/11/26.
//  Copyright © 2015年 SW2. All rights reserved.
//

#import "AssetCell.h"

@implementation AssetCell{
    CGRect _originFrame;
}


#pragma mark - Initialization
- (id)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

- (id)init {
    self = [super init];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)awakeFromNib{
    [self setup];
}

- (void)prepareForReuse {
    [super prepareForReuse];
    [_thumbnailImageView setImage:nil];
    [_movieMarkImageView setHidden:YES];
    [_selectFlagImageView setHidden:YES];
    _cellSelected = NO;
}

-(UIImageView *)thumbnailImageView {
    if (!_thumbnailImageView) {
        
        _thumbnailImageView = [[UIImageView alloc] initWithFrame:CGRectInset(self.frame, 1.0, 1.0)];
        [_thumbnailImageView setAutoresizingMask:(UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight)];
        [_thumbnailImageView setContentMode:UIViewContentModeScaleAspectFill];
        [_thumbnailImageView setClipsToBounds:YES];
    }
    return _thumbnailImageView;
}

- (UIImageView *)movieMarkImageView {
    if (!_movieMarkImageView) {
        _movieMarkImageView = [[UIImageView alloc] initWithFrame:[[self class] preferedMovieMarkRect]];
        [_movieMarkImageView setImage:[[self class] preferedMovieMarkImage]];
        [_movieMarkImageView setAutoresizingMask:(UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight)];
    }
    return _movieMarkImageView;
}

- (UIImageView *)selectFlagImageView{
    if(_selectFlagImageView == nil){
        CGRect rect = [[self class] preferedSelectFlagMarkRect];
        rect.origin.x = self.frame.origin.x + self.frame.size.width - rect.size.width;
        _selectFlagImageView = [[UIImageView alloc] initWithFrame:rect];
        _selectFlagImageView.image = [UIImage imageNamed:@"btn_checked.png"];
        
        [_selectFlagImageView setAutoresizingMask:(UIViewAutoresizingFlexibleWidth | UIViewAutoresizingFlexibleHeight)];
    }
    return _selectFlagImageView;
}

- (void)setup {
    
    CGRect rect = CGRectZero;
    rect.size = self.frame.size;
    self.frame = rect;
    
    [self addSubview:self.thumbnailImageView];
    [self addSubview:self.movieMarkImageView];
    [self addSubview:self.selectFlagImageView];
    
    [_thumbnailImageView setImage:nil];
    [_movieMarkImageView setHidden:YES];
    [_selectFlagImageView setHidden:YES];
    _cellSelected = NO;
}


#pragma mark - Modificators
- (void)markAsSelected:(BOOL)selected {
    _cellSelected = selected;
    if(CGRectIsEmpty(_originFrame)){
        _originFrame = self.frame;
    }
    
    if (selected ) {
        [self.selectFlagImageView setHidden:NO];
        //self.frame = CGRectInset(self.frame, 2, 2);
    } else {
        //self.frame = _originFrame;
        [self.selectFlagImageView setHidden:YES];
    }
}


#pragma mark - Customization

+ (CGRect)preferedSelectFlagMarkRect{
    return CGRectMake(0, 0, 15, 15);
}

+ (CGSize)preferedCellSize {
    return CGSizeMake(74, 74);
}

+ (CGRect)preferedThumbnailRect {
    return CGRectMake(5, 5, 64, 64);
}

+ (CGRect)preferedMovieMarkRect {
    return CGRectMake(46, 46, 20, 20);
}

+ (UIImage *)preferedMovieMarkImage {
    return [UIImage imageNamed:@"movieMark"];
}

+ (UIColor *)preferedBackgroundColorForStateNormal {
    return [UIColor colorWithWhite:0.7 alpha:0.3];
}

+ (UIColor *)preferedBackgroundColorForStateSelected {
    return [UIColor colorWithRed:21.0f/255.0f green:150.0f/255.0f blue:210.0f/255.0f alpha:1.0f];
}
@end
