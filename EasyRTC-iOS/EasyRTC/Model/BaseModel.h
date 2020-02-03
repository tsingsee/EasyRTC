//
//  BaseModel.h
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_include(<YYModel/YYModel.h>)
    FOUNDATION_EXPORT double YYModelVersionNumber;
    FOUNDATION_EXPORT const unsigned char YYModelVersionString[];
    #import <YYModel/NSObject+YYModel.h>
    #import <YYModel/YYClassInfo.h>
#else
    #import "NSObject+YYModel.h"
    #import "YYClassInfo.h"
#endif

/**
 model的基类
 */
@interface BaseModel : NSObject<NSCoding, NSCopying>

+ (instancetype) convertFromDict:(NSDictionary *)dict;
+ (NSMutableArray *) convertFromArray:(NSArray *)array;

@end
