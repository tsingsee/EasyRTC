//
//  RecordListViewController.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/23.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"
#import "VideoModel.h"

/**
 列表的录像
 */
@interface RecordListViewController : BaseViewController

@property (nonatomic, strong) Channel *channel;
@property (nonatomic, strong) NSDate *selectDate;

@property (nonatomic, strong) RACSubject *subject;

- (instancetype) initWithStoryboard;

@end
