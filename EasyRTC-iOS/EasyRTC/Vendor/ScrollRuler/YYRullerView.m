//
//  RullerView.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "YYRullerView.h"

@interface YYRullerView() {
    float coarseness;  // 标尺粗
    
    float num_height;  // 数字高度
    float num_top;     // 数字头部位置
    
    float mark_bottom; // 刻度尾部位置
    float short_mark_top;  // 短刻度头部位置
    float long_mark_top;   // 长刻度头部位置
}

@end

@implementation YYRullerView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
    }
    
    return self;
}

#pragma mark - 绘制方法

- (void)drawRect:(CGRect)rect {
    [super drawRect:rect];
    
    CGContextRef context = UIGraphicsGetCurrentContext();
    CGContextSetRGBStrokeColor(context, self.lineColor.R, self.lineColor.G, self.lineColor.B, 1.0);
    CGContextSetLineWidth(context, coarseness);
    
    // 画轴
    CGPoint aPoints[2]; // X轴
    if (_rulerDirection == RulerDirectionHorizontal) {
        aPoints[0] = CGPointMake(0, mark_bottom);              // 起始点
        aPoints[1] = CGPointMake(cy_selfWidth, mark_bottom);   // 终点
    } else if (_rulerDirection == RulerDirectionVertical) {
        aPoints[0] = CGPointMake(mark_bottom, 0);              // 起始点
        aPoints[1] = CGPointMake(mark_bottom, cy_selfHeight);  // 终点
    } else {
        NSAssert(NO, @"error");
    }
    
    CGContextAddLines(context, aPoints, 2);     // 添加线
    CGContextDrawPath(context, kCGPathStroke);  // 根据坐标绘制路径
    
    // 画刻度
    for (NSInteger i = _lockMin / _unitValue; i <= (_lockMax / _unitValue); i++) {
        CGContextSetRGBStrokeColor(context, self.lineColor.R, self.lineColor.G, self.lineColor.B, 1.0);
        CGContextSetLineWidth(context, coarseness);
        CGPoint aPoints[2];// X轴
        if (_rulerDirection == RulerDirectionHorizontal) {
            // 起始点
            aPoints[0] = CGPointMake(_pointerFrame.origin.x + _pointerFrame.size.width / 2.0 + self.unitPX * i,
                                     i % self.step == 0 ? long_mark_top : short_mark_top);
            // 终点
            aPoints[1] = CGPointMake(_pointerFrame.origin.x + _pointerFrame.size.width / 2.0 + self.unitPX * i,
                                     mark_bottom);
        } else if (_rulerDirection == RulerDirectionVertical) {
            // 起始点
            aPoints[0] = CGPointMake(i % self.step == 0 ? long_mark_top : short_mark_top,
                                     _pointerFrame.origin.y + _pointerFrame.size.height / 2.0 + self.unitPX * i);
            // 终点
            aPoints[1] = CGPointMake(mark_bottom,
                                     _pointerFrame.origin.y + _pointerFrame.size.height / 2.0 + self.unitPX * i);
        } else {
            NSAssert(NO, @"error");
        }
        
        CGContextAddLines(context, aPoints, 2);     // 添加线
        CGContextDrawPath(context, kCGPathStroke);  // 根据坐标绘制路径
        
        // 画数值
        if (_isShowRulerValue && i % self.step == 0) {
            NSMutableParagraphStyle *textStyle = [[NSMutableParagraphStyle defaultParagraphStyle] mutableCopy];
            textStyle.lineBreakMode = NSLineBreakByWordWrapping;
            textStyle.alignment = NSTextAlignmentCenter;
            UIFont *font = [UIFont systemFontOfSize:num_height];
            NSDictionary *attributes = @{NSForegroundColorAttributeName:self.txtColor,NSFontAttributeName:font, NSParagraphStyleAttributeName:textStyle};
            if (_rulerDirection == RulerDirectionHorizontal) {
                CGRect r = CGRectMake(_pointerFrame.origin.x + _pointerFrame.size.width / 2.0 + self.unitPX * (i-4),
                                      num_top,
                                      self.unitPX * 8,
                                      num_height);
                
                if (self.isTimeAlias) {
                    [[self toTimeDesc:(i * self.unitValue)] drawInRect:r withAttributes:attributes];
                } else {
                    [@(i * _unitValue).stringValue drawInRect:r withAttributes:attributes];
                }
            } else if (_rulerDirection == RulerDirectionVertical) {
                CGRect r = CGRectMake(num_top,
                                      _pointerFrame.origin.y + _pointerFrame.size.height / 2.0 + self.unitPX * (i-1),
                                      self.unitPX * 8,
                                      num_height);
                if (self.isTimeAlias) {
                    [[self toTimeDesc:(i * self.unitValue)] drawInRect:r withAttributes:attributes];
                } else {
                    [@(i * _unitValue).stringValue drawInRect:r withAttributes:attributes];
                }
            } else {
                NSAssert(NO, @"error");
            }
        }
    }
    
    // 画时间段高亮的部分
    float y = long_mark_top + self.h_height + 2;// 紧贴时间轴下面
    CGContextSetRGBStrokeColor(context, _segmentColor.R, _segmentColor.G, _segmentColor.B, 1.0);
    CGContextSetLineWidth(context, 3);
    
    float startX = _pointerFrame.origin.x + _pointerFrame.size.width / 2.0;
    
    for (int i = 0; i < self.segments.count; i++) {
        TimeSegment *seg = self.segments[i];
        float x0 = startX + (self.unitPX / self.unitValue) * seg.startAt;
        float x1 = startX + (self.unitPX / self.unitValue) * (seg.startAt + seg.duration);
        
        CGPoint aPoints[2];// X轴
        aPoints[0] = CGPointMake(x0, y);// 起始点
        aPoints[1] = CGPointMake(x1, y);// 终点
        
        CGContextAddLines(context, aPoints, 2);     // 添加线
        CGContextDrawPath(context, kCGPathStroke);  // 根据坐标绘制路径
    }
}

