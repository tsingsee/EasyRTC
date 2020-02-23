//
//  RecordViewModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/8/28.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "RecordViewModel.h"
#import "RecordModel.h"
#import "DateUtil.h"

@interface RecordViewModel()

@end

@implementation RecordViewModel

- (void) easy_initialize {
    [self.querydailyCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
         if (model.type == ReturnSuccess) {
             self.records = [RecordModel convertFromArray:model.result[@"list"]];
             self.curRecord = self.records.firstObject;
             [self.querydailySubject sendNext:self.records];
         } else if (model.type == ReturnFailure) {
             [self.querydailySubject sendNext:nil];
         } else if (model.type == ReturnValidToken) {
             [self.tokenSubject sendNext:self.querydailyCommand];
         }
     }];
    
    [self.querymonthlyCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            // flag 标记当月每一天是否有录像, 0 - 没有录像, 1 - 有录像
            [self.querymonthlySubject sendNext:model.result[@"flags"]];
        } else if (model.type == ReturnFailure) {
            NSError *error = [NSError errorWithDomain:model.error code:0 userInfo:nil];
            [self.querymonthlySubject sendError:error];
        } else if (model.type == ReturnValidToken) {
            [self.tokenSubject sendNext:self.querymonthlyCommand];
        }
    }];
    
    [self.removeCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            [self.removeSubject sendNext:nil];
        } else if (model.type == ReturnFailure) {
            [self.removeSubject sendNext:model.error];
        } else if (model.type == ReturnValidToken) {
            [self.tokenSubject sendNext:self.removeCommand];
        }
    }];
}

#pragma mark - RACCommand

- (RACCommand *) querydailyCommand {
    if (!_querydailyCommand) {
        _querydailyCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(NSString * channelID) {// 通道号
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            NSString *period = [DateUtil dateYYYYMMDD:self.selectDate];// 日期, YYYYMMDD
            
            NSString *url = [NSString stringWithFormat:@"%@/query_record_daily?id=%@&period=%@", ip, channelID, period];
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _querydailyCommand;
}

- (RACCommand *) querymonthlyCommand {
    if (!_querymonthlyCommand) {
        _querymonthlyCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(NSString * channelID) {// 通道号
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            
            // 月份, YYYYMM
            NSString *period = [DateUtil dateYYYYMM:self.chooseMonth];
            
            NSString *url = [NSString stringWithFormat:@"%@/query_record_monthly?id=%@&period=%@", ip, channelID, period];
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _querymonthlyCommand;
}

- (RACCommand *) removeCommand {
    if (!_removeCommand) {
        _removeCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(NSString * channelID) {// 通道号
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            
            // 录像开始时间, YYYYMMDDHHmmss
            NSString *period = self.curRecord.startAt;// [DateUtil dateYYYYMMDDHHmmss:self.selectDate];
            
            NSString *url = [NSString stringWithFormat:@"%@/api/v1/record/remove?id=%@&period=%@", ip, channelID, period];
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _removeCommand;
}

#pragma mark - RACSubject

- (RACSubject *) querydailySubject {
    if (!_querydailySubject) {
        _querydailySubject = [[RACSubject alloc] init];
    }
    
    return _querydailySubject;
}

- (RACSubject *) querymonthlySubject {
    if (!_querymonthlySubject) {
        _querymonthlySubject = [[RACSubject alloc] init];
    }
    
    return _querymonthlySubject;
}

- (RACSubject *) removeSubject {
    if (!_removeSubject) {
        _removeSubject = [[RACSubject alloc] init];
    }
    
    return _removeSubject;
}

#pragma mark - getter

- (NSDate *) selectDate {
    if (!_selectDate) {
        _selectDate = [NSDate date];// 默认显示当天
    }
    
    return _selectDate;
}

@end
