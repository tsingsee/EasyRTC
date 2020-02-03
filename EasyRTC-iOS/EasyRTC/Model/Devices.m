//
//  Devices.m
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "Devices.h"

@implementation Devices

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    Devices *model = [Devices modelWithDictionary:dict];
    
    return model;
}

@end
