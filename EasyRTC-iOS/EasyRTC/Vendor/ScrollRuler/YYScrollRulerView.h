//
//  YYScrollRulerView.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//
//  https://www.jianshu.com/p/4760cf287df3
//

#import <UIKit/UIKit.h>
#import "YYRullerView.h"

#pragma mark - RulerDelegate

@protocol RulerDelegate <NSObject>

@required

/**
 回调到处标尺当前刻度值
 
 @param value 刻度值
 */
- (void)rulerValue:(NSInteger)value;

@optional

/**
 标尺滚动结束，回调
 
 @param value 刻度值
 */
- (void)rulerRunEndValue:(NSInteger)value;

- (void) touchesBegan;

@end

#pragma mark - YYScrollRulerView

@interface YYScrollRulerView : UIView

@property (nonatomic, weak) id<RulerDelegate> rulerDelegate;

// 标尺视图的宽度，默认 手机屏幕宽度
@property (nonatomic, assign) float rulerWidth;

// 标尺视图的高度，默认 fit(150)
@property (nonatomic, assign) float rulerHeight;

// 标尺视图的背景颜色 默认白色
@property (nonatomic, strong) UIColor *rulerBackgroundColor;

// 标尺显示的最小值,默认 0
@property (nonatomic, assign) NSInteger lockMin;

// 标尺显示的最大值，默认 360
@property (nonatomic, assign) NSInteger lockMax;

// 标尺加载后，刻度值偏移的位置，默认 10
@property (nonatomic, assign) NSInteger lockDefault;

// 一个刻度代表的值是多少，默认 1
@property (nonatomic, assign) NSInteger unitValue;

// 显示数值的步数，默认10
@property (nonatomic, assign) NSInteger step;

// 是否显示时间轴
@property (nonatomic, assign) BOOL isTimeAlias;

// 默认横向滚动
@property (nonatomic, assign) RulerDirection rulerDirection;

// 默认朝上，朝左
@property (nonatomic, assign) RulerFace rulerFace;

// 是否显示刻度值
@property (nonatomic, assign)BOOL isShowRulerValue;

// 长刻度的长度 默认 fit(24)
@property (nonatomic, assign) float h_height;

// 短刻度的长度 默认 fit(12)
@property (nonatomic, assign) float m_height;

// 指针位置，默认居中
@property (nonatomic, assign) CGRect pointerFrame;

// 当前值的位置，默认居中
@property (nonatomic, assign) CGRect curLabelFrame;

// 指针图片，默认红色细杠
@property (nonatomic, strong) UIImage *pointerImage;

// 指针视图背景颜色
@property (nonatomic, strong) UIColor *pointerBackgroundColor;

// 当前值的颜色
@property (nonatomic, strong) UIColor *curLabelColor;

// 高亮的时间段 的颜色
@property (nonatomic) CustomeColor segmentColor;

// 高亮的时间段 的集合
@property (nonatomic, strong) NSMutableArray<TimeSegment *> *segments;

/**
 经典样式的标尺
 */
- (void)classicRuler;

/**
 自定义样式的标尺
 
 @param lineColor 自定义线颜色，不能为 nil
 @param numColor 自定义数字颜色，默认 0xdddddd
 @param enable 标尺是否可用，默认可用
 */
- (void)customRulerWithLineColor:(CustomeColor)lineColor numColor:(UIColor *)numColor scrollEnable:(BOOL)enable;

/**
 如需生成标尺后，修改标尺参数，此处提供重绘方法
 */
- (void)reDrawerRuler;

/**
 滚动到某个位置
 
 @param value 刻度尺上的某个数值，默认滚动到顶部
 @param flag 是否有动画，默认无
 */
- (void) scrollToValue:(NSInteger)value animation:(BOOL)flag;

@end
