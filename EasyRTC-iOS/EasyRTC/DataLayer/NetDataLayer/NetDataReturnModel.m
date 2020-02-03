//
//  NetDataReturnModel.m
//  Easy
//
//  Created by leo on 2018/6/8.
//  Copyright © 2018年 leo. All rights reserved.
//

#import "NetDataReturnModel.h"

@implementation NetDataReturnModel

- (NSString *) error {
    if (!_error) {
        return @"";
    }
    
    return _error;
}

@end
