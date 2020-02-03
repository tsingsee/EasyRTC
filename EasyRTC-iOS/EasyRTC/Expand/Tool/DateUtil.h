//
//  DateUtil.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface DateUtil : NSObject

+ (NSString *) dateYYYY_MM_DD:(NSDate *)date;
+ (NSString *) dateYYYYMMDD:(NSDate *)date;
+ (NSString *) dateYYYYMMDDHHmmss:(NSDate *)date;
+ (NSString *) dateYYYYMM:(NSDate *)date;

+ (NSDate *) dateFormatYYYYMMDD:(NSString *)str;

@end
