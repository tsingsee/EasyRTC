//
//  SignalRequest.h
//  venus
//
//  Created by Jac Chen on 2018/8/1.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SignalParameter.h"

@interface SignalRequest : NSObject
@property (nonatomic, copy) NSString *method;
@property (nonatomic, strong) SignalParameter *params;
@property (nonatomic, assign) long requestId;
@property (nonatomic, copy) NSString *jsonrpc;

+ (SignalRequest *)newSignalRequestWithmethod:(NSString *)method signalParam:(SignalParameter *)param;

@end
