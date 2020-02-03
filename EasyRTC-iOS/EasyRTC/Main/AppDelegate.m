//
//  AppDelegate.m
//  EasyRTC
//
//  Created by leo on 2019/1/12.
//  Copyright © 2019年 easydarwin. All rights reserved.
//

#import "AppDelegate.h"
#import <IQKeyboardManager/IQKeyboardManager.h>
#import <Bugly/Bugly.h>

#import "LoginViewController.h"
#import "MainViewController.h"
#import "NetWorkUtil.h"

@interface AppDelegate ()

@end

@implementation AppDelegate

+ (instancetype) sharedDelegate {
    return (AppDelegate *)[UIApplication sharedApplication].delegate;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // Bugly
    [Bugly startWithAppId:@"38fd960a00"];
    
    // IQKeyboardManager
    IQKeyboardManager *keyboardManager = [IQKeyboardManager sharedManager]; // 获取类库的单例变量
    keyboardManager.enable = YES; // 控制整个功能是否启用
    keyboardManager.shouldResignOnTouchOutside = YES; // 控制点击背景是否收起键盘
    keyboardManager.shouldToolbarUsesTextFieldTintColor = YES; // 控制键盘上的工具条文字颜色是否用户自定义
    keyboardManager.toolbarManageBehaviour = IQAutoToolbarBySubviews; // 有多个输入框时，可以通过点击Toolbar 上的“前一个”“后一个”按钮来实现移动到不同的输入框
    keyboardManager.enableAutoToolbar = YES; // 控制是否显示键盘上的工具条
    keyboardManager.toolbarDoneBarButtonItemText = @"完成";
//    keyboardManager.shouldShowToolbarPlaceholder = YES; // 是否显示占位文字
    keyboardManager.placeholderFont = [UIFont boldSystemFontOfSize:17]; // 设置占位文字的字体
    keyboardManager.keyboardDistanceFromTextField = 10.0f; // 输入框距离键盘的距离
    
    // 1.获取网络权限 根绝权限进行人机交互
    [NetWorkUtil checkNetWork];
    
    // UI逻辑
    CGRect frame = CGRectMake(0, 0, EasyScreenWidth, EasyScreenHeight);
    self.window = [[UIWindow alloc] initWithFrame:frame];
    self.window.backgroundColor = UIColorFromRGB(0xf5f5f5);
    [self.window makeKeyAndVisible];
    
    // 设置UI
    self.rootVC = [[RTRootNavigationController alloc] init];
    
//    NSString *token = [[LoginInfoLocalData sharedInstance] getLoginModel].token;// 获取登录的用户信息
//    NSString *name = [[LoginInfoLocalData sharedInstance] gainName];
//    name = (name == nil ? @"" : name);
//    NSString *pwd = [[LoginInfoLocalData sharedInstance] gainPWD];
//    pwd = (pwd == nil ? @"" : pwd);
//
//    if (![name isEqualToString:@""] && ![pwd isEqualToString:@""] &&
//        token && ![token isEqualToString:@""]) {
//        MainViewController *vc = [[MainViewController alloc] initWithStoryboard];
//        [self.rootVC setViewControllers:@[ vc ]];
//    } else {
//        LoginViewController *vc = [[LoginViewController alloc] initWithStoryborad];
//        [self.rootVC setViewControllers:@[ vc ]];
//    }
    
    LoginViewController *loginVC = [[LoginViewController alloc] initWithStoryborad];
    MainViewController *mainVC = [[MainViewController alloc] init];
    [self.rootVC setViewControllers:@[ mainVC, loginVC ]];
    
    self.window.rootViewController = self.rootVC;
    
    NSString *pname = [[NSProcessInfo processInfo] processName];
    NSLog(@"----->>>>  %@", pname);
    
    return YES;
}

@end
