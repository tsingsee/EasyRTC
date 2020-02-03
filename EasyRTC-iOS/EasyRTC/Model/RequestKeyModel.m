//
//  RequestKeyModel.m
//  EasyRTC
//
//  Created by liyy on 2020/2/1.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "RequestKeyModel.h"

@implementation RequestKeyModel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"requestKey" : @"RequestKey"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    RequestKeyModel *model = [RequestKeyModel modelWithDictionary:dict];
    
    return model;
}

@end
