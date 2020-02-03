//
//  CalendarViewController.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/27.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewController.h"

/**
 展示日历，选择日期
 */
@interface CalendarViewController : BaseViewController

@property (nonatomic, copy) NSString *channelID;
@property (nonatomic, strong) NSDate *chooseMonth;

@property (nonatomic, strong) RACSubject *subject;

- (instancetype) initWithStoryborad;

@end