#pragma mark - public method

- (void)drawRuler:(Handler)block {
    [self start];
    
    if (block) {
        block();
    }
    
    // UI重绘
    [self setNeedsDisplay];
}

#pragma mark - private method

- (void)start {
//    self.backgroundColor = _rulerBackgroundColor ? _rulerBackgroundColor : [UIColor whiteColor];
    self.backgroundColor = [UIColor whiteColor];
    coarseness = cy_fit(1);
    num_height = cy_fit(24);
    _txtColor = YYUIColorFromRGB(0xDDDDDD);
    _lineColor = customColorMake(221, 221, 221);
    
    if (_rulerDirection == RulerDirectionHorizontal) {
        if (_rulerFace == RulerFace_up_left) {
            mark_bottom = cy_selfHeight / 2.0;
            short_mark_top = mark_bottom - _m_height;
            long_mark_top = mark_bottom - _h_height;
//            num_top = long_mark_top - num_height - cy_fit(self.step);
            num_top = long_mark_top + self.h_height + 8;
        } else if (_rulerFace == RulerFace_down_right) {
            mark_bottom = cy_selfHeight / 2.0;
            short_mark_top = mark_bottom + _m_height;
            long_mark_top = mark_bottom + _h_height;
            num_top = long_mark_top + num_height - cy_fit(self.step);
        } else {
            NSAssert(NO, @"error");
        }
    } else if (_rulerDirection == RulerDirectionVertical) {
        if (_rulerFace == RulerFace_up_left) {
            mark_bottom = cy_selfWidth / 2.0;
            short_mark_top = mark_bottom - _m_height;
            long_mark_top = mark_bottom - _h_height;
            num_top = long_mark_top - self.unitPX * 8 + cy_fit(self.step);
        } else if (_rulerFace == RulerFace_down_right) {
            mark_bottom = cy_selfWidth / 2.0;
            short_mark_top = mark_bottom + _m_height;
            long_mark_top = mark_bottom + _h_height;
            num_top = long_mark_top - cy_fit(self.step);
        } else {
            NSAssert(NO, @"error");
        }
    } else {
        NSAssert(NO, @"error");
    }
}

// c 函数构造结构体
CustomeColor customColorMake(CGFloat R,CGFloat G,CGFloat B) {
    CustomeColor l;
    l.R = R / 255.0;
    l.G = G / 255.0;
    l.B = B / 255.0;
    return l;
}

#pragma mark - 秒数转时间值

- (NSString *) toTimeDesc:(NSInteger) seconds {
    // format of hour
    NSString *str_hour = [NSString stringWithFormat:@"%02ld", seconds / 3600];
    // format of minute
    NSString *str_minute = [NSString stringWithFormat:@"%02ld", (seconds % 3600) / 60];
    // format of time
    NSString *format_time = [NSString stringWithFormat:@"%@:%@", str_hour, str_minute];
    
    return format_time;
}

@end
