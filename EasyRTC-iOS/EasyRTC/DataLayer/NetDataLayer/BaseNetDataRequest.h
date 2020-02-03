//
//  BaseNetDataRequest.h
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseNetDataRequestTool.h"
#import "BaseNetDataRequestModel.h"
#import "AFNetworking.h"
#import "NetDataReturnModel.h"

/**
 请求方式
 */
typedef NS_ENUM(NSInteger, RequestType) {
    RequestTypeGet,
    RequestTypePost,
    RequestTypePut,
    RequestTypeDelete,
    RequestTypeUpLoad,// 单个上传
    RequestTypeMultiUpload,// 多个上传
    RequestTypeDownload
};

/**
 可使用缓存的AFNetworking请求
 */
@interface BaseNetDataRequest : NSObject

#pragma mark - Delete方法

- (RACSignal *) httpDeleteRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;

#pragma mark - Put方法

- (RACSignal *) httpPutRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;

#pragma mark - Get方法

// 不带缓存
- (RACSignal *) httpGetRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;
// 带缓存
- (RACSignal *) httpGetCacheRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;
// 只要有缓存，则只使用缓存；否则网络请求
- (RACSignal *) httpGetPriorUseCacheRequest:(NSString *)url params:(id)params isOnlyCache:(BOOL)isOnlyCache requestModel:(BaseNetDataRequestModel *)model;

#pragma mark - Post方法(默认方法)

// 不带缓存
- (RACSignal *) httpPostRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;
// 带缓存的post 请求
- (RACSignal *) httpPostCacheRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;
// 只要有缓存，则只使用缓存；否则网络请求
- (RACSignal *) httpPostPriorUseCacheRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model;

#pragma mark - 上传文件方法

//上传单张图片
- (RACSignal *) uploadDataWithUrlStr:(NSString *)url params:(id)params imageName:(NSString *)name fileName:(NSString *)fileName withData:(NSData *)data requestModel:(BaseNetDataRequestModel *)model;
//上传多张图片
- (RACSignal *) uploadDataWithUrlStr:(NSString *)url params:(id)params imageName:(NSString *)name withDataArray:(NSArray *)dataArray requestModel:(BaseNetDataRequestModel *)model;

@end
