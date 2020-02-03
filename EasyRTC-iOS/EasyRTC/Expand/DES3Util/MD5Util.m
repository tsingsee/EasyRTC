//
//  MD5Util.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/21.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "MD5Util.h"
#import <CommonCrypto/CommonDigest.h>

@implementation MD5Util

+ (NSString *) MD5ForLower32Bate:(NSString *)str {
    if (!str || [str isEqualToString:@""]) {
        return @"";
    }
    
    //要进行UTF8的转码
    const char* input = [str UTF8String];
    unsigned char result[CC_MD5_DIGEST_LENGTH];
    CC_MD5(input, (CC_LONG)strlen(input), result);
    
    NSMutableString *digest = [NSMutableString stringWithCapacity:CC_MD5_DIGEST_LENGTH * 2];
    for (NSInteger i = 0; i < CC_MD5_DIGEST_LENGTH; i++) {
        [digest appendFormat:@"%02x", result[i]];
    }
    
    return digest;
}

@end
