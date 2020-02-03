//
//  BaseNetDataRequest.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseNetDataRequest.h"
#import <YYKit/YYKit.h>
#import "YYCacheManager.h"
#import "LoginInfoLocalData.h"

@implementation BaseNetDataRequest

#pragma mark - 各种请求方式

// Delete方法
- (RACSignal *) httpDeleteRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypeDelete
                               isCache:NO isPriorCache:NO
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

// Put方法
- (RACSignal *) httpPutRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypePut
                               isCache:NO isPriorCache:NO
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

// Get方法(默认方法)
- (RACSignal *) httpGetRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypeGet
                               isCache:NO isPriorCache:NO
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

- (RACSignal *) httpGetCacheRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypeGet
                               isCache:YES isPriorCache:NO
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

- (RACSignal *) httpGetPriorUseCacheRequest:(NSString *)url params:(id)params isOnlyCache:(BOOL)isOnlyCache requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypeGet
                               isCache:YES isPriorCache:YES
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

// Post方法
- (RACSignal *) httpPostRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypePost
                               isCache:NO isPriorCache:NO
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

- (RACSignal *) httpPostCacheRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypePost
                               isCache:YES isPriorCache:NO
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

- (RACSignal *) httpPostPriorUseCacheRequest:(NSString *)url params:(id)params requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypePost
                               isCache:YES isPriorCache:YES
                             imageName:nil fileName:nil withData:nil withDataArray:nil
                          requestModel:model];
}

// 上传文件方法（上传单张图片 name一般写file）
-(RACSignal *)uploadDataWithUrlStr:(NSString *)url params:(id)params imageName:(NSString *)name fileName:(NSString *)fileName
                   withData:(NSData *)data requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypeUpLoad
                               isCache:NO isPriorCache:NO
                             imageName:name fileName:fileName withData:data withDataArray:nil
                          requestModel:model];
}

// 上传多张图片
-(RACSignal *)uploadDataWithUrlStr:(NSString *)url params:(id)params imageName:(NSString *)name
              withDataArray:(NSArray *)dataArray requestModel:(BaseNetDataRequestModel *)model {
    return [self httpRequestWithUrlStr:url params:params requestType:RequestTypeMultiUpload
                               isCache:NO isPriorCache:NO
                             imageName:name fileName:nil withData:nil withDataArray:dataArray
                          requestModel:model];
}

#pragma mark - 网络请求的方法

/**
 网络请求的统一入口：先判断网络状态
 @param url                 请求URL
 @param params              参数dict
 @param requestType         请求类型
 @param isCache             是否缓存标志
 @param isPriorCache        只要有缓存，则只使用缓存；否则网络请求
 @param name                图片上传的名字(upload)
 @param fileName            fileName
 @param data                图片的二进制数据(upload)
 @param dataArray           多图片上传时的imageDataArray
 @param model               属性设置
 */
-(RACSignal *)httpRequestWithUrlStr:(NSString *)url params:(id) params requestType:(RequestType)requestType
                            isCache:(BOOL)isCache isPriorCache:(BOOL)isPriorCache
                          imageName:(NSString *)name fileName:(NSString *)fileName withData:(NSData *)data withDataArray:(NSArray *)dataArray
                       requestModel:(BaseNetDataRequestModel *)model {
    
    // model为空，则使用默认值
    if (!model) {
        model = [[BaseNetDataRequestModel alloc] init];
    }
    
    // 设置cache和cacheKey
    YYCache *cache = [[YYCache alloc] initWithName:HttpCache];
    NSString *cacheKey = [[BaseNetDataRequestTool sharedInstance] gainCacheKeyWithUrlStr:url paramsForCacheKey:params];
    
    if (isPriorCache) {
        // 优先使用缓存，不要提示
        model.dataFromCacheHint = nil;
        
        // 根据网址从Cache中取数据
        id cacheData = [cache objectForKey:cacheKey];// cacheData 缓存内容
        if (cacheData) {
            return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
                // 既然优先使用缓存，那就跟token无关了，则loginSuccessBlock设置空, 直接取缓存，所以isPriorUseCache=NO
                [self gainCacheDataWithSubscriber:subscriber error:nil cache:cache cacheKey:cacheKey isCache:YES tint:model.offNetHint requestModel:model];
                
                return nil;
            }];
        }
    }
    
    // 判断网络状态
    if ([[BaseNetDataRequestTool sharedInstance] isNetworkEnable]) {
        return [self requestWithURL:url params:params requestType:requestType isCache:isCache cache:cache cacheKey:cacheKey imageName:name fileName:fileName withData:data withDataArray:dataArray requestModel:model];
    } else {
        // 没网络直接取缓存数据
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            [self gainCacheDataWithSubscriber:subscriber error:nil cache:cache cacheKey:cacheKey isCache:isCache tint:model.offNetHint requestModel:model];
            
            return nil;
        }];
    }
}

