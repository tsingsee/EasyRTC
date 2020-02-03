//
//  BaseViewModel.m
//  Easy
//
//  Created by leo on 2018/6/7.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "BaseViewModel.h"

@interface BaseViewModel()

@end

@implementation BaseViewModel

+ (instancetype)allocWithZone:(struct _NSZone *)zone {
    BaseViewModel *viewModel = [super allocWithZone:zone];
    
    if (viewModel) {
        [viewModel easy_initialize];
    }
    
    return viewModel;
}

- (instancetype)initWithModel:(id)model {
    if (self = [super init]) {
        
    }
    
    return self;
}

- (BaseNetDataRequest *)request {
    if (!_request) {
        _request = [[BaseNetDataRequest alloc] init];
    }
    
    return _request;
}

- (void)easy_initialize {
    
}

- (RACSubject *) tokenSubject {
    if (!_tokenSubject) {
        _tokenSubject = [RACSubject subject];
    }
    
    return _tokenSubject;
}

@end
