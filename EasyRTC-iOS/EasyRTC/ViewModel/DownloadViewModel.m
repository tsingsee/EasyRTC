//
//  DownloadViewModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/10/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "DownloadViewModel.h"
#import "LoginInfoLocalData.h"

@implementation DownloadViewModel

#pragma mark - RACSubject

- (RACSubject *) downloadSubject {
    if (!_downloadSubject) {
        _downloadSubject = [[RACSubject alloc] init];
    }
    
    return _downloadSubject;
}

#pragma mark - 下载视频

- (void)downLoadVedio {
    NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
    
    // 录像开始时间, YYYYMMDDHHmmss
    NSString *period = self.curRecord.startAt;
    NSString *url = [NSString stringWithFormat:@"%@/api/v1/record/download/%@/%@", ip, self.channel.channel, period];
    NSLog(@"下载录像的地址L：%@", url);
    
    NSURLSessionConfiguration *config = [NSURLSessionConfiguration defaultSessionConfiguration];
    NSURLSession *session = [NSURLSession sessionWithConfiguration:config delegate:self delegateQueue:[NSOperationQueue mainQueue]];
    
    NSURLSessionDownloadTask *downloadTask = [session downloadTaskWithURL:[NSURL URLWithString:url]];
    [downloadTask resume];
}

#pragma mark - NSSessionUrlDelegate

- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didWriteData:(int64_t)bytesWritten totalBytesWritten:(int64_t)totalBytesWritten totalBytesExpectedToWrite:(int64_t)totalBytesExpectedToWrite {
    // 下载进度
    CGFloat progress = totalBytesWritten / (double)totalBytesExpectedToWrite;
    [self.downloadSubject sendNext:@(progress * 100)];
}

// 下载完成 保存到本地相册
- (void)URLSession:(NSURLSession *)session downloadTask:(NSURLSessionDownloadTask *)downloadTask didFinishDownloadingToURL:(NSURL *)location {
    // 1.拿到cache文件夹的路径
    NSString *cache = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES)lastObject];
    // 2.拿到cache文件夹和文件名
    NSString *file = [cache stringByAppendingPathComponent:downloadTask.response.suggestedFilename];
    [[NSFileManager defaultManager] moveItemAtURL:location toURL:[NSURL fileURLWithPath:file] error:nil];
    
    // 3.保存视频到相册
    if (UIVideoAtPathIsCompatibleWithSavedPhotosAlbum(file)) {
        // 保存相册核心代码
        UISaveVideoAtPathToSavedPhotosAlbum(file, self, nil, nil);
    }
}

@end
