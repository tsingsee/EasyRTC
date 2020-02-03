//
//  ConferenceCommandParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/24.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ConferenceCommand.h"
#import "SignalParameter.h"

@interface ConferenceCommandParameter : SignalParameter
@property (nonatomic, copy) NSString *eventChannel;
@property (nonatomic, strong) ConferenceCommand *data;

@end
