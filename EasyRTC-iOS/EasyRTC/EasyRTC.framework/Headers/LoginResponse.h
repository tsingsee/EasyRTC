//
//  LoginResponse.h
//  venus
//
//  Created by Jac Chen on 2018/8/2.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SignalParameter.h"

@interface LoginResponse : SignalParameter

@property(nonatomic, copy) NSString *message;

@end
