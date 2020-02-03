//
//  RecordPlayerViewController.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/9/1.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"
#import "RecordModel.h"

/**
 播放录像
 */
@interface RecordPlayerViewController : BaseViewController

@property (nonatomic, strong) RecordModel *model;

- (instancetype) initWithStoryboard;

@end
