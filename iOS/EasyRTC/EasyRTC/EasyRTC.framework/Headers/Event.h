//
//  Event.h
//  venus
//
//  Created by Jac Chen on 2018/8/26.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EventParameter.h"

@interface Event : NSObject
@property(nonatomic, copy) NSString *eventType;
@property(nonatomic, copy) NSString *eventChannel;
@property(nonatomic, strong) EventParameter *params;

@end
