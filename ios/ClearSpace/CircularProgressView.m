//
//  CircularProgressView.m
//  CircularProgressView

#import "CircularProgressView.h"
#import "CommonFunctions.h"

@interface CircularProgressView ()

@property (nonatomic) NSTimer *timer;
@property (assign, nonatomic) float progress;
@property (assign, nonatomic) CGFloat angle;//angle between two lines

@property (nonatomic) CAShapeLayer *shapeBackgroudLayer;
@property (nonatomic) CAShapeLayer *shapeLayer;
@end

@implementation CircularProgressView

- (void) awakeFromNib{
    self.backgroundColor = [UIColor clearColor];
    
    _progress = 0.5;
    _progressFillColor = [UIColor clearColor];;
    _progressColor = [UIColor whiteColor];
    _progressBackgroudColor = [UIColor whiteColor];
    _lineWidth = 0;
    
    _shapeLayer = [CAShapeLayer layer];
    _shapeLayer.frame = CGRectMake(0, 0, self.frame.size.width, self.frame.size.height);
    _shapeLayer.position = CGPointMake(self.frame.size.width / 2, self.frame.size.height / 2);
    
    _shapeBackgroudLayer = [CAShapeLayer layer];
    _shapeBackgroudLayer.frame = _shapeLayer.frame;
    _shapeBackgroudLayer.position = _shapeLayer.position;
    
    [self.layer insertSublayer:_shapeLayer atIndex:0];
    [self.layer insertSublayer:_shapeBackgroudLayer below:_shapeLayer];
}

-(void) drawProgress:(CGFloat)strokeStart end:(CGFloat)strokeEnd{
    if (_shapeBackgroudLayer && _progressBackgroudColor) {
        _shapeBackgroudLayer.fillColor = _progressFillColor.CGColor;
        _shapeBackgroudLayer.lineWidth = _lineWidth;
        _shapeBackgroudLayer.strokeColor = _progressBackgroudColor.CGColor;
        UIBezierPath *path = [UIBezierPath bezierPathWithOvalInRect:_shapeBackgroudLayer.frame];
        _shapeBackgroudLayer.path = path.CGPath;
    }
    if (_shapeLayer) {
        _shapeLayer.fillColor = _progressFillColor.CGColor;
        _shapeLayer.lineWidth = _lineWidth;
        _shapeLayer.strokeColor = _progressColor.CGColor;
        CGFloat radius = _shapeLayer.frame.size.width / 2;
        UIBezierPath *path = [UIBezierPath bezierPathWithArcCenter:CGPointMake(radius, radius) radius:radius startAngle:(-3.14/2) endAngle:(3*3.14/2) clockwise:YES];
        _shapeLayer.strokeStart = strokeStart;
        _shapeLayer.strokeEnd = strokeEnd;
        _shapeLayer.path = path.CGPath;
    }
}

- (void)updateProgress:(float)progress{
    if(_shapeLayer){
        _shapeLayer.strokeEnd = progress;
    }
}
/*- (void)drawRect:(CGRect)rect
{
    [super drawRect:rect];
    
    //draw background circle
    UIBezierPath *backCircle = [UIBezierPath bezierPathWithArcCenter:CGPointMake(CGRectGetWidth(self.bounds) / 2, CGRectGetHeight(self.bounds) / 2)
                                                              radius:(CGRectGetWidth(self.bounds) - self.lineWidth) / 2
                                                          startAngle:(CGFloat) - M_PI_2
                                                            endAngle:(CGFloat)(1.5 * M_PI)
                                                           clockwise:YES];
    [self.backColor setFill];
    backCircle.lineWidth = self.lineWidth;
    [backCircle fill];
    
    if (self.progress) {
        //draw progress circle
        UIBezierPath *progressCircle = [UIBezierPath bezierPathWithArcCenter:CGPointMake(CGRectGetWidth(self.bounds) / 2,CGRectGetHeight(self.bounds) / 2)
                                                                      radius:(CGRectGetWidth(self.bounds) - self.lineWidth) / 2
                                                                  startAngle:(CGFloat) - M_PI_2
                                                                    endAngle:(CGFloat)(- M_PI_2 + self.progress * 2 * M_PI)
                                                                   clockwise:YES];
        [self.progressColor setStroke];
        progressCircle.lineWidth = self.lineWidth;
        [progressCircle stroke];
    }
}

- (void)updateProgress:(float)progress{
    //update progress value
    self.progress = progress;
    //redraw back & progress circles
    [self setNeedsDisplay];
}

//calculate angle between start to point
- (CGFloat)angleFromStartToPoint:(CGPoint)point{
    CGFloat angle = [self angleBetweenLinesWithLine1Start:CGPointMake(CGRectGetWidth(self.bounds) / 2,CGRectGetHeight(self.bounds) / 2) Line1End:CGPointMake(CGRectGetWidth(self.bounds) / 2,CGRectGetHeight(self.bounds) / 2 - 1) Line2Start:CGPointMake(CGRectGetWidth(self.bounds) / 2,CGRectGetHeight(self.bounds) / 2) Line2End:point];
    if (CGRectContainsPoint(CGRectMake(0, 0, CGRectGetWidth(self.frame) / 2, CGRectGetHeight(self.frame)), point)) {
        angle = 2 * M_PI - angle;
    }
    return angle;
}


//calculate angle between 2 lines
- (CGFloat)angleBetweenLinesWithLine1Start:(CGPoint)line1Start
                                  Line1End:(CGPoint)line1End
                                Line2Start:(CGPoint)line2Start
                                  Line2End:(CGPoint)line2End{
    CGFloat a = line1End.x - line1Start.x;
    CGFloat b = line1End.y - line1Start.y;
    CGFloat c = line2End.x - line2Start.x;
    CGFloat d = line2End.y - line2Start.y;
    return acos(((a*c) + (b*d)) / ((sqrt(a*a + b*b)) * (sqrt(c*c + d*d))));
}*/
@end
