//
//  DownloadViewController.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/10/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"
#import "RecordModel.h"

NS_ASSUME_NONNULL_BEGIN

/**
 下载
 */
@interface DownloadViewController : BaseViewController

@property (nonatomic, copy) NSString *recordId;
@property (nonatomic, strong) RecordModel *curRecord;

- (instancetype) initWithStoryborad;

@end

NS_ASSUME_NONNULL_END
