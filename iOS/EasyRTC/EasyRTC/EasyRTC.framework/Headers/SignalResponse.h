//
//  SignalResponse.h
//  venus
//
//  Created by Jac Chen on 2018/8/1.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface SignalResponse : NSObject
@property (nonatomic, copy) NSString *jsonrpc;
@property (nonatomic, copy) NSString *result;
@property (nonatomic, copy) NSString *error;
@property (nonatomic, assign) long responseId;

@end
