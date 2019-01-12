//
//  InviteParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/2.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "DialogParameter.h"
#import "SignalParameter.h"

@interface InviteParameter : SignalParameter
@property(nonatomic, strong)    DialogParameter *dialogParams;
@property(nonatomic, copy)      NSString *sdp;

+ (InviteParameter *)createWithDestNumber:(NSString *)destNumber userName:(NSString *)username displayName:(NSString *)displayName email:(NSString *)email andSdp:(NSString *)sdp callId:(NSString *)callId;
@end
