//
//  BootstrapParameter.h
//  venus
//
//  Created by Jac Chen on 2018/8/26.
//  Copyright © 2018 Matrix. All rights reserved.
//

#import "SignalParameter.h"

@interface LiveArray : NSObject
@property(nonatomic, copy) NSString *command;
@property(nonatomic, copy) NSString *context;
@property(nonatomic, copy) NSString *name;

@end

@interface Data : NSObject
@property(nonatomic, strong) LiveArray *liveArray;

@end

@interface BootstrapParameter : SignalParameter
@property(nonatomic, copy) NSString *eventChannel;
@property(nonatomic, strong) Data *data;

- (instancetype)initWithLiveString:(NSString *)live liveName:(NSString *)name;

@end
