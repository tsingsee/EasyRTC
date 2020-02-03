//
//  LiveModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/17.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "LiveModel.h"
#import "LoginInfoLocalData.h"

@implementation LiveModel

//返回一个 Dict，将 Model 属性名对映射到 JSON 的 Key。
+ (nullable NSDictionary<NSString *, id> *)modelCustomPropertyMapper {
    return @{ @"channelName" : @"ChannelName",
              @"url" : @"URL",
              @"deviceType" : @"DeviceType"
              };
}

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    LiveModel *model = [LiveModel modelWithDictionary:dict];
    
    return model;
}

- (NSString *) url {
    if ([_url hasSuffix:@".m3u8"]) {
        if ([_url hasPrefix:@"http"]) {
            return _url;
        }
        
        NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
        NSString *urlStr = [NSString stringWithFormat:@"%@%@", ip, _url];
        return urlStr;
    }
    
    return _url;
}

@end
