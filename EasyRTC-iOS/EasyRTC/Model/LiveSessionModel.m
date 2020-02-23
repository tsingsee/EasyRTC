//
//  LiveSessionModel.m
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "LiveSessionModel.h"

@implementation Session

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"flv" : @"HTTP-FLV",
              @"sessionID" : @"Id"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    Session *model = [Session modelWithDictionary:dict];
    
    return model;
}

@end

@implementation Sessions

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"sessions" : @"Sessions"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    Sessions *model = [Sessions modelWithDictionary:dict];
    model.sessions = [Session convertFromArray:model.sessions];
    
    return model;
}

@end

@implementation LiveSessionModel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"sessionCount" : @"SessionCount",
              @"sessions" : @"Sessions"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    LiveSessionModel *model = [LiveSessionModel modelWithDictionary:dict];
    
    return model;
}

@end
