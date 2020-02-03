//
//  SubscribeLiveArrayParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/26.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import "SignalParameter.h"

@interface SubscribeLiveArrayParameter : SignalParameter
@property(nonatomic, copy) NSString *eventChannel;

@end
