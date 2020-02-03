//
//  BaseViewController.h
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <RTRootNavigationController/RTRootNavigationController.h>
#import "LoginInfoLocalData.h"
#import "LoginModel.h"

@protocol EasyViewControllerProtocol <NSObject>

@optional

- (void)bindViewModel;

@end

@interface BaseViewController : UIViewController<EasyViewControllerProtocol>

@property (nonatomic, assign) BOOL isCanPushViewController;// 保证push ViewController只会被调用一次

@property (nonatomic, retain) LoginModel *loginModel;
@property (nonatomic, assign) BOOL isHideBack;

- (void) showHub;
- (void) showHubWithLoadText:(NSString *) text;
- (void) showTextHubWithContent:(NSString *) content;
- (void) hideHub;

/**
 操作前，先登录
 */
- (void) loginFirstWithCommend:(RACCommand *)commend;

// 统一的push处理
- (void) basePushViewController:(UIViewController *)controller;
- (void) basePushViewController:(UIViewController *)controller removeSelf:(BOOL)remove;

@end
