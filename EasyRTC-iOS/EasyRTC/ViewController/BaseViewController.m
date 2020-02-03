//
//  BaseViewController.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"
#import "LoginViewController.h"
#import "MBProgressHUDTool.h"
#import "AppDelegate.h"
//#import "LoginViewModel.h"

@interface BaseViewController ()

@property (nonatomic, retain) MBProgressHUDTool *progressHUD;

@end

@implementation BaseViewController

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    BaseViewController *viewController = [super allocWithZone:zone];
    
    @weakify(viewController)
    
    [[viewController rac_signalForSelector:@selector(viewDidLoad)] subscribeNext:^(id x) {
        @strongify(viewController)
        
        [viewController bindViewModel];
    }];
    
    return viewController;
}

#pragma mark - life cycle

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor whiteColor];
    self.navigationController.navigationBar.barTintColor = UIColorFromRGB(EasyThemeColor);// 导航栏背景颜色
    self.navigationController.navigationBar.tintColor = [UIColor whiteColor];// item字体颜色
    
    [self.navigationController.navigationBar setTitleTextAttributes:@{NSForegroundColorAttributeName:[UIColor whiteColor]}];
    // 默认使用的是RTRoot框架内部的导航效果和返回按钮，如果要自定义，必须将此属性设置为NO，然后实现下方方法；
    self.rt_navigationController.useSystemBackBarButtonItem = NO;
    
    UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"back"] style:UIBarButtonItemStyleDone target:self action:@selector(back)];
    self.parentViewController.navigationItem.leftBarButtonItem = item;
    self.navigationItem.leftBarButtonItem = item;
}

- (void)back {
    [self.navigationController popViewControllerAnimated:YES];
}

- (void) viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    
    self.isCanPushViewController = YES;// 每次进入，重新置为YES,表示可以push ViewController
}

-(void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    
    NSLog(@"%@ dealloc", NSStringFromClass([self class]));
}

- (UIBarButtonItem *)rt_customBackItemWithTarget:(id)target action:(SEL)action {
    UIButton *btn = [UIButton buttonWithType:UIButtonTypeCustom];
    
    if (self.isHideBack) {
        [btn setImage:[UIImage imageNamed:@""] forState:UIControlStateNormal];
    } else {
        [btn setImage:[UIImage imageNamed:@"back"] forState:UIControlStateNormal];
    }
    
    [btn sizeToFit];
    [btn addTarget:target action:action forControlEvents:UIControlEventTouchUpInside];
    
    return [[UIBarButtonItem alloc] initWithCustomView:btn];
}

#pragma mark - StatusBar

- (UIStatusBarStyle) preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

#pragma mark - MBProgressHUD

- (void) showHubWithLoadText:(NSString *) text {
    [self.progressHUD showHubWithLoadText:text superView:self.view];
}

- (void) showHub {
    [self.progressHUD showHubWithLoadText:@"查询中..." superView:self.view];
}

- (void) hideHub {
    [self.progressHUD hideHub];
}

- (void) showTextHubWithContent:(NSString *) content {
    [self.progressHUD showTextHubWithContent:content];
}

- (MBProgressHUDTool *)progressHUD {
    if (!_progressHUD) {
        _progressHUD = [[MBProgressHUDTool alloc] init];
    }
    
    return _progressHUD;
}

#pragma mark - 操作前 需要登录/Token有效

- (void) loginFirstWithCommend:(RACCommand *)commend {
//    // 获取帐号信息
//    NSString *name = [[LoginInfoLocalData sharedInstance] gainName];
//    NSString *psw = [[LoginInfoLocalData sharedInstance] gainPWD];
//
//    if (name && ![name isEqualToString:@""] &&
//        psw && ![psw isEqualToString:@""]) {
//        // 有帐号密码，则需要更新Token
//        LoginViewModel *loginViewModel = [[LoginViewModel alloc] init];
//
//        Account *account = [[Account alloc] init];
//        account.name = name;
//        account.pwd = psw;
//        loginViewModel.account = account;
//
//        [loginViewModel.loginCommand execute:nil];
//
//        [loginViewModel.loginResultSubject subscribeNext:^(id x) {
//            if ([x isKindOfClass:[NSString class]]) { // 登录失败
//                [self toLoginViewWithCommend:commend];
//            } else {// 登录成功
//                if (commend) {
//                    [commend execute:nil];
//                }
//            }
//        }];
//    } else {
//        [self toLoginViewWithCommend:commend];// 没有帐号密码，则需要登录
//    }
}

- (void) toLoginViewWithCommend:(RACCommand *)commend {
//    LoginViewController *controller = [[LoginViewController alloc] initWithStoryborad];
//    [controller.loginSuccessSubject subscribeNext:^(id x) {
//        if (commend) {
//            [commend execute:nil];// 登录成功之后的操作
//        }
//    }];
//    RTRootNavigationController *naviController = [[RTRootNavigationController alloc] initWithRootViewController:controller];
//    [self presentViewController:naviController animated:YES completion:nil];
}

#pragma mark - getter

- (LoginModel *) loginModel {
    _loginModel = [[LoginInfoLocalData sharedInstance] getLoginModel];// 获取登录的用户信息
    
    return _loginModel;
}

#pragma mark - public method

- (void) basePushViewController:(UIViewController *)controller {
    [self basePushViewController:controller removeSelf:NO];
}

- (void) basePushViewController:(UIViewController *)controller removeSelf:(BOOL)remove {
    if (self.isCanPushViewController) {
        self.isCanPushViewController = NO;
        
        // 注意这里push的时候需要使用rt_navigation push出去
//        [self.navigationController pushViewController:controller animated:YES];
        [self.rt_navigationController pushViewController:controller animated:YES complete:^(BOOL finished) {
            if (remove) {
                [self.rt_navigationController removeViewController:self];
            }
        }];
    }
}

@end
