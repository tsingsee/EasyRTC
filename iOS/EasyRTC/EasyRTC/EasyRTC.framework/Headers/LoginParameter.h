//
//  LoginParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/2.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SignalParameter.h"

@interface LoginParameter : SignalParameter

@property(nonatomic, copy) NSString *login;
@property(nonatomic, copy) NSString *passwd;

+ (LoginParameter *)LoginParameterWithUserName:(NSString *)userName andPwd:(NSString *)passwd;

@end
