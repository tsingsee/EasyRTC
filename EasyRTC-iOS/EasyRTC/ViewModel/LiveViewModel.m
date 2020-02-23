//
//  LiveViewModel.m
//  EasyRTC
//
//  Created by liyy on 2020/2/2.
//  Copyright © 2020年 easydarwin. All rights reserved.
//

#import "LiveViewModel.h"

@implementation LiveViewModel

- (instancetype) init {
    if (self = [super init]) {
        self.sessions = [[NSMutableArray alloc] init];
    }
    
    return self;
}

- (void) easy_initialize {
    [self.dataCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            LiveSessionModel *live = [LiveSessionModel convertFromDict:model.result];
            NSArray<Session *> *arr = [Session convertFromArray:live.sessions.sessions];
            
            [self.sessions removeAllObjects];
            for (Session *item in arr) {
                if ([item.Application isEqualToString:@"hls"]) {
                    [self.sessions addObject:item];
                }
            }
            
            [self.dataSubject sendNext:nil];
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
            NSString *url = [NSString stringWithFormat:@"%@/getlivesessions", ip];
            
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
