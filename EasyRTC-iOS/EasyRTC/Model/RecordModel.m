//
//  RecordModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RecordModel.h"
#import "LoginInfoLocalData.h"
#import <YYKit/YYKit.h>

@interface RecordModel()

@property (nonatomic, strong) NSDateFormatter *formatter;

@end

@implementation RecordModel

+ (instancetype) convertFromDict:(NSDictionary *)dict {
    RecordModel *model = [RecordModel modelWithDictionary:dict];
    
    return model;
}

- (NSString *) hls {
    if ([_hls hasSuffix:@".m3u8"]) {
        if ([_hls hasPrefix:@"http"]) {
            return _hls;
        }
        
        NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
        NSString *urlStr = [NSString stringWithFormat:@"%@%@", ip, _hls];
        return urlStr;
    }
    
    return _hls;
}

- (NSString *) startAtFormat {
    if (_startAt.length == 14) {
        NSMutableString *time = [[NSMutableString alloc] init];
        [time appendString:_startAt];
        
        [time insertString:@"-" atIndex:4];
        [time insertString:@"-" atIndex:7];
        
        [time insertString:@" " atIndex:10];
        
        [time insertString:@":" atIndex:13];
        [time insertString:@":" atIndex:16];
        
        return time;
    }
    
    return _startAt;
}

- (NSString *) durationFormat {
    NSString *str_hour = [NSString stringWithFormat:@"%02ld", _duration / 3600];
    NSString *str_minute = [NSString stringWithFormat:@"%02ld", (_duration % 3600) / 60];
    NSString *str_second = [NSString stringWithFormat:@"%02ld", _duration % 60];
    NSString *format_time = [NSString stringWithFormat:@"%@:%@:%@", str_hour, str_minute, str_second];
    
    return format_time;
}

- (NSInteger) startAtSecond {
    if (_startAt.length == 14) {// 20180918000003
        NSInteger hour = [[_startAt substringWithRange:NSMakeRange(8, 2)] integerValue];
        NSInteger minute = [[_startAt substringWithRange:NSMakeRange(10, 2)] integerValue];
        NSInteger second = [[_startAt substringWithRange:NSMakeRange(12, 2)] integerValue];
        
        return hour * 3600 + minute * 60 + second;
    }
    
    if (!self.formatter) {
        self.formatter = [[NSDateFormatter alloc] init];
    }

    [self.formatter setDateFormat:@"YYYYMMDDHHmmss"];
//    [self.formatter setTimeZone:[NSTimeZone timeZoneForSecondsFromGMT:8]];// 解决8小时时间差问题
    NSDate *birthdayDate = [self.formatter dateFromString:_startAt];
    NSInteger hour = [birthdayDate hour];
    NSInteger minute = [birthdayDate minute];
    NSInteger second = [birthdayDate second];
    
    return hour * 3600 + minute * 60 + second;
}

@end
