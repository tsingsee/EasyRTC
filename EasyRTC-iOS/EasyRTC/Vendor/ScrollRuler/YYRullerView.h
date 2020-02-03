//
//  RullerView.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "TimeSegment.h"

#pragma mark - 自定义的宏

// 屏幕尺寸
#define cy_ScreenW [UIScreen mainScreen].bounds.size.width
#define cy_ScreenH [UIScreen mainScreen].bounds.size.height

// 视图尺寸
#define cy_selfWidth self.bounds.size.width
#define cy_selfHeight self.bounds.size.height

// px宏除2
#define cy_px(value) (value)/2.0
// 按宽度适配
#define cy_fit(value) (cy_px(value))*cy_ScreenW / 375.0

// RGB颜色转换（16进制->10进制）
#define YYUIColorFromRGB(rgbValue)\
    [UIColor colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 \
        green:((float)((rgbValue & 0xFF00) >> 8))/255.0 \
        blue:((float)(rgbValue & 0xFF))/255.0 \
        alpha:1.0]

#pragma mark - ENUM

typedef NS_ENUM(NSUInteger, RulerDirection) {
    RulerDirectionHorizontal,   // 横向
    RulerDirectionVertical,     // 纵向
};

typedef NS_ENUM(NSUInteger, RulerFace) {
    RulerFace_up_left,          // 横向朝上，纵向朝左
    RulerFace_down_right,       // 横向朝下，纵向朝右
};

#pragma mark - struct

typedef struct LineColor {
    CGFloat R;
    CGFloat G;
    CGFloat B;
} CustomeColor;

// c 函数构造结构体
CustomeColor customColorMake(CGFloat R, CGFloat G, CGFloat B);

typedef void(^Handler)(void);

#pragma mark - RullerView

@interface YYRullerView : UIView

// 长刻度的长度 默认 fit(24)
@property (nonatomic,assign) float h_height;

// 短刻度的长度 默认 fit(12)
@property (nonatomic,assign) float m_height;

// 标尺显示的最小值,默认 0
@property (nonatomic,assign) NSInteger lockMin;

// 标尺显示的最大值，默认 360
@property (nonatomic,assign) NSInteger lockMax;

// 一个刻度代表的值是多少，默认 1
@property (nonatomic,assign) NSInteger unitValue;

// 显示数值的步数，默认10
@property (nonatomic, assign) NSInteger step;

// 标尺单位长度
@property (nonatomic, assign) CGFloat unitPX;

// 是否显示时间轴
@property (nonatomic, assign) BOOL isTimeAlias;

// 默认横向滚动
@property (nonatomic, assign) RulerDirection rulerDirection;

// 默认朝上，朝左
@property (nonatomic, assign) RulerFace rulerFace;

// 是否显示刻度值
@property (nonatomic, assign) BOOL isShowRulerValue;

// 标尺数字颜色
@property (nonatomic, strong) UIColor *txtColor;

// 标尺线的颜色
@property (nonatomic) CustomeColor lineColor;

// 指针位置，默认居中
@property (nonatomic, assign) CGRect pointerFrame;

// 标尺视图的背景颜色 默认白色
@property (nonatomic, strong) UIColor *rulerBackgroundColor;

// 高亮的时间段 的颜色
@property (nonatomic) CustomeColor segmentColor;

// 高亮的时间段 的集合
@property (nonatomic, strong) NSMutableArray<TimeSegment *> *segments;

/**
 UI重绘

 @param block block
 */
- (void)drawRuler:(Handler)block;

@end
