//
//  VideoModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "VideoModel.h"

@implementation Channel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"channel" : @"Channel",
              @"name" : @"Name",
              @"online" : @"Online",
              @"snapUrl" : @"SnapURL",
              @"errorString" : @"ErrorString" };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    Channel *model = [Channel modelWithDictionary:dict];
    
    return model;
}

@end

@implementation VideoModel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"channelCount" : @"ChannelCount",
              @"channels" : @"Channels"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    VideoModel *model = [VideoModel modelWithDictionary:dict];
    model.channels = [Channel convertFromArray:model.channels];
    
    return model;
}

@end
