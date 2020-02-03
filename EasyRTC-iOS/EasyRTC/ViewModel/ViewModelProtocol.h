//
//  ViewModelProtocol.h
//  Easy
//
//  Created by leo on 2018/6/7.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BaseNetDataRequest.h"

@protocol ViewModelProtocol <NSObject>

@optional

- (instancetype)initWithModel:(id)model;

/**
 *  初始化
 */
- (void)easy_initialize;

@end
