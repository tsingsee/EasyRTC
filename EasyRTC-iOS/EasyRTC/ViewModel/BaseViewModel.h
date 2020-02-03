//
//  BaseViewModel.h
//  Easy
//
//  Created by leo on 2018/6/7.
//  Copyright © 2018年 leo. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ViewModelProtocol.h"
#import "LoginInfoLocalData.h"
#import "MD5Util.h"

@interface BaseViewModel : NSObject<ViewModelProtocol>

@property (nonatomic, strong) BaseNetDataRequest *request;

@property (nonatomic, strong) RACSubject *tokenSubject;

@end
