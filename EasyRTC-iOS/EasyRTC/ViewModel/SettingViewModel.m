//
//  SettingViewModel.m
//  EasyNVR_iOS
//
//  Created by leo on 2018/7/13.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "SettingViewModel.h"
#import "InfoModel.h"
#import "RequestKeyModel.h"

@implementation SettingViewModel

- (void) easy_initialize {
    [self.infoCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            InfoModel *info = [InfoModel convertFromDict:model.result];
            
            [self.infoSubject sendNext:info];
        } else if (model.type == ReturnFailure) {
            [self.infoSubject sendNext:model.error];
        } else if (model.type == ReturnValidToken) {
            [self.tokenSubject sendNext:self.infoCommand];
        }
    }];
    
    [self.keyCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            RequestKeyModel *m = [RequestKeyModel convertFromDict:model.result];
            
            [self.keySubject sendNext:m];
        } else if (model.type == ReturnFailure) {
            [self.keySubject sendNext:model.error];
        } else if (model.type == ReturnValidToken) {
            [self.tokenSubject sendNext:self.keyCommand];
        }
    }];
    
    [self.submitCommand.executionSignals.switchToLatest subscribeNext:^(NetDataReturnModel *model) {
        if (model.type == ReturnSuccess) {
            [self.submitSubject sendNext:nil];
        } else if (model.type == ReturnFailure) {
            [self.submitSubject sendNext:model.error];
        } else if (model.type == ReturnValidToken) {
            [self.tokenSubject sendNext:self.keyCommand];
        }
    }];
}

- (RACCommand *) infoCommand {
    if (!_infoCommand) {
        _infoCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(id input) {
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            NSString *url = [NSString stringWithFormat:@"%@/getserverinfo", ip];
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _infoCommand;
}

- (RACCommand *) keyCommand {
    if (!_keyCommand) {
        _keyCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(id input) {
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            NSString *url = [NSString stringWithFormat:@"%@/getrequestkey", ip];
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _keyCommand;
}

- (RACCommand *) submitCommand {
    if (!_submitCommand) {
        _submitCommand = [[RACCommand alloc] initWithSignalBlock:^RACSignal *(NSString *code) {
            NSString *ip = [[LoginInfoLocalData sharedInstance] gainIPAddress];
            NSString *url = [NSString stringWithFormat:@"%@/verifyproductcode?productcode=%@", ip, code];
            return [self.request httpGetRequest:url params:nil requestModel:nil];
        }];
    }
    
    return _submitCommand;
}

- (RACSubject *) infoSubject {
    if (!_infoSubject) {
        _infoSubject = [RACSubject subject];
    }
    
    return _infoSubject;
}

- (RACSubject *) keySubject {
    if (!_keySubject) {
        _keySubject = [RACSubject subject];
    }
    
    return _keySubject;
}

- (RACSubject *) submitSubject {
    if (!_submitSubject) {
        _submitSubject = [RACSubject subject];
    }
    
    return _submitSubject;
}

@end
