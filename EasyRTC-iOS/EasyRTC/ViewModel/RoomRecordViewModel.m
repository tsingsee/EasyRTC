//
//  RoomRecordViewModel.m
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "RoomRecordViewModel.h"

@implementation RoomRecordViewModel

- (void) easy_initialize {
    [self.dataCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            self.model = [Devices convertFromDict:model.result];
            [self.dataSubject sendNext:self.model];
        } else if (model.type == ReturnFailure) {
            [self.dataSubject sendNext:nil];
        } else if (model.type == ReturnValidToken) {
            [self.tokenSubject sendNext:self.dataCommand];
        }
    }];
}

#pragma mark - RACCommand

- (RACCommand *) dataCommand {
    if (!_dataCommand) {
        _dataCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(id o) {// 通道号
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            NSString *url = [NSString stringWithFormat:@"%@/query_devices", ip];
            
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _dataCommand;
}

#pragma mark - RACSubject

- (RACSubject *) dataSubject {
    if (!_dataSubject) {
        _dataSubject = [[RACSubject alloc] init];
    }
    
    return _dataSubject;
}

@end
