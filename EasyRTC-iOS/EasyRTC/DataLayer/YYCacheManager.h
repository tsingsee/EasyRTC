//
//  YYCacheManager.h
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseLocalData.h"

// 网络请求的YYCache缓存文件存放的文件夹
extern NSString * const HttpCache;
// 本地数据的YYCache缓存文件存放的文件夹
extern NSString * const ClearableLocalCache;
// 账户信息的YYCache缓存文件存放的文件夹
extern NSString * const LoginInfoDataCache;
// 定位信息
extern NSString * const LocationDataCache;

/**
 所有可以清理的缓存的统一管理
 */
@interface YYCacheManager : NSObject

+ (instancetype) sharedInstance;

/**
 缓存大小
 @return 缓存大小
 */
- (NSString *) sizeOfCache;

/**
 清理缓存
 */
- (void) clearCache;

@end
