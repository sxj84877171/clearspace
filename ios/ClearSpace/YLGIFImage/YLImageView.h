//
//  YLImageView.h
//  YLGIFImage
//
//  Created by Yong Li on 14-3-2.
//  Copyright (c) 2014年 Yong Li. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface YLImageView : UIImageView

typedef void (^gifAnimationStop) ();

@property (nonatomic, copy) NSString *runLoopMode;

/*modify by jerry begin*/
@property (nonatomic) NSUInteger loopCountdown;
@property (nonatomic, strong) gifAnimationStop gifStopCallback;
/*modify by jerry end*/

@end