/**
 网络请求统一处理
 @param url                 请求URL
 @param params              参数dict
 @param requestType         请求类型
 @param isCache             是否缓存标志
 @param cacheKey            缓存的对应key值
 @param name                图片上传的名字(upload)
 @param fileName            fileName
 @param data                图片的二进制数据(upload)
 @param dataArray           多图片上传时的imageDataArray
 @param model               属性设置
 */
-(RACSignal *) requestWithURL:(NSString *) url params:(id) params requestType:(RequestType) requestType
                      isCache:(BOOL)isCache cache:(YYCache *)cache cacheKey:(NSString *) cacheKey
                    imageName:(NSString *)name fileName:(NSString *)fileName
                     withData:(NSData *) data withDataArray:(NSArray *) dataArray
                 requestModel:(BaseNetDataRequestModel *)model {
    
    // 可重用的转换成响应式请求
    return [[RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        // 实例化AFHTTPSessionManager
        AFHTTPSessionManager *session = [AFHTTPSessionManager manager];
        // 调出请求头
        session.requestSerializer = [AFJSONRequestSerializer serializer];
        session.requestSerializer.timeoutInterval = model.timeoutInterval;// 超时时间
        //将token封装入请求头
        NSString *token = [[LoginInfoLocalData sharedInstance] getLoginModel].token;
        if (token) {
            NSString *value = [NSString stringWithFormat:@"token=%@", token];
            [session.requestSerializer setValue:value forHTTPHeaderField:@"Cookie"];
        }
        
        session.responseSerializer = [AFHTTPResponseSerializer serializer];
        session.responseSerializer.acceptableContentTypes = [NSSet setWithObjects:@"application/json", @"text/json", @"text/javascript", @"text/html", @"text/plain", @"application/vnd.apple.mpegurl", nil];
        
        if (requestType == RequestTypeGet) {// Get
            [session GET:url parameters:params progress:^(NSProgress * _Nonnull downloadProgress) {
                // 请求数据，没有进度
            } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                [self dealWithSubscriber:subscriber responseObject:responseObject cache:cache isCache:isCache cacheKey:cacheKey requestModel:model];
            } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                [self gainCacheDataWithSubscriber:subscriber error:error cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
            }];
        } else if (requestType == RequestTypePost) {// Post
            [session POST:url parameters:params progress:^(NSProgress * _Nonnull downloadProgress) {
                // 请求数据，没有进度
            } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                [self dealWithSubscriber:subscriber responseObject:responseObject cache:cache isCache:isCache cacheKey:cacheKey requestModel:model];
            } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                [self gainCacheDataWithSubscriber:subscriber error:error cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
            }];
        } else if (requestType == RequestTypePut) {// Put
            [session PUT:url parameters:params success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                [self dealWithSubscriber:subscriber responseObject:responseObject cache:cache isCache:isCache cacheKey:cacheKey requestModel:model];
            } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                [self gainCacheDataWithSubscriber:subscriber error:error cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
            }];
        } else if (requestType == RequestTypeDelete) {// Delete
            [session DELETE:url parameters:params success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                [self dealWithSubscriber:subscriber responseObject:responseObject cache:cache isCache:isCache cacheKey:cacheKey requestModel:model];
            } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                [self gainCacheDataWithSubscriber:subscriber error:error cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
            }];
        } else if (requestType == RequestTypeUpLoad) {// UpLoad
            [session POST:url parameters:params constructingBodyWithBlock:^(id<AFMultipartFormData>  _Nonnull formData) {
                if (fileName) {
                    [formData appendPartWithFileData:data name:name fileName:fileName mimeType:model.imageMimeType];
                } else {
                    NSString *newFileName = [[BaseNetDataRequestTool sharedInstance] gainImageNameWithImageMimeType:model.imageMimeType];
                    [formData appendPartWithFileData:data name:name fileName:newFileName mimeType:model.imageMimeType];
                }
            } progress:^(NSProgress * _Nonnull uploadProgress) {
                NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
                returnModel.type = ReturnProcess;
                returnModel.process = (float)uploadProgress.completedUnitCount / (float)uploadProgress.totalUnitCount;
                [subscriber sendNext:returnModel];
                [subscriber sendCompleted];
            } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                [self dealWithSubscriber:subscriber responseObject:responseObject cache:cache isCache:isCache cacheKey:cacheKey requestModel:model];
            } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                [self gainCacheDataWithSubscriber:subscriber error:error cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
            }];
        } else if (requestType == RequestTypeMultiUpload) {
            [session POST:url parameters:params constructingBodyWithBlock:^(id<AFMultipartFormData>  _Nonnull formData) {
                for (NSInteger i = 0; i < dataArray.count; i++) {
                    NSData *imageData = [dataArray objectAtIndex:i];
                    
                    NSString *fileName;
                    if (model.fileName) {
                        fileName = model.fileName;
                    } else {
                        fileName = [[BaseNetDataRequestTool sharedInstance] gainImageNameWithImageMimeType:model.imageMimeType];
                    }
                    
                    [formData appendPartWithFileData:imageData name:name fileName:fileName mimeType:model.imageMimeType];
                }
            } progress:^(NSProgress * _Nonnull uploadProgress) {
                NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
                returnModel.type = ReturnProcess;
                returnModel.process = (float)uploadProgress.completedUnitCount / (float)uploadProgress.totalUnitCount;
                [subscriber sendNext:returnModel];
                [subscriber sendCompleted];
            } success:^(NSURLSessionDataTask * _Nonnull task, id  _Nullable responseObject) {
                [self dealWithSubscriber:subscriber responseObject:responseObject cache:cache isCache:isCache cacheKey:cacheKey requestModel:model];
            } failure:^(NSURLSessionDataTask * _Nullable task, NSError * _Nonnull error) {
                [self gainCacheDataWithSubscriber:subscriber error:error cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
            }];
        }
        
        return nil;
    }] setNameWithFormat:@"<%@: %p> -post2racWthURL: %@, params: %@", self.class, self, url, params];
}

