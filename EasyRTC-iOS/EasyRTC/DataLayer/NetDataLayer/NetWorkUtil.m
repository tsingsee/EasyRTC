//
//  NetWorkUtil.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/17.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "NetWorkUtil.h"
#import <CoreTelephony/CTCellularData.h>
#import "AppDelegate.h"

@implementation NetWorkUtil

#pragma mark - public

+ (void) checkNetWork {
    // 1.获取网络权限 根绝权限进行人机交互
    if (__IPHONE_10_0) {
        [NetWorkUtil networkStatus];
    } else {
        // 2.已经开启网络权限 监听网络状态
    }
}

/*
 CTCellularData在iOS9之前是私有类，权限设置是iOS10开始的，所以App Store审核没有问题
 获取网络权限状态
 */
+ (void)networkStatus {
    //2.根据权限执行相应的交互
    CTCellularData *cellularData = [[CTCellularData alloc] init];
    
    /*
     此函数会在网络权限改变时再次调用
     */
    cellularData.cellularDataRestrictionDidUpdateNotifier = ^(CTCellularDataRestrictedState state) {
        switch (state) {
            case kCTCellularDataRestricted:// Restricted
                // 2.1权限关闭的情况下 再次请求网络数据会弹出设置网络提示
                [self getAppInfo];
                break;
            case kCTCellularDataNotRestricted:// NotRestricted
                // 2.2已经开启网络权限 监听网络状态
                //                [self addReachabilityManager:application didFinishLaunchingWithOptions:launchOptions];
                //                [self getInfo_application:application didFinishLaunchingWithOptions:launchOptions];
                break;
            case kCTCellularDataRestrictedStateUnknown:// Unknown
                // 2.3未知情况 （还没有遇到推测是有网络但是连接不正常的情况下）
                [self getAppInfo];
                break;
                
            default:
                break;
        }
    };
}

+ (void) getAppInfo {
    dispatch_async(dispatch_get_main_queue(), ^{
        UIAlertController *alertControl = [UIAlertController alertControllerWithTitle:@"是否开启网络权限" message:@"您尚未开启网络权限" preferredStyle:UIAlertControllerStyleAlert];
        UIAlertAction *trueAction = [UIAlertAction actionWithTitle:@"开启" style:UIAlertActionStyleDefault handler:^(UIAlertAction * _Nonnull action) {
            [[UIApplication sharedApplication] openURL:[NSURL URLWithString:UIApplicationOpenSettingsURLString]];
        }];
        
        UIAlertAction *cancleAlertion = [UIAlertAction actionWithTitle:@"取消" style:UIAlertActionStyleCancel handler:^(UIAlertAction * _Nonnull action) {
        }];
        
        [alertControl addAction:trueAction];
        [alertControl addAction:cancleAlertion];
        
        [[AppDelegate sharedDelegate].rootVC presentViewController:alertControl animated:true completion:nil];
    });
}

@end
