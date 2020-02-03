//
//  RecordViewModel.h
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/28.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewModel.h"
#import "RecordModel.h"

@interface RecordViewModel : BaseViewModel

@property (nonatomic, strong) NSDate *selectDate;
@property (nonatomic, strong) NSDate *chooseMonth;

@property (nonatomic, strong) NSArray *records;
@property (nonatomic, strong) RecordModel *curRecord;

// 按日查询通道录像
@property (nonatomic, strong) RACSubject *querydailySubject;
@property (nonatomic, strong) RACCommand *querydailyCommand;

// 按月查询通道录像记录
@property (nonatomic, strong) RACSubject *querymonthlySubject;
@property (nonatomic, strong) RACCommand *querymonthlyCommand;

// 删除单条录像
@property (nonatomic, strong) RACSubject *removeSubject;
@property (nonatomic, strong) RACCommand *removeCommand;

@end
