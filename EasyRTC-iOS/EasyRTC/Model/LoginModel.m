//
//  LoginModel.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "LoginModel.h"

@implementation LoginModel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"token" : @"Token",
              @"tokenTimeout" : @"TokenTimeout"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    LoginModel *model = [LoginModel modelWithDictionary:dict];
    
    return model;
}

@end
