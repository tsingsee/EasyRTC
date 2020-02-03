//
//  LoginModel.h
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseModel.h"

/**
 登录成功后的实体类
 */
@interface LoginModel : BaseModel

@property (nonatomic, copy) NSString *token;          // 用户票据编码
@property (nonatomic, copy) NSString *tokenTimeout;

@end
