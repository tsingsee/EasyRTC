//
//  DownloadViewModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/10/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewModel.h"
#import "RecordModel.h"
#import "VideoModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface DownloadViewModel : BaseViewModel<NSURLSessionDelegate>

@property (nonatomic, strong) Channel *channel;         // 通道号
@property (nonatomic, strong) RecordModel *curRecord;   // 录像的

@property (nonatomic, strong) RACSubject *downloadSubject;

/**
 下载录像文件
 https://www.jianshu.com/p/bd2379b6b907
 https://blog.csdn.net/a787188834/article/details/79895228
 
 demo:http://demo.easynvr.com:10800/api/v1/record/download/22/20180827090001
 */
- (void)downLoadVedio;

@end

NS_ASSUME_NONNULL_END
