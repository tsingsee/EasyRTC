//
//  YYCacheManager.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "YYCacheManager.h"
#import <WebKit/WebKit.h>

#define IOS9_PLUS [[[UIDevice currentDevice] systemVersion] floatValue] >= 9.0

// ----------------------- 可清理的 -----------------------
NSString * const HttpCache = @"HttpRequestCache";
NSString * const ClearableLocalCache = @"ClearableLocalCache";

// ----------------------- 不可清理的 -----------------------
// 登录信息的文件夹
NSString * const LoginInfoDataCache = @"LoginInfoDataCache";
NSString * const LocationDataCache = @"LocationDataCache";

@implementation YYCacheManager

#pragma mark - 单例模式

static YYCacheManager *instance;

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

- (NSString *) sizeOfCache {
    // Cache目录
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = paths.firstObject;// Caches目录
    
    // ClearableLocalCache
    NSString *clearablePath = [NSString stringWithFormat:@"%@/%@", cacheDirectory, ClearableLocalCache];
    float clearablesize = [self fileSizeForDir:clearablePath];
    
    // HttpCache
    NSString *HttpPath = [NSString stringWithFormat:@"%@/%@", cacheDirectory, HttpCache];
    float httpSize = [self fileSizeForDir:HttpPath];
    
    // webViewCache
    float webViewCache = 0;
    NSString *libraryDir = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES).firstObject;
    NSString *webkitFolderInLib = [NSString stringWithFormat:@"%@/WebKit", libraryDir];
    NSString *webKitFolderInCaches = [NSString stringWithFormat:@"%@/WebKit", cacheDirectory];
    
    NSString *bundleId = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleIdentifier"];
    NSString *webKitFolderInCachesfs = [NSString stringWithFormat:@"%@/%@/fsCachedData", cacheDirectory, bundleId];
    
    webViewCache += [self fileSizeForDir:webkitFolderInLib];
    webViewCache += [self fileSizeForDir:webKitFolderInCaches];
    webViewCache += [self fileSizeForDir:webKitFolderInCachesfs];
    
    float totalSize = clearablesize + httpSize + webViewCache;
    
    // 缓存做处理
    if (totalSize < 1) {
        totalSize = totalSize * 5;  // 小于1M，要扩大些
    } else if (totalSize > 10) {
        totalSize = 10;             // 不要超过10M吧
    }
    
    return [NSString stringWithFormat:@"%.1fM", totalSize];
}

- (void) clearCache {
    // Cache目录
    NSArray *paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES);
    NSString *cacheDirectory = paths.firstObject;// Caches目录
    
    // ClearableLocalCache
    NSString *clearablePath = [NSString stringWithFormat:@"%@/%@", cacheDirectory, ClearableLocalCache];
    [self clearCachesWithFilePath:clearablePath];
    
    // HttpCache
    NSString *HttpPath = [NSString stringWithFormat:@"%@/%@", cacheDirectory, HttpCache];
    [self clearCachesWithFilePath:HttpPath];
    
    // wbView的缓存
    NSString *libraryDir = NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES).firstObject;
    NSString *webkitFolderInLib = [NSString stringWithFormat:@"%@/WebKit", libraryDir];
    NSString *webKitFolderInCaches = [NSString stringWithFormat:@"%@/WebKit", cacheDirectory];
    NSString *bundleId = [[[NSBundle mainBundle] infoDictionary] objectForKey:@"CFBundleIdentifier"];
    NSString *webKitFolderInCachesfs = [NSString stringWithFormat:@"%@/%@/fsCachedData", cacheDirectory, bundleId];
    
    // iOS8.0 WebView Cache的存放路径
    [self clearCachesWithFilePath:webkitFolderInLib];
    [self clearCachesWithFilePath:webKitFolderInCaches];
    // iOS7.0 WebView Cache的存放路径
    [self clearCachesWithFilePath:webKitFolderInCachesfs];
    
    if (IOS9_PLUS) {
        NSArray * types = @[ WKWebsiteDataTypeMemoryCache, WKWebsiteDataTypeDiskCache ];  // 9.0之后才有的
        NSSet *websiteDataTypes = [NSSet setWithArray:types];
        NSDate *dateFrom = [NSDate dateWithTimeIntervalSince1970:0];
        
        // 删除
        [[WKWebsiteDataStore defaultDataStore] removeDataOfTypes:websiteDataTypes modifiedSince:dateFrom completionHandler:^{
            //
        }];
    }
}

#pragma mark - private method

// 删除指定目录或文件
- (BOOL)clearCachesWithFilePath:(NSString *)path {
    NSFileManager *manager = [NSFileManager defaultManager];
    
    NSError *error;
    BOOL result = [manager removeItemAtPath:path error:&error];
    
    return result;
}

// 计算文件夹下文件的总大小
- (float)fileSizeForDir:(NSString*)path {
    NSFileManager *fileManager = [[NSFileManager alloc] init];
    float size = 0;
    NSArray* array = [fileManager contentsOfDirectoryAtPath:path error:nil];
    for(int i = 0; i<[array count]; i++) {
        NSString *fullPath = [path stringByAppendingPathComponent:[array objectAtIndex:i]];
        BOOL isDir;
        if (!([fileManager fileExistsAtPath:fullPath isDirectory:&isDir] && isDir)) {
            NSDictionary *fileAttributeDic = [fileManager attributesOfItemAtPath:fullPath error:nil];
            size += fileAttributeDic.fileSize / 1024.0 / 1024.0;
        } else {
            [self fileSizeForDir:fullPath];
        }
    }
    
    return size;
}

@end