#pragma mark - 统一处理网络请求成功返回的数据

/**
 网络请求有2个出口，这是第1个出口
 
 @param responseData        请求结果数据
 @param cache               cache
 @param isCache             是否缓存
 @param cacheKey            缓存key (cacheData暂不理会)
 @param model               属性设置
 */
-(void)dealWithSubscriber:(id <RACSubscriber>)subscriber
           responseObject:(NSData *)responseData
                    cache:(YYCache *)cache isCache:(BOOL)isCache cacheKey:(NSString *)cacheKey
             requestModel:(BaseNetDataRequestModel *)model {
    
    // 关闭网络指示器
    dispatch_async(dispatch_get_main_queue(), ^{
        [UIApplication sharedApplication].networkActivityIndicatorVisible = NO;
    });
    
    NSString * dataString = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
    dataString = [[BaseNetDataRequestTool sharedInstance] deleteSpecialCodeWithStr:dataString];
    
    NSString *res = [dataString stringByReplacingOccurrencesOfString:@"0" withString:@""];
    res = [res stringByReplacingOccurrencesOfString:@"\"" withString:@""];
    if ([res isEqualToString:@""] || [self isPureInt:res]) {
        NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
        returnModel.type = ReturnSuccess;
        returnModel.result = dataString;
        
        [subscriber sendNext:returnModel];
        [subscriber sendCompleted];
        
        return;
    }
    
    // 请求.m3u8，返回的结果
    if ([dataString hasSuffix:@".ts"]) {
        NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
        returnModel.type = ReturnSuccess;
        returnModel.result = dataString;
        [subscriber sendNext:returnModel];
        [subscriber sendCompleted];
        
        return;
    }
    
    NSData *requestData = [dataString dataUsingEncoding:NSUTF8StringEncoding];
    
    // 处理并显示数据(网络请求的数据，需要cache取保存requestData)
    [self returnDataWithSubscriber:subscriber
                       requestData:requestData requestTint:nil
                             cache:cache isCache:isCache cacheKey:cacheKey
                      requestModel:model];
}

/**
 网络请求有2个出口，这是第2个出口
 
 @param error               error
 @param cache               cache
 @param cacheKey            key
 @param isCache             是否使用缓存
 @param tint                提示
 @param model               属性设置
 */
- (void) gainCacheDataWithSubscriber:(id <RACSubscriber>)subscriber error:(NSError *)error
                               cache:(YYCache *)cache cacheKey:(NSString *)cacheKey isCache:(BOOL)isCache
                                tint:(NSString *)tint requestModel:(BaseNetDataRequestModel *)model {
    if (error) {
        NSDictionary *userInfo = error.userInfo;
        NSString *desc = userInfo[@"NSLocalizedDescription"];
        if ([desc containsString:@"401"]) {
            // 即使取缓存数据，如果token失效，依旧强调token失效 --- 登录接口千万不可返回这个，否则就是死循环
            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
            returnModel.type = ReturnValidToken;
            [subscriber sendNext:returnModel];
            [subscriber sendCompleted];
            
            return;
        }
    }
    
    if (isCache) {
        // 根据网址从Cache中取数据
        id cacheData = [cache objectForKey:cacheKey];// cacheData 缓存内容
        
        if(cacheData) {
            // 读取缓存数据，则不需要cache取保存cacheData，都为nil
            [self returnDataWithSubscriber:subscriber requestData:cacheData requestTint:tint cache:nil isCache:NO cacheKey:nil requestModel:model];
        } else {
            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
            returnModel.type = ReturnFailure;
            returnModel.error = tint;
            [subscriber sendNext:returnModel];
            [subscriber sendCompleted];
        }
    } else {
        NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
        returnModel.type = ReturnFailure;
        returnModel.error = tint;
        [subscriber sendNext:returnModel];
        [subscriber sendCompleted];
    }
}

