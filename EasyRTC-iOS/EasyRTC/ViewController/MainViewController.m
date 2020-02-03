//
//  MainViewController.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/12.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "MainViewController.h"
#import <RTRootNavigationController/RTRootNavigationController.h>
#import "RoomViewController.h"
#import "LiveViewController.h"
#import "RecordViewController.h"

@interface MainViewController ()

@end

@implementation MainViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.tabBar.barTintColor = [UIColor whiteColor];
    
    NSDictionary *normalDict = @{ NSForegroundColorAttributeName:UIColorFromRGB(EasyTextGrayColor) };
    NSDictionary *themeDict = @{ NSForegroundColorAttributeName:UIColorFromRGB(EasyThemeColor) };
    
    RoomViewController *homeVC = [[RoomViewController alloc] initWithStoryborad];
    homeVC.tabBarItem.title = @"会议";
    homeVC.tabBarItem.image = [[UIImage imageNamed:@"room"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    homeVC.tabBarItem.selectedImage = [[UIImage imageNamed:@"room_click"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    [homeVC.tabBarItem setTitleTextAttributes:normalDict forState:UIControlStateNormal];
    [homeVC.tabBarItem setTitleTextAttributes:themeDict forState:UIControlStateSelected];
    
    LiveViewController *newsVC = [[LiveViewController alloc] initWithStoryborad];
    newsVC.tabBarItem.title = @"直播";
    newsVC.tabBarItem.image = [[UIImage imageNamed:@"play"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    newsVC.tabBarItem.selectedImage = [[UIImage imageNamed:@"play_click"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    [newsVC.tabBarItem setTitleTextAttributes:normalDict forState:UIControlStateNormal];
    [newsVC.tabBarItem setTitleTextAttributes:themeDict forState:UIControlStateSelected];
    
    RecordViewController *mallVC = [[RecordViewController alloc] initWithStoryborad];
    mallVC.tabBarItem.title = @"回看";
    mallVC.tabBarItem.image = [[UIImage imageNamed:@"record"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    mallVC.tabBarItem.selectedImage = [[UIImage imageNamed:@"record_click"] imageWithRenderingMode:UIImageRenderingModeAlwaysOriginal];
    [mallVC.tabBarItem setTitleTextAttributes:normalDict forState:UIControlStateNormal];
    [mallVC.tabBarItem setTitleTextAttributes:themeDict forState:UIControlStateSelected];
    
    //    self.viewControllers = @[ [[RTRootNavigationController alloc] initWithRootViewController:homeVC],
    //                              [[RTRootNavigationController alloc] initWithRootViewController:categoryVC],
    //                              [[RTRootNavigationController alloc] initWithRootViewController:shopVC],
    //                              [[RTRootNavigationController alloc] initWithRootViewController:mineVC] ];
    
    self.viewControllers = @[ homeVC, newsVC, mallVC ];
}

-(void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

#pragma mark - override

- (BOOL)prefersStatusBarHidden {
    return self.selectedViewController.prefersStatusBarHidden;
}

- (UIStatusBarAnimation)preferredStatusBarUpdateAnimation {
    return self.selectedViewController.preferredStatusBarUpdateAnimation;
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return self.selectedViewController.preferredStatusBarStyle;
}

@end
