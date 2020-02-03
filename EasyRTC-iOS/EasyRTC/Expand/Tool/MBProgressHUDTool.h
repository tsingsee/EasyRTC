//
//  MBProgressHUDTool.h
//  Easy
//
//  Created by leo on 2017/6/27.
//  Copyright © 2017年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MBProgressHUD.h"

@interface MBProgressHUDTool : NSObject

#pragma mark - 显示加载动画

/**
 显示加载动画

 @param text 动画的提示信息
 @param view 父view
 */
- (void) showHubWithLoadText:(NSString *) text superView:(UIView *)view;

/**
 取消加载动画
 */
- (void) hideHub;

#pragma mark - 显示提示信息

/**
 显示提示信息,默认显示在window上

 @param content 提示信息
 */
- (void) showTextHubWithContent:(NSString *) content;

/**
 显示提示信息,显示在view上

 @param content 提示信息
 @param view 父view
 */
- (void) showTextHubWithContent:(NSString *) content superView:(UIView *)view;

@end
