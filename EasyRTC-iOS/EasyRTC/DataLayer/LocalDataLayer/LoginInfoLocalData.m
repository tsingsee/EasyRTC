//
//  LoginInfoLocalData.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "LoginInfoLocalData.h"

static NSString *ipKey = @"ipKey";
static NSString *portKey = @"portKey";
static NSString *accountKey = @"accountKey";
static NSString *passwordKey = @"passwordKey";
static NSString *loginModelKey = @"loginModelKey";

@implementation LoginInfoLocalData

#pragma mark - 单例模式

static LoginInfoLocalData *instance;

+ (id) allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [super allocWithZone:zone];
    });
    return instance;
}

+ (instancetype) sharedInstance {
    static dispatch_once_t oncetToken;
    dispatch_once(&oncetToken, ^{
        instance = [[self alloc] init];
    });
    
    return instance;
}

- (id) copyWithZone:(NSZone *)zone {
    return instance;
}

- (instancetype) init {
    if (self = [super init]) {
        _yyCache = [YYCache cacheWithName:LoginInfoDataCache];
    }
    return self;
}

#pragma mark - 帐号密码 信息

- (void) saveName:(NSString *)name psw:(NSString *)psw {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    [defaults setObject:name forKey:accountKey];
    [defaults setObject:psw forKey:passwordKey];
    
    [defaults synchronize];
}

- (NSString *) gainName {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *account = [defaults objectForKey:accountKey];
    
    return account;
}

- (NSString *) gainPWD {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *pwd = [defaults objectForKey:passwordKey];
    
    return pwd;
}

// 清除登录信息
- (void) clearInfo {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    [defaults setObject:@"" forKey:passwordKey];
    [defaults synchronize];
}

- (void) saveIP:(NSString *)ip port:(NSString *)port {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    [defaults setObject:ip forKey:ipKey];
    [defaults setObject:port forKey:portKey];
    
    [defaults synchronize];
}

- (NSString*) gainIPAddress {
//    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
//    NSString *ip = [defaults objectForKey:ipKey];
//    NSString *port = [defaults objectForKey:portKey];
//
//    if (![ip hasSuffix:@"http"]) {
//        ip = [NSString stringWithFormat:@"http://%@", ip];
//    }
//
//    return [NSString stringWithFormat:@"%@:%@", ip, port];
    return @"https://demo.easyrtc.cn/api/v1";
}

- (NSString*) gainIP {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *ip = [defaults objectForKey:ipKey];
    
    return ip;
}

- (NSString*) gainPort {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    NSString *port = [defaults objectForKey:portKey];
    
    return port;
}

#pragma mark - 保存登录后的信息

- (void) saveLoginModel:(LoginModel *)loginModel {
    [_yyCache setObject:loginModel forKey:loginModelKey withBlock:^{
        NSLog(@"setObject sucess");
    }];
}

- (LoginModel *) getLoginModel {
    //根据key读取数据
    LoginModel * model = (LoginModel *) [_yyCache objectForKey:loginModelKey];
    return model;
}

- (void) removeLoginModel {
    [_yyCache removeObjectForKey:loginModelKey];
}

@end
