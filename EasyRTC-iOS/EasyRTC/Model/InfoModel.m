//
//  InfoModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "InfoModel.h"

@implementation InfoModel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"hardware" : @"Hardware",
              @"interfaceVersion" : @"InterfaceVersion",
              @"liveCount" : @"LiveCount",
              @"productType" : @"ProductType",
              @"runningTime" : @"RunningTime",
              @"server" : @"Server",
              @"validity" : @"Validity",
              @"virtualLiveCount" : @"VirtualLiveCount"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    InfoModel *model = [InfoModel modelWithDictionary:dict];
    
    return model;
}

@end
