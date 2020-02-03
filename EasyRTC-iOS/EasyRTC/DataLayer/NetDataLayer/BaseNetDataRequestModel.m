//
//  BaseNetDataRequestModel.m
//  Easy
//
//  Created by leo on 2018/5/10.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseNetDataRequestModel.h"

@implementation BaseNetDataRequestModel

- (instancetype) init {
    if (self = [super init]) {
        // 默认值
        
        self.paramsKey = @"params";
        
        self.resultKey = @"code";
        self.dataKey = @"data";
        self.msgKey = @"message";
        self.successIden = @"200";
        
        self.timeoutInterval = 10;
        
        self.inValidTokenCode = @"401";   // 用户票据超时
        self.nullTokenCode = @"401";      // 用户票据不能为空
        
        self.offNetHint = @"网络开小差, 请检查网络连接";
        self.dataFromCacheHint = @"";   // @"网络错误, 数据来自缓存";
        self.queryFailureHint = @"网络错误, 请稍后重试";
        
        self.uploadFileErrorHint = @"上传文件出错";
        self.imageMimeType = @"image/png";
        self.fileName = nil;
        
        self.wanNetTint = @"您当前使用的是流量";
        self.unknowNetTint = @"您当前正在使用位置网络";
        self.isHintNetStatus = NO;
    }
    
    return self;
}

@end
