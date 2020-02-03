//
//  LoginInfoLocalData.h
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseLocalData.h"
#import <YYKit/YYKit.h>
#import "LoginModel.h"

/**
 登录信息数据
 */
@interface LoginInfoLocalData : BaseLocalData

@property (nonatomic, retain) YYCache *yyCache;

+ (instancetype) sharedInstance;

// 帐号密码 信息
- (void) saveName:(NSString *)name psw:(NSString *)psw;
- (NSString*) gainName;
- (NSString*) gainPWD;
- (void) clearInfo;// 清除登录信息

- (void) saveIP:(NSString *)ip port:(NSString *)port;
- (NSString*) gainIPAddress;
- (NSString*) gainIP;
- (NSString*) gainPort;

- (void) saveLoginModel:(LoginModel *)loginModel;
- (LoginModel *) getLoginModel;
- (void) removeLoginModel;

@end
