//
//  BaseNetDataRequestTool.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseNetDataRequestTool.h"
#import "AFNetworking.h"

@implementation BaseNetDataRequestTool

#pragma mark - 单例模式

static BaseNetDataRequestTool *instance;

+ (id) allocWithZone:(struct _NSZone *)zone {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [super allocWithZone:zone];
    });
    
    return instance;
}

+ (instancetype) sharedInstance {
    static dispatch_once_t oncetToken;
    dispatch_once(&oncetToken, ^{
        instance = [[self alloc] init];
    });
    
    return instance;
}

- (id) copyWithZone:(NSZone *)zone {
    return instance;
}

#pragma mark - public method

- (NSString *) gainImageNameWithImageMimeType:(NSString *)imageMimeType {
    NSTimeInterval timeInterVal = [[NSDate date] timeIntervalSince1970];
    int random = arc4random() % 10000;
    // 默认png图
    NSString *type = @"png";
    if ([imageMimeType containsString:@"/"]) {
        type = [imageMimeType componentsSeparatedByString:@"/"].lastObject;
    }
    NSString * fileName = [NSString stringWithFormat:@"%@_%@.%@", @(timeInterVal), @(random), type];
    
    return fileName;
}

- (NSString *) gainCacheKeyWithUrlStr:(NSString *)url
                    paramsForCacheKey:(NSDictionary *)paramCacheKey {
    // url parmas
    url = [url stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
    if (paramCacheKey == nil) {
        paramCacheKey = [NSMutableDictionary dictionary];
    }
    
    NSString *allUrl = [[BaseNetDataRequestTool sharedInstance] urlDictToStringWithUrlStr:url
                                                                        paramsForCacheKey:paramCacheKey];
    
    // 设置cache和cacheKey
    NSString *cacheKey = [self dealCacheKey:allUrl];
    
    return cacheKey;
}

- (NSString *) dealCacheKey:(NSString *)cacheKey {
    NSString *result = cacheKey;
    static int length = 128;
    if ([cacheKey length] > length) {
        result = [cacheKey substringFromIndex:(cacheKey.length - length)];
    }
    return result;
}

- (NSString *) urlDictToStringWithUrlStr:(NSString *)urlString paramsForCacheKey:(NSDictionary *)paramCacheKey {
    if (!paramCacheKey) {
        return urlString;
    }
    
    NSMutableArray *parts = [NSMutableArray array];
    /* enumerateKeysAndObjectsUsingBlock会遍历dictionary并把里面所有的key和value一组一组的展示给你每组都会执行这个block,
     这其实就是传递一个block到另一个方法，在这里它会带着特定参数被反复调用，直到找到一个ENOUGH的key，
     然后就会通过重新赋值那个BOOL *stop来停止运行，停止遍历同时停止调用block
     */
    [paramCacheKey enumerateKeysAndObjectsUsingBlock:^(id _Nonnull key, id _Nonnull obj, BOOL * _Nonnull stop) {
        // 字符串处理
        key = [NSString stringWithFormat:@"%@", key];
        
        obj = [NSString stringWithFormat:@"%@", obj];
        // 接收key
        NSString *finalKey = [key stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
        // 接收值
        NSString *finalValue = [obj stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]];
        NSString *part =[NSString stringWithFormat:@"%@=%@",finalKey,finalValue];
        [parts addObject:part];
    }];
    
    NSString *queryString = [parts componentsJoinedByString:@"&"];
    queryString = (queryString.length != 0) ? [NSString stringWithFormat:@"?%@", queryString] : @"";
    NSString *pathStr = [NSString stringWithFormat:@"%@%@", urlString, queryString];
    return pathStr;
}

- (NSString *) deleteSpecialCodeWithStr:(NSString *)str {
    // 空格不能替换
    NSString *string = [str stringByReplacingOccurrencesOfString:@"\r" withString:@""];
    string = [string stringByReplacingOccurrencesOfString:@"\n" withString:@""];
    string = [string stringByReplacingOccurrencesOfString:@"\t" withString:@""];
    string = [string stringByReplacingOccurrencesOfString:@"(" withString:@""];
    string = [string stringByReplacingOccurrencesOfString:@")" withString:@""];
    
    return string;
}

- (BOOL) isNetworkEnable {
    struct sockaddr zeroAddress;
    bzero(&zeroAddress, sizeof(zeroAddress));
    zeroAddress.sa_len = sizeof(zeroAddress);
    zeroAddress.sa_family = AF_INET;
    SCNetworkReachabilityRef defaultRouteReachability =
    SCNetworkReachabilityCreateWithAddress(NULL, (struct sockaddr *)&zeroAddress);
    SCNetworkReachabilityFlags flags;
    BOOL didRetrieveFlags =
    SCNetworkReachabilityGetFlags(defaultRouteReachability, &flags);
    CFRelease(defaultRouteReachability);
    if (!didRetrieveFlags) {
        printf("Error. Count not recover network reachability flags\n");
        return NO;
    }
    BOOL isReachable = flags & kSCNetworkFlagsReachable;
    BOOL needsConnection = flags & kSCNetworkFlagsConnectionRequired;
    BOOL isNetworkEnable  =(isReachable && !needsConnection) ? YES : NO;
    dispatch_async(dispatch_get_main_queue(), ^{
        /*  网络指示器的状态： 有网络 ： 开  没有网络： 关  */
        [UIApplication sharedApplication].networkActivityIndicatorVisible =isNetworkEnable;
    });
    return isNetworkEnable;
}

@end