#pragma mark - 根据返回的数据进行统一的格式处理

/**
 @param requestData         网络或者是缓存的数据
 @param requestTint         有数据，则表示取自cache；为空则是网络获取的数据
 @param cache               cache
 @param isCache             isCache
 @param cacheKey            缓存key (cacheData暂不理会)
 @param model               属性设置
 */
- (void)returnDataWithSubscriber:(id <RACSubscriber>)subscriber
                     requestData:(NSData *)requestData requestTint:(NSString *)requestTint
                           cache:(YYCache *)cache isCache:(BOOL)isCache cacheKey:(NSString *)cacheKey
                    requestModel:(BaseNetDataRequestModel *)model {
    
    // 解析json格式的结果
    id result = [NSJSONSerialization JSONObjectWithData:requestData options:NSJSONReadingMutableContainers error:nil];
    
    // 判断是否为字典
    if ([result isKindOfClass:[NSDictionary class]]) {
        NSDictionary *response = (NSDictionary *)result;
        
        if ([response.allKeys containsObject:@"EasyDSS"]) {
            NSDictionary *darwin = response[@"EasyDSS"];
//            NSDictionary *header = darwin[@"Header"];
            
            if (isCache && cache) {
                [cache setObject:requestData forKey:cacheKey withBlock:^{
                    NSLog(@"setObject for %@ sucess ", cacheKey);
                }];
            }
            
            // 请求的数据做处理，把data层剥掉
            id data = darwin[@"Body"];
            
            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
            returnModel.type = ReturnSuccess;
            returnModel.result = data;
            [subscriber sendNext:returnModel];
            [subscriber sendCompleted];
        } else {
            id data = response[@"data"];
            
            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
            returnModel.type = ReturnSuccess;
            returnModel.result = data;
            [subscriber sendNext:returnModel];
            [subscriber sendCompleted];
        }
        
//        NSString *errorNum = header[@"ErrorNum"];
        
//        if (!errorNum) {
//            // 请求结果不一致时，直接将结果返回给ViewController处理
//            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
//            returnModel.type = ReturnSuccess;
//            returnModel.result = response;
//            [subscriber sendNext:returnModel];
//            [subscriber sendCompleted];
//        } else if ([errorNum isEqualToString:@"200"]) {    // 成功
            // 只有请求的数据正确，那么需要缓存时, 才可以缓存网络请求的数据
//        } else if ([errorNum isEqualToString:@"401"]) { // token失效
//            // 即使取缓存数据，如果token失效，依旧强调token失效 --- 登录接口千万不可返回这个，否则就是死循环
//            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
//            returnModel.type = ReturnValidToken;
//            [subscriber sendNext:returnModel];
//            [subscriber sendCompleted];
//        } else {
//            if (cache && isCache) {
//                // 来自网络请求数据 错误了，就去读取缓存数据
//                [self gainCacheDataWithSubscriber:subscriber error:nil cache:cache cacheKey:cacheKey isCache:isCache tint:header[@"ErrorString"] requestModel:model];
//            } else {
//                // 来自缓存数据 错误了,只能提示用户 数据错误
//                NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
//                returnModel.type = ReturnFailure;
//                returnModel.error = requestTint ? requestTint : header[@"ErrorString"];
//                [subscriber sendNext:returnModel];
//                [subscriber sendCompleted];
//            }
//        }
    } else {
        if (cache && isCache) {
            // 来自网络请求数据 错误了，就去读取缓存数据
            [self gainCacheDataWithSubscriber:subscriber error:nil cache:cache cacheKey:cacheKey isCache:isCache tint:model.queryFailureHint requestModel:model];
        } else {
            // 来自缓存数据 错误了,只能提示用户 数据错误
            NetDataReturnModel *returnModel = [[NetDataReturnModel alloc] init];
            returnModel.type = ReturnFailure;
            returnModel.error = model.queryFailureHint;
            [subscriber sendNext:returnModel];
            [subscriber sendCompleted];
        }
    }
}

- (BOOL)isPureInt:(NSString*)string {
    NSScanner* scan = [NSScanner scannerWithString:string];
    int val;
    return [scan scanInt:&val] && [scan isAtEnd];
}

@end
