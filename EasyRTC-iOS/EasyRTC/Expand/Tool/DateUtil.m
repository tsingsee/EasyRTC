//
//  DateUtil.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "DateUtil.h"

@implementation DateUtil

+ (NSString *) dateYYYY_MM_DD:(NSDate *)date {
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
    format.dateFormat = @"yyyy-MM-dd";
    return [format stringFromDate:date];
}

+ (NSString *) dateYYYYMMDD:(NSDate *)date {
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
    format.dateFormat = @"yyyyMMdd";
    return [format stringFromDate:date];
}

+ (NSString *) dateYYYYMMDDHHmmss:(NSDate *)date {
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
    format.dateFormat = @"yyyyMMddHHmmss";
    return [format stringFromDate:date];
}

+ (NSString *) dateYYYYMM:(NSDate *)date {
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
    format.dateFormat = @"yyyyMM";
    return [format stringFromDate:date];
}

+ (NSDate *) dateFormatYYYYMMDD:(NSString *)str {
    NSDateFormatter *format = [[NSDateFormatter alloc] init];
    [format setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
    format.dateFormat = @"yyyyMMdd";
    return [format dateFromString:str];
}

@end
